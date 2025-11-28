package com.economique.common.money

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `should create money with valid amount`() {
        val money = Money(BigDecimal("10.00"), Currency.EUR)
        assertEquals(BigDecimal("10.00"), money.amount)
        assertEquals(Currency.EUR, money.currency)
    }

    @Test
    fun `should throw exception when amount is negative`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            Money(BigDecimal("-1.00"), Currency.EUR)
        }
        assertEquals("Amount must be greater than or equal to zero", exception.message)
    }

    @Test
    fun `should add money of same currency`() {
        val m1 = Money(BigDecimal("10.00"), Currency.EUR)
        val m2 = Money(BigDecimal("5.50"), Currency.EUR)
        val result = m1 + m2
        assertEquals(BigDecimal("15.50"), result.amount)
        assertEquals(Currency.EUR, result.currency)
    }

    @Test
    fun `should throw exception when adding money of different currency`() {
        val m1 = Money(BigDecimal("10.00"), Currency.EUR)
        val m2 = Money(BigDecimal("5.50"), Currency.USD)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            m1 + m2
        }
        assertEquals("Cannot add money of different currencies", exception.message)
    }

    @Test
    fun `should subtract money of same currency`() {
        val m1 = Money(BigDecimal("10.00"), Currency.EUR)
        val m2 = Money(BigDecimal("4.00"), Currency.EUR)
        val result = m1 - m2
        assertEquals(BigDecimal("6.00"), result.amount)
        assertEquals(Currency.EUR, result.currency)
    }

    @Test
    fun `should throw exception when subtracting results in negative amount`() {
        val m1 = Money(BigDecimal("5.00"), Currency.EUR)
        val m2 = Money(BigDecimal("10.00"), Currency.EUR)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            m1 - m2
        }
        assertEquals("Amount must be greater than or equal to zero", exception.message)
    }

    @Test
    fun `should multiply money by big decimal factor`() {
        val m = Money(BigDecimal("10.00"), Currency.EUR)
        val result = m * BigDecimal("2.5")
        // Note: scale might differ depending on implementation, but value should be equal
        assertEquals(0, BigDecimal("25.00").compareTo(result.amount))
    }

    @Test
    fun `should multiply money by int factor`() {
        val m = Money(BigDecimal("10.00"), Currency.EUR)
        val result = m * 2
        assertEquals(0, BigDecimal("20.00").compareTo(result.amount))
    }
}
