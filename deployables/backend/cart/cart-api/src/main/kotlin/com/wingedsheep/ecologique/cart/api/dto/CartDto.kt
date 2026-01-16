package com.wingedsheep.ecologique.cart.api.dto

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.api.UserId
import java.math.BigDecimal

data class CartDto(
    val userId: UserId,
    val items: List<CartItemDto>,
    val totalItems: Int,
    val subtotal: BigDecimal,
    val currency: Currency
)

data class CartItemDto(
    val productId: ProductId,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)
