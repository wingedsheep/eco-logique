package com.wingedsheep.ecologique.inventory.api.error

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
    data class ReservationNotFound(val reservationId: String) : InventoryError()

    /**
     * General inventory error.
     */
    data class InventoryUnavailable(val reason: String) : InventoryError()
}
