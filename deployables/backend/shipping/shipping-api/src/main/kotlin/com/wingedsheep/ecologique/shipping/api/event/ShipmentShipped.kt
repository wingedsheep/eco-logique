package com.wingedsheep.ecologique.shipping.api.event

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import java.time.Instant

/**
 * Domain event published when a shipment has been marked as shipped by warehouse staff.
 *
 * This event signals that the physical shipment has been handed over to the carrier
 * and is now in transit to the customer.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to SHIPPED
 * - Sending shipment notification emails to customers
 * - Updating tracking information
 */
data class ShipmentShipped(
    val shipmentId: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: String,
    val shippedAt: Instant,
    val timestamp: Instant
)
