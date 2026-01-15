package com.wingedsheep.ecologique.products.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.SustainabilityRating
import java.math.BigDecimal

internal data class Product(
    val id: ProductId,
    val name: String,
    val description: String,
    val category: ProductCategory,
    val price: Money,
    val weight: Weight,
    val sustainabilityRating: SustainabilityRating,
    val carbonFootprint: CarbonFootprint
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(name.length <= 255) { "Product name cannot exceed 255 characters" }
        require(description.length <= 2000) { "Product description cannot exceed 2000 characters" }
        require(price.amount > BigDecimal.ZERO) { "Price must be positive" }
    }

    fun updatePrice(newPrice: Money): Product {
        require(newPrice.amount > BigDecimal.ZERO) { "Price must be positive" }
        return copy(price = newPrice)
    }

    companion object {
        fun create(
            name: String,
            description: String,
            category: ProductCategory,
            priceAmount: BigDecimal,
            priceCurrency: Currency,
            weightGrams: Int,
            carbonFootprintKg: BigDecimal
        ): Product {
            val carbonFootprint = CarbonFootprint(carbonFootprintKg)
            return Product(
                id = ProductId.generate(),
                name = name,
                description = description,
                category = category,
                price = Money(priceAmount, priceCurrency),
                weight = Weight(weightGrams),
                sustainabilityRating = SustainabilityRatingCalculator.calculate(category, carbonFootprint),
                carbonFootprint = carbonFootprint
            )
        }
    }
}
