package com.wingedsheep.ecologique.users.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("users", schema = "users")
internal class UserEntity(
    @Id private val id: String,
    val externalSubject: String,
    val name: String,
    val email: String,
    val street: String?,
    val houseNumber: String?,
    val postalCode: String?,
    val city: String?,
    val countryCode: String?
) : Persistable<String> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): String = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): UserEntity {
        isNewEntity = false
        return this
    }
}
