package com.wingedsheep.ecologique.users.application

import com.ecologique.common.country.Country
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.domain.Address
import com.wingedsheep.ecologique.users.domain.User
import com.wingedsheep.ecologique.users.domain.UserError
import com.wingedsheep.ecologique.users.domain.UserId
import com.wingedsheep.ecologique.users.infrastructure.persistence.UserEntity
import com.wingedsheep.ecologique.users.infrastructure.persistence.UserRepository
import com.wingedsheep.ecologique.users.infrastructure.persistence.toEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class UserServiceImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        userService = UserServiceImpl(userRepository)
    }

    @Test
    fun `createProfile should create user when valid`() {
        val subject = "subject-123"
        val name = "Test User"
        val email = "test@example.com"
        val address = Address("Street", "1", "1234", "City", Country.NETHERLANDS)

        whenever(userRepository.existsByKeycloakSubject(subject)).thenReturn(false)
        whenever(userRepository.existsByEmail(email)).thenReturn(false)

        val result = userService.createProfile(subject, name, email, address)

        assertThat(result).isInstanceOf(Result.Ok::class.java)
        val user = (result as Result.Ok).value
        assertThat(user.keycloakSubject).isEqualTo(subject)
        assertThat(user.name).isEqualTo(name)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.defaultAddress).isEqualTo(address)

        verify(userRepository).save(any())
    }

    @Test
    fun `createProfile should return error when subject exists`() {
        val subject = "subject-123"
        whenever(userRepository.existsByKeycloakSubject(subject)).thenReturn(true)

        val result = userService.createProfile(subject, "Name", "email@example.com", null)

        assertThat(result).isInstanceOf(Result.Err::class.java)
        val error = (result as Result.Err).error
        assertThat(error).isInstanceOf(UserError.UserAlreadyExists::class.java)
    }

    @Test
    fun `getProfile should return user when exists`() {
        val subject = "subject-123"
        val userEntity = UserEntity(
            id = "id",
            keycloakSubject = subject,
            name = "Name",
            email = "email",
            street = null,
            houseNumber = null,
            postalCode = null,
            city = null,
            countryCode = null
        )
        whenever(userRepository.findByKeycloakSubject(subject)).thenReturn(Optional.of(userEntity))

        val result = userService.getProfile(subject)

        assertThat(result).isInstanceOf(Result.Ok::class.java)
        assertThat((result as Result.Ok).value.keycloakSubject).isEqualTo(subject)
    }

    @Test
    fun `updateAddress should update address when user exists`() {
        val subject = "subject-123"
        val address = Address("Street", "1", "1234", "City", Country.NETHERLANDS)
        val userEntity = UserEntity(
            id = "id",
            keycloakSubject = subject,
            name = "Name",
            email = "email",
            street = null,
            houseNumber = null,
            postalCode = null,
            city = null,
            countryCode = null
        )
        whenever(userRepository.findByKeycloakSubject(subject)).thenReturn(Optional.of(userEntity))

        val result = userService.updateAddress(subject, address)

        assertThat(result).isInstanceOf(Result.Ok::class.java)
        val user = (result as Result.Ok).value
        assertThat(user.defaultAddress).isEqualTo(address)

        val captor = argumentCaptor<UserEntity>()
        verify(userRepository).save(captor.capture())
        assertThat(captor.firstValue.street).isEqualTo("Street")
    }
}
