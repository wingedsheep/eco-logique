package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("warehouses", schema = "inventory")
internal class WarehouseEntity(
    @Id private val id: UUID,
    val name: String,
    val countryCode: String,
    val street: String? = null,
    val houseNumber: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    @Column("country_code_address")
    val countryCodeAddress: String? = null
) : Persistable<UUID> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): WarehouseEntity {
        isNewEntity = false
        return this
    }
}
