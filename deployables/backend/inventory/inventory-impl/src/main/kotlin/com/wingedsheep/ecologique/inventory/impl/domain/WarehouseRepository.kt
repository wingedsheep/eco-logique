package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.WarehouseId

internal interface WarehouseRepository {
    fun save(warehouse: Warehouse): Warehouse
    fun findById(id: WarehouseId): Warehouse?
    fun findByName(name: String): Warehouse?
    fun findAll(): List<Warehouse>
    fun delete(id: WarehouseId)
    fun existsById(id: WarehouseId): Boolean
}
