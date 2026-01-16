package com.wingedsheep.ecologique.orders.api

import java.util.UUID

@JvmInline
value class OrderId(val value: UUID) {
    companion object {
        fun generate(): OrderId = OrderId(UUID.randomUUID())
    }
}
