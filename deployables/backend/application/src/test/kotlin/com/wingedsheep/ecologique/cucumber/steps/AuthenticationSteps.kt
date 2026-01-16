package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.cucumber.ScenarioContext
import io.cucumber.java.en.Given
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder

class AuthenticationSteps(
    private val context: ScenarioContext,
    private val jwtDecoder: JwtDecoder
) {
    // Fixed UUIDs for test users to ensure consistency across tests
    private val customerUserId = "11111111-1111-1111-1111-111111111111"
    private val adminUserId = "22222222-2222-2222-2222-222222222222"

    @Given("I am authenticated as a customer")
    fun authenticatedAsCustomer() {
        authenticate(userId = customerUserId, roles = listOf("ROLE_CUSTOMER"))
    }

    @Given("I am authenticated as an admin")
    fun authenticatedAsAdmin() {
        authenticate(userId = adminUserId, roles = listOf("ROLE_ADMIN", "ROLE_CUSTOMER"))
    }

    @Given("I am authenticated as user {string}")
    fun authenticatedAsUser(userId: String) {
        // Allow explicit UUID or generate one for named users
        val actualUserId = if (userId.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))) {
            userId
        } else {
            java.util.UUID.nameUUIDFromBytes(userId.toByteArray()).toString()
        }
        authenticate(userId = actualUserId, roles = listOf("ROLE_CUSTOMER"))
    }

    private fun authenticate(userId: String, roles: List<String>) {
        val tokenValue = "test-token-$userId"
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", userId)
            .claim("realm_access", mapOf("roles" to roles))
            .build()

        whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)

        context.authToken = tokenValue
        context.currentUserId = userId
    }
}
