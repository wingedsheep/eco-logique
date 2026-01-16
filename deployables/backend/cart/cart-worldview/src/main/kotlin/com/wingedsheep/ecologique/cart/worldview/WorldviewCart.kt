package com.wingedsheep.ecologique.cart.worldview

import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.api.UserId
import java.math.BigDecimal
import java.util.UUID

object WorldviewCart {

    private val JOHN_USER_ID = UserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"))
    private val JANE_USER_ID = UserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))

    val johnDoeCart = CartDto(
        userId = JOHN_USER_ID,
        items = listOf(
            CartItemDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                productName = "Organic Cotton T-Shirt",
                unitPrice = BigDecimal("29.99"),
                quantity = 2,
                lineTotal = BigDecimal("59.98")
            ),
            CartItemDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                productName = "Bamboo Toothbrush Set (4 pack)",
                unitPrice = BigDecimal("12.50"),
                quantity = 1,
                lineTotal = BigDecimal("12.50")
            )
        ),
        totalItems = 3,
        subtotal = BigDecimal("72.48"),
        currency = Currency.EUR
    )

    val janeSmithCart = CartDto(
        userId = JANE_USER_ID,
        items = listOf(
            CartItemDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000003")),
                productName = "Solar Powered Phone Charger",
                unitPrice = BigDecimal("45.00"),
                quantity = 1,
                lineTotal = BigDecimal("45.00")
            )
        ),
        totalItems = 1,
        subtotal = BigDecimal("45.00"),
        currency = Currency.EUR
    )

    val allCarts = listOf(johnDoeCart, janeSmithCart)

    fun findByUserId(userId: UserId): CartDto? =
        allCarts.find { it.userId == userId }
}
