package com.wingedsheep.ecologique.inventory.api.dto

data class WarehouseCreateRequest(
    val name: String,
    val countryCode: String,
    val address: AddressDto? = null
) {
    init {
        require(name.isNotBlank()) { "Warehouse name cannot be blank" }
        require(countryCode.isNotBlank()) { "Country code cannot be blank" }
        require(countryCode.length in 2..3) { "Country code must be 2 or 3 characters" }
    }
}
