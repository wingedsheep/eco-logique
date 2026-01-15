package com.wingedsheep.ecologique.products.impl.domain

import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.SustainabilityRating
import java.math.BigDecimal

internal object SustainabilityRatingCalculator {

    fun calculate(category: ProductCategory, carbonFootprint: CarbonFootprint): SustainabilityRating {
        val threshold = when (category) {
            ProductCategory.ELECTRONICS -> BigDecimal("5.0")
            ProductCategory.CLOTHING -> BigDecimal("3.0")
            ProductCategory.HOUSEHOLD -> BigDecimal("2.0")
            ProductCategory.FOOD -> BigDecimal("1.0")
            ProductCategory.PERSONAL_CARE -> BigDecimal("1.5")
        }

        val co2 = carbonFootprint.kgCo2
        return when {
            co2 <= threshold * BigDecimal("0.3") -> SustainabilityRating.A_PLUS
            co2 <= threshold * BigDecimal("0.5") -> SustainabilityRating.A
            co2 <= threshold * BigDecimal("0.7") -> SustainabilityRating.B
            co2 <= threshold -> SustainabilityRating.C
            else -> SustainabilityRating.D
        }
    }
}
