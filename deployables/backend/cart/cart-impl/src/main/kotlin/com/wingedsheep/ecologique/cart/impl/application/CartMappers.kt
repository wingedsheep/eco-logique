package com.wingedsheep.ecologique.cart.impl.application

import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import com.wingedsheep.ecologique.cart.impl.domain.Cart
import com.wingedsheep.ecologique.cart.impl.domain.CartItem

internal fun Cart.toDto(): CartDto = CartDto(
    userId = userId,
    items = items.map { it.toDto() },
    totalItems = totalItems,
    subtotal = subtotal,
    currency = "EUR"
)

internal fun CartItem.toDto(): CartItemDto = CartItemDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)
