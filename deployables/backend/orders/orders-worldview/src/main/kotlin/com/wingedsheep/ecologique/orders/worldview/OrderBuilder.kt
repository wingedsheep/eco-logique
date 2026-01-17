package com.wingedsheep.ecologique.orders.worldview

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal
import java.time.Instant

fun buildWorldviewOrder(
    id: OrderId = OrderId.generate(),
    userId: String = "worldview-user",
    status: OrderStatus = OrderStatus.CREATED,
    lines: List<OrderLineDto> = listOf(buildWorldviewOrderLine()),
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

fun buildWorldviewOrderLine(
    productId: ProductId = ProductId.generate(),
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
    currency: Currency = Currency.EUR
): OrderCreateRequest = OrderCreateRequest(
    lines = lines,
    currency = currency
)

fun buildWorldviewOrderLineCreateRequest(
    productId: ProductId = ProductId.generate(),
    productName: String = "Worldview Test Product",
    unitPrice: BigDecimal = BigDecimal("29.99"),
    quantity: Int = 1
): OrderLineCreateRequest = OrderLineCreateRequest(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity
)
