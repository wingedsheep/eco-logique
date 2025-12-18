package com.wingedsheep.ecologique.orders.api.event

import java.math.BigDecimal
import java.time.Instant

data class OrderCreated(
    val orderId: String,
    val userId: String,
    val grandTotal: BigDecimal,
    val currency: String,
    val timestamp: Instant
)