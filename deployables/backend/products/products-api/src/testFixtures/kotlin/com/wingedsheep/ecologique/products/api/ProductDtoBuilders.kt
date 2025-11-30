package com.wingedsheep.ecologique.products.api

import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import java.math.BigDecimal

fun buildProductDto(
    id: String = "PROD-TEST-001",
    name: String = "Test Product",
    description: String = "A test product description",
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

fun buildProductCreateRequest(
    name: String = "Test Product",
    description: String = "A test product description",
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

fun buildProductUpdatePriceRequest(
    priceAmount: BigDecimal = BigDecimal("24.99"),
    priceCurrency: String = "EUR"
): ProductUpdatePriceRequest = ProductUpdatePriceRequest(
    priceAmount = priceAmount,
    priceCurrency = priceCurrency
)
