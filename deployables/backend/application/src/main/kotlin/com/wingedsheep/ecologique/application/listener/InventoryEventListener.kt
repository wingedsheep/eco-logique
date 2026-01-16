package com.wingedsheep.ecologique.application.listener

import com.wingedsheep.ecologique.inventory.api.event.InventoryReserved
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.logging.Logger

/**
 * Listens to inventory events and updates order status accordingly.
 *
 * This is the application-level integration between the inventory and orders modules.
 * The inventory module publishes events, and this listener coordinates the order updates.
 */
@Component
class InventoryEventListener(
    private val orderService: OrderService
) {
    private val logger = Logger.getLogger(InventoryEventListener::class.java.name)

    @EventListener
    fun onInventoryReserved(event: InventoryReserved) {
        // The correlationId should be the order ID when coming from checkout
        val correlationId = event.correlationId

        // Only process if it looks like an order ID (UUID format)
        val orderId = try {
            UUID.fromString(correlationId)
        } catch (e: IllegalArgumentException) {
            logger.fine("InventoryReserved event with non-UUID correlationId: $correlationId, skipping order update")
            return
        }

        logger.info("Inventory reserved for order $correlationId, updating status to RESERVED")

        orderService.updateStatus(OrderId(orderId), OrderStatus.RESERVED).fold(
            onSuccess = {
                logger.info("Order $correlationId status updated to RESERVED")
            },
            onFailure = { error ->
                logger.warning("Failed to update order $correlationId status: $error")
            }
        )
    }
}
