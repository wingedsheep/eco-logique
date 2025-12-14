package com.wingedsheep.ecologique.users.infrastructure.persistence

import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface UserRepository : CrudRepository<UserEntity, String> {
    fun findByKeycloakSubject(keycloakSubject: String): Optional<UserEntity>
    fun existsByEmail(email: String): Boolean
    fun existsByKeycloakSubject(keycloakSubject: String): Boolean
}
