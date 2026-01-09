package com.wingedsheep.ecologique.users.api

import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.api.dto.UserUpdateAddressRequest
import java.util.UUID

fun buildUserDto(
    id: UUID = UUID.randomUUID(),
    name: String = "Test User",
    email: String = "test@example.com",
    defaultAddress: AddressDto? = buildAddressDto()
): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    defaultAddress = defaultAddress
)

fun buildAddressDto(
    street: String = "Test Street",
    houseNumber: String = "1",
    postalCode: String = "1234 AB",
    city: String = "Amsterdam",
    countryCode: String = "NL"
): AddressDto = AddressDto(
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    countryCode = countryCode
)

fun buildUserCreateRequest(
    name: String = "Test User",
    email: String = "test@example.com",
    address: AddressDto? = buildAddressDto()
): UserCreateRequest = UserCreateRequest(
    name = name,
    email = email,
    address = address
)

fun buildUserUpdateAddressRequest(
    street: String = "New Street",
    houseNumber: String = "42",
    postalCode: String = "5678 CD",
    city: String = "Rotterdam",
    countryCode: String = "NL"
): UserUpdateAddressRequest = UserUpdateAddressRequest(
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    countryCode = countryCode
)
