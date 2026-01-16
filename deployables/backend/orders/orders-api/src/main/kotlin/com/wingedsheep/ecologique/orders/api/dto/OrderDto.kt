package com.wingedsheep.ecologique.orders.api.dto

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal
import java.time.Instant

data class OrderDto(
    val id: OrderId,
    val userId: String,
    val status: OrderStatus,
    val lines: List<OrderLineDto>,
    val subtotal: BigDecimal,
    val grandTotal: BigDecimal,
    val currency: Currency,
    val createdAt: Instant
)

data class OrderLineDto(
    val productId: ProductId,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)