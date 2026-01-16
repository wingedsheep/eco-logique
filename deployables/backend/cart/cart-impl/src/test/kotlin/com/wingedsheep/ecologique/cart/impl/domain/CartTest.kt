package com.wingedsheep.ecologique.cart.impl.domain

import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.api.UserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class CartTest {

    private val userId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000100"))
    private val productId1 = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    private val productId2 = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000002"))

    @Test
    fun `should create empty cart`() {
        // When
        val cart = Cart.empty(userId)

        // Then
        assertThat(cart.userId).isEqualTo(userId)
        assertThat(cart.items).isEmpty()
        assertThat(cart.totalItems).isEqualTo(0)
        assertThat(cart.subtotal).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `should add item to empty cart`() {
        // Given
        val cart = Cart.empty(userId)
        val item = buildCartItem()

        // When
        val updatedCart = cart.addItem(item)

        // Then
        assertThat(updatedCart.items).hasSize(1)
        assertThat(updatedCart.items[0].productId).isEqualTo(productId1)
        assertThat(updatedCart.totalItems).isEqualTo(2)
    }

    @Test
    fun `should merge quantities when adding same product`() {
        // Given
        val cart = Cart.empty(userId)
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
        val cart = Cart.empty(userId)
            .addItem(buildCartItem(productId = productId1, unitPrice = BigDecimal("10.00"), quantity = 2))
            .addItem(buildCartItem(productId = productId2, unitPrice = BigDecimal("15.50"), quantity = 3))

        // Then
        assertThat(cart.subtotal).isEqualByComparingTo(BigDecimal("66.50"))
        assertThat(cart.totalItems).isEqualTo(5)
    }

    @Test
    fun `should update item quantity`() {
        // Given
        val cart = Cart.empty(userId)
            .addItem(buildCartItem(quantity = 2))

        // When
        val updatedCart = cart.updateItemQuantity(productId1, 5)

        // Then
        assertThat(updatedCart.items[0].quantity).isEqualTo(5)
    }

    @Test
    fun `should remove item from cart`() {
        // Given
        val cart = Cart.empty(userId)
            .addItem(buildCartItem(productId = productId1))
            .addItem(buildCartItem(productId = productId2))

        // When
        val updatedCart = cart.removeItem(productId1)

        // Then
        assertThat(updatedCart.items).hasSize(1)
        assertThat(updatedCart.items[0].productId).isEqualTo(productId2)
    }

    @Test
    fun `should clear cart`() {
        // Given
        val cart = Cart.empty(userId)
            .addItem(buildCartItem(productId = productId1))
            .addItem(buildCartItem(productId = productId2))

        // When
        val clearedCart = cart.clear()

        // Then
        assertThat(clearedCart.items).isEmpty()
        assertThat(clearedCart.userId).isEqualTo(userId)
    }

    @Test
    fun `should check if cart contains product`() {
        // Given
        val cart = Cart.empty(userId)
            .addItem(buildCartItem(productId = productId1))

        // Then
        assertThat(cart.containsProduct(productId1)).isTrue()
        assertThat(cart.containsProduct(productId2)).isFalse()
    }

    private fun buildCartItem(
        productId: ProductId = productId1,
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
