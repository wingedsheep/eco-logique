package com.wingedsheep.ecologique.products.impl.domain.model

internal enum class ProductCategory {
    CLOTHING,
    HOUSEHOLD,
    ELECTRONICS,
    FOOD,
    PERSONAL_CARE;

    companion object {
        fun fromString(value: String): ProductCategory? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
