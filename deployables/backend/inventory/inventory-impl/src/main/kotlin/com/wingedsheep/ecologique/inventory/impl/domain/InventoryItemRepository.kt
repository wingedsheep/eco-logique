package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.products.api.ProductId

internal interface InventoryItemRepository {
    fun save(item: InventoryItem): InventoryItem
    fun findByProductIdAndWarehouseId(productId: ProductId, warehouseId: WarehouseId): InventoryItem?
    fun findByProductId(productId: ProductId): List<InventoryItem>
    fun findByWarehouseId(warehouseId: WarehouseId): List<InventoryItem>
    fun findAll(): List<InventoryItem>
}
