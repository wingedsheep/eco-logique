package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine

internal fun Order.toDto(): OrderDto = OrderDto(
    id = id,
    userId = userId,
    status = status,
    lines = lines.map { it.toDto() },
    subtotal = totals.subtotal,
    vatAmount = totals.vatAmount,
    vatRate = totals.vatRate,
    grandTotal = totals.grandTotal,
    currency = totals.currency,
    createdAt = createdAt,
    paymentId = paymentId
)

internal fun OrderLine.toDto(): OrderLineDto = OrderLineDto(
    productId = productId,
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)