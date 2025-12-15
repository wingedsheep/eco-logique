package com.wingedsheep.ecologique.products.impl.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class WeightTest {

    @Test
    fun `should create Weight with valid grams`() {
        // Given & When
        val weight = Weight(150)

        // Then
        assertThat(weight.grams).isEqualTo(150)
    }

    @Test
    fun `should throw exception when grams is zero`() {
        // Given & When & Then
        assertThatThrownBy { Weight(0) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Weight must be positive")
    }

    @Test
    fun `should throw exception when grams is negative`() {
        // Given & When & Then
        assertThatThrownBy { Weight(-10) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Weight must be positive")
    }

    @Test
    fun `toKilograms should convert correctly`() {
        // Given
        val weight = Weight(2500)

        // When
        val kilograms = weight.toKilograms()

        // Then
        assertThat(kilograms).isEqualTo(2.5)
    }
}
