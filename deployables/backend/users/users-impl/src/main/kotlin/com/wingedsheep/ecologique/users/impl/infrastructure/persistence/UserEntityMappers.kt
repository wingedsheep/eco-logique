package com.wingedsheep.ecologique.users.impl.infrastructure.persistence

import com.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.impl.domain.Address
import com.wingedsheep.ecologique.users.impl.domain.Email
import com.wingedsheep.ecologique.users.impl.domain.User
import com.wingedsheep.ecologique.users.impl.domain.UserId

internal fun UserEntity.toUser(): User {
    val address = if (street != null && houseNumber != null && postalCode != null && city != null && countryCode != null) {
        Address(
            street = street,
            houseNumber = houseNumber,
            postalCode = postalCode,
            city = city,
            country = Country.valueOf(countryCode)
        )
    } else {
        null
    }

    return User(
        id = UserId(id),
        externalSubject = externalSubject,
        name = name,
        email = Email(email),
        defaultAddress = address
    )
}

internal fun User.toEntity(): UserEntity = UserEntity(
    id = id.value,
    externalSubject = externalSubject,
    name = name,
    email = email.value,
    street = defaultAddress?.street,
    houseNumber = defaultAddress?.houseNumber,
    postalCode = defaultAddress?.postalCode,
    city = defaultAddress?.city,
    countryCode = defaultAddress?.country?.name
)
