package com.wingedsheep.ecologique.products.impl.application.service

import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.impl.domain.model.Product

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
