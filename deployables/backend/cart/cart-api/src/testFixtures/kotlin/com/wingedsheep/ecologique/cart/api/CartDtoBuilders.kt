package com.wingedsheep.ecologique.cart.api

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.api.UserId
import java.math.BigDecimal
import java.util.UUID

fun buildCartDto(
    userId: UserId = UserId.generate(),
    items: List<CartItemDto> = listOf(buildCartItemDto()),
    totalItems: Int = items.sumOf { it.quantity },
    subtotal: BigDecimal = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.lineTotal) },
    currency: Currency = Currency.EUR
): CartDto = CartDto(
    userId = userId,
    items = items,
    totalItems = totalItems,
    subtotal = subtotal,
    currency = currency
)

fun buildCartItemDto(
    productId: ProductId = ProductId(UUID.randomUUID()),
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
    productId: ProductId = ProductId(UUID.randomUUID()),
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
