package com.wingedsheep.ecologique.orders.impl.domain

import java.math.BigDecimal

internal data class OrderLine(
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
) {
    init {
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(quantity > 0) { "Quantity must be positive" }
        require(lineTotal >= BigDecimal.ZERO) { "Line total must be non-negative" }
    }

    companion object {
        fun create(
            productId: String,
            productName: String,
            unitPrice: BigDecimal,
            quantity: Int
        ): OrderLine = OrderLine(
            productId = productId,
            productName = productName,
            unitPrice = unitPrice,
            quantity = quantity,
            lineTotal = unitPrice.multiply(BigDecimal(quantity))
        )
    }
}