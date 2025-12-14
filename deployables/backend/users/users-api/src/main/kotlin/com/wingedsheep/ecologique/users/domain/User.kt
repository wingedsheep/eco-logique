package com.wingedsheep.ecologique.users.domain

data class User(
    val id: UserId,
    val keycloakSubject: String,
    val name: String,
    val email: String,
    val defaultAddress: Address?
)

@JvmInline
value class UserId(val value: String)
