package com.wingedsheep.ecologique.products.api.dto

import java.math.BigDecimal

data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String,
    val carbonFootprintKg: BigDecimal
)
