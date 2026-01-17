package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.buildOrderCreateRequest
import com.wingedsheep.ecologique.orders.api.buildOrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.error.OrderError
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.OrderRepository
import com.wingedsheep.ecologique.orders.impl.domain.TotalsSnapshot
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.buildProductDto
import com.wingedsheep.ecologique.products.api.error.ProductError
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.users.api.error.UserError
import com.wingedsheep.ecologique.common.outbox.OutboxEventPublisher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    private lateinit var userService: UserService

    @Mock
    private lateinit var outboxEventPublisher: OutboxEventPublisher

    private lateinit var orderService: OrderServiceImpl

    @BeforeEach
    fun setUp() {
        orderService = OrderServiceImpl(
            orderRepository = orderRepository,
            productService = productService,
            userService = userService,
            outboxEventPublisher = outboxEventPublisher
        )
    }

    private val testProductUuid = UUID.randomUUID()
    private val testProductId = ProductId(testProductUuid)
    private val testUserId = UUID.randomUUID().toString()
    private val anotherUserId = UUID.randomUUID().toString()

    @Test
    fun `createOrder should return OrderDto when valid request`() {
        // Given
        val request = buildOrderCreateRequest(
            lines = listOf(buildOrderLineCreateRequest(productId = testProductId))
        )
        whenever(productService.getProduct(testProductId)).thenReturn(Result.ok(buildProductDto(id = testProductId)))
        whenever(userService.getProfile(UserId(UUID.fromString(testUserId))))
            .thenReturn(Result.err(UserError.NotFound(testUserId)))
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

        // When
        val result = orderService.createOrder(testUserId, request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.userId).isEqualTo(testUserId)
                assertThat(dto.status).isEqualTo(OrderStatus.CREATED)
            },
            onFailure = { }
        )
        val eventCaptor = argumentCaptor<OrderCreatedOutboxEvent>()
        verify(outboxEventPublisher).publishEvent(eventCaptor.capture())
        assertThat(eventCaptor.firstValue).isInstanceOf(OrderCreatedOutboxEvent::class.java)
    }

    @Test
    fun `createOrder should return ProductNotFound error when product does not exist`() {
        // Given
        val nonExistentUuid = UUID.randomUUID()
        val nonExistentProductId = ProductId(nonExistentUuid)
        val request = buildOrderCreateRequest(
            lines = listOf(buildOrderLineCreateRequest(productId = nonExistentProductId))
        )
        whenever(productService.getProduct(nonExistentProductId))
            .thenReturn(Result.err(ProductError.NotFound(nonExistentProductId)))

        // When
        val result = orderService.createOrder(testUserId, request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ProductNotFound::class.java)
                assertThat((error as OrderError.ProductNotFound).productId).isEqualTo(nonExistentProductId)
            }
        )
    }

    @Test
    fun `createOrder should validate all products in order`() {
        // Given
        val productUuid1 = UUID.randomUUID()
        val productUuid2 = UUID.randomUUID()
        val productId1 = ProductId(productUuid1)
        val productId2 = ProductId(productUuid2)
        val request = buildOrderCreateRequest(
            lines = listOf(
                buildOrderLineCreateRequest(productId = productId1),
                buildOrderLineCreateRequest(productId = productId2)
            )
        )
        whenever(productService.getProduct(productId1)).thenReturn(Result.ok(buildProductDto(id = productId1)))
        whenever(productService.getProduct(productId2)).thenReturn(Result.err(ProductError.NotFound(productId2)))

        // When
        val result = orderService.createOrder(testUserId, request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(OrderError.ProductNotFound::class.java)
                assertThat((error as OrderError.ProductNotFound).productId).isEqualTo(productId2)
            }
        )
    }

    @Test
    fun `getOrder should return OrderDto when order exists and user owns it`() {
        // Given
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val orderId = OrderId(orderUuid)
        val order = buildOrder(id = orderId, userId = "testUserId")
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.getOrder(orderId, "testUserId")

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.id).isEqualTo(orderId)
            },
            onFailure = { }
        )
    }

    @Test
    fun `getOrder should return NotFound error when order does not exist`() {
        // Given
        val orderUuid = UUID.fromString("00000000-0000-0000-0000-000000000999")
        val orderId = OrderId(orderUuid)
        whenever(orderRepository.findById(orderId)).thenReturn(null)

        // When
        val result = orderService.getOrder(orderId, "testUserId")

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
        val order = buildOrder(id = orderId, userId = "testUserId")
        whenever(orderRepository.findById(orderId)).thenReturn(order)

        // When
        val result = orderService.getOrder(orderId, "USER-002")

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
        whenever(orderRepository.findByUserId("testUserId")).thenReturn(orders)

        // When
        val result = orderService.findOrdersForUser("testUserId")

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
        val result = orderService.updateStatus(orderId, OrderStatus.RESERVED)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.status).isEqualTo(OrderStatus.RESERVED)
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
        val result = orderService.updateStatus(orderId, OrderStatus.CREATED)

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
        userId: String = "testUserId",
        status: OrderStatus = OrderStatus.CREATED
    ): Order = Order(
        id = id,
        userId = userId,
        status = status,
        lines = listOf(
            OrderLine.create(
                productId = ProductId(UUID.randomUUID()),
                productName = "Test Product",
                unitPrice = BigDecimal("29.99"),
                quantity = 1
            )
        ),
        totals = TotalsSnapshot(
            subtotal = BigDecimal("29.99"),
            vatAmount = BigDecimal.ZERO,
            vatRate = BigDecimal.ZERO,
            grandTotal = BigDecimal("29.99"),
            currency = Currency.EUR
        ),
        createdAt = Instant.now()
    )
}
