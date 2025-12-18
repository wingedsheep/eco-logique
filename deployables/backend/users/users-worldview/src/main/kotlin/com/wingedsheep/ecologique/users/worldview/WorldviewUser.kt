package com.wingedsheep.ecologique.users.worldview

import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JANE_KEYCLOAK_ID
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JOHN_KEYCLOAK_ID

object WorldviewUser {

    val johnDoe = UserDto(
        id = "USER-001",
        name = "John Doe",
        email = "john@demo.com",
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
        email = "jane@demo.com",
        defaultAddress = AddressDto(
            street = "Alexanderplatz",
            houseNumber = "1",
            postalCode = "10178",
            city = "Berlin",
            countryCode = "GERMANY"
        )
    )

    val allUsers = listOf(johnDoe, janeSmith)

    val keycloakSubjects = mapOf(
        JOHN_KEYCLOAK_ID to johnDoe,
        JANE_KEYCLOAK_ID to janeSmith
    )

    fun findByName(name: String): UserDto? =
        allUsers.find { it.name == name }

    fun findByEmail(email: String): UserDto? =
        allUsers.find { it.email == email }

    fun findByKeycloakSubject(subject: String): UserDto? =
        keycloakSubjects[subject]
}
