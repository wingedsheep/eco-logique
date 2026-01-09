package com.wingedsheep.ecologique.cart.impl.domain

import java.math.BigDecimal

internal data class Cart(
    val userId: String,
    val items: List<CartItem>
) {
    init {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
    }

    val totalItems: Int
        get() = items.sumOf { it.quantity }

    val subtotal: BigDecimal
        get() = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.lineTotal) }

    fun addItem(item: CartItem): Cart {
        val existingItem = items.find { it.productId == item.productId }
        return if (existingItem != null) {
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + item.quantity)
            copy(items = items.map { if (it.productId == item.productId) updatedItem else it })
        } else {
            copy(items = items + item)
        }
    }

    fun updateItemQuantity(productId: String, quantity: Int): Cart {
        require(quantity > 0) { "Quantity must be positive" }
        return copy(items = items.map {
            if (it.productId == productId) it.copy(quantity = quantity) else it
        })
    }

    fun removeItem(productId: String): Cart {
        return copy(items = items.filter { it.productId != productId })
    }

    fun containsProduct(productId: String): Boolean {
        return items.any { it.productId == productId }
    }

    fun clear(): Cart = copy(items = emptyList())

    companion object {
        fun empty(userId: String): Cart = Cart(
            userId = userId,
            items = emptyList()
        )
    }
}
