package com.wingedsheep.ecologique.orders.impl.infrastructure.listener

import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.shipping.api.event.ShipmentCreated
import com.wingedsheep.ecologique.shipping.api.event.ShipmentDelivered
import com.wingedsheep.ecologique.shipping.api.event.ShipmentReturned
import com.wingedsheep.ecologique.shipping.api.event.ShipmentShipped
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.logging.Logger

/**
 * Listens to shipment events and updates order status accordingly.
 */
@Component
internal class ShipmentEventListener(
    private val orderService: OrderService
) {
    private val logger = Logger.getLogger(ShipmentEventListener::class.java.name)

    @EventListener
    fun onShipmentCreated(event: ShipmentCreated) {
        logger.info(
            "Shipment created for order ${event.orderId.value}, tracking: ${event.trackingNumber}. " +
                "Awaiting warehouse processing."
        )
    }

    @EventListener
    fun onShipmentShipped(event: ShipmentShipped) {
        logger.info("Shipment shipped for order ${event.orderId.value}, updating order status to SHIPPED")

        orderService.updateStatus(event.orderId, OrderStatus.SHIPPED).fold(
            onSuccess = {
                logger.info("Order ${event.orderId.value} status updated to SHIPPED")
            },
            onFailure = { error ->
                logger.warning("Failed to update order ${event.orderId.value} status: $error")
            }
        )
    }

    @EventListener
    fun onShipmentDelivered(event: ShipmentDelivered) {
        logger.info("Shipment delivered for order ${event.orderId.value}, updating order status to DELIVERED")

        orderService.updateStatus(event.orderId, OrderStatus.DELIVERED).fold(
            onSuccess = {
                logger.info("Order ${event.orderId.value} status updated to DELIVERED")
            },
            onFailure = { error ->
                logger.warning("Failed to update order ${event.orderId.value} status: $error")
            }
        )
    }

    @EventListener
    fun onShipmentReturned(event: ShipmentReturned) {
        logger.info("Shipment returned for order ${event.orderId.value}, updating order status to RETURNED")

        orderService.updateStatus(event.orderId, OrderStatus.RETURNED).fold(
            onSuccess = {
                logger.info("Order ${event.orderId.value} status updated to RETURNED")
            },
            onFailure = { error ->
                logger.warning("Failed to update order ${event.orderId.value} status: $error")
            }
        )
    }
}
