package com.wingedsheep.ecologique.users.impl.infrastructure.web

import com.wingedsheep.ecologique.users.api.RegistrationService
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.dto.RegistrationRequest
import com.wingedsheep.ecologique.users.api.dto.RegistrationResponse
import com.wingedsheep.ecologique.users.api.dto.VerifyEmailRequest
import com.wingedsheep.ecologique.users.api.error.RegistrationError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration and authentication")
class RegistrationControllerV1(
    private val registrationService: RegistrationService
) {

    @PostMapping("/register")
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account and sends a verification email"
    )
    fun register(@RequestBody request: RegistrationRequest): ResponseEntity<RegistrationResponse> {
        return registrationService.register(request).fold(
            onSuccess = { response ->
                ResponseEntity.status(HttpStatus.CREATED).body(response)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @PostMapping("/verify-email")
    @Operation(
        summary = "Verify email address",
        description = "Verifies the user's email address using the verification code"
    )
    fun verifyEmail(@RequestBody request: VerifyEmailRequest): ResponseEntity<VerificationResponse> {
        return registrationService.verifyEmail(request).fold(
            onSuccess = { userId ->
                ResponseEntity.ok(VerificationResponse(userId = userId, message = "Email verified successfully"))
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @PostMapping("/resend-verification")
    @Operation(
        summary = "Resend verification email",
        description = "Resends the verification email with a new verification code"
    )
    fun resendVerification(@RequestParam email: String): ResponseEntity<RegistrationResponse> {
        return registrationService.resendVerificationEmail(email).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    data class VerificationResponse(
        val userId: UserId,
        val message: String
    )
}

private fun RegistrationError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is RegistrationError.EmailAlreadyExists -> Triple(
            HttpStatus.CONFLICT,
            "Email Already Exists",
            "An account with email '$email' already exists"
        )
        is RegistrationError.InvalidEmail -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Email",
            reason
        )
        is RegistrationError.InvalidPassword -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Password",
            reason
        )
        is RegistrationError.InvalidVerificationCode -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Verification Code",
            "The verification code for '$email' is invalid"
        )
        is RegistrationError.VerificationCodeExpired -> Triple(
            HttpStatus.BAD_REQUEST,
            "Verification Code Expired",
            "The verification code for '$email' has expired. Please request a new one."
        )
        is RegistrationError.VerificationNotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Verification Not Found",
            "No pending verification found for '$email'"
        )
        is RegistrationError.IdentityProviderUnavailable -> Triple(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "Identity provider is currently unavailable. Please try again later."
        )
        is RegistrationError.EmailServiceUnavailable -> Triple(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "Email service is currently unavailable. Please try again later."
        )
        is RegistrationError.RateLimitExceeded -> Triple(
            HttpStatus.TOO_MANY_REQUESTS,
            "Rate Limit Exceeded",
            reason
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}
