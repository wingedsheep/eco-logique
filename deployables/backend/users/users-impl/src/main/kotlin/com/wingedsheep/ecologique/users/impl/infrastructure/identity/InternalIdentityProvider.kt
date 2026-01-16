package com.wingedsheep.ecologique.users.impl.infrastructure.identity

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.error.RegistrationError

/**
 * Internal interface for identity provider operations.
 *
 * This interface is internal to the users module and handles all
 * communication with the identity provider (e.g., Keycloak).
 * No other module should need access to these operations.
 */
internal interface InternalIdentityProvider {

    /**
     * Creates a new user in the identity provider.
     *
     * @param userId The application's user ID to associate with this identity
     * @param email The user's email address (used as username)
     * @param password The user's password
     * @return [Result.Ok] with [Unit] if the user was created,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun createUser(userId: UserId, email: String, password: String): Result<Unit, RegistrationError>

    /**
     * Checks if a user with the given email already exists.
     *
     * @param email The email address to check
     * @return [Result.Ok] with `true` if user exists, `false` otherwise,
     *         or [Result.Err] if the check failed
     */
    fun userExists(email: String): Result<Boolean, RegistrationError>

    /**
     * Marks a user's email as verified and enables the account.
     *
     * @param email The email address to mark as verified
     * @return [Result.Ok] with [Unit] on success,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun verifyAndEnableUser(email: String): Result<Unit, RegistrationError>

    /**
     * Retrieves the UserId associated with a user by their email.
     *
     * @param email The email address to look up
     * @return [Result.Ok] with the [UserId] if found,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun getUserIdByEmail(email: String): Result<UserId, RegistrationError>

    /**
     * Retrieves the external subject for a user by their UserId.
     *
     * This is used internally by the users module to store the mapping
     * between our UserId and the identity provider's subject.
     *
     * @param userId The user's ID
     * @return The external subject if found, null otherwise
     */
    fun getExternalSubjectByUserId(userId: UserId): String?

    /**
     * Retrieves the UserId for a user by their identity provider subject.
     *
     * This is used internally to resolve authenticated users from JWT.
     *
     * @param subject The identity provider's subject (e.g., from JWT)
     * @return The UserId if found, null otherwise
     */
    fun getUserIdBySubject(subject: String): UserId?
}
