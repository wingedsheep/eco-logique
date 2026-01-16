package com.wingedsheep.ecologique.email.api.error

sealed class EmailError {
    data class InvalidRecipient(val email: String, val reason: String) : EmailError()
    data class DeliveryFailed(val email: String, val reason: String) : EmailError()
    data object ServiceUnavailable : EmailError()
    data class RateLimitExceeded(val email: String) : EmailError()
}
