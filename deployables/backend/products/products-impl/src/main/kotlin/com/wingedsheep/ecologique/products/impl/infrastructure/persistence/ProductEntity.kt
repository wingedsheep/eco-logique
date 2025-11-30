package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("products", schema = "products")
internal class ProductEntity(
    @Id private val id: String,
    val name: String,
    val description: String,
    val categoryCode: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String,
    val carbonFootprintKg: BigDecimal
) : Persistable<String> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): String = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): ProductEntity {
        isNewEntity = false
        return this
    }
}
