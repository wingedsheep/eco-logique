package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.SustainabilityRating
import com.wingedsheep.ecologique.products.impl.domain.CarbonFootprint
import com.wingedsheep.ecologique.products.impl.domain.Product
import com.wingedsheep.ecologique.products.impl.domain.Weight
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.math.BigDecimal

@DataJdbcTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = ["com.wingedsheep.ecologique.products.impl.infrastructure.persistence"])
class ProductRepositoryImplIntegrationTest {

    companion object {
        @Container
        @JvmField
        val postgresContainer = PostgreSQLContainer("postgres:14-alpine").apply {
            withDatabaseName("ecologique")
            withUsername("user")
            withPassword("password")
        }

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.flyway.locations") { "classpath:db/migration/products" }
            registry.add("spring.flyway.create-schemas") { "true" }
        }
    }

    @Autowired
    private lateinit var productRepository: ProductRepositoryImpl

    @BeforeEach
    fun cleanup() {
        productRepository.findAll().forEach { product ->
            productRepository.deleteById(product.id)
        }
    }

    @Test
    fun `save should persist and return product`() {
        // Given
        val product = buildProduct(name = "Integration Test Product")

        // When
        val saved = productRepository.save(product)

        // Then
        assertThat(saved.id).isEqualTo(product.id)
        assertThat(saved.name).isEqualTo("Integration Test Product")
    }

    @Test
    fun `findById should return product when exists`() {
        // Given
        val product = buildProduct()
        productRepository.save(product)

        // When
        val found = productRepository.findById(product.id)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(product.id)
    }

    @Test
    fun `findById should return null when not exists`() {
        // Given & When
        val found = productRepository.findById(ProductId.generate())

        // Then
        assertThat(found).isNull()
    }

    @Test
    fun `findByCategory should return products in category`() {
        // Given
        val clothingProduct = buildProduct(category = ProductCategory.CLOTHING, name = "Clothing Item")
        val householdProduct = buildProduct(category = ProductCategory.HOUSEHOLD, name = "Household Item")
        productRepository.save(clothingProduct)
        productRepository.save(householdProduct)

        // When
        val clothingProducts = productRepository.findByCategory(ProductCategory.CLOTHING)

        // Then
        assertThat(clothingProducts).hasSize(1)
        assertThat(clothingProducts[0].category).isEqualTo(ProductCategory.CLOTHING)
    }

    @Test
    fun `deleteById should remove product`() {
        // Given
        val product = buildProduct()
        productRepository.save(product)

        // When
        val deleted = productRepository.deleteById(product.id)

        // Then
        assertThat(deleted).isTrue()
        assertThat(productRepository.findById(product.id)).isNull()
    }

    private fun buildProduct(
        id: ProductId = ProductId.generate(),
        name: String = "Test Product",
        category: ProductCategory = ProductCategory.HOUSEHOLD
    ): Product = Product(
        id = id,
        name = name,
        description = "Test description",
        category = category,
        price = Money(BigDecimal("19.99"), Currency.EUR),
        weight = Weight(100),
        sustainabilityRating = SustainabilityRating.B,
        carbonFootprint = CarbonFootprint(BigDecimal("1.5"))
    )
}
