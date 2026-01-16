package com.wingedsheep.ecologique.users.impl.domain

import com.wingedsheep.ecologique.users.api.UserId

internal interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByExternalSubject(externalSubject: String): User?
    fun findByEmail(email: Email): User?
    fun existsByExternalSubject(externalSubject: String): Boolean
    fun existsByEmail(email: Email): Boolean
}
