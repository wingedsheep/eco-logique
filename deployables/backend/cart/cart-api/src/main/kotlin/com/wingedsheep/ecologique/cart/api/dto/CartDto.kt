package com.wingedsheep.ecologique.cart.api.dto

import java.math.BigDecimal

data class CartDto(
    val userId: String,
    val items: List<CartItemDto>,
    val totalItems: Int,
    val subtotal: BigDecimal,
    val currency: String
)

data class CartItemDto(
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)
