package com.wingedsheep.ecologique.products.impl.application

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import com.wingedsheep.ecologique.products.api.error.ProductError
import com.wingedsheep.ecologique.products.api.event.ProductCreated
import com.wingedsheep.ecologique.products.impl.domain.Product
import com.wingedsheep.ecologique.products.impl.domain.ProductCategory
import com.wingedsheep.ecologique.products.impl.domain.ProductId
import com.wingedsheep.ecologique.products.impl.domain.ProductRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
internal class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher
) : ProductService {

    @Transactional
    override fun createProduct(request: ProductCreateRequest): Result<ProductDto, ProductError> {
        val category = ProductCategory.fromString(request.category)
            ?: return Result.err(ProductError.InvalidCategory(request.category))

        val currency = try {
            Currency.valueOf(request.priceCurrency)
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed("Invalid currency: ${request.priceCurrency}"))
        }

        productRepository.findByName(request.name)?.let {
            return Result.err(ProductError.DuplicateName(request.name))
        }

        val product = try {
            Product.create(
                name = request.name,
                description = request.description,
                category = category,
                priceAmount = request.priceAmount,
                priceCurrency = currency,
                weightGrams = request.weightGrams,
                carbonFootprintKg = request.carbonFootprintKg
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed(e.message ?: "Validation failed"))
        }

        val savedProduct = productRepository.save(product)

        eventPublisher.publishEvent(
            ProductCreated(
                productId = savedProduct.id.value,
                name = savedProduct.name,
                category = savedProduct.category.name,
                priceAmount = savedProduct.price.amount,
                priceCurrency = savedProduct.price.currency.name,
                timestamp = Instant.now()
            )
        )

        return Result.ok(savedProduct.toDto())
    }

    @Transactional(readOnly = true)
    override fun getProduct(id: String): Result<ProductDto, ProductError> {
        val productId = try {
            ProductId(id)
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed("Invalid product ID"))
        }

        val product = productRepository.findById(productId)
            ?: return Result.err(ProductError.NotFound(id))

        return Result.ok(product.toDto())
    }

    @Transactional(readOnly = true)
    override fun findAllProducts(): Result<List<ProductDto>, ProductError> {
        val products = productRepository.findAll()
        return Result.ok(products.map { it.toDto() })
    }

    @Transactional(readOnly = true)
    override fun findProductsByCategory(category: String): Result<List<ProductDto>, ProductError> {
        val productCategory = ProductCategory.fromString(category)
            ?: return Result.err(ProductError.InvalidCategory(category))

        val products = productRepository.findByCategory(productCategory)
        return Result.ok(products.map { it.toDto() })
    }

    @Transactional
    override fun updateProductPrice(id: String, request: ProductUpdatePriceRequest): Result<ProductDto, ProductError> {
        val productId = try {
            ProductId(id)
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed("Invalid product ID"))
        }

        val currency = try {
            Currency.valueOf(request.priceCurrency)
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed("Invalid currency: ${request.priceCurrency}"))
        }

        val product = productRepository.findById(productId)
            ?: return Result.err(ProductError.NotFound(id))

        val updatedProduct = try {
            product.updatePrice(Money(request.priceAmount, currency))
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed(e.message ?: "Validation failed"))
        }

        val savedProduct = productRepository.save(updatedProduct)
        return Result.ok(savedProduct.toDto())
    }

    @Transactional
    override fun deleteProduct(id: String): Result<Unit, ProductError> {
        val productId = try {
            ProductId(id)
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed("Invalid product ID"))
        }

        if (!productRepository.existsById(productId)) {
            return Result.err(ProductError.NotFound(id))
        }

        productRepository.deleteById(productId)
        return Result.ok(Unit)
    }
}
