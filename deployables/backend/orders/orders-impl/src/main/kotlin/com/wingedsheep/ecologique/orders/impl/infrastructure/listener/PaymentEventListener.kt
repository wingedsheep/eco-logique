package com.wingedsheep.ecologique.orders.impl.infrastructure.listener

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.payment.api.event.PaymentCompleted
import com.wingedsheep.ecologique.payment.api.event.PaymentFailed
import com.wingedsheep.ecologique.payment.api.event.PaymentInitiated
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.logging.Logger

/**
 * Listens to payment events and updates order status accordingly.
 */
@Component
internal class PaymentEventListener(
    private val orderService: OrderService
) {
    private val logger = Logger.getLogger(PaymentEventListener::class.java.name)

    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        logger.info("Payment completed for order ${event.orderId}, updating status to PAID")

        val orderId = OrderId(UUID.fromString(event.orderId))
        orderService.updateStatus(orderId, OrderStatus.PAID).fold(
            onSuccess = {
                logger.info("Order ${event.orderId} status updated to PAID")
            },
            onFailure = { error ->
                logger.warning("Failed to update order ${event.orderId} status: $error")
            }
        )
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
