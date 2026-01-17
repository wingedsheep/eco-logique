package com.wingedsheep.ecologique.orders.impl.infrastructure.persistence

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.TotalsSnapshot
import com.wingedsheep.ecologique.payment.api.PaymentId
import com.wingedsheep.ecologique.products.api.ProductId
import java.util.UUID

internal fun OrderEntity.toOrder(lines: List<OrderLineEntity>): Order = Order(
    id = OrderId(id),
    userId = userId,
    status = OrderStatus.valueOf(status),
    lines = lines.map { it.toOrderLine() },
    totals = TotalsSnapshot(
        subtotal = subtotal,
        vatAmount = vatAmount,
        vatRate = vatRate,
        grandTotal = grandTotal,
        currency = Currency.valueOf(currency)
    ),
    createdAt = createdAt,
    paymentId = paymentId?.let { PaymentId(it) }
)

internal fun OrderLineEntity.toOrderLine(): OrderLine = OrderLine(
    productId = ProductId(UUID.fromString(productId)),
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
    vatAmount = totals.vatAmount,
    vatRate = totals.vatRate,
    grandTotal = totals.grandTotal,
    currency = totals.currency.name,
    createdAt = createdAt,
    paymentId = paymentId?.value
)

internal fun OrderLine.toEntity(orderId: UUID): OrderLineEntity = OrderLineEntity(
    id = null,
    orderId = orderId,
    productId = productId.value.toString(),
    productName = productName,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal
)
