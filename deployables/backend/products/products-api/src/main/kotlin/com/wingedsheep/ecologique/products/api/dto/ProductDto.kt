package com.wingedsheep.ecologique.products.api.dto

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.SustainabilityRating
import java.math.BigDecimal

data class ProductDto(
    val id: ProductId,
    val name: String,
    val description: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val weightGrams: Int,
    val sustainabilityRating: SustainabilityRating,
    val carbonFootprintKg: BigDecimal
)
