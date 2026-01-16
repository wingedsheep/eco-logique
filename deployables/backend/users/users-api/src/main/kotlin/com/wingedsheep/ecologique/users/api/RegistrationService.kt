package com.wingedsheep.ecologique.users.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.dto.RegistrationRequest
import com.wingedsheep.ecologique.users.api.dto.RegistrationResponse
import com.wingedsheep.ecologique.users.api.dto.VerifyEmailRequest
import com.wingedsheep.ecologique.users.api.error.RegistrationError

/**
 * Service for user registration.
 *
 * Handles the complete registration flow:
 * 1. Validate email and password
 * 2. Create user in identity provider (Keycloak)
 * 3. Send verification email
 * 4. Verify email address
 */
interface RegistrationService {

    /**
     * Initiates user registration.
     *
     * Creates a new user account in the identity provider and sends
     * a verification email. The account remains inactive until the
     * email is verified.
     *
     * @param request Contains email and password for the new account
     * @return [Result.Ok] with registration confirmation,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun register(request: RegistrationRequest): Result<RegistrationResponse, RegistrationError>

    /**
     * Verifies a user's email address.
     *
     * Completes the registration by verifying the email address
     * using the code sent during registration.
     *
     * @param request Contains email and verification code
     * @return [Result.Ok] with the user's ID upon successful verification,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun verifyEmail(request: VerifyEmailRequest): Result<UserId, RegistrationError>

    /**
     * Resends the verification email.
     *
     * Generates a new verification code and sends it to the specified email.
     * The previous verification code is invalidated.
     *
     * @param email The email address to send the verification to
     * @return [Result.Ok] with confirmation,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun resendVerificationEmail(email: String): Result<RegistrationResponse, RegistrationError>

    /**
     * Creates a pre-verified user for demo/test purposes.
     *
     * This method bypasses the email verification flow and creates
     * a user that is immediately active. Intended for local development
     * and worldview data seeding.
     *
     * @param userId The user ID to assign
     * @param email The user's email address
     * @param password The user's password
     * @return [Result.Ok] with the user's ID upon success,
     *         or [Result.Err] with a [RegistrationError]
     */
    fun createDemoUser(userId: UserId, email: String, password: String): Result<UserId, RegistrationError>
}
