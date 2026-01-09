package com.wingedsheep.ecologique.users.api.dto

import java.util.UUID

data class UserDto(
    val id: UUID,
    val name: String,
    val email: String,
    val defaultAddress: AddressDto?
)

data class AddressDto(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String
)
