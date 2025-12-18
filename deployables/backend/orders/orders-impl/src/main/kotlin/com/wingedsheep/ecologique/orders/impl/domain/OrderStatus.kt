package com.wingedsheep.ecologique.orders.impl.domain

internal enum class OrderStatus {
    CREATED,
    RESERVED,
    PAYMENT_PENDING,
    PAID,
    CANCELLED,
    SHIPPED,
    DELIVERED;

    fun canTransitionTo(target: OrderStatus): Boolean = when (this) {
        CREATED -> target in listOf(RESERVED, CANCELLED)
        RESERVED -> target in listOf(PAYMENT_PENDING, CANCELLED)
        PAYMENT_PENDING -> target in listOf(PAID, CANCELLED)
        PAID -> target in listOf(SHIPPED, CANCELLED)
        SHIPPED -> target == DELIVERED
        CANCELLED -> false
        DELIVERED -> false
    }

    companion object {
        fun fromString(value: String): OrderStatus? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
