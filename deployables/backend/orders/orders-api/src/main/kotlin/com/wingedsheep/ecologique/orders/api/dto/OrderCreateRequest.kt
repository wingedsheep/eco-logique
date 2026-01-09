package com.wingedsheep.ecologique.orders.api.dto

import java.math.BigDecimal

data class OrderCreateRequest(
    val lines: List<OrderLineCreateRequest>,
    val currency: String
) {
    init {
        require(lines.isNotEmpty()) { "Order must have at least one line" }
        require(currency.isNotBlank()) { "Currency is required" }
    }
}

data class OrderLineCreateRequest(
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) {
    init {
        require(productId.isNotBlank()) { "Product ID is required" }
        require(productName.isNotBlank()) { "Product name is required" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(quantity > 0) { "Quantity must be positive" }
    }
}