package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.WarehouseId

internal interface WarehouseRepository {
    fun save(warehouse: Warehouse): Warehouse
    fun findById(id: WarehouseId): Warehouse?
    fun findAll(): List<Warehouse>
}
