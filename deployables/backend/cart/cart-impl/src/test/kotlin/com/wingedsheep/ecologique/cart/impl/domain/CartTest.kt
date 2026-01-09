package com.wingedsheep.ecologique.cart.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CartTest {

    @Test
    fun `should create empty cart`() {
        // When
        val cart = Cart.empty("USER-001")

        // Then
        assertThat(cart.userId).isEqualTo("USER-001")
        assertThat(cart.items).isEmpty()
        assertThat(cart.totalItems).isEqualTo(0)
        assertThat(cart.subtotal).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `should add item to empty cart`() {
        // Given
        val cart = Cart.empty("USER-001")
        val item = buildCartItem()

        // When
        val updatedCart = cart.addItem(item)

        // Then
        assertThat(updatedCart.items).hasSize(1)
        assertThat(updatedCart.items[0].productId).isEqualTo("PROD-001")
        assertThat(updatedCart.totalItems).isEqualTo(2)
    }

    @Test
    fun `should merge quantities when adding same product`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem(quantity = 2))
        val newItem = buildCartItem(quantity = 3)

        // When
        val updatedCart = cart.addItem(newItem)

        // Then
        assertThat(updatedCart.items).hasSize(1)
        assertThat(updatedCart.items[0].quantity).isEqualTo(5)
    }

    @Test
    fun `should calculate subtotal correctly`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem(productId = "PROD-001", unitPrice = BigDecimal("10.00"), quantity = 2))
            .addItem(buildCartItem(productId = "PROD-002", unitPrice = BigDecimal("15.50"), quantity = 3))

        // Then
        assertThat(cart.subtotal).isEqualByComparingTo(BigDecimal("66.50"))
        assertThat(cart.totalItems).isEqualTo(5)
    }

    @Test
    fun `should update item quantity`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem(quantity = 2))

        // When
        val updatedCart = cart.updateItemQuantity("PROD-001", 5)

        // Then
        assertThat(updatedCart.items[0].quantity).isEqualTo(5)
    }

    @Test
    fun `should throw when updating with invalid quantity`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem())

        // When & Then
        assertThatThrownBy { cart.updateItemQuantity("PROD-001", 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Quantity must be positive")
    }

    @Test
    fun `should remove item from cart`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem(productId = "PROD-001"))
            .addItem(buildCartItem(productId = "PROD-002"))

        // When
        val updatedCart = cart.removeItem("PROD-001")

        // Then
        assertThat(updatedCart.items).hasSize(1)
        assertThat(updatedCart.items[0].productId).isEqualTo("PROD-002")
    }

    @Test
    fun `should clear cart`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem(productId = "PROD-001"))
            .addItem(buildCartItem(productId = "PROD-002"))

        // When
        val clearedCart = cart.clear()

        // Then
        assertThat(clearedCart.items).isEmpty()
        assertThat(clearedCart.userId).isEqualTo("USER-001")
    }

    @Test
    fun `should check if cart contains product`() {
        // Given
        val cart = Cart.empty("USER-001")
            .addItem(buildCartItem(productId = "PROD-001"))

        // Then
        assertThat(cart.containsProduct("PROD-001")).isTrue()
        assertThat(cart.containsProduct("PROD-002")).isFalse()
    }

    @Test
    fun `should throw when user ID is blank`() {
        // When & Then
        assertThatThrownBy { Cart(userId = "", items = emptyList()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("User ID cannot be blank")
    }

    private fun buildCartItem(
        productId: String = "PROD-001",
        productName: String = "Test Product",
        unitPrice: BigDecimal = BigDecimal("29.99"),
        quantity: Int = 2
    ): CartItem = CartItem.create(
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity
    )
}
