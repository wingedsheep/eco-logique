package com.wingedsheep.ecologique.orders.impl.domain

import java.util.UUID

@JvmInline
internal value class OrderId(val value: String) {
    init {
        require(value.isNotBlank()) { "OrderId cannot be blank" }
    }

    companion object {
        fun generate(): OrderId = OrderId("ORD-${UUID.randomUUID()}")
    }
}