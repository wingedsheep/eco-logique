package com.wingedsheep.ecologique.orders.worldview

import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

fun buildWorldviewOrder(
    id: String = "ORD-${UUID.randomUUID().toString().take(8)}",
    userId: String = "worldview-user",
    status: String = "CREATED",
    lines: List<OrderLineDto> = listOf(buildWorldviewOrderLine()),
    subtotal: BigDecimal = BigDecimal("29.99"),
    grandTotal: BigDecimal = BigDecimal("29.99"),
    currency: String = "EUR",
    createdAt: Instant = Instant.now()
): OrderDto = OrderDto(
    id = id,
    userId = userId,
    status = status,
    lines = lines,
    subtotal = subtotal,
    grandTotal = grandTotal,
    currency = currency,
    createdAt = createdAt
)

fun buildWorldviewOrderLine(
    productId: String = "PROD-WV-001",
    productName: String = "Worldview Test Product",
    unitPrice: BigDecimal = BigDecimal("29.99"),
    quantity: Int = 1,
    lineTotal: BigDecimal = BigDecimal("29.99")
): OrderLineDto = OrderLineDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)

fun buildWorldviewOrderCreateRequest(
    lines: List<OrderLineCreateRequest> = listOf(buildWorldviewOrderLineCreateRequest()),
    currency: String = "EUR"
): OrderCreateRequest = OrderCreateRequest(
    lines = lines,
    currency = currency
)

fun buildWorldviewOrderLineCreateRequest(
    productId: String = "PROD-WV-001",
    productName: String = "Worldview Test Product",
    unitPrice: BigDecimal = BigDecimal("29.99"),
    quantity: Int = 1
): OrderLineCreateRequest = OrderLineCreateRequest(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)