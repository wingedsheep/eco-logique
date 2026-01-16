package com.wingedsheep.ecologique.users.api.error

sealed class RegistrationError {
    data class EmailAlreadyExists(val email: String) : RegistrationError()
    data class InvalidEmail(val email: String, val reason: String) : RegistrationError()
    data class InvalidPassword(val reason: String) : RegistrationError()
    data class InvalidVerificationCode(val email: String) : RegistrationError()
    data class VerificationCodeExpired(val email: String) : RegistrationError()
    data class VerificationNotFound(val email: String) : RegistrationError()
    data object IdentityProviderUnavailable : RegistrationError()
    data object EmailServiceUnavailable : RegistrationError()
    data class RateLimitExceeded(val reason: String) : RegistrationError()
}
