package com.wingedsheep.ecologique.orders.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class OrderIdTest {

    @Test
    fun `should create OrderId with valid value`() {
        // Given & When
        val orderId = OrderId("ORD-001")

        // Then
        assertThat(orderId.value).isEqualTo("ORD-001")
    }

    @Test
    fun `should throw exception when value is blank`() {
        // Given & When & Then
        assertThatThrownBy { OrderId("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("OrderId cannot be blank")
    }

    @Test
    fun `generate should create unique OrderId`() {
        // Given & When
        val id1 = OrderId.generate()
        val id2 = OrderId.generate()

        // Then
        assertThat(id1.value).startsWith("ORD-")
        assertThat(id2.value).startsWith("ORD-")
        assertThat(id1).isNotEqualTo(id2)
    }
}