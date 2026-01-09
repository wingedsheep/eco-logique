package com.wingedsheep.ecologique.cart.worldview

import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.CartItemDto
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JANE_KEYCLOAK_ID
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JOHN_KEYCLOAK_ID
import java.math.BigDecimal

object WorldviewCart {

    val johnDoeCart = CartDto(
        userId = JOHN_KEYCLOAK_ID,
        items = listOf(
            CartItemDto(
                productId = "PROD-001",
                productName = "Organic Cotton T-Shirt",
                unitPrice = BigDecimal("29.99"),
                quantity = 2,
                lineTotal = BigDecimal("59.98")
            ),
            CartItemDto(
                productId = "PROD-002",
                productName = "Bamboo Toothbrush Set (4 pack)",
                unitPrice = BigDecimal("12.50"),
                quantity = 1,
                lineTotal = BigDecimal("12.50")
            )
        ),
        totalItems = 3,
        subtotal = BigDecimal("72.48"),
        currency = "EUR"
    )

    val janeSmithCart = CartDto(
        userId = JANE_KEYCLOAK_ID,
        items = listOf(
            CartItemDto(
                productId = "PROD-003",
                productName = "Solar Powered Phone Charger",
                unitPrice = BigDecimal("45.00"),
                quantity = 1,
                lineTotal = BigDecimal("45.00")
            )
        ),
        totalItems = 1,
        subtotal = BigDecimal("45.00"),
        currency = "EUR"
    )

    val allCarts = listOf(johnDoeCart, janeSmithCart)

    fun findByUserId(userId: String): CartDto? =
        allCarts.find { it.userId == userId }
}
