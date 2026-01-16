package com.wingedsheep.ecologique.users.api.dto

data class RegistrationRequest(
    val email: String,
    val password: String,
) {
    init {
        require(email.isNotBlank()) { "Email is required" }
        require(password.isNotBlank()) { "Password is required" }
    }
}
