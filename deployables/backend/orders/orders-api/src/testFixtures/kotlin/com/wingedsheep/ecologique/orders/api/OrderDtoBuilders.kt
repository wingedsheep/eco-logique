package com.wingedsheep.ecologique.orders.api

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

fun buildOrderDto(
    id: OrderId = OrderId.generate(),
    userId: String = "USER-TEST-001",
    status: OrderStatus = OrderStatus.CREATED,
    lines: List<OrderLineDto> = listOf(buildOrderLineDto()),
    subtotal: BigDecimal = BigDecimal("29.99"),
    vatAmount: BigDecimal = BigDecimal.ZERO,
    vatRate: BigDecimal = BigDecimal.ZERO,
    grandTotal: BigDecimal = BigDecimal("29.99"),
    currency: Currency = Currency.EUR,
    createdAt: Instant = Instant.now()
): OrderDto = OrderDto(
    id = id,
    userId = userId,
    status = status,
    lines = lines,
    subtotal = subtotal,
    vatAmount = vatAmount,
    vatRate = vatRate,
    grandTotal = grandTotal,
    currency = currency,
    createdAt = createdAt
)

fun buildOrderLineDto(
    productId: ProductId = ProductId(UUID.randomUUID()),
    productName: String = "Test Product",
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

fun buildOrderCreateRequest(
    lines: List<OrderLineCreateRequest> = listOf(buildOrderLineCreateRequest()),
    currency: Currency = Currency.EUR
): OrderCreateRequest = OrderCreateRequest(
    lines = lines,
    currency = currency
)

fun buildOrderLineCreateRequest(
    productId: ProductId = ProductId(UUID.randomUUID()),
    productName: String = "Test Product",
    unitPrice: BigDecimal = BigDecimal("29.99"),
    quantity: Int = 1
): OrderLineCreateRequest = OrderLineCreateRequest(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)
