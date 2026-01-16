package com.wingedsheep.ecologique.inventory.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.dto.ReservationResult
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.error.InventoryError
import com.wingedsheep.ecologique.products.api.ProductId

/**
 * Service for managing inventory and stock reservations.
 */
interface InventoryService {

    /**
     * Checks the available stock for a product.
     *
     * @param productId The product to check stock for.
     * @return [Result.Ok] with [StockLevel] if found,
     *         or [Result.Err] with [InventoryError] if not found.
     */
    fun checkStock(productId: ProductId): Result<StockLevel, InventoryError>

    /**
     * Reserves stock for a business operation.
     *
     * @param productId The product to reserve.
     * @param quantity The quantity to reserve.
     * @param correlationId Caller-provided identifier to track this reservation (e.g., order ID, transfer ID).
     * @return [Result.Ok] with [ReservationResult] on success,
     *         or [Result.Err] with [InventoryError] on failure.
     */
    fun reserveStock(
        productId: ProductId,
        quantity: Int,
        correlationId: String
    ): Result<ReservationResult, InventoryError>

    /**
     * Releases a previously made reservation.
     *
     * @param reservationId The reservation to release.
     * @return [Result.Ok] on success, or [Result.Err] on failure.
     */
    fun releaseReservation(reservationId: ReservationId): Result<Unit, InventoryError>
}
