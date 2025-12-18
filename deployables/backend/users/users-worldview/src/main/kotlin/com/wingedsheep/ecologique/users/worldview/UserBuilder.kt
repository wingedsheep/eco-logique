package com.wingedsheep.ecologique.users.worldview

import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserDto
import java.util.UUID

fun buildWorldviewUser(
    id: String = "USER-${UUID.randomUUID().toString().take(8)}",
    name: String = "Worldview Test User",
    email: String = "worldview-${UUID.randomUUID().toString().take(8)}@example.com",
    defaultAddress: AddressDto? = buildWorldviewAddress()
): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    defaultAddress = defaultAddress
)

fun buildWorldviewAddress(
    street: String = "Test Street",
    houseNumber: String = "1",
    postalCode: String = "1234 AB",
    city: String = "Amsterdam",
    countryCode: String = "NETHERLANDS"
): AddressDto = AddressDto(
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    countryCode = countryCode
)

fun buildWorldviewUserCreateRequest(
    name: String = "Worldview Test User",
    email: String = "worldview-${UUID.randomUUID().toString().take(8)}@example.com",
    address: AddressDto? = buildWorldviewAddress()
): UserCreateRequest = UserCreateRequest(
    name = name,
    email = email,
    address = address
)