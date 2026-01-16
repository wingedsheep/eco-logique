package com.wingedsheep.ecologique.inventory.impl.application

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.api.WarehouseService
import com.wingedsheep.ecologique.inventory.api.dto.AddressDto
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseDto
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseUpdateRequest
import com.wingedsheep.ecologique.inventory.api.error.InventoryError
import com.wingedsheep.ecologique.inventory.impl.domain.Address
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItem
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItemRepository
import com.wingedsheep.ecologique.inventory.impl.domain.Warehouse
import com.wingedsheep.ecologique.inventory.impl.domain.WarehouseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class WarehouseServiceImpl(
    private val warehouseRepository: WarehouseRepository,
    private val inventoryItemRepository: InventoryItemRepository
) : WarehouseService {

    override fun createWarehouse(request: WarehouseCreateRequest): Result<WarehouseDto, InventoryError> {
        // Validate country code
        val country = Country.fromCode(request.countryCode)
            ?: return Result.err(InventoryError.InvalidCountryCode(request.countryCode))

        // Check for duplicate name
        val existing = warehouseRepository.findByName(request.name)
        if (existing != null) {
            return Result.err(InventoryError.DuplicateWarehouseName(request.name))
        }

        // Validate address country code if provided
        val address = request.address?.let { addressDto ->
            val addressCountry = Country.fromCode(addressDto.countryCode)
                ?: return Result.err(InventoryError.InvalidCountryCode(addressDto.countryCode))
            Address(
                street = addressDto.street,
                houseNumber = addressDto.houseNumber,
                postalCode = addressDto.postalCode,
                city = addressDto.city,
                country = addressCountry
            )
        }

        val warehouse = Warehouse(
            id = WarehouseId.generate(),
            name = request.name,
            countryCode = country.name,
            address = address
        )

        val saved = warehouseRepository.save(warehouse)
        return Result.ok(saved.toDto())
    }

    override fun updateWarehouse(
        id: WarehouseId,
        request: WarehouseUpdateRequest
    ): Result<WarehouseDto, InventoryError> {
        val existing = warehouseRepository.findById(id)
            ?: return Result.err(InventoryError.WarehouseNotFound(id))

        // Validate country code if provided
        val countryCode = request.countryCode?.let { code ->
            Country.fromCode(code)?.name
                ?: return Result.err(InventoryError.InvalidCountryCode(code))
        } ?: existing.countryCode

        // Check for duplicate name if changing
        request.name?.let { newName ->
            if (newName != existing.name) {
                val duplicate = warehouseRepository.findByName(newName)
                if (duplicate != null) {
                    return Result.err(InventoryError.DuplicateWarehouseName(newName))
                }
            }
        }

        // Validate address country code if provided
        val address = request.address?.let { addressDto ->
            val addressCountry = Country.fromCode(addressDto.countryCode)
                ?: return Result.err(InventoryError.InvalidCountryCode(addressDto.countryCode))
            Address(
                street = addressDto.street,
                houseNumber = addressDto.houseNumber,
                postalCode = addressDto.postalCode,
                city = addressDto.city,
                country = addressCountry
            )
        } ?: existing.address

        val updated = existing.copy(
            name = request.name ?: existing.name,
            countryCode = countryCode,
            address = address
        )

        val saved = warehouseRepository.save(updated)
        return Result.ok(saved.toDto())
    }

    override fun deleteWarehouse(id: WarehouseId): Result<Unit, InventoryError> {
        if (!warehouseRepository.existsById(id)) {
            return Result.err(InventoryError.WarehouseNotFound(id))
        }
        warehouseRepository.delete(id)
        return Result.ok(Unit)
    }

    override fun getWarehouse(id: WarehouseId): Result<WarehouseDto, InventoryError> {
        val warehouse = warehouseRepository.findById(id)
            ?: return Result.err(InventoryError.WarehouseNotFound(id))
        return Result.ok(warehouse.toDto())
    }

    override fun getAllWarehouses(): Result<List<WarehouseDto>, InventoryError> {
        val warehouses = warehouseRepository.findAll()
        return Result.ok(warehouses.map { it.toDto() })
    }

    @Transactional
    override fun updateStock(
        warehouseId: WarehouseId,
        request: StockUpdateRequest
    ): Result<StockLevel, InventoryError> {
        if (!warehouseRepository.existsById(warehouseId)) {
            return Result.err(InventoryError.WarehouseNotFound(warehouseId))
        }

        val existing = inventoryItemRepository.findByProductIdAndWarehouseId(
            request.productId,
            warehouseId
        )

        val item = if (existing != null) {
            existing.copy(quantityOnHand = request.quantityOnHand)
        } else {
            InventoryItem(
                productId = request.productId,
                warehouseId = warehouseId,
                quantityOnHand = request.quantityOnHand,
                quantityReserved = 0
            )
        }

        val saved = inventoryItemRepository.save(item)
        return Result.ok(
            StockLevel(
                productId = saved.productId,
                available = saved.quantityAvailable,
                reserved = saved.quantityReserved
            )
        )
    }

    override fun getWarehouseStock(warehouseId: WarehouseId): Result<List<StockLevel>, InventoryError> {
        if (!warehouseRepository.existsById(warehouseId)) {
            return Result.err(InventoryError.WarehouseNotFound(warehouseId))
        }

        val items = inventoryItemRepository.findByWarehouseId(warehouseId)
        return Result.ok(
            items.map { item ->
                StockLevel(
                    productId = item.productId,
                    available = item.quantityAvailable,
                    reserved = item.quantityReserved
                )
            }
        )
    }
}

private fun Warehouse.toDto() = WarehouseDto(
    id = id,
    name = name,
    countryCode = countryCode,
    address = address?.toDto()
)

private fun Address.toDto() = AddressDto(
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    countryCode = country.name
)
