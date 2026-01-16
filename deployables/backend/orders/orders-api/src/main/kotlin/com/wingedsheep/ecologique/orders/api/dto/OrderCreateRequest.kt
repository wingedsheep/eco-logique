package com.wingedsheep.ecologique.orders.api.dto

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal

data class OrderCreateRequest(
    val lines: List<OrderLineCreateRequest>,
    val currency: Currency
) {
    init {
        require(lines.isNotEmpty()) { "Order must have at least one line" }
    }
}

data class OrderLineCreateRequest(
    val productId: ProductId,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) {
    init {
        require(productName.isNotBlank()) { "Product name is required" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(quantity > 0) { "Quantity must be positive" }
    }
}