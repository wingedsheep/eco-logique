package com.wingedsheep.ecologique.users.application

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.domain.Address
import com.wingedsheep.ecologique.users.domain.User
import com.wingedsheep.ecologique.users.domain.UserError
import com.wingedsheep.ecologique.users.domain.UserId
import com.wingedsheep.ecologique.users.domain.UserService
import com.wingedsheep.ecologique.users.infrastructure.persistence.UserRepository
import com.wingedsheep.ecologique.users.infrastructure.persistence.toDomain
import com.wingedsheep.ecologique.users.infrastructure.persistence.toEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    @Transactional
    override fun createProfile(keycloakSubject: String, name: String, email: String, address: Address?): Result<User, UserError> {
        if (userRepository.existsByKeycloakSubject(keycloakSubject)) {
            return Result.Err(UserError.UserAlreadyExists(keycloakSubject))
        }

        if (userRepository.existsByEmail(email)) {
            return Result.Err(UserError.EmailAlreadyExists(email))
        }

        val newUser = User(
            id = UserId(UUID.randomUUID().toString()),
            keycloakSubject = keycloakSubject,
            name = name,
            email = email,
            defaultAddress = address
        )

        userRepository.save(newUser.toEntity())
        return Result.Ok(newUser)
    }

    @Transactional(readOnly = true)
    override fun getProfile(keycloakSubject: String): Result<User, UserError> {
        val userEntity = userRepository.findByKeycloakSubject(keycloakSubject)
        return if (userEntity.isPresent) {
            Result.Ok(userEntity.get().toDomain())
        } else {
            Result.Err(UserError.UserNotFound(keycloakSubject = keycloakSubject))
        }
    }

    @Transactional
    override fun updateAddress(keycloakSubject: String, address: Address): Result<User, UserError> {
        val userEntityOpt = userRepository.findByKeycloakSubject(keycloakSubject)
        if (userEntityOpt.isEmpty) {
            return Result.Err(UserError.UserNotFound(keycloakSubject = keycloakSubject))
        }

        val userEntity = userEntityOpt.get()
        val updatedUser = userEntity.toDomain().copy(defaultAddress = address)
        userRepository.save(updatedUser.toEntity())
        return Result.Ok(updatedUser)
    }
}
