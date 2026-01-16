package com.wingedsheep.ecologique.users.impl.application

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.email.api.EmailService
import com.wingedsheep.ecologique.email.api.dto.Email
import com.wingedsheep.ecologique.email.api.dto.EmailAddress
import com.wingedsheep.ecologique.email.api.dto.EmailBody
import com.wingedsheep.ecologique.users.api.RegistrationService
import com.wingedsheep.ecologique.users.impl.infrastructure.identity.InternalIdentityProvider
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.dto.RegistrationRequest
import com.wingedsheep.ecologique.users.api.dto.RegistrationResponse
import com.wingedsheep.ecologique.users.api.dto.VerifyEmailRequest
import com.wingedsheep.ecologique.users.api.error.RegistrationError
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Service
internal class RegistrationServiceImpl(
    private val identityProvider: InternalIdentityProvider,
    private val emailService: EmailService
) : RegistrationService {

    private val pendingVerifications = ConcurrentHashMap<String, PendingVerification>()
    private val secureRandom = SecureRandom()

    override fun register(request: RegistrationRequest): Result<RegistrationResponse, RegistrationError> {
        // Validate email format
        val emailValidation = validateEmail(request.email)
        if (emailValidation != null) {
            return Result.err(emailValidation)
        }

        // Validate password
        val passwordValidation = validatePassword(request.password)
        if (passwordValidation != null) {
            return Result.err(passwordValidation)
        }

        // Check if user already exists
        val existsResult = identityProvider.userExists(request.email)
        if (existsResult is Result.Err) {
            return Result.err(RegistrationError.IdentityProviderUnavailable)
        }
        if ((existsResult as Result.Ok).value) {
            return Result.err(RegistrationError.EmailAlreadyExists(request.email))
        }

        // Check for pending verification
        val normalizedEmail = request.email.lowercase()
        if (pendingVerifications.containsKey(normalizedEmail)) {
            return Result.err(RegistrationError.EmailAlreadyExists(request.email))
        }

        // Generate UserId
        val userId = UserId.generate()

        // Create user in identity provider
        val createResult = identityProvider.createUser(userId, request.email, request.password)
        if (createResult is Result.Err) {
            return Result.err(createResult.error)
        }

        // Generate verification code
        val verificationCode = generateVerificationCode()

        // Store pending verification
        pendingVerifications[normalizedEmail] = PendingVerification(
            userId = userId,
            email = request.email,
            verificationCode = verificationCode,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(VERIFICATION_CODE_EXPIRY_SECONDS)
        )

        // Send verification email
        val sendResult = sendVerificationEmail(request.email, verificationCode)
        if (sendResult is Result.Err) {
            // Rollback: remove pending verification
            pendingVerifications.remove(normalizedEmail)
            return Result.err(RegistrationError.EmailServiceUnavailable)
        }

        return Result.ok(
            RegistrationResponse(
                email = request.email,
                message = "Registration initiated. Please check your email for the verification code."
            )
        )
    }

    override fun verifyEmail(request: VerifyEmailRequest): Result<UserId, RegistrationError> {
        val normalizedEmail = request.email.lowercase()

        val pending = pendingVerifications[normalizedEmail]
            ?: return Result.err(RegistrationError.VerificationNotFound(request.email))

        // Check if expired
        if (Instant.now().isAfter(pending.expiresAt)) {
            pendingVerifications.remove(normalizedEmail)
            return Result.err(RegistrationError.VerificationCodeExpired(request.email))
        }

        // Verify code
        if (pending.verificationCode != request.verificationCode) {
            return Result.err(RegistrationError.InvalidVerificationCode(request.email))
        }

        // Enable user in identity provider
        val enableResult = identityProvider.verifyAndEnableUser(request.email)
        if (enableResult is Result.Err) {
            return Result.err(enableResult.error)
        }

        // Remove pending verification
        pendingVerifications.remove(normalizedEmail)

        return Result.ok(pending.userId)
    }

    override fun resendVerificationEmail(email: String): Result<RegistrationResponse, RegistrationError> {
        val normalizedEmail = email.lowercase()

        val pending = pendingVerifications[normalizedEmail]
            ?: return Result.err(RegistrationError.VerificationNotFound(email))

        // Generate new verification code
        val verificationCode = generateVerificationCode()

        // Update pending verification
        pendingVerifications[normalizedEmail] = pending.copy(
            verificationCode = verificationCode,
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(VERIFICATION_CODE_EXPIRY_SECONDS)
        )

        // Send verification email
        val sendResult = sendVerificationEmail(email, verificationCode)
        if (sendResult is Result.Err) {
            return Result.err(RegistrationError.EmailServiceUnavailable)
        }

        return Result.ok(
            RegistrationResponse(
                email = email,
                message = "Verification email resent. Please check your email for the new verification code."
            )
        )
    }

    override fun createDemoUser(userId: UserId, email: String, password: String): Result<UserId, RegistrationError> {
        // Check if user already exists
        val existsResult = identityProvider.userExists(email)
        if (existsResult is Result.Err) {
            return Result.err(RegistrationError.IdentityProviderUnavailable)
        }
        if ((existsResult as Result.Ok).value) {
            return Result.err(RegistrationError.EmailAlreadyExists(email))
        }

        // Create user in identity provider
        val createResult = identityProvider.createUser(userId, email, password)
        if (createResult is Result.Err) {
            return Result.err(createResult.error)
        }

        // Immediately verify and enable the user
        val enableResult = identityProvider.verifyAndEnableUser(email)
        if (enableResult is Result.Err) {
            return Result.err(enableResult.error)
        }

        return Result.ok(userId)
    }

    private fun validateEmail(email: String): RegistrationError? {
        if (email.isBlank()) {
            return RegistrationError.InvalidEmail(email, "Email is required")
        }
        if (!email.contains("@")) {
            return RegistrationError.InvalidEmail(email, "Invalid email format")
        }
        if (email.length > 255) {
            return RegistrationError.InvalidEmail(email, "Email must not exceed 255 characters")
        }
        return null
    }

    private fun validatePassword(password: String): RegistrationError? {
        if (password.length < 8) {
            return RegistrationError.InvalidPassword("Password must be at least 8 characters")
        }
        if (!password.any { it.isUpperCase() }) {
            return RegistrationError.InvalidPassword("Password must contain an uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            return RegistrationError.InvalidPassword("Password must contain a lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            return RegistrationError.InvalidPassword("Password must contain a number")
        }
        return null
    }

    private fun generateVerificationCode(): String {
        return (100000 + secureRandom.nextInt(900000)).toString()
    }

    private fun sendVerificationEmail(email: String, verificationCode: String): Result<Unit, RegistrationError> {
        val emailMessage = Email(
            to = EmailAddress(email),
            subject = "Verify your eco-nomique account",
            body = EmailBody.Plain(
                """
                Welcome to eco-nomique!

                Your verification code is: $verificationCode

                This code will expire in 24 hours.

                If you did not request this verification, please ignore this email.
                """.trimIndent()
            )
        )

        return emailService.send(emailMessage).fold(
            onSuccess = { Result.ok(Unit) },
            onFailure = { Result.err(RegistrationError.EmailServiceUnavailable) }
        )
    }

    /**
     * Gets a pending verification for testing purposes.
     */
    internal fun getPendingVerification(email: String): PendingVerification? {
        return pendingVerifications[email.lowercase()]
    }

    /**
     * Clears all pending verifications for testing purposes.
     */
    internal fun clearPendingVerifications() {
        pendingVerifications.clear()
    }

    internal data class PendingVerification(
        val userId: UserId,
        val email: String,
        val verificationCode: String,
        val createdAt: Instant,
        val expiresAt: Instant
    )

    companion object {
        private const val VERIFICATION_CODE_EXPIRY_SECONDS = 24 * 60 * 60L // 24 hours
    }
}
