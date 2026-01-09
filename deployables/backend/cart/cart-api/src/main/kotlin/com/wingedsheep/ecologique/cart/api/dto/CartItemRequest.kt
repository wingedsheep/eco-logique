package com.wingedsheep.ecologique.cart.api.dto

data class AddCartItemRequest(
    val productId: String,
    val quantity: Int
) {
    init {
        require(productId.isNotBlank()) { "Product ID is required" }
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
