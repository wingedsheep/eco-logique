package com.wingedsheep.ecologique.products.api.dto

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductCategory
import java.math.BigDecimal

data class ProductCreateRequest(
    val name: String,
    val description: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val weightGrams: Int,
    val carbonFootprintKg: BigDecimal
)
