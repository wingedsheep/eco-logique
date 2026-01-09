package com.wingedsheep.ecologique.cart.impl.infrastructure.persistence

import com.wingedsheep.ecologique.cart.impl.domain.CartItem

internal fun CartItemEntity.toCartItem(): CartItem = CartItem(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)

internal fun CartItem.toEntity(userId: String): CartItemEntity = CartItemEntity(
    id = null,
    userId = userId,
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)
