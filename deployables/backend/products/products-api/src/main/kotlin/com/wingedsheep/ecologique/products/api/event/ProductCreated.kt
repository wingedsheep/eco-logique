package com.wingedsheep.ecologique.products.api.event

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal
import java.time.Instant

data class ProductCreated(
    val productId: ProductId,
    val name: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val timestamp: Instant
)
