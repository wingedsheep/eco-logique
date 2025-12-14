package com.wingedsheep.ecologique.common.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class DayNLTest {

    @Test
    fun `today returns current date in Amsterdam`() {
        val expected = LocalDate.now(ZoneId.of("Europe/Amsterdam"))
        val actual = DayNL.today().value
        assertEquals(expected, actual)
    }

    @Test
    fun `tomorrow is one day after today`() {
        val today = DayNL.today()
        val tomorrow = DayNL.tomorrow()
        assertEquals(today.plusDays(1), tomorrow)
    }

    @Test
    fun `yesterday is one day before today`() {
        val today = DayNL.today()
        val yesterday = DayNL.yesterday()
        assertEquals(today.minusDays(1), yesterday)
    }

    @Test
    fun `plusDays handles month boundary`() {
        val date = DayNL.of(2023, 1, 31)
        val nextDay = date.plusDays(1)
        assertEquals(LocalDate.of(2023, 2, 1), nextDay.value)
    }

    @Test
    fun `plusDays handles leap year`() {
        val date = DayNL.of(2024, 2, 28) // 2024 is leap
        val nextDay = date.plusDays(1)
        assertEquals(LocalDate.of(2024, 2, 29), nextDay.value)

        val afterLeap = nextDay.plusDays(1)
        assertEquals(LocalDate.of(2024, 3, 1), afterLeap.value)
    }

    @Test
    fun `plusDays handles non-leap year`() {
        val date = DayNL.of(2023, 2, 28)
        val nextDay = date.plusDays(1)
        assertEquals(LocalDate.of(2023, 3, 1), nextDay.value)
    }

    @Test
    fun `plusDays handles year boundary`() {
        val date = DayNL.of(2023, 12, 31)
        val nextDay = date.plusDays(1)
        assertEquals(LocalDate.of(2024, 1, 1), nextDay.value)
    }

    @Test
    fun `minusDays works correctly`() {
         val date = DayNL.of(2023, 1, 1)
         val prev = date.minusDays(1)
         assertEquals(LocalDate.of(2022, 12, 31), prev.value)
    }

    @Test
    fun `builders work correctly`() {
        val d = buildDayNL(LocalDate.of(2023, 5, 5))
        assertEquals(LocalDate.of(2023, 5, 5), d.value)
    }

    @Test
    fun `default builder uses today`() {
        val d = buildDayNL()
        assertEquals(DayNL.today(), d)
    }
}
