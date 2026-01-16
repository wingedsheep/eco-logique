package com.wingedsheep.ecologique.shipping.api.error

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus

/**
 * Errors that can occur during shipping operations.
 */
sealed class ShippingError {

    /**
     * Shipment was not found.
     */
    data class NotFound(val shipmentId: ShipmentId) : ShippingError()

    /**
     * Shipment for the specified order was not found.
     */
    data class NotFoundForOrder(val orderId: OrderId) : ShippingError()

    /**
     * Order was not found when creating shipment.
     */
    data class OrderNotFound(val orderId: OrderId) : ShippingError()

    /**
     * A shipment already exists for this order.
     */
    data class DuplicateShipment(val orderId: OrderId) : ShippingError()

    /**
     * No warehouse found for the specified country.
     */
    data class NoWarehouseForCountry(val countryCode: String) : ShippingError()

    /**
     * Invalid status transition requested.
     */
    data class InvalidStatusTransition(
        val shipmentId: ShipmentId,
        val currentStatus: ShipmentStatus,
        val requestedStatus: ShipmentStatus
    ) : ShippingError()

    /**
     * Validation failed.
     */
    data class ValidationFailed(val reason: String) : ShippingError()
}
