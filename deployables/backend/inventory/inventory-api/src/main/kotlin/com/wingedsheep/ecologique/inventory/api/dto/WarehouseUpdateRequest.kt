package com.wingedsheep.ecologique.inventory.api.dto

data class WarehouseUpdateRequest(
    val name: String? = null,
    val countryCode: String? = null,
    val address: AddressDto? = null
) {
    init {
        name?.let {
            require(it.isNotBlank()) { "Warehouse name cannot be blank" }
        }
        countryCode?.let {
            require(it.isNotBlank()) { "Country code cannot be blank" }
            require(it.length in 2..3) { "Country code must be 2 or 3 characters" }
        }
    }
}
