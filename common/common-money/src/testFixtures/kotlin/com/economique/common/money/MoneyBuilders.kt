package com.economique.common.money

import java.math.BigDecimal

fun buildMoney(
    amount: BigDecimal = BigDecimal("10.00"),
    currency: Currency = Currency.EUR
): Money {
    return Money(amount, currency)
}

fun buildMoney(
    amount: Double,
    currency: Currency = Currency.EUR
): Money {
    return Money(BigDecimal.valueOf(amount), currency)
}

fun buildMoney(
    amount: Int,
    currency: Currency = Currency.EUR
): Money {
    return Money(BigDecimal(amount), currency)
}
