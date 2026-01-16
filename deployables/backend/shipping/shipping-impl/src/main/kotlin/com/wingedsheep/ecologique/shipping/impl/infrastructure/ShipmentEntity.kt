package com.wingedsheep.ecologique.shipping.impl.infrastructure

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Table("shipments", schema = "shipping")
internal class ShipmentEntity(
    @Id private val id: UUID,
    val orderId: UUID,
    val trackingNumber: String,
    val status: String,
    val recipientName: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String,
    val warehouseId: UUID,
    val weightKg: BigDecimal?,
    val createdAt: Instant,
    val shippedAt: Instant?,
    val deliveredAt: Instant?
) : Persistable<UUID> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): ShipmentEntity {
        isNewEntity = false
        return this
    }
}
