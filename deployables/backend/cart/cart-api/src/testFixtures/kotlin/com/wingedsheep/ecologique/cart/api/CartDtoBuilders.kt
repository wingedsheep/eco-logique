package com.wingedsheep.ecologique.cart.api

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import java.math.BigDecimal

fun buildCartDto(
    userId: String = "USER-TEST-001",
    items: List<CartItemDto> = listOf(buildCartItemDto()),
    totalItems: Int = items.sumOf { it.quantity },
    subtotal: BigDecimal = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.lineTotal) },
    currency: String = "EUR"
): CartDto = CartDto(
    userId = userId,
    items = items,
    totalItems = totalItems,
    subtotal = subtotal,
    currency = currency
)

fun buildCartItemDto(
    productId: String = "PROD-TEST-001",
    productName: String = "Test Product",
    unitPrice: BigDecimal = BigDecimal("29.99"),
    quantity: Int = 1,
    lineTotal: BigDecimal = unitPrice.multiply(BigDecimal(quantity))
): CartItemDto = CartItemDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)

fun buildAddCartItemRequest(
    productId: String = "PROD-TEST-001",
    quantity: Int = 1
): AddCartItemRequest = AddCartItemRequest(
    productId = productId,
    quantity = quantity
)

fun buildUpdateCartItemRequest(
    quantity: Int = 2
): UpdateCartItemRequest = UpdateCartItemRequest(
    quantity = quantity
)
