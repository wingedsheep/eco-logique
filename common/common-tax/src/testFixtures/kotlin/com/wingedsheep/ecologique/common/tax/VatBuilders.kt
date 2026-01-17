package com.wingedsheep.ecologique.common.tax

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.products.api.ProductCategory
import java.math.BigDecimal

fun buildVatBreakdown(
    grossAmount: BigDecimal = BigDecimal("121.00"),
    netAmount: BigDecimal = BigDecimal("100.00"),
    vatAmount: BigDecimal = BigDecimal("21.00"),
    vatRate: BigDecimal = BigDecimal("0.21")
): VatCalculator.VatBreakdown = VatCalculator.VatBreakdown(
    grossAmount = grossAmount,
    netAmount = netAmount,
    vatAmount = vatAmount,
    vatRate = vatRate
)

fun calculateVat(
    grossAmount: BigDecimal = BigDecimal("121.00"),
    country: Country = Country.NL,
    productCategory: ProductCategory = ProductCategory.CLOTHING
): VatCalculator.VatBreakdown = VatCalculator.calculate(
    grossAmount = grossAmount,
    country = country,
    productCategory = productCategory
)
