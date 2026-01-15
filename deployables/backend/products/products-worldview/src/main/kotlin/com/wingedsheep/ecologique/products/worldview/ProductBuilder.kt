package com.wingedsheep.ecologique.products.worldview

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.SustainabilityRating
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import java.math.BigDecimal

fun buildWorldviewProduct(
    id: ProductId = ProductId.generate(),
    name: String = "Test Eco Product",
    description: String = "A sustainable test product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    priceAmount: BigDecimal = BigDecimal("19.99"),
    priceCurrency: Currency = Currency.EUR,
    weightGrams: Int = 100,
    sustainabilityRating: SustainabilityRating = SustainabilityRating.B,
    carbonFootprintKg: BigDecimal = BigDecimal("1.5")
): ProductDto = ProductDto(
    id = id,
    name = name,
    description = description,
    category = category,
    priceAmount = priceAmount,
    priceCurrency = priceCurrency,
    weightGrams = weightGrams,
    sustainabilityRating = sustainabilityRating,
    carbonFootprintKg = carbonFootprintKg
)

fun buildWorldviewProductCreateRequest(
    name: String = "Test Eco Product",
    description: String = "A sustainable test product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    priceAmount: BigDecimal = BigDecimal("19.99"),
    priceCurrency: Currency = Currency.EUR,
    weightGrams: Int = 100,
    carbonFootprintKg: BigDecimal = BigDecimal("1.5")
): ProductCreateRequest = ProductCreateRequest(
    name = name,
    description = description,
    category = category,
    priceAmount = priceAmount,
    priceCurrency = priceCurrency,
    weightGrams = weightGrams,
    carbonFootprintKg = carbonFootprintKg
)
