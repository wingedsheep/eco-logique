package com.wingedsheep.ecologique.users.impl.domain

import java.util.UUID

internal data class User(
    val id: UserId,
    val externalSubject: String,
    val name: String,
    val email: Email,
    val defaultAddress: Address?
) {
    init {
        require(name.isNotBlank()) { "User name cannot be blank" }
        require(name.length <= 255) { "User name cannot exceed 255 characters" }
        require(externalSubject.isNotBlank()) { "External subject cannot be blank" }
    }

    fun updateAddress(newAddress: Address): User = copy(defaultAddress = newAddress)

    companion object {
        fun create(
            externalSubject: String,
            name: String,
            email: String,
            address: Address?
        ): User = User(
            id = UserId.generate(),
            externalSubject = externalSubject,
            name = name,
            email = Email(email),
            defaultAddress = address
        )
    }
}

@JvmInline
internal value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }

    companion object {
        fun generate(): UserId = UserId("USER-${UUID.randomUUID()}")
    }
}

@JvmInline
internal value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.contains("@")) { "Email must contain @" }
        require(value.length <= 255) { "Email cannot exceed 255 characters" }
    }
}
