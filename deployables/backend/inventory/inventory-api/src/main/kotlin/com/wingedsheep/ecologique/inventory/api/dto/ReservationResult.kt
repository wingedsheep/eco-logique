package com.wingedsheep.ecologique.inventory.api.dto

import com.wingedsheep.ecologique.products.api.ProductId

/**
 * Result of a successful stock reservation.
 */
data class ReservationResult(
    val reservationId: String,
    val productId: ProductId,
    val quantity: Int,
    val correlationId: String
)
