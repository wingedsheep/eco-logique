package com.wingedsheep.ecologique.products.api.dto

import java.math.BigDecimal

data class ProductUpdatePriceRequest(
    val priceAmount: BigDecimal,
    val priceCurrency: String
)
