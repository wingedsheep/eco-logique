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
    @Given("I am authenticated as a customer")
    fun authenticatedAsCustomer() {
        authenticate(userId = "customer-user", roles = listOf("ROLE_CUSTOMER"))
    }

    @Given("I am authenticated as an admin")
    fun authenticatedAsAdmin() {
        authenticate(userId = "admin-user", roles = listOf("ROLE_ADMIN", "ROLE_CUSTOMER"))
    }

    @Given("I am authenticated as user {string}")
    fun authenticatedAsUser(userId: String) {
        authenticate(userId = userId, roles = listOf("ROLE_CUSTOMER"))
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
