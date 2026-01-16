package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("stock_reservations", schema = "inventory")
internal class StockReservationEntity(
    @Id private val id: UUID,
    val productId: UUID,
    val warehouseId: UUID,
    val quantity: Int,
    val correlationId: String,
    val status: String,
    val createdAt: Instant
) : Persistable<UUID> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): StockReservationEntity {
        isNewEntity = false
        return this
    }
}
