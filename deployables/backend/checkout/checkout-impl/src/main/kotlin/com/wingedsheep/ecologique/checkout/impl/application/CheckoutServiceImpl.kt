package com.wingedsheep.ecologique.checkout.impl.application

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.checkout.api.CheckoutService
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutRequest
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutResult
import com.wingedsheep.ecologique.checkout.api.error.CheckoutError
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.InventoryService
import com.wingedsheep.ecologique.inventory.api.dto.ReservationResult
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.payment.api.PaymentService
import com.wingedsheep.ecologique.payment.api.dto.PaymentMethod
import com.wingedsheep.ecologique.payment.api.dto.PaymentRequest
import com.wingedsheep.ecologique.users.api.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import java.util.logging.Logger

@Service
internal class CheckoutServiceImpl(
    private val cartService: CartService,
    private val orderService: OrderService,
    private val inventoryService: InventoryService,
    private val paymentService: PaymentService
) : CheckoutService {

    private val logger = Logger.getLogger(CheckoutServiceImpl::class.java.name)

    @Transactional
    override fun checkout(userId: String, request: CheckoutRequest): Result<CheckoutResult, CheckoutError> {
        logger.info("Starting checkout for user $userId")

        // 1. Load and validate cart
        val cartResult = cartService.getCart(UserId(UUID.fromString(userId)))
        val cart = when (cartResult) {
            is Result.Ok -> cartResult.value
            is Result.Err -> return Result.err(CheckoutError.CheckoutUnavailable("Failed to load cart"))
        }

        if (cart.items.isEmpty()) {
            return Result.err(CheckoutError.EmptyCart)
        }

        // 2. Check stock availability (without reserving yet)
        for (item in cart.items) {
            val stockResult = inventoryService.checkStock(item.productId)
            when (stockResult) {
                is Result.Ok -> {
                    val stock = stockResult.value
                    if (stock.available < item.quantity) {
                        return Result.err(
                            CheckoutError.InsufficientStock(item.productId, item.quantity, stock.available)
                        )
                    }
                }
                is Result.Err -> return Result.err(CheckoutError.CheckoutUnavailable("Inventory check failed"))
            }
        }

        // 3. Create order with price snapshots
        val orderRequest = OrderCreateRequest(
            lines = cart.items.map { item ->
                OrderLineCreateRequest(
                    productId = item.productId,
                    productName = item.productName,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity
                )
            },
            currency = cart.currency
        )

        val orderResult = orderService.createOrder(userId, orderRequest)
        val order = when (orderResult) {
            is Result.Ok -> orderResult.value
            is Result.Err -> return Result.err(CheckoutError.OrderCreationFailed("Failed to create order"))
        }

        // 4. Reserve inventory for all items (uses order ID as correlation)
        // InventoryReserved events will update order status to RESERVED
        val reservations = mutableListOf<ReservationResult>()
        for (item in cart.items) {
            val reserveResult = inventoryService.reserveStock(
                productId = item.productId,
                quantity = item.quantity,
                correlationId = order.id.value.toString()
            )

            when (reserveResult) {
                is Result.Ok -> reservations.add(reserveResult.value)
                is Result.Err -> {
                    // Rollback previous reservations
                    reservations.forEach { inventoryService.releaseReservation(it.reservationId) }

                    val error = reserveResult.error
                    return when (error) {
                        is com.wingedsheep.ecologique.inventory.api.error.InventoryError.InsufficientStock ->
                            Result.err(CheckoutError.InsufficientStock(error.productId, error.requested, error.available))
                        else ->
                            Result.err(CheckoutError.CheckoutUnavailable("Inventory reservation failed"))
                    }
                }
            }
        }

        // 5. Process payment (PaymentInitiated event updates order to PAYMENT_PENDING,
        //    PaymentCompleted event updates order to PAID)
        val paymentRequest = PaymentRequest(
            orderId = order.id.value.toString(),
            amount = com.wingedsheep.ecologique.common.money.Money(order.grandTotal, order.currency),
            paymentMethod = PaymentMethod.Card(
                token = request.cardToken,
                last4 = request.cardLast4,
                brand = request.cardBrand
            ),
            description = "Payment for order ${order.id.value}"
        )

        val paymentResult = paymentService.processPayment(paymentRequest)
        val payment = when (paymentResult) {
            is Result.Ok -> paymentResult.value
            is Result.Err -> {
                // Payment failed - order stays in PAYMENT_PENDING, customer can retry
                // Inventory stays reserved for this order
                val errorReason = when (val error = paymentResult.error) {
                    is com.wingedsheep.ecologique.payment.api.error.PaymentError.CardDeclined -> error.reason
                    is com.wingedsheep.ecologique.payment.api.error.PaymentError.InsufficientFunds -> "Insufficient funds"
                    is com.wingedsheep.ecologique.payment.api.error.PaymentError.FraudDetected -> "Payment rejected"
                    else -> "Payment processing failed"
                }
                return Result.err(CheckoutError.PaymentFailed(order.id, errorReason))
            }
        }

        // 6. Clear cart on success
        cartService.clearCart(UserId(UUID.fromString(userId)))

        logger.info("Checkout completed for user $userId - order: ${order.id.value}, payment: ${payment.id.value}")

        return Result.ok(
            CheckoutResult(
                orderId = order.id,
                orderStatus = OrderStatus.PAID,
                paymentId = payment.id,
                paymentStatus = payment.status
            )
        )
    }
}
