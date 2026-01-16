package com.wingedsheep.ecologique.cart.worldview

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.api.UserId
import java.math.BigDecimal

fun buildWorldviewCartDto(
    userId: UserId = UserId.generate(),
    items: List<CartItemDto> = listOf(buildWorldviewCartItemDto()),
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

fun buildWorldviewCartItemDto(
    productId: ProductId = ProductId.generate(),
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
    productId: ProductId = ProductId.generate(),
    quantity: Int = 1
): AddCartItemRequest = AddCartItemRequest(
    productId = productId,
    quantity = quantity
)
