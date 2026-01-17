package com.wingedsheep.ecologique.orders.impl.infrastructure.listener

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.event.OrderReadyForShipment
import com.wingedsheep.ecologique.orders.api.event.ShippingAddressSnapshot
import com.wingedsheep.ecologique.payment.api.event.PaymentCompleted
import com.wingedsheep.ecologique.payment.api.event.PaymentFailed
import com.wingedsheep.ecologique.payment.api.event.PaymentInitiated
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger

/**
 * Listens to payment events and updates order status accordingly.
 */
@Component
internal class PaymentEventListener(
    private val orderService: OrderService,
    private val userService: UserService,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val logger = Logger.getLogger(PaymentEventListener::class.java.name)

    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        logger.info("Payment completed for order ${event.orderId}, updating status to PAID")

        val orderId = OrderId(UUID.fromString(event.orderId))

        // Mark order as paid and store paymentId
        val orderResult = orderService.markAsPaid(orderId, event.paymentId)
        val order = when (orderResult) {
            is Result.Ok -> {
                logger.info("Order ${event.orderId} status updated to PAID")
                orderResult.value
            }
            is Result.Err -> {
                logger.warning("Failed to update order ${event.orderId} status: ${orderResult.error}")
                return
            }
        }

        // Fetch user profile for shipping address
        val userId = UserId(UUID.fromString(order.userId))
        val userResult = userService.getProfile(userId)
        val user = when (userResult) {
            is Result.Ok -> userResult.value
            is Result.Err -> {
                logger.warning("Failed to get user profile for order ${event.orderId}: ${userResult.error}")
                return
            }
        }

        val address = user.defaultAddress
        if (address == null) {
            logger.warning("User ${order.userId} has no default address, cannot initiate shipment for order ${event.orderId}")
            return
        }

        // Publish OrderReadyForShipment event with address snapshot
        eventPublisher.publishEvent(
            OrderReadyForShipment(
                orderId = orderId,
                shippingAddress = ShippingAddressSnapshot(
                    recipientName = user.name,
                    street = address.street,
                    houseNumber = address.houseNumber,
                    postalCode = address.postalCode,
                    city = address.city,
                    countryCode = address.countryCode
                ),
                timestamp = Instant.now()
            )
        )
        logger.info("Published OrderReadyForShipment event for order ${event.orderId}")
    }

    @EventListener
    fun onPaymentInitiated(event: PaymentInitiated) {
        logger.info("Payment initiated for order ${event.orderId}, updating status to PAYMENT_PENDING")

        val orderId = OrderId(UUID.fromString(event.orderId))
        orderService.updateStatus(orderId, OrderStatus.PAYMENT_PENDING).fold(
            onSuccess = {
                logger.info("Order ${event.orderId} status updated to PAYMENT_PENDING")
            },
            onFailure = { error ->
                logger.warning("Failed to update order ${event.orderId} status: $error")
            }
        )
    }

    @EventListener
    fun onPaymentFailed(event: PaymentFailed) {
        logger.info("Payment failed for order ${event.orderId}: ${event.failureReason}")
    }
}
