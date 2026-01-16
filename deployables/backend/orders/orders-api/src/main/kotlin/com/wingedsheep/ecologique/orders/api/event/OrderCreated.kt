package com.wingedsheep.ecologique.orders.api.event

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import java.math.BigDecimal
import java.time.Instant

data class OrderCreated(
    val orderId: OrderId,
    val userId: String,
    val grandTotal: BigDecimal,
    val currency: Currency,
    val timestamp: Instant
)