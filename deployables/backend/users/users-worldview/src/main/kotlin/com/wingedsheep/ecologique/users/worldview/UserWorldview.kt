package com.wingedsheep.ecologique.users.worldview

import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserDto

object WorldviewUser {

    val johnDoe = UserDto(
        id = "USER-001",
        name = "John Doe",
        email = "john@example.com",
        defaultAddress = AddressDto(
            street = "Kalverstraat",
            houseNumber = "1",
            postalCode = "1012 NX",
            city = "Amsterdam",
            countryCode = "NETHERLANDS"
        )
    )

    val janeSmith = UserDto(
        id = "USER-002",
        name = "Jane Smith",
        email = "jane@example.com",
        defaultAddress = AddressDto(
            street = "Alexanderplatz",
            houseNumber = "1",
            postalCode = "10178",
            city = "Berlin",
            countryCode = "GERMANY"
        )
    )

    val userWithoutAddress = UserDto(
        id = "USER-003",
        name = "Bob Wilson",
        email = "bob@example.com",
        defaultAddress = null
    )

    val allUsers = listOf(johnDoe, janeSmith, userWithoutAddress)

    fun findByName(name: String): UserDto? =
        allUsers.find { it.name == name }

    fun findByEmail(email: String): UserDto? =
        allUsers.find { it.email == email }
}
