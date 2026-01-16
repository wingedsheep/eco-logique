package com.wingedsheep.ecologique.orders.api

enum class OrderStatus {
    CREATED,
    RESERVED,
    PAYMENT_PENDING,
    PAID,
    CANCELLED,
    SHIPPED,
    DELIVERED
}