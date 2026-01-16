package com.wingedsheep.ecologique.cart.impl.domain

import com.wingedsheep.ecologique.products.api.ProductId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CartItemTest {

    private val testProductId = ProductId.generate()

    @Test
    fun `should create cart item with calculated line total`() {
        // Given & When
        val item = CartItem.create(
            productId = testProductId,
            productName = "Test Product",
            unitPrice = BigDecimal("10.00"),
            quantity = 3
        )

        // Then
        assertThat(item.lineTotal).isEqualByComparingTo(BigDecimal("30.00"))
    }

    @Test
    fun `should throw when product name is blank`() {
        // When & Then
        assertThatThrownBy {
            CartItem.create(
                productId = testProductId,
                productName = "",
                unitPrice = BigDecimal("10.00"),
                quantity = 1
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Product name cannot be blank")
    }

    @Test
    fun `should throw when unit price is zero`() {
        // When & Then
        assertThatThrownBy {
            CartItem.create(
                productId = testProductId,
                productName = "Test",
                unitPrice = BigDecimal.ZERO,
                quantity = 1
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unit price must be positive")
    }

    @Test
    fun `should throw when quantity is zero`() {
        // When & Then
        assertThatThrownBy {
            CartItem.create(
                productId = testProductId,
                productName = "Test",
                unitPrice = BigDecimal("10.00"),
                quantity = 0
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Quantity must be positive")
    }
}
