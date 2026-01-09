package com.wingedsheep.ecologique.users.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UserIdTest {

    @Test
    fun `should create UserId with valid value`() {
        // Given & When
        val userId = UserId("USER-001")

        // Then
        assertThat(userId.value).isEqualTo("USER-001")
    }

    @Test
    fun `should throw exception when value is blank`() {
        // Given & When & Then
        assertThatThrownBy { UserId("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("UserId cannot be blank")
    }

    @Test
    fun `should throw exception when value is whitespace only`() {
        // Given & When & Then
        assertThatThrownBy { UserId("   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("UserId cannot be blank")
    }

    @Test
    fun `generate should create unique UserId`() {
        // Given & When
        val id1 = UserId.generate()
        val id2 = UserId.generate()

        // Then
        assertThat(id1.value).startsWith("USER-")
        assertThat(id2.value).startsWith("USER-")
        assertThat(id1).isNotEqualTo(id2)
    }
}
