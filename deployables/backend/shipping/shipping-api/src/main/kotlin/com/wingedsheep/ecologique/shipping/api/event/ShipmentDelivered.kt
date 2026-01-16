package com.wingedsheep.ecologique.shipping.api.event

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import java.time.Instant

/**
 * Domain event published when a shipment has been delivered.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to DELIVERED
 * - Sending delivery confirmation emails
 * - Triggering post-delivery surveys
 */
data class ShipmentDelivered(
    val shipmentId: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: String,
    val deliveredAt: Instant,
    val timestamp: Instant
)
