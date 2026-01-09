package com.wingedsheep.ecologique.common.country

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CountryTest {
    @Test
    fun `should have correct display names`() {
        assertEquals("Netherlands", Country.NL.displayName)
        assertEquals("Germany", Country.DE.displayName)
        assertEquals("Belgium", Country.BE.displayName)
        assertEquals("France", Country.FR.displayName)
    }

    @Test
    fun `should find country from code`() {
        assertEquals(Country.NL, Country.fromCode("NL"))
        assertEquals(Country.NL, Country.fromCode("nl"))
        assertEquals(Country.DE, Country.fromCode("DE"))
        assertNull(Country.fromCode("INVALID"))
    }
}
