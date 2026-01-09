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
import java.util.UUID

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

    private val testProductId = UUID.randomUUID()

    @Test
    fun `createOrder should return OrderDto when valid request`() {
        // Given
        val request = buildOrderCreateRequest(
            lines = listOf(buildOrderLineCreateRequest(productId = testProductId.toString()))
        )
        whenever(productService.getProduct(testProductId)).thenReturn(Result.ok(buildProductDto(id = testProductId)))
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
        val nonExistentProductId = UUID.randomUUID()
        val request = buildOrderCreateRequest(
            lines = listOf(buildOrderLineCreateRequest(productId = nonExistentProductId.toString()))
        )
        whenever(productService.getProduct(nonExistentProductId))
            .thenReturn(Result.err(ProductError.NotFound(nonExistentProductId)))

        // When
        val result = orderService.createOrder("USER-001", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ProductNotFound::class.java)
                assertThat((error as OrderError.ProductNotFound).productId).isEqualTo(nonExistentProductId.toString())
            }
        )
    }

    @Test
    fun `createOrder should validate all products in order`() {
        // Given
        val productId1 = UUID.randomUUID()
        val productId2 = UUID.randomUUID()
        val request = buildOrderCreateRequest(
            lines = listOf(
                buildOrderLineCreateRequest(productId = productId1.toString()),
                buildOrderLineCreateRequest(productId = productId2.toString())
            )
        )
        whenever(productService.getProduct(productId1)).thenReturn(Result.ok(buildProductDto(id = productId1)))
        whenever(productService.getProduct(productId2)).thenReturn(Result.err(ProductError.NotFound(productId2)))

        // When
        val result = orderService.createOrder("USER-001", request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ProductNotFound::class.java)
                assertThat((error as OrderError.ProductNotFound).productId).isEqualTo(productId2.toString())
            }
        )
    }

    @Test
    fun `getOrder should return OrderDto when order exists and user owns it`() {
        // Given
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val orderId = OrderId(orderUuid)
        val order = buildOrder(id = orderId, userId = "USER-001")
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.getOrder(orderUuid, "USER-001")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.id).isEqualTo(orderUuid)
            },
            onFailure = { }
        )
    }

    @Test
    fun `getOrder should return NotFound error when order does not exist`() {
        // Given
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000999")
        whenever(orderRepository.findById(OrderId(orderUuid))).thenReturn(null)

        // When
        val result = orderService.getOrder(orderUuid, "USER-001")

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
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val orderId = OrderId(orderUuid)
        val order = buildOrder(id = orderId, userId = "USER-001")
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.getOrder(orderUuid, "USER-002")

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
        val orders = listOf(
            buildOrder(),
            buildOrder(id = OrderId(UUID.fromString("00000000-0000-0000-0000-000000000002")))
        )
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
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val orderId = OrderId(orderUuid)
        val order = buildOrder(id = orderId, status = OrderStatus.CREATED)
        whenever(orderRepository.findById(orderId)).thenReturn(order)
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

        // When
        val result = orderService.updateStatus(orderUuid, "RESERVED")

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
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val orderId = OrderId(orderUuid)
        val order = buildOrder(id = orderId, status = OrderStatus.DELIVERED)
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.updateStatus(orderUuid, "CREATED")

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
        id: OrderId = OrderId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
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
