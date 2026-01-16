package com.wingedsheep.ecologique.users.api.dto

data class VerifyEmailRequest(
    val email: String,
    val verificationCode: String,
) {
    init {
        require(email.isNotBlank()) { "Email is required" }
        require(verificationCode.isNotBlank()) { "Verification code is required" }
    }
}
