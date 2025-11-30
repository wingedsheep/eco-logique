package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("products", schema = "products")
data class ProductEntity(
    @Id val id: String,
    val name: String,
    val description: String,
    val categoryCode: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String,
    val carbonFootprintKg: BigDecimal
)
