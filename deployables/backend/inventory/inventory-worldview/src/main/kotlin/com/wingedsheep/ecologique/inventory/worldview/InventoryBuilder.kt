package com.wingedsheep.ecologique.inventory.worldview

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.api.dto.AddressDto
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseDto
import com.wingedsheep.ecologique.products.api.ProductId
import java.util.UUID

object InventoryBuilder {

    fun warehouseDto(
        id: WarehouseId = WarehouseId(UUID.randomUUID()),
        name: String = "Test Warehouse",
        countryCode: String = "NL",
        address: AddressDto? = null
    ) = WarehouseDto(
        id = id,
        name = name,
        countryCode = countryCode,
        address = address
    )

    fun warehouseCreateRequest(
        name: String = "Test Warehouse",
        countryCode: String = "NL",
        address: AddressDto? = null
    ) = WarehouseCreateRequest(
        name = name,
        countryCode = countryCode,
        address = address
    )

    fun addressDto(
        street: String = "Test Street",
        houseNumber: String = "1",
        postalCode: String = "1234 AB",
        city: String = "Test City",
        countryCode: String = "NL"
    ) = AddressDto(
        street = street,
        houseNumber = houseNumber,
        postalCode = postalCode,
        city = city,
        countryCode = countryCode
    )

    fun stockLevel(
        productId: ProductId = ProductId(UUID.randomUUID()),
        available: Int = 100,
        reserved: Int = 0
    ) = StockLevel(
        productId = productId,
        available = available,
        reserved = reserved
    )
}
