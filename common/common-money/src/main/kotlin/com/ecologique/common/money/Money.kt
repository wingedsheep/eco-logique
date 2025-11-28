package com.ecologique.common.money

import java.math.BigDecimal

enum class Currency {
    EUR,
    USD,
    GBP
}

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount must be greater than or equal to zero" }
    }

    operator fun plus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot add money of different currencies" }
        return Money(this.amount.add(other.amount), this.currency)
    }

    operator fun minus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot subtract money of different currencies" }
        return Money(this.amount.subtract(other.amount), this.currency)
    }

    operator fun times(factor: BigDecimal): Money {
        require(factor >= BigDecimal.ZERO) { "Factor must be non-negative" }
        return Money(this.amount.multiply(factor), this.currency)
    }

    operator fun times(factor: Int): Money = times(BigDecimal(factor))

    operator fun times(factor: Double): Money = times(BigDecimal.valueOf(factor))
}
