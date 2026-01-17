package com.wingedsheep.ecologique.orders.api.event

import com.wingedsheep.ecologique.orders.api.OrderId
import java.time.Instant

/**
 * Domain event published when an order is ready for shipment creation.
 *
 * This event is published after payment is completed and includes the shipping
 * address snapshot. Consumers (like shipping-impl) can use this event to create
 * shipments without needing to call the user service.
 */
data class OrderReadyForShipment(
    val orderId: OrderId,
    val shippingAddress: ShippingAddressSnapshot,
    val timestamp: Instant
)

/**
 * Snapshot of the shipping address at the time of order payment.
 * This decouples shipping from the users module.
 */
data class ShippingAddressSnapshot(
    val recipientName: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String
)
