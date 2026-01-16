package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.orders.api.OrderStatus

internal fun OrderStatus.canTransitionTo(target: OrderStatus): Boolean = when (this) {
    OrderStatus.CREATED -> target in listOf(OrderStatus.RESERVED, OrderStatus.CANCELLED)
    OrderStatus.RESERVED -> target in listOf(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED)
    OrderStatus.PAYMENT_PENDING -> target in listOf(OrderStatus.PAID, OrderStatus.CANCELLED)
    OrderStatus.PAID -> target in listOf(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
    OrderStatus.SHIPPED -> target == OrderStatus.DELIVERED
    OrderStatus.CANCELLED -> false
    OrderStatus.DELIVERED -> false
}

internal fun orderStatusFromString(value: String): OrderStatus? =
    OrderStatus.entries.find { it.name.equals(value, ignoreCase = true) }
