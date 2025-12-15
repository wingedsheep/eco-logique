package com.wingedsheep.ecologique.products.impl.domain

internal enum class SustainabilityRating {
    A_PLUS,
    A,
    B,
    C,
    D;

    companion object {
        fun calculate(category: ProductCategory, carbonFootprint: CarbonFootprint): SustainabilityRating {
            val threshold = when (category) {
                ProductCategory.ELECTRONICS -> 5.0
                ProductCategory.CLOTHING -> 3.0
                ProductCategory.HOUSEHOLD -> 2.0
                ProductCategory.FOOD -> 1.0
                ProductCategory.PERSONAL_CARE -> 1.5
            }

            return when {
                carbonFootprint.kgCo2.toDouble() <= threshold * 0.3 -> A_PLUS
                carbonFootprint.kgCo2.toDouble() <= threshold * 0.5 -> A
                carbonFootprint.kgCo2.toDouble() <= threshold * 0.7 -> B
                carbonFootprint.kgCo2.toDouble() <= threshold -> C
                else -> D
            }
        }
    }
}
