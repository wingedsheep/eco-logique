package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.buildOrderCreateRequest
import com.wingedsheep.ecologique.orders.api.buildOrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.error.OrderError
import com.wingedsheep.ecologique.orders.api.event.OrderCreated
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderId
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.OrderRepository
import com.wingedsheep.ecologique.orders.impl.domain.OrderStatus
import com.wingedsheep.ecologique.orders.impl.domain.TotalsSnapshot
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class OrderServiceImplTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var productService: ProductService

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var orderService: OrderServiceImpl

    @Test
    fun `createOrder should return OrderDto when valid request`() {
        // Given
        val request = buildOrderCreateRequest()
        whenever(productService.getProduct("PROD-TEST-001")).thenReturn(Result.ok(buildProductDto(id = "PROD-TEST-001")))
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

        // When
        val result = orderService.createOrder("USER-001", request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.userId).isEqualTo("USER-001")
                assertThat(dto.status).isEqualTo("CREATED")
            },
            onFailure = { }
        )
        val eventCaptor = argumentCaptor<Any>()
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertThat(eventCaptor.firstValue).isInstanceOf(OrderCreated::class.java)
    }

    @Test
    fun `createOrder should return ValidationFailed error for invalid currency`() {
        // Given
        val request = buildOrderCreateRequest(currency = "INVALID")

        // When
        val result = orderService.createOrder("USER-001", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ValidationFailed::class.java)
            }
        )
    }

    @Test
    fun `createOrder should return ProductNotFound error when product does not exist`() {
        // Given
        val request = buildOrderCreateRequest(
            lines = listOf(buildOrderLineCreateRequest(productId = "PROD-NONEXISTENT"))
        )
        whenever(productService.getProduct("PROD-NONEXISTENT"))
            .thenReturn(Result.err(ProductError.NotFound("PROD-NONEXISTENT")))

        // When
        val result = orderService.createOrder("USER-001", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ProductNotFound::class.java)
                assertThat((error as OrderError.ProductNotFound).productId).isEqualTo("PROD-NONEXISTENT")
            }
        )
    }

    @Test
    fun `createOrder should validate all products in order`() {
        // Given
        val request = buildOrderCreateRequest(
            lines = listOf(
                buildOrderLineCreateRequest(productId = "PROD-001"),
                buildOrderLineCreateRequest(productId = "PROD-002")
            ),
            subtotal = BigDecimal("59.98"),
            grandTotal = BigDecimal("59.98")
        )
        whenever(productService.getProduct("PROD-001")).thenReturn(Result.ok(buildProductDto(id = "PROD-001")))
        whenever(productService.getProduct("PROD-002")).thenReturn(Result.err(ProductError.NotFound("PROD-002")))

        // When
        val result = orderService.createOrder("USER-001", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ProductNotFound::class.java)
                assertThat((error as OrderError.ProductNotFound).productId).isEqualTo("PROD-002")
            }
        )
    }

    @Test
    fun `getOrder should return OrderDto when order exists and user owns it`() {
        // Given
        val orderId = OrderId("ORD-001")
        val order = buildOrder(id = orderId, userId = "USER-001")
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.getOrder("ORD-001", "USER-001")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.id).isEqualTo("ORD-001")
            },
            onFailure = { }
        )
    }

    @Test
    fun `getOrder should return NotFound error when order does not exist`() {
        // Given
        whenever(orderRepository.findById(OrderId("ORD-999"))).thenReturn(null)

        // When
        val result = orderService.getOrder("ORD-999", "USER-001")

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.NotFound::class.java)
            }
        )
    }

    @Test
    fun `getOrder should return AccessDenied error when user does not own order`() {
        // Given
        val orderId = OrderId("ORD-001")
        val order = buildOrder(id = orderId, userId = "USER-001")
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.getOrder("ORD-001", "USER-002")

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.AccessDenied::class.java)
            }
        )
    }

    @Test
    fun `findOrdersForUser should return list of OrderDto`() {
        // Given
        val orders = listOf(buildOrder(), buildOrder(id = OrderId("ORD-002")))
        whenever(orderRepository.findByUserId("USER-001")).thenReturn(orders)

        // When
        val result = orderService.findOrdersForUser("USER-001")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dtos ->
                assertThat(dtos).hasSize(2)
            },
            onFailure = { }
        )
    }

    @Test
    fun `updateStatus should return updated OrderDto`() {
        // Given
        val orderId = OrderId("ORD-001")
        val order = buildOrder(id = orderId, status = OrderStatus.CREATED)
        whenever(orderRepository.findById(orderId)).thenReturn(order)
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

        // When
        val result = orderService.updateStatus("ORD-001", "RESERVED")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.status).isEqualTo("RESERVED")
            },
            onFailure = { }
        )
    }

    @Test
    fun `updateStatus should return InvalidStatus error for invalid transition`() {
        // Given
        val orderId = OrderId("ORD-001")
        val order = buildOrder(id = orderId, status = OrderStatus.DELIVERED)
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.updateStatus("ORD-001", "CREATED")

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.InvalidStatus::class.java)
            }
        )
    }

    private fun buildOrder(
        id: OrderId = OrderId("ORD-001"),
        userId: String = "USER-001",
        status: OrderStatus = OrderStatus.CREATED
    ): Order = Order(
        id = id,
        userId = userId,
        status = status,
        lines = listOf(
            OrderLine.create(
                productId = "PROD-001",
                productName = "Test Product",
                unitPrice = BigDecimal("29.99"),
                quantity = 1
            )
        ),
        totals = TotalsSnapshot(
            subtotal = BigDecimal("29.99"),
            grandTotal = BigDecimal("29.99"),
            currency = Currency.EUR
        ),
        createdAt = Instant.now()
    )
}
