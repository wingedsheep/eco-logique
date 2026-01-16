package com.wingedsheep.ecologique.inventory.api.dto

data class AddressDto(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(houseNumber.isNotBlank()) { "House number cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(countryCode.isNotBlank()) { "Country code cannot be blank" }
    }
}
