package com.wingedsheep.ecologique.users.impl.domain

import com.ecologique.common.country.Country

internal data class Address(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val country: Country
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(houseNumber.isNotBlank()) { "House number cannot be blank" }
        require(postalCode.isNotBlank()) { "Postal code cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
    }
}