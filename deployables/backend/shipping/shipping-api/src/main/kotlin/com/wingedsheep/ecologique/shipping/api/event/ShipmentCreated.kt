package com.wingedsheep.ecologique.shipping.api.event

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import java.time.Instant

/**
 * Domain event published when a shipment has been created.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to SHIPPED
 * - Sending shipment confirmation emails
 * - Notifying warehouse management systems
 */
data class ShipmentCreated(
    val shipmentId: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: String,
    val warehouseId: WarehouseId,
    val timestamp: Instant
)
