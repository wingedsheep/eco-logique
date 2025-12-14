package com.wingedsheep.ecologique.users.impl.application

import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.impl.domain.Address
import com.wingedsheep.ecologique.users.impl.domain.User

internal fun User.toDto(): UserDto = UserDto(
    id = id.value,
    name = name,
    email = email.value,
    defaultAddress = defaultAddress?.toDto()
)

internal fun Address.toDto(): AddressDto = AddressDto(
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    countryCode = country.name
)
