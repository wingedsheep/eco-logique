package com.wingedsheep.ecologique.application.messaging

import java.time.Instant

/**
 * Message contract for delivery status updates from driver tablets.
 *
 * Drivers use their tablets to report delivery status changes such as
 * marking shipments as in transit, delivered, or returned (failed delivery).
 */
data class DeliveryStatusMessage(
    val trackingNumber: String,
    val newStatus: String,
    val timestamp: Instant,
    val driverNotes: String? = null
)
