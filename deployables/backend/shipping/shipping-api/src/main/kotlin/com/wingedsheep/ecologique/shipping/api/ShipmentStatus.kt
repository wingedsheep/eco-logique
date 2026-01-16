package com.wingedsheep.ecologique.shipping.api

/**
 * Status of a shipment throughout its lifecycle.
 */
enum class ShipmentStatus {
    /**
     * Shipment has been created but not yet processed.
     */
    CREATED,

    /**
     * Shipment is being prepared at the warehouse.
     */
    PROCESSING,

    /**
     * Shipment has been handed over to the carrier.
     */
    SHIPPED,

    /**
     * Shipment is in transit to the destination.
     */
    IN_TRANSIT,

    /**
     * Shipment has been delivered to the recipient.
     */
    DELIVERED,

    /**
     * Shipment was returned (failed delivery attempt).
     */
    RETURNED,

    /**
     * Shipment was cancelled.
     */
    CANCELLED
}
