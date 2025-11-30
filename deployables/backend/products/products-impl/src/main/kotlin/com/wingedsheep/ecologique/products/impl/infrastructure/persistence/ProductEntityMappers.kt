package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import com.ecologique.common.money.Currency
import com.ecologique.common.money.Money
import com.wingedsheep.ecologique.products.impl.domain.model.CarbonFootprint
import com.wingedsheep.ecologique.products.impl.domain.model.Product
import com.wingedsheep.ecologique.products.impl.domain.model.ProductCategory
import com.wingedsheep.ecologique.products.impl.domain.model.ProductId
import com.wingedsheep.ecologique.products.impl.domain.model.SustainabilityRating
import com.wingedsheep.ecologique.products.impl.domain.model.Weight

internal fun ProductEntity.toProduct(): Product = Product(
    id = ProductId(id),
    name = name,
    description = description,
    category = ProductCategory.valueOf(categoryCode),
    price = Money(priceAmount, Currency.valueOf(priceCurrency)),
    weight = Weight(weightGrams),
    sustainabilityRating = SustainabilityRating.valueOf(sustainabilityRating),
    carbonFootprint = CarbonFootprint(carbonFootprintKg)
)

internal fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id.value,
    name = name,
    description = description,
    categoryCode = category.name,
    priceAmount = price.amount,
    priceCurrency = price.currency.name,
    weightGrams = weight.grams,
    sustainabilityRating = sustainabilityRating.name,
    carbonFootprintKg = carbonFootprint.kgCo2
)
