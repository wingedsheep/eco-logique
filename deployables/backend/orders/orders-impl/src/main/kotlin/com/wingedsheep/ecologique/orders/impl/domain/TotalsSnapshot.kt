package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import java.math.BigDecimal

internal data class TotalsSnapshot(
    val subtotal: BigDecimal,
    val vatAmount: BigDecimal,
    val vatRate: BigDecimal,
    val grandTotal: BigDecimal,
    val currency: Currency
) {
    init {
        require(subtotal >= BigDecimal.ZERO) { "Subtotal must be non-negative" }
        require(vatAmount >= BigDecimal.ZERO) { "VAT amount must be non-negative" }
        require(grandTotal >= BigDecimal.ZERO) { "Grand total must be non-negative" }
    }

    companion object {
        fun fromOrderLines(
            lines: List<OrderLine>,
            currency: Currency,
            vatAmount: BigDecimal = BigDecimal.ZERO,
            vatRate: BigDecimal = BigDecimal.ZERO
        ): TotalsSnapshot {
            val subtotal = lines.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.lineTotal) }
            return TotalsSnapshot(
                subtotal = subtotal,
                vatAmount = vatAmount,
                vatRate = vatRate,
                grandTotal = subtotal,
                currency = currency
            )
        }
    }
}