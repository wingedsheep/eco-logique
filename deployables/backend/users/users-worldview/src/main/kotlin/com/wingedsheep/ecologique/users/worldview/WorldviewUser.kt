package com.wingedsheep.ecologique.users.worldview

import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JANE_USER_ID
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JOHN_USER_ID

object WorldviewUser {

    val johnDoe = UserDto(
        id = JOHN_USER_ID,
        name = "John Doe",
        email = "john@demo.com",
        defaultAddress = AddressDto(
            street = "Kalverstraat",
            houseNumber = "1",
            postalCode = "1012 NX",
            city = "Amsterdam",
            countryCode = "NL"
        )
    )

    val janeSmith = UserDto(
        id = JANE_USER_ID,
        name = "Jane Smith",
        email = "jane@demo.com",
        defaultAddress = AddressDto(
            street = "Alexanderplatz",
            houseNumber = "1",
            postalCode = "10178",
            city = "Berlin",
            countryCode = "DE"
        )
    )

    val allUsers = listOf(johnDoe, janeSmith)

    fun findByName(name: String): UserDto? =
        allUsers.find { it.name == name }

    fun findByEmail(email: String): UserDto? =
        allUsers.find { it.email == email }
}
