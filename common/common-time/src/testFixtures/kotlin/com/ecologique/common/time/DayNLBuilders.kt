package com.ecologique.common.time

import java.time.LocalDate
import java.time.ZoneId

fun buildDayNL(
    date: LocalDate = LocalDate.now(ZoneId.of("Europe/Amsterdam"))
): DayNL = DayNL(date)
