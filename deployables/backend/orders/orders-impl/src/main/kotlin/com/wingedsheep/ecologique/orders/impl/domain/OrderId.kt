package com.wingedsheep.ecologique.orders.impl.domain

import java.util.UUID

@JvmInline
internal value class OrderId(val value: UUID) {
    companion object {
        fun generate(): OrderId = OrderId(UUID.randomUUID())
    }
}
