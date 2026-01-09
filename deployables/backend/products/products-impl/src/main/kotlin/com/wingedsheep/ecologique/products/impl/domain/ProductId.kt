package com.wingedsheep.ecologique.products.impl.domain

import java.util.UUID

@JvmInline
internal value class ProductId(val value: UUID) {
    companion object {
        fun generate(): ProductId = ProductId(UUID.randomUUID())
    }
}
