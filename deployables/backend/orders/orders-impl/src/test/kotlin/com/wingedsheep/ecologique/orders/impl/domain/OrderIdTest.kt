package com.wingedsheep.ecologique.orders.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class OrderIdTest {

    @Test
    fun `should create OrderId with UUID value`() {
        // Given
        val uuid = UUID.randomUUID()

        // When
        val orderId = OrderId(uuid)

        // Then
        assertThat(orderId.value).isEqualTo(uuid)
    }

    @Test
    fun `generate should create unique OrderId with valid UUID`() {
        // Given & When
        val id1 = OrderId.generate()
        val id2 = OrderId.generate()

        // Then
        assertThat(id1.value).isNotNull()
        assertThat(id2.value).isNotNull()
        assertThat(id1).isNotEqualTo(id2)
    }

    @Test
    fun `value class should have proper equality`() {
        // Given
        val uuid = UUID.randomUUID()

        // When
        val orderId1 = OrderId(uuid)
        val orderId2 = OrderId(uuid)

        // Then
        assertThat(orderId1).isEqualTo(orderId2)
    }
}
