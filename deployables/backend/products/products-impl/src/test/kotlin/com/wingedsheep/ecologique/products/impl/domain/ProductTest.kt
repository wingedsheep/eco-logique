package com.wingedsheep.ecologique.products.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.products.impl.domain.Product
import com.wingedsheep.ecologique.products.impl.domain.ProductCategory
import com.wingedsheep.ecologique.products.impl.domain.SustainabilityRating
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProductTest {

    @Test
    fun `should create Product with valid data`() {
        // Given & When
        val product = Product.create(
            name = "Organic Cotton T-Shirt",
            description = "A sustainable t-shirt",
            category = ProductCategory.CLOTHING,
            priceAmount = BigDecimal("29.99"),
            priceCurrency = Currency.EUR,
            weightGrams = 150,
            carbonFootprintKg = BigDecimal("2.1")
        )

        // Then
        assertThat(product.name).isEqualTo("Organic Cotton T-Shirt")
        assertThat(product.category).isEqualTo(ProductCategory.CLOTHING)
        assertThat(product.price.amount).isEqualByComparingTo(BigDecimal("29.99"))
        assertThat(product.weight.grams).isEqualTo(150)
    }

    @Test
    fun `should throw exception when name is blank`() {
        // Given & When & Then
        assertThatThrownBy {
            Product.create(
                name = "",
                description = "Description",
                category = ProductCategory.CLOTHING,
                priceAmount = BigDecimal("29.99"),
                priceCurrency = Currency.EUR,
                weightGrams = 150,
                carbonFootprintKg = BigDecimal("2.1")
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Product name cannot be blank")
    }

    @Test
    fun `should throw exception when price is zero`() {
        // Given & When & Then
        assertThatThrownBy {
            Product.create(
                name = "Test Product",
                description = "Description",
                category = ProductCategory.CLOTHING,
                priceAmount = BigDecimal.ZERO,
                priceCurrency = Currency.EUR,
                weightGrams = 150,
                carbonFootprintKg = BigDecimal("2.1")
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Price must be positive")
    }

    @Test
    fun `should calculate sustainability rating based on category and carbon footprint`() {
        // Given & When
        val lowCarbonProduct = Product.create(
            name = "Low Carbon Food",
            description = "Very sustainable food item",
            category = ProductCategory.FOOD,
            priceAmount = BigDecimal("10.00"),
            priceCurrency = Currency.EUR,
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("0.2")
        )

        val highCarbonProduct = Product.create(
            name = "High Carbon Electronics",
            description = "Less sustainable electronics",
            category = ProductCategory.ELECTRONICS,
            priceAmount = BigDecimal("100.00"),
            priceCurrency = Currency.EUR,
            weightGrams = 500,
            carbonFootprintKg = BigDecimal("10.0")
        )

        // Then
        assertThat(lowCarbonProduct.sustainabilityRating).isEqualTo(SustainabilityRating.A_PLUS)
        assertThat(highCarbonProduct.sustainabilityRating).isEqualTo(SustainabilityRating.D)
    }

    @Test
    fun `updatePrice should return new product with updated price`() {
        // Given
        val product = Product.create(
            name = "Test Product",
            description = "Description",
            category = ProductCategory.HOUSEHOLD,
            priceAmount = BigDecimal("20.00"),
            priceCurrency = Currency.EUR,
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("1.0")
        )

        // When
        val updatedProduct = product.updatePrice(Money(BigDecimal("25.00"), Currency.EUR))

        // Then
        assertThat(updatedProduct.price.amount).isEqualByComparingTo(BigDecimal("25.00"))
        assertThat(updatedProduct.id).isEqualTo(product.id)
        assertThat(updatedProduct.name).isEqualTo(product.name)
    }

    @Test
    fun `updatePrice should throw exception when new price is zero`() {
        // Given
        val product = Product.create(
            name = "Test Product",
            description = "Description",
            category = ProductCategory.HOUSEHOLD,
            priceAmount = BigDecimal("20.00"),
            priceCurrency = Currency.EUR,
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("1.0")
        )

        // When & Then
        assertThatThrownBy { product.updatePrice(Money(BigDecimal.ZERO, Currency.EUR)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Price must be positive")
    }
}
