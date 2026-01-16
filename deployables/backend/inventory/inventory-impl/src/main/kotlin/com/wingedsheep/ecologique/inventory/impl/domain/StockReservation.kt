package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.products.api.ProductId
import java.time.Instant

/**
 * Status of a stock reservation.
 */
internal enum class ReservationStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}

/**
 * Represents a stock reservation for a product.
 */
internal data class StockReservation(
    val id: ReservationId,
    val productId: ProductId,
    val warehouseId: WarehouseId,
    val quantity: Int,
    val correlationId: String,
    val status: ReservationStatus,
    val createdAt: Instant
) {
    fun complete(): StockReservation = copy(status = ReservationStatus.COMPLETED)

    fun cancel(): StockReservation = copy(status = ReservationStatus.CANCELLED)

    val isActive: Boolean get() = status == ReservationStatus.ACTIVE
}
