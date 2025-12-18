package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OrderTest {

    @Test
    fun `should create order with valid data`() {
        // Given & When
        val order = Order.create(
            userId = "USER-001",
            lines = listOf(buildOrderLine()),
            subtotal = BigDecimal("29.99"),
            grandTotal = BigDecimal("29.99"),
            currency = Currency.EUR
        )

        // Then
        assertThat(order.userId).isEqualTo("USER-001")
        assertThat(order.status).isEqualTo(OrderStatus.CREATED)
        assertThat(order.lines).hasSize(1)
        assertThat(order.totals.grandTotal).isEqualByComparingTo(BigDecimal("29.99"))
    }

    @Test
    fun `should throw exception when user ID is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Order.create(
                userId = "",
                lines = listOf(buildOrderLine()),
                subtotal = BigDecimal("29.99"),
                grandTotal = BigDecimal("29.99"),
                currency = Currency.EUR
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("User ID cannot be blank")
    }

    @Test
    fun `should throw exception when lines are empty`() {
        // Given & When & Then
        assertThatThrownBy {
            Order.create(
                userId = "USER-001",
                lines = emptyList(),
                subtotal = BigDecimal("29.99"),
                grandTotal = BigDecimal("29.99"),
                currency = Currency.EUR
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Order must have at least one line")
    }

    @Test
    fun `should transition from CREATED to RESERVED`() {
        // Given
        val order = buildOrder(status = OrderStatus.CREATED)

        // When
        val updatedOrder = order.transitionTo(OrderStatus.RESERVED)

        // Then
        assertThat(updatedOrder.status).isEqualTo(OrderStatus.RESERVED)
    }

    @Test
    fun `should throw exception for invalid status transition`() {
        // Given
        val order = buildOrder(status = OrderStatus.DELIVERED)

        // When & Then
        assertThatThrownBy { order.transitionTo(OrderStatus.CREATED) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Cannot transition from DELIVERED to CREATED")
    }

    @Test
    fun `isOwnedBy should return true for matching user`() {
        // Given
        val order = buildOrder(userId = "USER-001")

        // When & Then
        assertThat(order.isOwnedBy("USER-001")).isTrue()
        assertThat(order.isOwnedBy("USER-002")).isFalse()
    }

    private fun buildOrder(
        userId: String = "USER-001",
        status: OrderStatus = OrderStatus.CREATED
    ): Order = Order(
        id = OrderId.generate(),
        userId = userId,
        status = status,
        lines = listOf(buildOrderLine()),
        totals = TotalsSnapshot(
            subtotal = BigDecimal("29.99"),
            grandTotal = BigDecimal("29.99"),
            currency = Currency.EUR
        ),
        createdAt = java.time.Instant.now()
    )

    private fun buildOrderLine(): OrderLine = OrderLine.create(
        productId = "PROD-001",
        productName = "Test Product",
        unitPrice = BigDecimal("29.99"),
        quantity = 1
    )
}