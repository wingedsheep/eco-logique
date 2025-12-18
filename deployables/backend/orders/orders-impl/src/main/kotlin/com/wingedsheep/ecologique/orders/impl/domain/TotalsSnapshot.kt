package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import java.math.BigDecimal

internal data class TotalsSnapshot(
    val subtotal: BigDecimal,
    val grandTotal: BigDecimal,
    val currency: Currency
) {
    init {
        require(subtotal >= BigDecimal.ZERO) { "Subtotal must be non-negative" }
        require(grandTotal >= BigDecimal.ZERO) { "Grand total must be non-negative" }
    }
}