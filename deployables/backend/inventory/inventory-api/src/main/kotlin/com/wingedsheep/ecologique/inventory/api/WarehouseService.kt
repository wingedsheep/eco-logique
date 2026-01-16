package com.wingedsheep.ecologique.inventory.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseDto
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseUpdateRequest
import com.wingedsheep.ecologique.inventory.api.error.InventoryError

/**
 * Service for managing warehouses and their stock.
 */
interface WarehouseService {

    /**
     * Creates a new warehouse.
     */
    fun createWarehouse(request: WarehouseCreateRequest): Result<WarehouseDto, InventoryError>

    /**
     * Updates an existing warehouse.
     */
    fun updateWarehouse(id: WarehouseId, request: WarehouseUpdateRequest): Result<WarehouseDto, InventoryError>

    /**
     * Deletes a warehouse.
     */
    fun deleteWarehouse(id: WarehouseId): Result<Unit, InventoryError>

    /**
     * Gets a warehouse by ID.
     */
    fun getWarehouse(id: WarehouseId): Result<WarehouseDto, InventoryError>

    /**
     * Gets all warehouses.
     */
    fun getAllWarehouses(): Result<List<WarehouseDto>, InventoryError>

    /**
     * Updates stock for a product in a warehouse.
     */
    fun updateStock(warehouseId: WarehouseId, request: StockUpdateRequest): Result<StockLevel, InventoryError>

    /**
     * Gets all stock levels for a warehouse.
     */
    fun getWarehouseStock(warehouseId: WarehouseId): Result<List<StockLevel>, InventoryError>
}
