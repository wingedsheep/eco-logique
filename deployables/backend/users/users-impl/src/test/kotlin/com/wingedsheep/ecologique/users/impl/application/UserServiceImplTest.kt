package com.wingedsheep.ecologique.users.impl.application

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.api.buildAddressDto
import com.wingedsheep.ecologique.users.api.buildUserCreateRequest
import com.wingedsheep.ecologique.users.api.buildUserUpdateAddressRequest
import com.wingedsheep.ecologique.users.api.error.UserError
import com.wingedsheep.ecologique.users.impl.domain.Address
import com.wingedsheep.ecologique.users.impl.domain.Email
import com.wingedsheep.ecologique.users.impl.domain.User
import com.wingedsheep.ecologique.users.impl.domain.UserId
import com.wingedsheep.ecologique.users.impl.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class UserServiceImplTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserServiceImpl

    @Test
    fun `createProfile should return UserDto when valid request`() {
        // Given
        val request = buildUserCreateRequest(
            name = "John Doe",
            email = "john@example.com",
            address = buildAddressDto(city = "Amsterdam", countryCode = "NETHERLANDS")
        )
        whenever(userRepository.existsByExternalSubject("test-subject")).thenReturn(false)
        whenever(userRepository.existsByEmail(Email("john@example.com"))).thenReturn(false)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        // When
        val result = userService.createProfile("test-subject", request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.name).isEqualTo("John Doe")
                assertThat(dto.email).isEqualTo("john@example.com")
                assertThat(dto.defaultAddress?.city).isEqualTo("Amsterdam")
            },
            onFailure = { }
        )
        verify(userRepository).save(any())
    }

    @Test
    fun `createProfile should return AlreadyExists error when subject exists`() {
        // Given
        val request = buildUserCreateRequest()
        whenever(userRepository.existsByExternalSubject("existing-subject")).thenReturn(true)

        // When
        val result = userService.createProfile("existing-subject", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(UserError.AlreadyExists::class.java)
            }
        )
    }

    @Test
    fun `createProfile should return EmailAlreadyExists error when email exists`() {
        // Given
        val request = buildUserCreateRequest(email = "taken@example.com")
        whenever(userRepository.existsByExternalSubject("new-subject")).thenReturn(false)
        whenever(userRepository.existsByEmail(Email("taken@example.com"))).thenReturn(true)

        // When
        val result = userService.createProfile("new-subject", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(UserError.EmailAlreadyExists::class.java)
                assertThat((error as UserError.EmailAlreadyExists).email).isEqualTo("taken@example.com")
            }
        )
    }

    @Test
    fun `createProfile should return InvalidCountry error for unknown country`() {
        val request = buildUserCreateRequest(
            address = buildAddressDto(countryCode = "INVALID_COUNTRY")
        )

        whenever(userRepository.existsByExternalSubject("test-subject")).thenReturn(false)
        whenever(userRepository.existsByEmail(Email(request.email))).thenReturn(false) // ✅ no matcher

        val result = userService.createProfile("test-subject", request)

        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(UserError.InvalidCountry::class.java)
            }
        )
    }

    @Test
    fun `getProfile should return UserDto when user exists`() {
        // Given
        val user = buildUser()
        whenever(userRepository.findByExternalSubject("test-subject")).thenReturn(user)

        // When
        val result = userService.getProfile("test-subject")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.name).isEqualTo("Test User")
            },
            onFailure = { }
        )
    }

    @Test
    fun `getProfile should return NotFound error when user does not exist`() {
        // Given
        whenever(userRepository.findByExternalSubject("unknown-subject")).thenReturn(null)

        // When
        val result = userService.getProfile("unknown-subject")

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(UserError.NotFound::class.java)
            }
        )
    }

    @Test
    fun `updateAddress should return updated UserDto`() {
        // Given
        val user = buildUser()
        val request = buildUserUpdateAddressRequest(city = "Berlin", countryCode = "GERMANY")
        whenever(userRepository.findByExternalSubject("test-subject")).thenReturn(user)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        // When
        val result = userService.updateAddress("test-subject", request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.defaultAddress?.city).isEqualTo("Berlin")
                assertThat(dto.defaultAddress?.countryCode).isEqualTo("GERMANY")
            },
            onFailure = { }
        )
    }

    @Test
    fun `updateAddress should return NotFound error when user does not exist`() {
        // Given
        val request = buildUserUpdateAddressRequest()
        whenever(userRepository.findByExternalSubject("unknown-subject")).thenReturn(null)

        // When
        val result = userService.updateAddress("unknown-subject", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(UserError.NotFound::class.java)
            }
        )
    }

    private fun buildUser(
        id: UserId = UserId("USER-001"),
        externalSubject: String = "test-subject",
        name: String = "Test User",
        email: String = "test@example.com"
    ): User = User(
        id = id,
        externalSubject = externalSubject,
        name = name,
        email = Email(email),
        defaultAddress = Address(
            street = "Test Street",
            houseNumber = "1",
            postalCode = "1234 AB",
            city = "Amsterdam",
            country = Country.NETHERLANDS
        )
    )
}
