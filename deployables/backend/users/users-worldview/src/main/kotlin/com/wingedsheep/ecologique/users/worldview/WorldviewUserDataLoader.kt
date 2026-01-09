package com.wingedsheep.ecologique.users.worldview

import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(2)
class WorldviewUserDataLoader(
    private val userService: UserService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        if (activeProfile.contains("prod") || activeProfile.contains("test")) {
            logger.info("Skipping worldview user data for profile: $activeProfile")
            return
        }

        logger.info("Loading worldview user data...")
        loadUsers()
        logger.info("Worldview user data loaded successfully")
    }

    private fun loadUsers() {
        worldviewUsers.forEach { (keycloakId, userData) ->
            val existingProfile = userService.getProfile(keycloakId)
            if (existingProfile.isOk) {
                logger.debug("Worldview user already exists: ${userData.name}")
                return@forEach
            }

            val request = UserCreateRequest(
                name = userData.name,
                email = userData.email,
                address = userData.address
            )

            userService.createProfile(keycloakId, request)
                .onSuccess { created ->
                    logger.debug("Created worldview user: ${created.name} (${created.id})")
                }
                .onFailure { error ->
                    logger.warn("Failed to create worldview user ${userData.name}: $error")
                }
        }
    }

    companion object {
        const val JOHN_KEYCLOAK_ID = "550e8400-e29b-41d4-a716-446655440001"
        const val JANE_KEYCLOAK_ID = "550e8400-e29b-41d4-a716-446655440002"

        private data class WorldviewUserData(
            val name: String,
            val email: String,
            val address: AddressDto?
        )

        private val worldviewUsers = mapOf(
            JOHN_KEYCLOAK_ID to WorldviewUserData(
                name = "John Doe",
                email = "john@demo.com",
                address = AddressDto(
                    street = "Kalverstraat",
                    houseNumber = "1",
                    postalCode = "1012 NX",
                    city = "Amsterdam",
                    countryCode = "NL"
                )
            ),
            JANE_KEYCLOAK_ID to WorldviewUserData(
                name = "Jane Smith",
                email = "jane@demo.com",
                address = AddressDto(
                    street = "Alexanderplatz",
                    houseNumber = "1",
                    postalCode = "10178",
                    city = "Berlin",
                    countryCode = "DE"
                )
            )
        )
    }
}
