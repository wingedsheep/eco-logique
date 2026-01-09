package com.wingedsheep.ecologique.users.impl.domain

import com.wingedsheep.ecologique.common.country.Country
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AddressTest {

    @Test
    fun `should create Address with valid data`() {
        // Given & When
        val address = Address(
            street = "Main Street",
            houseNumber = "123",
            postalCode = "12345",
            city = "Amsterdam",
            country = Country.NETHERLANDS
        )

        // Then
        assertThat(address.street).isEqualTo("Main Street")
        assertThat(address.houseNumber).isEqualTo("123")
        assertThat(address.postalCode).isEqualTo("12345")
        assertThat(address.city).isEqualTo("Amsterdam")
        assertThat(address.country).isEqualTo(Country.NETHERLANDS)
    }

    @Test
    fun `should throw exception when street is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Address(
                street = "",
                houseNumber = "123",
                postalCode = "12345",
                city = "Amsterdam",
                country = Country.NETHERLANDS
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Street cannot be blank")
    }

    @Test
    fun `should throw exception when house number is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Address(
                street = "Main Street",
                houseNumber = "",
                postalCode = "12345",
                city = "Amsterdam",
                country = Country.NETHERLANDS
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("House number cannot be blank")
    }

    @Test
    fun `should throw exception when postal code is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Address(
                street = "Main Street",
                houseNumber = "123",
                postalCode = "",
                city = "Amsterdam",
                country = Country.NETHERLANDS
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Postal code cannot be blank")
    }

    @Test
    fun `should throw exception when city is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Address(
                street = "Main Street",
                houseNumber = "123",
                postalCode = "12345",
                city = "",
                country = Country.NETHERLANDS
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("City cannot be blank")
    }

    @Test
    fun `should throw exception when street is whitespace only`() {
        // Given & When & Then
        assertThatThrownBy {
            Address(
                street = "   ",
                houseNumber = "123",
                postalCode = "12345",
                city = "Amsterdam",
                country = Country.NETHERLANDS
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Street cannot be blank")
    }
}
