package com.wingedsheep.ecologique.common.tax

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.products.api.ProductCategory
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Calculates VAT from gross (VAT-inclusive) prices.
 *
 * All prices in this e-commerce platform are VAT-inclusive (gross prices).
 * This calculator extracts the VAT amount from those prices.
 */
object VatCalculator {

    /**
     * Result of VAT calculation.
     */
    data class VatBreakdown(
        val grossAmount: BigDecimal,
        val netAmount: BigDecimal,
        val vatAmount: BigDecimal,
        val vatRate: BigDecimal
    )

    /**
     * Calculate VAT breakdown from a gross (VAT-inclusive) price.
     *
     * Formula: netAmount = grossAmount / (1 + rate)
     *          vatAmount = grossAmount - netAmount
     */
    fun calculate(
        grossAmount: BigDecimal,
        country: Country,
        productCategory: ProductCategory
    ): VatBreakdown {
        val vatRate = VatRate.getRate(country, productCategory)
        return calculateWithRate(grossAmount, vatRate)
    }

    /**
     * Calculate VAT breakdown from a gross (VAT-inclusive) price with explicit rate.
     */
    fun calculateWithRate(
        grossAmount: BigDecimal,
        vatRate: BigDecimal
    ): VatBreakdown {
        val divisor = BigDecimal.ONE.add(vatRate)
        val netAmount = grossAmount.divide(divisor, 2, RoundingMode.HALF_UP)
        val vatAmount = grossAmount.subtract(netAmount)

        return VatBreakdown(
            grossAmount = grossAmount,
            netAmount = netAmount,
            vatAmount = vatAmount,
            vatRate = vatRate
        )
    }
}
