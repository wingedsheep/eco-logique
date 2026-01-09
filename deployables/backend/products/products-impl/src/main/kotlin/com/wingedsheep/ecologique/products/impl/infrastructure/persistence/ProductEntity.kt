package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("products", schema = "products")
internal class ProductEntity(
    @Id private val id: UUID,
    val name: String,
    val description: String,
    val categoryCode: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String,
    val carbonFootprintKg: BigDecimal
) : Persistable<UUID> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): ProductEntity {
        isNewEntity = false
        return this
    }
}
