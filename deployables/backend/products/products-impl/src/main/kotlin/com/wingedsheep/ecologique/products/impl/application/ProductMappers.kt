package com.wingedsheep.ecologique.products.impl.application

import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.impl.domain.Product

internal fun Product.toDto(): ProductDto = ProductDto(
    id = id.value,
    name = name,
    description = description,
    category = category.name,
    priceAmount = price.amount,
    priceCurrency = price.currency.name,
    weightGrams = weight.grams,
    sustainabilityRating = sustainabilityRating.name,
    carbonFootprintKg = carbonFootprint.kgCo2
)
