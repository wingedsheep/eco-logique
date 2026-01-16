package com.wingedsheep.ecologique.cart.impl.application

import com.wingedsheep.ecologique.cart.api.buildAddCartItemRequest
import com.wingedsheep.ecologique.cart.api.buildUpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import com.wingedsheep.ecologique.cart.impl.domain.Cart
import com.wingedsheep.ecologique.cart.impl.domain.CartItem
import com.wingedsheep.ecologique.cart.impl.domain.CartRepository
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.buildProductDto
import com.wingedsheep.ecologique.products.api.error.ProductError
import com.wingedsheep.ecologique.users.api.UserId
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
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CartServiceImplTest {

    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var productService: ProductService

    @InjectMocks
    private lateinit var cartService: CartServiceImpl

    private val testUserId = UserId(UUID.fromString("00000000-0000-0000-0000-000000000100"))
    private val testProductId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    private val nonExistentProductId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000999"))

    @Test
    fun `getCart should return empty cart when no cart exists`() {
        // Given
        whenever(cartRepository.findByUserId(testUserId)).thenReturn(null)

        // When
        val result = cartService.getCart(testUserId)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { cart ->
                assertThat(cart.userId).isEqualTo(testUserId)
                assertThat(cart.items).isEmpty()
            },
            onFailure = { }
        )
    }

    @Test
    fun `getCart should return existing cart`() {
        // Given
        val cart = buildCart()
        whenever(cartRepository.findByUserId(testUserId)).thenReturn(cart)

        // When
        val result = cartService.getCart(testUserId)

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
        val request = buildAddCartItemRequest(productId = nonExistentProductId)
        whenever(productService.getProduct(nonExistentProductId))
            .thenReturn(Result.err(ProductError.NotFound(nonExistentProductId)))

        // When
        val result = cartService.addItem(testUserId, request)

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
        val request = buildAddCartItemRequest(productId = testProductId, quantity = 2)
        val product = buildProductDto(id = testProductId, name = "Test Product", priceAmount = BigDecimal("29.99"))

        whenever(productService.getProduct(testProductId)).thenReturn(Result.ok(product))
        whenever(cartRepository.findByUserId(testUserId)).thenReturn(null)
        whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] as Cart }

        // When
        val result = cartService.addItem(testUserId, request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { cart ->
                assertThat(cart.items).hasSize(1)
                assertThat(cart.items[0].productId).isEqualTo(testProductId)
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
        whenever(cartRepository.findByUserId(testUserId)).thenReturn(Cart.empty(testUserId))

        // When
        val result = cartService.updateItem(testUserId, nonExistentProductId, request)

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

        whenever(cartRepository.findByUserId(testUserId)).thenReturn(cart)
        whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] as Cart }

        // When
        val result = cartService.updateItem(testUserId, testProductId, request)

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
        whenever(cartRepository.findByUserId(testUserId)).thenReturn(Cart.empty(testUserId))

        // When
        val result = cartService.removeItem(testUserId, nonExistentProductId)

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
        whenever(cartRepository.findByUserId(testUserId)).thenReturn(cart)
        whenever(cartRepository.save(any())).thenAnswer { it.arguments[0] as Cart }

        // When
        val result = cartService.removeItem(testUserId, testProductId)

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
        val result = cartService.clearCart(testUserId)

        // Then
        assertThat(result.isOk).isTrue()
        verify(cartRepository).deleteByUserId(testUserId)
    }

    private fun buildCart(userId: UserId = testUserId): Cart = Cart(
        userId = userId,
        items = listOf(
            CartItem.create(
                productId = testProductId,
                productName = "Test Product",
                unitPrice = BigDecimal("29.99"),
                quantity = 2
            )
        )
    )
}
