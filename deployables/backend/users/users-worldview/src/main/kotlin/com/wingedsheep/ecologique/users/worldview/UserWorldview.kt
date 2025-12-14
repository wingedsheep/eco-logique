package com.wingedsheep.ecologique.users.worldview

import com.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.domain.Address
import com.wingedsheep.ecologique.users.domain.User
import com.wingedsheep.ecologique.users.domain.UserId

object UserWorldview {
    val johnDoe = User(
        id = UserId("user-id-john"),
        keycloakSubject = "john-subject-id",
        name = "John Doe",
        email = "john@example.com",
        defaultAddress = Address(
            street = "Kalverstraat",
            houseNumber = "1",
            postalCode = "1012 NX",
            city = "Amsterdam",
            country = Country.NETHERLANDS
        )
    )

    val janeSmith = User(
        id = UserId("user-id-jane"),
        keycloakSubject = "jane-subject-id",
        name = "Jane Smith",
        email = "jane@example.com",
        defaultAddress = Address(
            street = "Alexanderplatz",
            houseNumber = "1",
            postalCode = "10178",
            city = "Berlin",
            country = Country.GERMANY
        )
    )
}
