package com.wingedsheep.ecologique.cart.impl.domain

import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal

internal data class CartItem(
    val productId: ProductId,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int
) {
    init {
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(quantity > 0) { "Quantity must be positive" }
    }

    val lineTotal: BigDecimal
        get() = unitPrice.multiply(BigDecimal(quantity))

    companion object {
        fun create(
            productId: ProductId,
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
