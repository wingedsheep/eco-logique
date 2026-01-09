package com.wingedsheep.ecologique.users.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class EmailTest {

    @Test
    fun `should create Email with valid value`() {
        // Given & When
        val email = Email("john@example.com")

        // Then
        assertThat(email.value).isEqualTo("john@example.com")
    }

    @Test
    fun `should throw exception when email is blank`() {
        // Given & When & Then
        assertThatThrownBy { Email("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Email cannot be blank")
    }

    @Test
    fun `should throw exception when email is whitespace only`() {
        // Given & When & Then
        assertThatThrownBy { Email("   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Email cannot be blank")
    }

    @Test
    fun `should throw exception when email does not contain @`() {
        // Given & When & Then
        assertThatThrownBy { Email("john.example.com") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Email must contain @")
    }

    @Test
    fun `should throw exception when email exceeds 255 characters`() {
        // Given
        val longEmail = "a".repeat(250) + "@b.com"

        // When & Then
        assertThatThrownBy { Email(longEmail) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Email cannot exceed 255 characters")
    }

    @Test
    fun `should accept email at maximum length`() {
        // Given
        val maxLengthEmail = "a".repeat(243) + "@example.com"  // 255 chars total

        // When
        val email = Email(maxLengthEmail)

        // Then
        assertThat(email.value).hasSize(255)
    }
}
