package com.wingedsheep.ecologique.orders.api.dto

import java.math.BigDecimal
import java.time.Instant

data class OrderDto(
    val id: String,
    val userId: String,
    val status: String,
    val lines: List<OrderLineDto>,
    val subtotal: BigDecimal,
    val grandTotal: BigDecimal,
    val currency: String,
    val createdAt: Instant
)

data class OrderLineDto(
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)