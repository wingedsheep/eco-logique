package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class TotalsSnapshotTest {

    @Test
    fun `should compute subtotal from order lines`() {
        // Given
        val lines = listOf(
            OrderLine.create(ProductId(UUID.randomUUID()), "Product 1", BigDecimal("10.00"), 2),
            OrderLine.create(ProductId(UUID.randomUUID()), "Product 2", BigDecimal("15.50"), 3)
        )

        // When
        val snapshot = TotalsSnapshot.fromOrderLines(lines, Currency.EUR)

        // Then
        assertThat(snapshot.subtotal).isEqualByComparingTo(BigDecimal("66.50"))
    }

    @Test
    fun `should set grandTotal equal to subtotal for demo pricing`() {
        // Given
        val lines = listOf(
            OrderLine.create(ProductId(UUID.randomUUID()), "Product 1", BigDecimal("25.00"), 4)
        )

        // When
        val snapshot = TotalsSnapshot.fromOrderLines(lines, Currency.EUR)

        // Then
        assertThat(snapshot.grandTotal).isEqualByComparingTo(snapshot.subtotal)
        assertThat(snapshot.grandTotal).isEqualByComparingTo(BigDecimal("100.00"))
    }

    @Test
    fun `should preserve currency`() {
        // Given
        val lines = listOf(
            OrderLine.create(ProductId(UUID.randomUUID()), "Product 1", BigDecimal("10.00"), 1)
        )

        // When
        val snapshot = TotalsSnapshot.fromOrderLines(lines, Currency.USD)

        // Then
        assertThat(snapshot.currency).isEqualTo(Currency.USD)
    }

    @Test
    fun `snapshot should preserve totals independently of original lines`() {
        // Given - create lines and compute snapshot
        val productId = ProductId(UUID.randomUUID())
        val lines = listOf(
            OrderLine.create(productId, "Product 1", BigDecimal("20.00"), 2)
        )
        val snapshot = TotalsSnapshot.fromOrderLines(lines, Currency.EUR)
        val originalSubtotal = snapshot.subtotal

        // When - create new lines with different prices (simulating price change)
        val newLinesWithDifferentPrices = listOf(
            OrderLine.create(productId, "Product 1", BigDecimal("30.00"), 2)
        )
        val newSnapshot = TotalsSnapshot.fromOrderLines(newLinesWithDifferentPrices, Currency.EUR)

        // Then - original snapshot should remain unchanged
        assertThat(snapshot.subtotal).isEqualByComparingTo(originalSubtotal)
        assertThat(snapshot.subtotal).isEqualByComparingTo(BigDecimal("40.00"))
        assertThat(newSnapshot.subtotal).isEqualByComparingTo(BigDecimal("60.00"))
    }

    @Test
    fun `should throw exception when subtotal is negative`() {
        // Given & When & Then
        assertThatThrownBy {
            TotalsSnapshot(
                subtotal = BigDecimal("-1.00"),
                grandTotal = BigDecimal("10.00"),
                currency = Currency.EUR
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Subtotal must be non-negative")
    }

    @Test
    fun `should throw exception when grandTotal is negative`() {
        // Given & When & Then
        assertThatThrownBy {
            TotalsSnapshot(
                subtotal = BigDecimal("10.00"),
                grandTotal = BigDecimal("-1.00"),
                currency = Currency.EUR
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Grand total must be non-negative")
    }
}
