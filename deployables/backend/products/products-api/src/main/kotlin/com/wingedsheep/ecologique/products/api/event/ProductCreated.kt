package com.wingedsheep.ecologique.products.api.event

import java.math.BigDecimal
import java.time.Instant

data class ProductCreated(
    val productId: String,
    val name: String,
    val category: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val timestamp: Instant
)
