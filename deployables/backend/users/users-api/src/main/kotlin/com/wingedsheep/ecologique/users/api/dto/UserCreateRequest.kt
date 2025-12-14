package com.wingedsheep.ecologique.users.api.dto

data class UserCreateRequest(
    val name: String,
    val email: String,
    val address: AddressDto?
)

data class UserUpdateAddressRequest(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String
)