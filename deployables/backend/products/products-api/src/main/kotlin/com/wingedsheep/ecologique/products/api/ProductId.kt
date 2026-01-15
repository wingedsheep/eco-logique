package com.wingedsheep.ecologique.products.api

import java.util.UUID

@JvmInline
value class ProductId(val value: UUID) {
    companion object {
        fun generate(): ProductId = ProductId(UUID.randomUUID())
    }
}