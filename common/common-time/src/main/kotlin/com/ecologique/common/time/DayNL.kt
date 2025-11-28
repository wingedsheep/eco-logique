package com.ecologique.common.time

import java.time.LocalDate
import java.time.ZoneId

@JvmInline
value class DayNL(val value: LocalDate) : Comparable<DayNL> {

    companion object {
        private val NL_ZONE = ZoneId.of("Europe/Amsterdam")

        fun today(): DayNL = DayNL(LocalDate.now(NL_ZONE))

        fun tomorrow(): DayNL = today().plusDays(1)

        fun yesterday(): DayNL = today().minusDays(1)

        fun of(date: LocalDate): DayNL = DayNL(date)

        fun of(year: Int, month: Int, dayOfMonth: Int): DayNL =
            DayNL(LocalDate.of(year, month, dayOfMonth))
    }

    fun plusDays(days: Long): DayNL = DayNL(value.plusDays(days))

    fun minusDays(days: Long): DayNL = DayNL(value.minusDays(days))

    override fun compareTo(other: DayNL): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()
}
