package com.wingedsheep.ecologique.cart.impl.domain

import java.math.BigDecimal

internal data class CartItem(
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) {
    init {
        require(productId.isNotBlank()) { "Product ID cannot be blank" }
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(quantity > 0) { "Quantity must be positive" }
    }

    val lineTotal: BigDecimal
        get() = unitPrice.multiply(BigDecimal(quantity))

    companion object {
        fun create(
            productId: String,
            productName: String,
            unitPrice: BigDecimal,
            quantity: Int
        ): CartItem = CartItem(
            productId = productId,
            productName = productName,
            unitPrice = unitPrice,
            quantity = quantity
        )
    }
}
