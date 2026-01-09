package com.wingedsheep.ecologique.products.worldview

import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import java.math.BigDecimal
import java.util.UUID

fun buildWorldviewProduct(
    id: UUID = UUID.randomUUID(),
    name: String = "Test Eco Product",
    description: String = "A sustainable test product",
    category: String = "HOUSEHOLD",
    priceAmount: BigDecimal = BigDecimal("19.99"),
    priceCurrency: String = "EUR",
    weightGrams: Int = 100,
    sustainabilityRating: String = "B",
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
    category: String = "HOUSEHOLD",
    priceAmount: BigDecimal = BigDecimal("19.99"),
    priceCurrency: String = "EUR",
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
