package com.wingedsheep.ecologique.products.impl.domain.model

import java.util.UUID

@JvmInline
internal value class ProductId(val value: String) {
    init {
        require(value.isNotBlank()) { "ProductId cannot be blank" }
    }

    companion object {
        fun generate(): ProductId = ProductId("PROD-${UUID.randomUUID()}")
    }
}
