package com.wingedsheep.ecologique.products.impl.application

import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import com.wingedsheep.ecologique.products.api.error.ProductError
import com.wingedsheep.ecologique.products.api.event.ProductCreated
import com.wingedsheep.ecologique.products.impl.domain.CarbonFootprint
import com.wingedsheep.ecologique.products.impl.domain.Product
import com.wingedsheep.ecologique.products.impl.domain.ProductRepository
import com.wingedsheep.ecologique.products.impl.domain.SustainabilityRatingCalculator
import com.wingedsheep.ecologique.products.impl.domain.Weight
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
        productRepository.findByName(request.name)?.let {
            return Result.err(ProductError.DuplicateName(request.name))
        }

        val carbonFootprint = CarbonFootprint(request.carbonFootprintKg)
        val product = try {
            Product(
                id = ProductId.generate(),
                name = request.name,
                description = request.description,
                category = request.category,
                price = Money(request.priceAmount, request.priceCurrency),
                weight = Weight(request.weightGrams),
                sustainabilityRating = SustainabilityRatingCalculator.calculate(request.category, carbonFootprint),
                carbonFootprint = carbonFootprint
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed(e.message ?: "Validation failed"))
        }

        val savedProduct = productRepository.save(product)

        eventPublisher.publishEvent(
            ProductCreated(
                productId = savedProduct.id,
                name = savedProduct.name,
                category = savedProduct.category,
                priceAmount = savedProduct.price.amount,
                priceCurrency = savedProduct.price.currency,
                timestamp = Instant.now()
            )
        )

        return Result.ok(savedProduct.toDto())
    }

    @Transactional(readOnly = true)
    override fun getProduct(id: ProductId): Result<ProductDto, ProductError> {
        val product = productRepository.findById(id)
            ?: return Result.err(ProductError.NotFound(id))
        return Result.ok(product.toDto())
    }

    @Transactional(readOnly = true)
    override fun findAllProducts(): Result<List<ProductDto>, ProductError> {
        return Result.ok(productRepository.findAll().map { it.toDto() })
    }

    @Transactional(readOnly = true)
    override fun findProductsByCategory(category: ProductCategory): Result<List<ProductDto>, ProductError> {
        return Result.ok(productRepository.findByCategory(category).map { it.toDto() })
    }

    @Transactional
    override fun updateProductPrice(id: ProductId, request: ProductUpdatePriceRequest): Result<ProductDto, ProductError> {
        val product = productRepository.findById(id)
            ?: return Result.err(ProductError.NotFound(id))

        val updatedProduct = try {
            product.updatePrice(Money(request.priceAmount, request.priceCurrency))
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed(e.message ?: "Validation failed"))
        }

        return Result.ok(productRepository.save(updatedProduct).toDto())
    }

    @Transactional
    override fun deleteProduct(id: ProductId): Result<Unit, ProductError> {
        if (!productRepository.existsById(id)) {
            return Result.err(ProductError.NotFound(id))
        }
        productRepository.deleteById(id)
        return Result.ok(Unit)
    }
}
