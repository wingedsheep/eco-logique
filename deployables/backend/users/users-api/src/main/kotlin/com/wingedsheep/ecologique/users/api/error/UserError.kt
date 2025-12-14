package com.wingedsheep.ecologique.users.api.error

sealed class UserError {
    data class EmailAlreadyExists(val email: String) : UserError()
    data class NotFound(val identifier: String) : UserError()
    data class AlreadyExists(val identifier: String) : UserError()
    data class InvalidCountry(val countryCode: String) : UserError()
    data class ValidationFailed(val reason: String) : UserError()
}
