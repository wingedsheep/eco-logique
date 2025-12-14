package com.wingedsheep.ecologique.users.infrastructure.web

import com.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.domain.Address

data class CreateUserRequest(
    val name: String,
    val email: String,
    val address: AddressDto?
)

data class AddressDto(
    val street: String,
    val houseNumber: String,
    val postalCode: String,
    val city: String,
    val countryCode: String
)

fun AddressDto.toDomain(): Address {
    return Address(
        street = street,
        houseNumber = houseNumber,
        postalCode = postalCode,
        city = city,
        country = Country.valueOf(countryCode)
    )
}

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val defaultAddress: AddressDto?
)

fun com.wingedsheep.ecologique.users.domain.User.toResponse(): UserResponse {
    return UserResponse(
        id = id.value,
        name = name,
        email = email,
        defaultAddress = defaultAddress?.toDto()
    )
}

fun Address.toDto(): AddressDto {
    return AddressDto(
        street = street,
        houseNumber = houseNumber,
        postalCode = postalCode,
        city = city,
        countryCode = country.name
    )
}
