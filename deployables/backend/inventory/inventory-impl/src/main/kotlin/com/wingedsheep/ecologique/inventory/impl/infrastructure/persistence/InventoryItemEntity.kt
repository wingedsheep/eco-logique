package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("inventory_items", schema = "inventory")
internal class InventoryItemEntity(
    @Id private val id: Long = 0L,
    val productId: UUID,
    val warehouseId: UUID,
    val quantityOnHand: Int,
    val quantityReserved: Int
) : Persistable<Long> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): Long = id

    override fun isNew(): Boolean = id == 0L || isNewEntity

    fun markAsExisting(): InventoryItemEntity {
        isNewEntity = false
        return this
    }
}
