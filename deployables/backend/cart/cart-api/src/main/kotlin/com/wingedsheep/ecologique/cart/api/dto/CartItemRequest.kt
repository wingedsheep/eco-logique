package com.wingedsheep.ecologique.cart.api.dto

import com.wingedsheep.ecologique.products.api.ProductId

data class AddCartItemRequest(
    val productId: ProductId,
    val quantity: Int
) {
    init {
        require(quantity > 0) { "Quantity must be positive" }
    }
}

data class UpdateCartItemRequest(
    val quantity: Int
) {
    init {
        require(quantity > 0) { "Quantity must be positive" }
    }
}
