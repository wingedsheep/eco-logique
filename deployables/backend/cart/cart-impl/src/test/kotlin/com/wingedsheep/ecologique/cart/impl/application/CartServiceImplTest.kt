package com.wingedsheep.ecologique.cart.impl.application

import com.wingedsheep.ecologique.cart.api.buildAddCartItemRequest
import com.wingedsheep.ecologique.cart.api.buildUpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import com.wingedsheep.ecologique.cart.impl.domain.Cart
import com.wingedsheep.ecologique.cart.impl.domain.CartItem
import com.wingedsheep.ecologique.cart.impl.domain.CartRepository
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.buildProductDto
import com.wingedsheep.ecologique.products.api.error.ProductError
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class CartServiceImplTest {

    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var productService: ProductService

    @InjectMocks
    private lateinit var cartService: CartServiceImpl

    @Test
    fun `getCart should return empty cart when no cart exists`() {
        // Given
        whenever(cartRepository.findByUserId("USER-001")).thenReturn(null)

        // When
        val result = cartService.getCart("USER-001")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { cart ->
                assertThat(cart.userId).isEqualTo("USER-001")
                assertThat(cart.items).isEmpty()
            },
            onFailure = { }
        )
    }

    @Test
    fun `getCart should return existing cart`() {
        // Given
        val cart = buildCart()
        whenever(cartRepository.findByUserId("USER-001")).thenReturn(cart)

        // When
        val result = cartService.getCart("USER-001")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.items).hasSize(1)
                assertThat(dto.totalItems).isEqualTo(2)
            },
            onFailure = { }
        )
    }

    @Test
    fun `addItem should return ProductNotFound when product does not exist`() {
        // Given
        val request = buildAddCartItemRequest(productId = "PROD-NONEXISTENT")
        whenever(productService.getProduct("PROD-NONEXISTENT"))
            .thenReturn(Result.err(ProductError.NotFound("PROD-NONEXISTENT")))

        // When
        val result = cartService.addItem("USER-001", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(CartError.ProductNotFound::class.java)
            }
        )
    }

    @Test
    fun `addItem should add item to cart`() {
        // Given
        val request = buildAddCartItemRequest(productId = "PROD-001", quantity = 2)
        val product = buildProductDto(id = "PROD-001", name = "Test Product", priceAmount = BigDecimal("29.99"))

        whenever(productService.getProduct("PROD-001")).thenReturn(Result.ok(product))
        whenever(cartRepository.findByUserId("USER-001")).thenReturn(null)
        whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] as Cart }

        // When
        val result = cartService.addItem("USER-001", request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { cart ->
                assertThat(cart.items).hasSize(1)
                assertThat(cart.items[0].productId).isEqualTo("PROD-001")
                assertThat(cart.items[0].quantity).isEqualTo(2)
            },
            onFailure = { }
        )
        verify(cartRepository).save(any())
    }

    @Test
    fun `updateItem should return ItemNotFound when item not in cart`() {
        // Given
        val request = buildUpdateCartItemRequest(quantity = 5)
        whenever(cartRepository.findByUserId("USER-001")).thenReturn(Cart.empty("USER-001"))

        // When
        val result = cartService.updateItem("USER-001", "PROD-NONEXISTENT", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(CartError.ItemNotFound::class.java)
            }
        )
    }

    @Test
    fun `updateItem should update quantity`() {
        // Given
        val request = buildUpdateCartItemRequest(quantity = 5)
        val cart = buildCart()

        whenever(cartRepository.findByUserId("USER-001")).thenReturn(cart)
        whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] as Cart }

        // When
        val result = cartService.updateItem("USER-001", "PROD-001", request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.items[0].quantity).isEqualTo(5)
            },
            onFailure = { }
        )
    }

    @Test
    fun `removeItem should return ItemNotFound when item not in cart`() {
        // Given
        whenever(cartRepository.findByUserId("USER-001")).thenReturn(Cart.empty("USER-001"))

        // When
        val result = cartService.removeItem("USER-001", "PROD-NONEXISTENT")

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(CartError.ItemNotFound::class.java)
            }
        )
    }

    @Test
    fun `removeItem should remove item from cart`() {
        // Given
        val cart = buildCart()
        whenever(cartRepository.findByUserId("USER-001")).thenReturn(cart)
        whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] as Cart }

        // When
        val result = cartService.removeItem("USER-001", "PROD-001")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.items).isEmpty()
            },
            onFailure = { }
        )
    }

    @Test
    fun `clearCart should delete cart`() {
        // When
        val result = cartService.clearCart("USER-001")

        // Then
        assertThat(result.isOk).isTrue()
        verify(cartRepository).deleteByUserId("USER-001")
    }

    private fun buildCart(userId: String = "USER-001"): Cart = Cart(
        userId = userId,
        items = listOf(
            CartItem.create(
                productId = "PROD-001",
                productName = "Test Product",
                unitPrice = BigDecimal("29.99"),
                quantity = 2
            )
        )
    )
}
