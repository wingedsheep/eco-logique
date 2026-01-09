package com.wingedsheep.ecologique.products.api.dto

import java.math.BigDecimal
import java.util.UUID

data class ProductDto(
    val id: UUID,
    val name: String,
    val description: String,
    val category: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String,
    val carbonFootprintKg: BigDecimal
)
