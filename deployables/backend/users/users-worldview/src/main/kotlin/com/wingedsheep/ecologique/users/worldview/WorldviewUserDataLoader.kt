package com.wingedsheep.ecologique.users.worldview

import com.wingedsheep.ecologique.users.api.RegistrationService
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Order(2)
class WorldviewUserDataLoader(
    private val userService: UserService,
    private val registrationService: RegistrationService,
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
        for (userData in worldviewUsers) {
            val existingProfile = userService.getProfile(userData.userId)
            if (existingProfile.isOk) {
                logger.debug("Worldview user already exists: ${userData.name}")
                continue
            }

            // Create demo user (pre-verified)
            val createResult = registrationService.createDemoUser(
                userData.userId,
                userData.email,
                "DemoPassword123!"
            )
            if (createResult.isErr) {
                logger.warn("Failed to create identity for ${userData.name}")
                continue
            }

            val request = UserCreateRequest(
                name = userData.name,
                email = userData.email,
                address = userData.address
            )

            userService.createProfile(userData.userId, request)
                .onSuccess { created ->
                    logger.debug("Created worldview user: ${created.name} (${created.id})")
                }
                .onFailure { error ->
                    logger.warn("Failed to create worldview user ${userData.name}: $error")
                }
        }
    }

    companion object {
        val JOHN_USER_ID = UserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"))
        val JANE_USER_ID = UserId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))

        private data class WorldviewUserData(
            val userId: UserId,
            val name: String,
            val email: String,
            val address: AddressDto?
        )

        private val worldviewUsers = listOf(
            WorldviewUserData(
                userId = JOHN_USER_ID,
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
            WorldviewUserData(
                userId = JANE_USER_ID,
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
