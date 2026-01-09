package com.wingedsheep.ecologique.cart.api.error

sealed class CartError {
    data class ProductNotFound(val productId: String) : CartError()
    data class ItemNotFound(val productId: String) : CartError()
    data class InvalidQuantity(val quantity: Int) : CartError()
    data class ValidationFailed(val reason: String) : CartError()
}
