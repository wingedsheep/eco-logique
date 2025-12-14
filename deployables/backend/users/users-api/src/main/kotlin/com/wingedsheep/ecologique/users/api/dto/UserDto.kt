package com.wingedsheep.ecologique.users.api.dto

data class UserDto(
    val id: String,
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