package com.wingedsheep.ecologique.common.tax

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.products.api.ProductCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class VatCalculatorTest {

    @Nested
    inner class StandardRates {

        @Test
        fun `should calculate 21% VAT for Netherlands standard rate`() {
            // Given
            val grossAmount = BigDecimal("121.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.NL, ProductCategory.CLOTHING)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.21"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("21.00"))
        }

        @Test
        fun `should calculate 19% VAT for Germany standard rate`() {
            // Given
            val grossAmount = BigDecimal("119.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.DE, ProductCategory.ELECTRONICS)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.19"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("19.00"))
        }

        @Test
        fun `should calculate 21% VAT for Belgium standard rate`() {
            // Given
            val grossAmount = BigDecimal("121.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.BE, ProductCategory.HOUSEHOLD)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.21"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("21.00"))
        }

        @Test
        fun `should calculate 20% VAT for France standard rate`() {
            // Given
            val grossAmount = BigDecimal("120.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.FR, ProductCategory.PERSONAL_CARE)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.20"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("20.00"))
        }
    }

    @Nested
    inner class ReducedRates {

        @Test
        fun `should calculate 9% VAT for Netherlands food`() {
            // Given
            val grossAmount = BigDecimal("109.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.NL, ProductCategory.FOOD)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.09"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("9.00"))
        }

        @Test
        fun `should calculate 7% VAT for Germany food`() {
            // Given
            val grossAmount = BigDecimal("107.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.DE, ProductCategory.FOOD)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.07"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("7.00"))
        }

        @Test
        fun `should calculate 6% VAT for Belgium food`() {
            // Given
            val grossAmount = BigDecimal("106.00")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.BE, ProductCategory.FOOD)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.06"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("6.00"))
        }

        @Test
        fun `should calculate 5_5% VAT for France food`() {
            // Given
            val grossAmount = BigDecimal("105.50")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.FR, ProductCategory.FOOD)

            // Then
            assertThat(result.vatRate).isEqualByComparingTo(BigDecimal("0.055"))
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("100.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("5.50"))
        }
    }

    @Nested
    inner class RoundingBehavior {

        @Test
        fun `should round VAT to 2 decimal places`() {
            // Given - amount that produces fractional cents
            val grossAmount = BigDecimal("29.99")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.NL, ProductCategory.CLOTHING)

            // Then
            assertThat(result.netAmount.scale()).isLessThanOrEqualTo(2)
            assertThat(result.vatAmount.scale()).isLessThanOrEqualTo(2)
            assertThat(result.netAmount.add(result.vatAmount)).isEqualByComparingTo(grossAmount)
        }

        @Test
        fun `should handle small amounts correctly`() {
            // Given
            val grossAmount = BigDecimal("1.21")

            // When
            val result = VatCalculator.calculate(grossAmount, Country.NL, ProductCategory.CLOTHING)

            // Then
            assertThat(result.netAmount).isEqualByComparingTo(BigDecimal("1.00"))
            assertThat(result.vatAmount).isEqualByComparingTo(BigDecimal("0.21"))
        }
    }

    @Nested
    inner class ProductCategoryMapping {

        @ParameterizedTest
        @CsvSource(
            "CLOTHING, STANDARD",
            "HOUSEHOLD, STANDARD",
            "ELECTRONICS, STANDARD",
            "PERSONAL_CARE, STANDARD",
            "FOOD, REDUCED"
        )
        fun `should map product category to correct VAT category`(
            productCategory: ProductCategory,
            expectedVatCategory: VatCategory
        ) {
            // When
            val vatCategory = VatRate.getVatCategory(productCategory)

            // Then
            assertThat(vatCategory).isEqualTo(expectedVatCategory)
        }
    }
}
