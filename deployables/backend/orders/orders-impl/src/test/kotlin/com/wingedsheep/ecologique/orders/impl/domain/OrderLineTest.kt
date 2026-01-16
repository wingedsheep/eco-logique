package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.products.api.ProductId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class OrderLineTest {

    @Test
    fun `should create order line with calculated total`() {
        // Given & When
        val line = OrderLine.create(
            productId = ProductId(UUID.randomUUID()),
            productName = "Test Product",
            unitPrice = BigDecimal("10.00"),
            quantity = 3
        )

        // Then
        assertThat(line.lineTotal).isEqualByComparingTo(BigDecimal("30.00"))
    }

    @Test
    fun `should throw exception when unit price is zero`() {
        // Given & When & Then
        assertThatThrownBy {
            OrderLine.create(
                productId = ProductId(UUID.randomUUID()),
                productName = "Test",
                unitPrice = BigDecimal.ZERO,
                quantity = 1
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unit price must be positive")
    }

    @Test
    fun `should throw exception when quantity is zero`() {
        // Given & When & Then
        assertThatThrownBy {
            OrderLine.create(
                productId = ProductId(UUID.randomUUID()),
                productName = "Test",
                unitPrice = BigDecimal("10.00"),
                quantity = 0
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Quantity must be positive")
    }
}