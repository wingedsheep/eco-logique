package com.wingedsheep.ecologique.users.domain

sealed class UserError {
    data class EmailAlreadyExists(val email: String) : UserError()
    data class UserNotFound(val id: UserId? = null, val keycloakSubject: String? = null) : UserError()
    data class UserAlreadyExists(val keycloakSubject: String) : UserError()
}
