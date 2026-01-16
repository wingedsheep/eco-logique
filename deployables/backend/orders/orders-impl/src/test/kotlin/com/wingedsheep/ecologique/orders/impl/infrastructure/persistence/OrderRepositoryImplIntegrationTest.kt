package com.wingedsheep.ecologique.orders.impl.infrastructure.persistence

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.TotalsSnapshot
import com.wingedsheep.ecologique.products.api.ProductId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.context.annotation.Import

@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderRepositoryImpl::class)
class OrderRepositoryImplIntegrationTest {

    companion object {
        @Container
        @JvmField
        val postgresContainer = PostgreSQLContainer("postgres:14-alpine").apply {
            withDatabaseName("ecologique")
            withUsername("user")
            withPassword("password")
        }

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }
            registry.add("spring.flyway.locations") { arrayOf("classpath:db/migration/orders") }
            registry.add("spring.flyway.create-schemas") { "true" }
        }
    }

    @Autowired
    private lateinit var orderRepository: OrderRepositoryImpl

    @BeforeEach
    fun cleanup() {
        orderRepository.findByUserId("TEST-USER").forEach { _ ->
            // Cannot delete directly, so we skip cleanup for now
        }
    }

    @Test
    fun `save should persist and return order with lines`() {
        // Given
        val order = buildOrder()

        // When
        val saved = orderRepository.save(order)

        // Then
        assertThat(saved.id).isEqualTo(order.id)
        assertThat(saved.lines).hasSize(1)
        assertThat(saved.lines[0].productName).isEqualTo("Test Product")
    }

    @Test
    fun `findById should return order when exists`() {
        // Given
        val order = buildOrder()
        orderRepository.save(order)

        // When
        val found = orderRepository.findById(order.id)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(order.id)
        assertThat(found?.lines).hasSize(1)
    }

    @Test
    fun `findById should return null when not exists`() {
        // Given & When
        val found = orderRepository.findById(OrderId(UUID.fromString("00000000-0000-0000-0000-000000000000")))

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `findByUserId should return orders for user`() {
        // Given
        val userId = "USER-${System.currentTimeMillis()}"
        val order1 = buildOrder(userId = userId)
        val order2 = buildOrder(id = OrderId.generate(), userId = userId)
        orderRepository.save(order1)
        orderRepository.save(order2)

        // When
        val orders = orderRepository.findByUserId(userId)

        // Then
        assertThat(orders).hasSize(2)
        assertThat(orders.all { it.userId == userId }).isTrue()
    }

    @Test
    fun `save should update existing order`() {
        // Given
        val order = buildOrder()
        orderRepository.save(order)
        val updatedOrder = order.transitionTo(OrderStatus.RESERVED)

        // When
        val saved = orderRepository.save(updatedOrder)

        // Then
        assertThat(saved.status).isEqualTo(OrderStatus.RESERVED)
    }

    private fun buildOrder(
        id: OrderId = OrderId.generate(),
        userId: String = "TEST-USER"
    ): Order = Order(
        id = id,
        userId = userId,
        status = OrderStatus.CREATED,
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
            grandTotal = BigDecimal("29.99"),
            currency = Currency.EUR
        ),
        createdAt = Instant.now()
    )
}
