package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.common.tax.VatCalculator
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.error.OrderError
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.OrderRepository
import com.wingedsheep.ecologique.orders.impl.domain.canTransitionTo
import com.wingedsheep.ecologique.payment.api.PaymentId
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.common.outbox.OutboxEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
internal class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val productService: ProductService,
    private val userService: UserService,
    private val outboxEventPublisher: OutboxEventPublisher
) : OrderService {

    @Transactional
    override fun createOrder(userId: String, request: OrderCreateRequest): Result<OrderDto, OrderError> {
        // Fetch and validate products, collecting their details for VAT calculation
        val products = mutableMapOf<String, ProductDto>()
        for (line in request.lines) {
            val productResult = productService.getProduct(line.productId)
            when (productResult) {
                is Result.Ok -> products[line.productId.value.toString()] = productResult.value
                is Result.Err -> return Result.err(OrderError.ProductNotFound(line.productId))
            }
        }

        val lines = try {
            request.lines.map { line ->
                OrderLine.create(
                    productId = line.productId,
                    productName = line.productName,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity
                )
            }
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.ValidationFailed(e.message ?: "Invalid order line"))
        }

        // Calculate VAT based on user's country and product categories
        val vatResult = calculateVat(userId, request, products)
        val (vatAmount, vatRate) = vatResult

        val order = try {
            Order.create(
                userId = userId,
                lines = lines,
                currency = request.currency,
                vatAmount = vatAmount,
                vatRate = vatRate
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.ValidationFailed(e.message ?: "Validation failed"))
        }

        val savedOrder = orderRepository.save(order)

        outboxEventPublisher.publishEvent(
            OrderCreatedOutboxEvent(
                orderId = savedOrder.id,
                userId = savedOrder.userId,
                grandTotal = savedOrder.totals.grandTotal,
                currency = savedOrder.totals.currency,
                timestamp = Instant.now()
            )
        )

        return Result.ok(savedOrder.toDto())
    }

    private fun calculateVat(
        userId: String,
        request: OrderCreateRequest,
        products: Map<String, ProductDto>
    ): Pair<BigDecimal, BigDecimal> {
        // Get user's country from their profile
        val country = try {
            val userResult = userService.getProfile(UserId(UUID.fromString(userId)))
            when (userResult) {
                is Result.Ok -> {
                    val countryCode = userResult.value.defaultAddress?.countryCode
                    if (countryCode != null) {
                        try {
                            Country.valueOf(countryCode)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    } else null
                }
                is Result.Err -> null
            }
        } catch (e: IllegalArgumentException) {
            // Invalid UUID - return no country
            null
        }

        // If no country, return zero VAT
        if (country == null) {
            return BigDecimal.ZERO to BigDecimal.ZERO
        }

        // Calculate VAT per line item and sum up
        var totalVat = BigDecimal.ZERO
        var dominantRate = BigDecimal.ZERO

        for (line in request.lines) {
            val product = products[line.productId.value.toString()] ?: continue
            val lineTotal = line.unitPrice.multiply(BigDecimal(line.quantity))

            try {
                val vatBreakdown = VatCalculator.calculate(lineTotal, country, product.category)
                totalVat = totalVat.add(vatBreakdown.vatAmount)
                // Use the rate from the first/largest line as the "dominant" rate
                if (dominantRate == BigDecimal.ZERO) {
                    dominantRate = vatBreakdown.vatRate
                }
            } catch (e: IllegalArgumentException) {
                // Country/category combination not supported, skip VAT for this line
            }
        }

        return totalVat to dominantRate
    }

    @Transactional(readOnly = true)
    override fun getOrder(orderId: OrderId, userId: String): Result<OrderDto, OrderError> {
        val order = orderRepository.findById(orderId)
            ?: return Result.err(OrderError.NotFound(orderId))

        if (!order.isOwnedBy(userId)) {
            return Result.err(OrderError.AccessDenied(orderId, userId))
        }

        return Result.ok(order.toDto())
    }

    @Transactional(readOnly = true)
    override fun getOrderInternal(orderId: OrderId): Result<OrderDto, OrderError> {
        val order = orderRepository.findById(orderId)
            ?: return Result.err(OrderError.NotFound(orderId))

        return Result.ok(order.toDto())
    }

    @Transactional(readOnly = true)
    override fun findOrdersForUser(userId: String): Result<List<OrderDto>, OrderError> {
        val orders = orderRepository.findByUserId(userId)
        return Result.ok(orders.map { it.toDto() })
    }

    @Transactional
    override fun updateStatus(orderId: OrderId, newStatus: OrderStatus): Result<OrderDto, OrderError> {
        val order = orderRepository.findById(orderId)
            ?: return Result.err(OrderError.NotFound(orderId))

        val updatedOrder = try {
            order.transitionTo(newStatus)
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.InvalidStatus(order.status, newStatus))
        }

        val savedOrder = orderRepository.save(updatedOrder)
        return Result.ok(savedOrder.toDto())
    }

    @Transactional
    override fun markAsPaid(orderId: OrderId, paymentId: PaymentId): Result<OrderDto, OrderError> {
        val order = orderRepository.findById(orderId)
            ?: return Result.err(OrderError.NotFound(orderId))

        val updatedOrder = try {
            order.transitionTo(OrderStatus.PAID).withPaymentId(paymentId)
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.InvalidStatus(order.status, OrderStatus.PAID))
        }

        val savedOrder = orderRepository.save(updatedOrder)
        return Result.ok(savedOrder.toDto())
    }

}
