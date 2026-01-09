package com.wingedsheep.ecologique.products.impl.application

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.products.api.buildProductCreateRequest
import com.wingedsheep.ecologique.products.api.buildProductUpdatePriceRequest
import com.wingedsheep.ecologique.products.api.error.ProductError
import com.wingedsheep.ecologique.products.api.event.ProductCreated
import com.wingedsheep.ecologique.products.impl.domain.CarbonFootprint
import com.wingedsheep.ecologique.products.impl.domain.Product
import com.wingedsheep.ecologique.products.impl.domain.ProductCategory
import com.wingedsheep.ecologique.products.impl.domain.ProductId
import com.wingedsheep.ecologique.products.impl.domain.SustainabilityRating
import com.wingedsheep.ecologique.products.impl.domain.Weight
import com.wingedsheep.ecologique.products.impl.domain.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ProductServiceImplTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var productService: ProductServiceImpl

    @Test
    fun `createProduct should return ProductDto when valid request`() {
        // Given
        val request = buildProductCreateRequest(
            name = "Test Product",
            category = "HOUSEHOLD"
        )
        whenever(productRepository.findByName("Test Product")).thenReturn(null)
        whenever(productRepository.save(any())).thenAnswer { it.arguments[0] as Product }

        // When
        val result = productService.createProduct(request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.name).isEqualTo("Test Product")
                assertThat(dto.category).isEqualTo("HOUSEHOLD")
            },
            onFailure = { }
        )
        val eventCaptor = argumentCaptor<Any>()
        verify(eventPublisher).publishEvent(eventCaptor.capture())
        assertThat(eventCaptor.firstValue).isInstanceOf(ProductCreated::class.java)
    }

    @Test
    fun `createProduct should return DuplicateName error when name exists`() {
        // Given
        val request = buildProductCreateRequest(name = "Existing Product")
        whenever(productRepository.findByName("Existing Product")).thenReturn(buildProduct())

        // When
        val result = productService.createProduct(request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(ProductError.DuplicateName::class.java)
                assertThat((error as ProductError.DuplicateName).name).isEqualTo("Existing Product")
            }
        )
    }

    @Test
    fun `createProduct should return InvalidCategory error for unknown category`() {
        // Given
        val request = buildProductCreateRequest(category = "INVALID_CATEGORY")

        // When
        val result = productService.createProduct(request)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(ProductError.InvalidCategory::class.java)
            }
        )
    }

    @Test
    fun `getProduct should return ProductDto when product exists`() {
        // Given
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val productId = ProductId(uuid)
        val product = buildProduct(id = productId)
        whenever(productRepository.findById(productId)).thenReturn(product)

        // When
        val result = productService.getProduct(uuid)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.id).isEqualTo(uuid)
            },
            onFailure = { }
        )
    }

    @Test
    fun `getProduct should return NotFound error when product does not exist`() {
        // Given
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000999")
        whenever(productRepository.findById(ProductId(uuid))).thenReturn(null)

        // When
        val result = productService.getProduct(uuid)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(ProductError.NotFound::class.java)
                assertThat((error as ProductError.NotFound).id).isEqualTo(uuid)
            }
        )
    }

    @Test
    fun `findAllProducts should return list of ProductDto`() {
        // Given
        val products = listOf(
            buildProduct(),
            buildProduct(id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000002")))
        )
        whenever(productRepository.findAll()).thenReturn(products)

        // When
        val result = productService.findAllProducts()

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dtos ->
                assertThat(dtos).hasSize(2)
            },
            onFailure = { }
        )
    }

    @Test
    fun `updateProductPrice should return updated ProductDto`() {
        // Given
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val productId = ProductId(uuid)
        val product = buildProduct(id = productId)
        val request = buildProductUpdatePriceRequest(priceAmount = BigDecimal("35.00"))
        whenever(productRepository.findById(productId)).thenReturn(product)
        whenever(productRepository.save(any())).thenAnswer { it.arguments[0] as Product }

        // When
        val result = productService.updateProductPrice(uuid, request)

        // Then
        assertThat(result.isOk).isTrue()
        result.fold(
            onSuccess = { dto ->
                assertThat(dto.priceAmount).isEqualByComparingTo(BigDecimal("35.00"))
            },
            onFailure = { }
        )
    }

    @Test
    fun `deleteProduct should return Unit when product exists`() {
        // Given
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val productId = ProductId(uuid)
        whenever(productRepository.existsById(productId)).thenReturn(true)
        whenever(productRepository.deleteById(productId)).thenReturn(true)

        // When
        val result = productService.deleteProduct(uuid)

        // Then
        assertThat(result.isOk).isTrue()
    }

    @Test
    fun `deleteProduct should return NotFound error when product does not exist`() {
        // Given
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000999")
        whenever(productRepository.existsById(ProductId(uuid))).thenReturn(false)

        // When
        val result = productService.deleteProduct(uuid)

        // Then
        assertThat(result.isErr).isTrue()
        result.fold(
            onSuccess = { },
            onFailure = { error ->
                assertThat(error).isInstanceOf(ProductError.NotFound::class.java)
            }
        )
    }

    private fun buildProduct(
        id: ProductId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        name: String = "Test Product"
    ): Product = Product(
        id = id,
        name = name,
        description = "Test description",
        category = ProductCategory.HOUSEHOLD,
        price = Money(BigDecimal("19.99"), Currency.EUR),
        weight = Weight(100),
        sustainabilityRating = SustainabilityRating.B,
        carbonFootprint = CarbonFootprint(BigDecimal("1.5"))
    )
}
