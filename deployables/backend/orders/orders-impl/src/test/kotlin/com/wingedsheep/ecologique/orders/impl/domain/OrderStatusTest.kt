package com.wingedsheep.ecologique.orders.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OrderStatusTest {

    @ParameterizedTest
    @CsvSource(
        "CREATED, RESERVED, true",
        "CREATED, CANCELLED, true",
        "CREATED, PAID, false",
        "RESERVED, PAYMENT_PENDING, true",
        "RESERVED, CANCELLED, true",
        "RESERVED, SHIPPED, false",
        "PAYMENT_PENDING, PAID, true",
        "PAYMENT_PENDING, CANCELLED, true",
        "PAYMENT_PENDING, DELIVERED, false",
        "PAID, SHIPPED, true",
        "PAID, CANCELLED, true",
        "PAID, RESERVED, false",
        "SHIPPED, DELIVERED, true",
        "SHIPPED, CANCELLED, false",
        "DELIVERED, CREATED, false",
        "CANCELLED, CREATED, false"
    )
    internal fun `canTransitionTo should validate transitions`(
        from: OrderStatus,
        to: OrderStatus,
        expected: Boolean
    ) {
        // Given & When & Then
        assertThat(from.canTransitionTo(to)).isEqualTo(expected)
    }

    @Test
    fun `fromString should return matching status`() {
        // Given & When & Then
        assertThat(OrderStatus.fromString("CREATED")).isEqualTo(OrderStatus.CREATED)
        assertThat(OrderStatus.fromString("created")).isEqualTo(OrderStatus.CREATED)
        assertThat(OrderStatus.fromString("INVALID")).isNull()
    }
}
