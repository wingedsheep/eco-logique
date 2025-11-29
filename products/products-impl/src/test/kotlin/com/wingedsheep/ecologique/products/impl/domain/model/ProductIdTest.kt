package com.wingedsheep.ecologique.products.impl.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ProductIdTest {

    @Test
    fun `should create ProductId with valid value`() {
        // Given & When
        val productId = ProductId("PROD-001")

        // Then
        assertThat(productId.value).isEqualTo("PROD-001")
    }

    @Test
    fun `should throw exception when value is blank`() {
        // Given & When & Then
        assertThatThrownBy { ProductId("") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("ProductId cannot be blank")
    }

    @Test
    fun `should throw exception when value is whitespace only`() {
        // Given & When & Then
        assertThatThrownBy { ProductId("   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("ProductId cannot be blank")
    }

    @Test
    fun `generate should create unique ProductId`() {
        // Given & When
        val id1 = ProductId.generate()
        val id2 = ProductId.generate()

        // Then
        assertThat(id1.value).startsWith("PROD-")
        assertThat(id2.value).startsWith("PROD-")
        assertThat(id1).isNotEqualTo(id2)
    }
}
