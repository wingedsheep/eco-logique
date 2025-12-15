package com.wingedsheep.ecologique.products.impl.domain

import java.math.BigDecimal

@JvmInline
internal value class CarbonFootprint(val kgCo2: BigDecimal) {
    init {
        require(kgCo2 >= BigDecimal.ZERO) { "Carbon footprint cannot be negative" }
    }
}
