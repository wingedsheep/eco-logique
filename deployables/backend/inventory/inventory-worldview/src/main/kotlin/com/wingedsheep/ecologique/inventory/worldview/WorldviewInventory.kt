package com.wingedsheep.ecologique.inventory.worldview

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.api.dto.AddressDto
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseDto
import java.util.UUID

object WorldviewInventory {

    val amsterdamWarehouse = WarehouseDto(
        id = WarehouseId(UUID.fromString("00000000-0000-0000-0001-000000000001")),
        name = "Amsterdam Central Warehouse",
        countryCode = "NL",
        address = AddressDto(
            street = "Keizersgracht",
            houseNumber = "100",
            postalCode = "1015 CW",
            city = "Amsterdam",
            countryCode = "NL"
        )
    )

    val berlinWarehouse = WarehouseDto(
        id = WarehouseId(UUID.fromString("00000000-0000-0000-0001-000000000002")),
        name = "Berlin Distribution Center",
        countryCode = "DE",
        address = AddressDto(
            street = "Friedrichstrasse",
            houseNumber = "50",
            postalCode = "10117",
            city = "Berlin",
            countryCode = "DE"
        )
    )

    val brusselsWarehouse = WarehouseDto(
        id = WarehouseId(UUID.fromString("00000000-0000-0000-0001-000000000003")),
        name = "Brussels Fulfillment Hub",
        countryCode = "BE",
        address = AddressDto(
            street = "Rue de la Loi",
            houseNumber = "175",
            postalCode = "1048",
            city = "Brussels",
            countryCode = "BE"
        )
    )

    val allWarehouses = listOf(
        amsterdamWarehouse,
        berlinWarehouse,
        brusselsWarehouse
    )

    fun findByName(name: String): WarehouseDto? =
        allWarehouses.find { it.name == name }
}
