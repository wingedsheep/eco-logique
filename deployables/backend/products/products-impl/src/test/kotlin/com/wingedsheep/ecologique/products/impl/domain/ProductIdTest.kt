package com.wingedsheep.ecologique.products.impl.domain

import com.wingedsheep.ecologique.products.api.ProductId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class ProductIdTest {

    @Test
    fun `should create ProductId with UUID value`() {
        // Given
        val uuid = UUID.randomUUID()

        // When
        val productId = ProductId(uuid)

        // Then
        assertThat(productId.value).isEqualTo(uuid)
    }

    @Test
    fun `generate should create unique ProductId with valid UUID`() {
        // Given & When
        val id1 = ProductId.generate()
        val id2 = ProductId.generate()

        // Then
        assertThat(id1.value).isNotNull()
        assertThat(id2.value).isNotNull()
        assertThat(id1).isNotEqualTo(id2)
    }

    @Test
    fun `equality should be based on UUID value`() {
        // Given
        val uuid = UUID.randomUUID()

        // When
        val id1 = ProductId(uuid)
        val id2 = ProductId(uuid)

        // Then
        assertThat(id1).isEqualTo(id2)
    }
}
