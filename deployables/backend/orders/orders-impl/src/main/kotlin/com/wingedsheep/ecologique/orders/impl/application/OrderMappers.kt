package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine

internal fun Order.toDto(): OrderDto = OrderDto(
    id = id.value,
    userId = userId,
    status = status.name,
    lines = lines.map { it.toDto() },
    subtotal = totals.subtotal,
    grandTotal = totals.grandTotal,
    currency = totals.currency.name,
    createdAt = createdAt
)

internal fun OrderLine.toDto(): OrderLineDto = OrderLineDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)