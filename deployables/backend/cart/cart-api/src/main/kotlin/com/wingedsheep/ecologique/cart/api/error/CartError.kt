package com.wingedsheep.ecologique.cart.api.error

import com.wingedsheep.ecologique.products.api.ProductId

sealed class CartError {
    data class ProductNotFound(val productId: ProductId) : CartError()
    data class ItemNotFound(val productId: ProductId) : CartError()
    data class InvalidQuantity(val quantity: Int) : CartError()
    data class ValidationFailed(val reason: String) : CartError()
}
