package com.wingedsheep.ecologique.users.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class UserIdTest {

    @Test
    fun `should create UserId wrapping UUID`() {
        // Given
        val uuid = UUID.randomUUID()

        // When
        val userId = UserId(uuid)

        // Then
        assertThat(userId.value).isEqualTo(uuid)
    }

    @Test
    fun `generate should create unique UserId with valid UUID`() {
        // Given & When
        val id1 = UserId.generate()
        val id2 = UserId.generate()

        // Then
        assertThat(id1.value).isNotNull()
        assertThat(id2.value).isNotNull()
        assertThat(id1).isNotEqualTo(id2)
    }
}
