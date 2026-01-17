package com.wingedsheep.ecologique.common.tax

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.products.api.ProductCategory
import java.math.BigDecimal

/**
 * EU VAT rates by country and category.
 *
 * Standard rates apply to most goods. Reduced rates apply to specific
 * categories like food as permitted under EU VAT Directive.
 */
object VatRate {

    private val rates: Map<Pair<Country, VatCategory>, BigDecimal> = mapOf(
        // Netherlands
        (Country.NL to VatCategory.STANDARD) to BigDecimal("0.21"),
        (Country.NL to VatCategory.REDUCED) to BigDecimal("0.09"),
        // Germany
        (Country.DE to VatCategory.STANDARD) to BigDecimal("0.19"),
        (Country.DE to VatCategory.REDUCED) to BigDecimal("0.07"),
        // Belgium
        (Country.BE to VatCategory.STANDARD) to BigDecimal("0.21"),
        (Country.BE to VatCategory.REDUCED) to BigDecimal("0.06"),
        // France
        (Country.FR to VatCategory.STANDARD) to BigDecimal("0.20"),
        (Country.FR to VatCategory.REDUCED) to BigDecimal("0.055")
    )

    /**
     * Get the VAT rate for a country and category.
     */
    fun getRate(country: Country, category: VatCategory): BigDecimal =
        rates[country to category]
            ?: throw IllegalArgumentException("No VAT rate defined for $country and $category")

    /**
     * Determine the VAT category for a product category.
     * Food uses reduced rate, all other categories use standard rate.
     */
    fun getVatCategory(productCategory: ProductCategory): VatCategory =
        when (productCategory) {
            ProductCategory.FOOD -> VatCategory.REDUCED
            ProductCategory.CLOTHING,
            ProductCategory.HOUSEHOLD,
            ProductCategory.ELECTRONICS,
            ProductCategory.PERSONAL_CARE -> VatCategory.STANDARD
        }

    /**
     * Get the VAT rate for a country and product category.
     */
    fun getRate(country: Country, productCategory: ProductCategory): BigDecimal =
        getRate(country, getVatCategory(productCategory))
}
