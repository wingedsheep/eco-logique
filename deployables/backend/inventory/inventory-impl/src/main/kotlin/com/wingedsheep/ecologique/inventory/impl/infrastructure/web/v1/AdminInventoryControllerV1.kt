package com.wingedsheep.ecologique.inventory.impl.infrastructure.web.v1

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.api.WarehouseService
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseDto
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/inventory")
@Tag(name = "Admin Inventory", description = "Warehouse and stock management (admin only)")
internal class AdminInventoryControllerV1(
    private val warehouseService: WarehouseService
) {

    @PostMapping("/warehouses")
    @Operation(summary = "Create a new warehouse")
    fun createWarehouse(@RequestBody request: WarehouseCreateRequest): ResponseEntity<WarehouseDto> {
        return warehouseService.createWarehouse(request).fold(
            onSuccess = { ResponseEntity.status(HttpStatus.CREATED).body(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @GetMapping("/warehouses")
    @Operation(summary = "List all warehouses")
    fun getAllWarehouses(): ResponseEntity<List<WarehouseDto>> {
        return warehouseService.getAllWarehouses().fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @GetMapping("/warehouses/{id}")
    @Operation(summary = "Get warehouse by ID")
    fun getWarehouse(@PathVariable id: UUID): ResponseEntity<WarehouseDto> {
        return warehouseService.getWarehouse(WarehouseId(id)).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @PutMapping("/warehouses/{id}")
    @Operation(summary = "Update warehouse")
    fun updateWarehouse(
        @PathVariable id: UUID,
        @RequestBody request: WarehouseUpdateRequest
    ): ResponseEntity<WarehouseDto> {
        return warehouseService.updateWarehouse(WarehouseId(id), request).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @DeleteMapping("/warehouses/{id}")
    @Operation(summary = "Delete warehouse")
    fun deleteWarehouse(@PathVariable id: UUID): ResponseEntity<Unit> {
        return warehouseService.deleteWarehouse(WarehouseId(id)).fold(
            onSuccess = { ResponseEntity.noContent().build() },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @PutMapping("/warehouses/{id}/stock")
    @Operation(summary = "Update stock for a product in a warehouse")
    fun updateStock(
        @PathVariable id: UUID,
        @RequestBody request: StockUpdateRequest
    ): ResponseEntity<StockLevel> {
        return warehouseService.updateStock(WarehouseId(id), request).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @GetMapping("/warehouses/{id}/stock")
    @Operation(summary = "Get all stock levels for a warehouse")
    fun getWarehouseStock(@PathVariable id: UUID): ResponseEntity<List<StockLevel>> {
        return warehouseService.getWarehouseStock(WarehouseId(id)).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }
}
