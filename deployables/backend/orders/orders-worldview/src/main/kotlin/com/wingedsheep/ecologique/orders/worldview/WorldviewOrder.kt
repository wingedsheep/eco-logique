package com.wingedsheep.ecologique.orders.worldview

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JANE_USER_ID
import com.wingedsheep.ecologique.users.worldview.WorldviewUserDataLoader.Companion.JOHN_USER_ID
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

object WorldviewOrder {

    val johnDoeCreatedOrder = OrderDto(
        id = OrderId(UUID.fromString("00000000-0000-0000-0001-000000000001")),
        userId = JOHN_USER_ID.value.toString(),
        status = OrderStatus.CREATED,
        lines = listOf(
            OrderLineDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                productName = "Organic Cotton T-Shirt",
                unitPrice = BigDecimal("29.99"),
                quantity = 2,
                lineTotal = BigDecimal("59.98")
            )
        ),
        subtotal = BigDecimal("59.98"),
        grandTotal = BigDecimal("59.98"),
        currency = Currency.EUR,
        createdAt = Instant.parse("2024-01-15T10:00:00Z")
    )

    val johnDoePaidOrder = OrderDto(
        id = OrderId(UUID.fromString("00000000-0000-0000-0001-000000000002")),
        userId = JOHN_USER_ID.value.toString(),
        status = OrderStatus.PAID,
        lines = listOf(
            OrderLineDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                productName = "Bamboo Toothbrush Set (4 pack)",
                unitPrice = BigDecimal("12.50"),
                quantity = 1,
                lineTotal = BigDecimal("12.50")
            ),
            OrderLineDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000004")),
                productName = "Stainless Steel Water Bottle 750ml",
                unitPrice = BigDecimal("24.99"),
                quantity = 2,
                lineTotal = BigDecimal("49.98")
            )
        ),
        subtotal = BigDecimal("62.48"),
        grandTotal = BigDecimal("62.48"),
        currency = Currency.EUR,
        createdAt = Instant.parse("2024-01-20T14:30:00Z")
    )

    val janeSmithShippedOrder = OrderDto(
        id = OrderId(UUID.fromString("00000000-0000-0000-0001-000000000003")),
        userId = JANE_USER_ID.value.toString(),
        status = OrderStatus.SHIPPED,
        lines = listOf(
            OrderLineDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000003")),
                productName = "Solar Powered Phone Charger",
                unitPrice = BigDecimal("45.00"),
                quantity = 1,
                lineTotal = BigDecimal("45.00")
            )
        ),
        subtotal = BigDecimal("45.00"),
        grandTotal = BigDecimal("45.00"),
        currency = Currency.EUR,
        createdAt = Instant.parse("2024-01-10T09:15:00Z")
    )

    val janeSmithDeliveredOrder = OrderDto(
        id = OrderId(UUID.fromString("00000000-0000-0000-0001-000000000004")),
        userId = JANE_USER_ID.value.toString(),
        status = OrderStatus.DELIVERED,
        lines = listOf(
            OrderLineDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000005")),
                productName = "Fair Trade Organic Coffee Beans 500g",
                unitPrice = BigDecimal("14.99"),
                quantity = 3,
                lineTotal = BigDecimal("44.97")
            ),
            OrderLineDto(
                productId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000006")),
                productName = "Recycled Rubber Yoga Mat",
                unitPrice = BigDecimal("59.99"),
                quantity = 1,
                lineTotal = BigDecimal("59.99")
            )
        ),
        subtotal = BigDecimal("104.96"),
        grandTotal = BigDecimal("104.96"),
        currency = Currency.EUR,
        createdAt = Instant.parse("2024-01-05T16:45:00Z")
    )

    val allOrders = listOf(
        johnDoeCreatedOrder,
        johnDoePaidOrder,
        janeSmithShippedOrder,
        janeSmithDeliveredOrder
    )

    fun findByUserId(userId: String): List<OrderDto> =
        allOrders.filter { it.userId == userId }

    fun findById(orderId: OrderId): OrderDto? =
        allOrders.find { it.id == orderId }
}
