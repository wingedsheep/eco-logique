package com.wingedsheep.ecologique.inventory.api.dto

import com.wingedsheep.ecologique.inventory.api.WarehouseId

data class WarehouseDto(
    val id: WarehouseId,
    val name: String,
    val countryCode: String,
    val address: AddressDto? = null
)
