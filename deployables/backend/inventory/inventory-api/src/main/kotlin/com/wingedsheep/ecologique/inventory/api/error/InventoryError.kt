package com.wingedsheep.ecologique.inventory.api.error

import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.products.api.ProductId

/**
 * Errors that can occur during inventory operations.
 */
sealed class InventoryError {

    /**
     * Product not found in inventory.
     */
    data class ProductNotFound(val productId: ProductId) : InventoryError()

    /**
     * Insufficient stock to fulfill the reservation.
     */
    data class InsufficientStock(
        val productId: ProductId,
        val requested: Int,
        val available: Int
    ) : InventoryError()

    /**
     * Reservation not found.
     */
    data class ReservationNotFound(val reservationId: ReservationId) : InventoryError()

    /**
     * General inventory error.
     */
    data class InventoryUnavailable(val reason: String) : InventoryError()

    /**
     * Warehouse not found.
     */
    data class WarehouseNotFound(val warehouseId: WarehouseId) : InventoryError()

    /**
     * Warehouse name already exists.
     */
    data class DuplicateWarehouseName(val name: String) : InventoryError()

    /**
     * Validation failed.
     */
    data class ValidationFailed(val reason: String) : InventoryError()

    /**
     * Invalid country code.
     */
    data class InvalidCountryCode(val countryCode: String) : InventoryError()
}
