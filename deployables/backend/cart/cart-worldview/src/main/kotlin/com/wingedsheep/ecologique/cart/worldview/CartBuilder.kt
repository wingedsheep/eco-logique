package com.wingedsheep.ecologique.cart.worldview

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import java.math.BigDecimal

fun buildWorldviewCartDto(
    userId: String = "worldview-user",
    items: List<CartItemDto> = listOf(buildWorldviewCartItemDto()),
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

fun buildWorldviewCartItemDto(
    productId: String = "PROD-WV-001",
    productName: String = "Worldview Product",
    unitPrice: BigDecimal = BigDecimal("19.99"),
    quantity: Int = 1,
    lineTotal: BigDecimal = unitPrice.multiply(BigDecimal(quantity))
): CartItemDto = CartItemDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)

fun buildWorldviewAddCartItemRequest(
    productId: String = "PROD-WV-001",
    quantity: Int = 1
): AddCartItemRequest = AddCartItemRequest(
    productId = productId,
    quantity = quantity
)
