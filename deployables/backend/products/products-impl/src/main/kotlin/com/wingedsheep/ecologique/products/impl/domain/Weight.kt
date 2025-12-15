package com.wingedsheep.ecologique.products.impl.domain

@JvmInline
internal value class Weight(val grams: Int) {
    init {
        require(grams > 0) { "Weight must be positive" }
    }

    fun toKilograms(): Double = grams / 1000.0
}
