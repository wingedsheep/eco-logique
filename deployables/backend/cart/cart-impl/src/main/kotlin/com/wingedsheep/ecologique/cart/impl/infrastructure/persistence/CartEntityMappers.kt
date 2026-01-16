package com.wingedsheep.ecologique.cart.impl.infrastructure.persistence

import com.wingedsheep.ecologique.cart.impl.domain.CartItem
import com.wingedsheep.ecologique.products.api.ProductId
import java.util.UUID

internal fun CartItemEntity.toCartItem(): CartItem = CartItem(
    productId = ProductId(UUID.fromString(productId)),
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)

internal fun CartItem.toEntity(userId: String): CartItemEntity = CartItemEntity(
    id = null,
    userId = userId,
    productId = productId.value.toString(),
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)
