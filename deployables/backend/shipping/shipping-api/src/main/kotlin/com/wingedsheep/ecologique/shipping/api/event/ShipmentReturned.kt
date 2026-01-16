package com.wingedsheep.ecologique.shipping.api.event

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import java.time.Instant

/**
 * Domain event published when a shipment has been returned (failed delivery attempt).
 *
 * This event signals that the delivery driver could not complete the delivery
 * and the shipment is being returned to the warehouse.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to RETURNED
 * - Sending return notification emails to customers
 * - Scheduling re-delivery attempts
 */
data class ShipmentReturned(
    val shipmentId: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: String,
    val returnedAt: Instant,
    val timestamp: Instant
)
