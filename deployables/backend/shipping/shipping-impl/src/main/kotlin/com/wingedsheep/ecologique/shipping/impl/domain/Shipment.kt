package com.wingedsheep.ecologique.shipping.impl.domain

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import java.math.BigDecimal
import java.time.Instant

/**
 * Domain model for a shipment.
 */
internal data class Shipment(
    val id: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: String,
    val status: ShipmentStatus,
    val shippingAddress: ShippingAddress,
    val warehouseId: WarehouseId,
    val weightKg: BigDecimal?,
    val createdAt: Instant,
    val shippedAt: Instant?,
    val deliveredAt: Instant?
) {
    init {
        require(trackingNumber.isNotBlank()) { "Tracking number cannot be blank" }
        weightKg?.let {
            require(it > BigDecimal.ZERO) { "Weight must be positive" }
        }
    }

    fun canTransitionTo(newStatus: ShipmentStatus): Boolean = when (status) {
        ShipmentStatus.CREATED -> newStatus in listOf(ShipmentStatus.PROCESSING, ShipmentStatus.CANCELLED)
        ShipmentStatus.PROCESSING -> newStatus in listOf(ShipmentStatus.SHIPPED, ShipmentStatus.CANCELLED)
        ShipmentStatus.SHIPPED -> newStatus in listOf(ShipmentStatus.IN_TRANSIT, ShipmentStatus.CANCELLED)
        ShipmentStatus.IN_TRANSIT -> newStatus in listOf(ShipmentStatus.DELIVERED, ShipmentStatus.RETURNED, ShipmentStatus.CANCELLED)
        ShipmentStatus.DELIVERED -> false // Terminal state
        ShipmentStatus.RETURNED -> newStatus == ShipmentStatus.IN_TRANSIT // Allow re-attempt delivery
        ShipmentStatus.CANCELLED -> false // Terminal state
    }

    fun transitionTo(newStatus: ShipmentStatus, now: Instant): Shipment {
        require(canTransitionTo(newStatus)) {
            "Cannot transition from $status to $newStatus"
        }
        return when (newStatus) {
            ShipmentStatus.SHIPPED -> copy(status = newStatus, shippedAt = now)
            ShipmentStatus.DELIVERED -> copy(status = newStatus, deliveredAt = now)
            else -> copy(status = newStatus)
        }
    }

    companion object {
        fun create(
            orderId: OrderId,
            trackingNumber: String,
            shippingAddress: ShippingAddress,
            warehouseId: WarehouseId,
            weightKg: BigDecimal?,
            now: Instant
        ): Shipment {
            return Shipment(
                id = ShipmentId.generate(),
                orderId = orderId,
                trackingNumber = trackingNumber,
                status = ShipmentStatus.CREATED,
                shippingAddress = shippingAddress,
                warehouseId = warehouseId,
                weightKg = weightKg,
                createdAt = now,
                shippedAt = null,
                deliveredAt = null
            )
        }
    }
}

/**
 * Shipping address within the domain.
 */
internal data class ShippingAddress(
    val recipientName: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String
) {
    init {
        require(recipientName.isNotBlank()) { "Recipient name cannot be blank" }
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(houseNumber.isNotBlank()) { "House number cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(countryCode.isNotBlank()) { "Country code cannot be blank" }
    }
}
