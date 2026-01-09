package com.wingedsheep.ecologique.orders.impl.infrastructure.persistence

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderId
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.OrderStatus
import com.wingedsheep.ecologique.orders.impl.domain.TotalsSnapshot
import java.util.UUID

internal fun OrderEntity.toOrder(lines: List<OrderLineEntity>): Order = Order(
    id = OrderId(id),
    userId = userId,
    status = OrderStatus.valueOf(status),
    lines = lines.map { it.toOrderLine() },
    totals = TotalsSnapshot(
        subtotal = subtotal,
        grandTotal = grandTotal,
        currency = Currency.valueOf(currency)
    ),
    createdAt = createdAt
)

internal fun OrderLineEntity.toOrderLine(): OrderLine = OrderLine(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)

internal fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id.value,
    userId = userId,
    status = status.name,
    subtotal = totals.subtotal,
    grandTotal = totals.grandTotal,
    currency = totals.currency.name,
    createdAt = createdAt
)

internal fun OrderLine.toEntity(orderId: UUID): OrderLineEntity = OrderLineEntity(
    id = null,
    orderId = orderId,
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)
