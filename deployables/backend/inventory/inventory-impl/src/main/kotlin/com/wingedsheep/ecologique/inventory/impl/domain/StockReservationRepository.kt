package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.products.api.ProductId

internal interface StockReservationRepository {
    fun save(reservation: StockReservation): StockReservation
    fun findById(id: ReservationId): StockReservation?
    fun findByProductIdAndStatus(productId: ProductId, status: ReservationStatus): List<StockReservation>
    fun findActiveByProductId(productId: ProductId): List<StockReservation>
}
