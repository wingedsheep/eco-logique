package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.products.api.ProductId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class OrderTest {

    @Test
    fun `should create order with valid data`() {
        // Given & When
        val order = Order.create(
            userId = "USER-001",
            lines = listOf(buildOrderLine()),
            currency = Currency.EUR
        )

        // Then
        assertThat(order.userId).isEqualTo("USER-001")
        assertThat(order.status).isEqualTo(OrderStatus.CREATED)
        assertThat(order.lines).hasSize(1)
        assertThat(order.totals.grandTotal).isEqualByComparingTo(BigDecimal("29.99"))
    }

    @Test
    fun `should compute totals from order lines`() {
        // Given
        val lines = listOf(
            OrderLine.create(ProductId(UUID.randomUUID()), "Product 1", BigDecimal("10.00"), 2),
            OrderLine.create(ProductId(UUID.randomUUID()), "Product 2", BigDecimal("15.50"), 3)
        )

        // When
        val order = Order.create(
            userId = "USER-001",
            lines = lines,
            currency = Currency.EUR
        )

        // Then
        assertThat(order.totals.subtotal).isEqualByComparingTo(BigDecimal("66.50"))
        assertThat(order.totals.grandTotal).isEqualByComparingTo(BigDecimal("66.50"))
        assertThat(order.totals.currency).isEqualTo(Currency.EUR)
    }

    @Test
    fun `should throw exception when user ID is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Order.create(
                userId = "",
                lines = listOf(buildOrderLine()),
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

    @Test
    fun `order snapshot should preserve totals even if product price changes later`() {
        // Given - Original product price is 25.00
        val productId = ProductId(UUID.randomUUID())
        val originalPrice = BigDecimal("25.00")
        val orderLine = OrderLine.create(
            productId = productId,
            productName = "Product A",
            unitPrice = originalPrice,
            quantity = 2
        )
        val order = Order.create(
            userId = "USER-001",
            lines = listOf(orderLine),
            currency = Currency.EUR
        )
        val originalTotals = order.totals

        // When - Product price changes to 35.00 (simulated by creating new order line)
        val newPrice = BigDecimal("35.00")
        val newOrderLine = OrderLine.create(
            productId = productId,
            productName = "Product A",
            unitPrice = newPrice,
            quantity = 2
        )

        // Then - Original order snapshot should be unchanged
        assertThat(order.totals.subtotal).isEqualByComparingTo(BigDecimal("50.00"))
        assertThat(order.totals.grandTotal).isEqualByComparingTo(BigDecimal("50.00"))
        assertThat(order.lines[0].unitPrice).isEqualByComparingTo(originalPrice)
        assertThat(order.lines[0].lineTotal).isEqualByComparingTo(BigDecimal("50.00"))

        // And - New order with new price would have different totals
        val newOrder = Order.create(
            userId = "USER-001",
            lines = listOf(newOrderLine),
            currency = Currency.EUR
        )
        assertThat(newOrder.totals.subtotal).isEqualByComparingTo(BigDecimal("70.00"))
        assertThat(newOrder.totals.grandTotal).isEqualByComparingTo(BigDecimal("70.00"))

        // Original order totals remain unchanged (snapshot consistency)
        assertThat(order.totals).isEqualTo(originalTotals)
    }

    @Test
    fun `order line should store product name snapshot at purchase time`() {
        // Given
        val orderLine = OrderLine.create(
            productId = ProductId(UUID.randomUUID()),
            productName = "Original Product Name",
            unitPrice = BigDecimal("10.00"),
            quantity = 1
        )

        val order = Order.create(
            userId = "USER-001",
            lines = listOf(orderLine),
            currency = Currency.EUR
        )

        // Then - Product name is snapshotted
        assertThat(order.lines[0].productName).isEqualTo("Original Product Name")
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
        productId = ProductId(UUID.randomUUID()),
        productName = "Test Product",
        unitPrice = BigDecimal("29.99"),
        quantity = 1
    )
}