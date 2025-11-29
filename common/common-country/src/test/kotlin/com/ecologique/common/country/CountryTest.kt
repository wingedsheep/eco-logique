package com.ecologique.common.country

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CountryTest {
    @Test
    fun `should have correct iso2 codes`() {
        assertEquals("NL", Country.NETHERLANDS.iso2)
        assertEquals("DE", Country.GERMANY.iso2)
        assertEquals("BE", Country.BELGIUM.iso2)
        assertEquals("FR", Country.FRANCE.iso2)
    }
}
