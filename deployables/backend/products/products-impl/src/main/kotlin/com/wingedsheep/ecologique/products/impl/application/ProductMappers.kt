package com.wingedsheep.ecologique.products.impl.application

import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.impl.domain.Product

internal fun Product.toDto(): ProductDto = ProductDto(
    id = id,
    name = name,
    description = description,
    category = category,
    priceAmount = price.amount,
    priceCurrency = price.currency,
    weightGrams = weight.grams,
    sustainabilityRating = sustainabilityRating,
    carbonFootprintKg = carbonFootprint.kgCo2
)
