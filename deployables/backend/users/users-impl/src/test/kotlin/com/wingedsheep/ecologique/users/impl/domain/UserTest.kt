package com.wingedsheep.ecologique.users.impl.domain

import com.wingedsheep.ecologique.common.country.Country
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun `should create User with valid data`() {
        // Given & When
        val user = User.create(
            externalSubject = "auth0|123456",
            name = "John Doe",
            email = "john@example.com",
            address = null
        )

        // Then
        assertThat(user.name).isEqualTo("John Doe")
        assertThat(user.email.value).isEqualTo("john@example.com")
        assertThat(user.externalSubject).isEqualTo("auth0|123456")
        assertThat(user.id.value).isNotNull()
    }

    @Test
    fun `should create User with address`() {
        // Given
        val address = Address(
            street = "Main Street",
            houseNumber = "123",
            postalCode = "12345",
            city = "Amsterdam",
            country = Country.NL
        )

        // When
        val user = User.create(
            externalSubject = "auth0|123456",
            name = "John Doe",
            email = "john@example.com",
            address = address
        )

        // Then
        assertThat(user.defaultAddress).isNotNull
        assertThat(user.defaultAddress?.city).isEqualTo("Amsterdam")
    }

    @Test
    fun `should throw exception when name is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            User.create(
                externalSubject = "auth0|123456",
                name = "",
                email = "john@example.com",
                address = null
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("User name cannot be blank")
    }

    @Test
    fun `should throw exception when name exceeds 255 characters`() {
        // Given & When & Then
        assertThatThrownBy {
            User.create(
                externalSubject = "auth0|123456",
                name = "a".repeat(256),
                email = "john@example.com",
                address = null
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("User name cannot exceed 255 characters")
    }

    @Test
    fun `should throw exception when external subject is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            User.create(
                externalSubject = "",
                name = "John Doe",
                email = "john@example.com",
                address = null
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("External subject cannot be blank")
    }

    @Test
    fun `updateAddress should return new user with updated address`() {
        // Given
        val user = User.create(
            externalSubject = "auth0|123456",
            name = "John Doe",
            email = "john@example.com",
            address = null
        )

        val newAddress = Address(
            street = "New Street",
            houseNumber = "456",
            postalCode = "54321",
            city = "Rotterdam",
            country = Country.NL
        )

        // When
        val updatedUser = user.updateAddress(newAddress)

        // Then
        assertThat(updatedUser.defaultAddress).isEqualTo(newAddress)
        assertThat(updatedUser.id).isEqualTo(user.id)
        assertThat(updatedUser.name).isEqualTo(user.name)
    }
}
