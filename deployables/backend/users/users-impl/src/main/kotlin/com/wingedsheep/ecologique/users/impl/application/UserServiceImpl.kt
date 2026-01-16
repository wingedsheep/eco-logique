package com.wingedsheep.ecologique.users.impl.application

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.api.dto.UserUpdateAddressRequest
import com.wingedsheep.ecologique.users.api.error.UserError
import com.wingedsheep.ecologique.users.impl.domain.Address
import com.wingedsheep.ecologique.users.impl.domain.Email
import com.wingedsheep.ecologique.users.impl.domain.User
import com.wingedsheep.ecologique.users.impl.domain.UserRepository
import com.wingedsheep.ecologique.users.impl.infrastructure.identity.InternalIdentityProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class UserServiceImpl(
    private val userRepository: UserRepository,
    private val identityProvider: InternalIdentityProvider
) : UserService {

    @Transactional
    override fun createProfile(
        userId: UserId,
        request: UserCreateRequest
    ): Result<UserDto, UserError> {
        if (userRepository.findById(userId) != null) {
            return Result.err(UserError.AlreadyExists(userId.value.toString()))
        }

        // Get external subject from identity provider (internal)
        val externalSubject = identityProvider.getExternalSubjectByUserId(userId)
            ?: return Result.err(UserError.NotFound(userId.value.toString()))

        if (userRepository.existsByExternalSubject(externalSubject)) {
            return Result.err(UserError.AlreadyExists(userId.value.toString()))
        }

        val email = try {
            Email(request.email)
        } catch (e: IllegalArgumentException) {
            return Result.err(UserError.ValidationFailed(e.message ?: "Invalid email"))
        }

        if (userRepository.existsByEmail(email)) {
            return Result.err(UserError.EmailAlreadyExists(request.email))
        }

        val address = request.address?.let { dto ->
            val country = try {
                Country.valueOf(dto.countryCode)
            } catch (e: IllegalArgumentException) {
                return Result.err(UserError.InvalidCountry(dto.countryCode))
            }
            try {
                Address(
                    street = dto.street,
                    houseNumber = dto.houseNumber,
                    postalCode = dto.postalCode,
                    city = dto.city,
                    country = country
                )
            } catch (e: IllegalArgumentException) {
                return Result.err(UserError.ValidationFailed(e.message ?: "Invalid address"))
            }
        }

        val user = try {
            User.create(
                id = userId,
                externalSubject = externalSubject,
                name = request.name,
                email = request.email,
                address = address
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(UserError.ValidationFailed(e.message ?: "Validation failed"))
        }

        val savedUser = userRepository.save(user)
        return Result.ok(savedUser.toDto())
    }

    @Transactional(readOnly = true)
    override fun getProfile(userId: UserId): Result<UserDto, UserError> {
        val user = userRepository.findById(userId)
            ?: return Result.err(UserError.NotFound(userId.value.toString()))

        return Result.ok(user.toDto())
    }

    @Transactional
    override fun updateAddress(
        userId: UserId,
        request: UserUpdateAddressRequest
    ): Result<UserDto, UserError> {
        val user = userRepository.findById(userId)
            ?: return Result.err(UserError.NotFound(userId.value.toString()))

        val country = try {
            Country.valueOf(request.countryCode)
        } catch (e: IllegalArgumentException) {
            return Result.err(UserError.InvalidCountry(request.countryCode))
        }

        val newAddress = try {
            Address(
                street = request.street,
                houseNumber = request.houseNumber,
                postalCode = request.postalCode,
                city = request.city,
                country = country
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(UserError.ValidationFailed(e.message ?: "Invalid address"))
        }

        val updatedUser = user.updateAddress(newAddress)
        val savedUser = userRepository.save(updatedUser)
        return Result.ok(savedUser.toDto())
    }
}
