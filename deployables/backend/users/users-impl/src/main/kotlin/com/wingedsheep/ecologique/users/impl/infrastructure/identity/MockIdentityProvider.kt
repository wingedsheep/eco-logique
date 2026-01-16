package com.wingedsheep.ecologique.users.impl.infrastructure.identity

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.error.RegistrationError
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.logging.Logger

/**
 * Mock implementation of [InternalIdentityProvider] for development and testing.
 *
 * This implementation stores users in memory instead of communicating
 * with a real identity provider like Keycloak.
 */
@Service
internal class MockIdentityProvider : InternalIdentityProvider {

    private val logger = Logger.getLogger(MockIdentityProvider::class.java.name)

    // Maps email -> MockUser
    private val usersByEmail = mutableMapOf<String, MockUser>()

    // Maps external subject (Keycloak subject) -> UserId
    private val subjectToUserId = mutableMapOf<String, UserId>()

    // Maps UserId -> external subject
    private val userIdToSubject = mutableMapOf<UserId, String>()

    override fun createUser(userId: UserId, email: String, password: String): Result<Unit, RegistrationError> {
        val normalizedEmail = email.lowercase()

        if (usersByEmail.containsKey(normalizedEmail)) {
            return Result.err(RegistrationError.EmailAlreadyExists(email))
        }

        // Generate a mock external subject (simulating what Keycloak would generate)
        val externalSubject = UUID.randomUUID().toString()

        val user = MockUser(
            userId = userId,
            externalSubject = externalSubject,
            email = email,
            password = password,
            emailVerified = false,
            enabled = false
        )

        usersByEmail[normalizedEmail] = user
        subjectToUserId[externalSubject] = userId
        userIdToSubject[userId] = externalSubject

        logger.info(
            """
            |
            |========================================
            | MOCK IDENTITY PROVIDER: User Created
            |========================================
            | UserId: ${userId.value}
            | External Subject: $externalSubject
            | Email: $email
            | Email Verified: false
            | Enabled: false
            |========================================
            """.trimMargin()
        )

        return Result.ok(Unit)
    }

    override fun userExists(email: String): Result<Boolean, RegistrationError> {
        return Result.ok(usersByEmail.containsKey(email.lowercase()))
    }

    override fun verifyAndEnableUser(email: String): Result<Unit, RegistrationError> {
        val normalizedEmail = email.lowercase()
        val user = usersByEmail[normalizedEmail]
            ?: return Result.err(RegistrationError.InvalidEmail(email, "User not found"))

        usersByEmail[normalizedEmail] = user.copy(emailVerified = true, enabled = true)

        logger.info("MOCK IDENTITY PROVIDER: Email verified and user enabled for $email")

        return Result.ok(Unit)
    }

    override fun getUserIdByEmail(email: String): Result<UserId, RegistrationError> {
        val user = usersByEmail[email.lowercase()]
            ?: return Result.err(RegistrationError.InvalidEmail(email, "User not found"))

        return Result.ok(user.userId)
    }

    override fun getUserIdBySubject(subject: String): UserId? {
        return subjectToUserId[subject]
    }

    override fun getExternalSubjectByUserId(userId: UserId): String? {
        return userIdToSubject[userId]
    }

    /**
     * Returns all users for test assertions.
     */
    fun getUsers(): Map<String, MockUser> = usersByEmail.toMap()

    /**
     * Clears all users for test setup.
     */
    fun clearUsers() {
        usersByEmail.clear()
        subjectToUserId.clear()
        userIdToSubject.clear()
    }

    /**
     * Gets a user by email for test assertions.
     */
    fun getUser(email: String): MockUser? = usersByEmail[email.lowercase()]

    /**
     * Creates a user with a specific external subject for test scenarios.
     * This allows tests to match the JWT subject with the identity provider subject.
     */
    fun createUserWithSubject(userId: UserId, externalSubject: String, email: String, password: String) {
        val normalizedEmail = email.lowercase()

        val user = MockUser(
            userId = userId,
            externalSubject = externalSubject,
            email = email,
            password = password,
            emailVerified = true,
            enabled = true
        )

        usersByEmail[normalizedEmail] = user
        subjectToUserId[externalSubject] = userId
        userIdToSubject[userId] = externalSubject

        logger.info("MOCK IDENTITY PROVIDER: Test user created with subject $externalSubject")
    }

    data class MockUser(
        val userId: UserId,
        val externalSubject: String,
        val email: String,
        val password: String,
        val emailVerified: Boolean,
        val enabled: Boolean,
    )
}
