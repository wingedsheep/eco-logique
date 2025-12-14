package com.wingedsheep.ecologique.users.domain

import com.ecologique.common.country.Country

data class Address(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val country: Country
)
