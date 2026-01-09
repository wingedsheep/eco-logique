package com.wingedsheep.ecologique.users.impl.infrastructure.persistence

import com.wingedsheep.ecologique.users.impl.domain.Email
import com.wingedsheep.ecologique.users.impl.domain.User
import com.wingedsheep.ecologique.users.impl.domain.UserId
import com.wingedsheep.ecologique.users.impl.domain.UserRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Component
internal class UserRepositoryImpl(
    private val jdbc: UserRepositoryJdbc
) : UserRepository {

    override fun save(user: User): User {
        val entity = user.toEntity()
        if (jdbc.existsById(user.id.value)) {
            entity.markAsExisting()
        }
        return jdbc.save(entity).toUser()
    }

    override fun findById(id: UserId): User? {
        return jdbc.findById(id.value)
            .map { it.toUser() }
            .orElse(null)
    }

    override fun findByExternalSubject(externalSubject: String): User? {
        return jdbc.findByExternalSubject(externalSubject)?.toUser()
    }

    override fun findByEmail(email: Email): User? {
        return jdbc.findByEmail(email.value)?.toUser()
    }

    override fun existsByExternalSubject(externalSubject: String): Boolean {
        return jdbc.existsByExternalSubject(externalSubject)
    }

    override fun existsByEmail(email: Email): Boolean {
        return jdbc.existsByEmail(email.value)
    }
}

@Repository
internal interface UserRepositoryJdbc : CrudRepository<UserEntity, UUID> {
    fun findByExternalSubject(externalSubject: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun existsByExternalSubject(externalSubject: String): Boolean
    fun existsByEmail(email: String): Boolean
}
