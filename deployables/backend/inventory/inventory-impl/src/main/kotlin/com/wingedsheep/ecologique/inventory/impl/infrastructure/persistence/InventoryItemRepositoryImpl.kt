package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItem
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItemRepository
import com.wingedsheep.ecologique.products.api.ProductId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Component
internal class InventoryItemRepositoryImpl(
    private val jdbc: InventoryItemRepositoryJdbc
) : InventoryItemRepository {

    override fun save(item: InventoryItem): InventoryItem {
        val existing = jdbc.findByProductIdAndWarehouseId(item.productId.value, item.warehouseId.value)
        val entity = item.toEntity(existing?.id)
        if (existing != null) {
            entity.markAsExisting()
        }
        return jdbc.save(entity).toDomain()
    }

    override fun findByProductIdAndWarehouseId(productId: ProductId, warehouseId: WarehouseId): InventoryItem? {
        return jdbc.findByProductIdAndWarehouseId(productId.value, warehouseId.value)?.toDomain()
    }

    override fun findByProductId(productId: ProductId): List<InventoryItem> {
        return jdbc.findByProductId(productId.value).map { it.toDomain() }
    }

    override fun findByWarehouseId(warehouseId: WarehouseId): List<InventoryItem> {
        return jdbc.findByWarehouseId(warehouseId.value).map { it.toDomain() }
    }

    override fun findAll(): List<InventoryItem> {
        return jdbc.findAll().map { it.toDomain() }
    }
}

@Repository
internal interface InventoryItemRepositoryJdbc : CrudRepository<InventoryItemEntity, Long> {
    fun findByProductIdAndWarehouseId(productId: UUID, warehouseId: UUID): InventoryItemEntity?
    fun findByProductId(productId: UUID): List<InventoryItemEntity>
    fun findByWarehouseId(warehouseId: UUID): List<InventoryItemEntity>
}
