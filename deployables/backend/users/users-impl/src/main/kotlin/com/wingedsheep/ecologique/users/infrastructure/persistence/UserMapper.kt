package com.wingedsheep.ecologique.users.infrastructure.persistence

import com.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.domain.Address
import com.wingedsheep.ecologique.users.domain.User
import com.wingedsheep.ecologique.users.domain.UserId

fun UserEntity.toDomain(): User {
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
        keycloakSubject = keycloakSubject,
        name = name,
        email = email,
        defaultAddress = address
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id.value,
        keycloakSubject = keycloakSubject,
        name = name,
        email = email,
        street = defaultAddress?.street,
        houseNumber = defaultAddress?.houseNumber,
        postalCode = defaultAddress?.postalCode,
        city = defaultAddress?.city,
        countryCode = defaultAddress?.country?.name
    )
}
