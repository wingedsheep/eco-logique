package com.wingedsheep.ecologique.users.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Id
    val id: String,
    val keycloakSubject: String,
    val name: String,
    val email: String,
    val street: String?,
    val houseNumber: String?,
    val postalCode: String?,
    val city: String?,
    val countryCode: String?
)
