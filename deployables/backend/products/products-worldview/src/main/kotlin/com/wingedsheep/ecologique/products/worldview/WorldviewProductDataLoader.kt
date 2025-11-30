package com.wingedsheep.ecologique.products.worldview

import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WorldviewProductDataLoader(
    private val productService: ProductService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun loadWorldviewData() {
        if (activeProfile.contains("prod")) {
            logger.info("Production profile active, skipping worldview product data")
            return
        }

        logger.info("Loading worldview product data...")
        loadProducts()
        logger.info("Worldview product data loaded successfully")
    }

    private fun loadProducts() {
        WorldviewProduct.allProducts.forEach { product ->
            val request = ProductCreateRequest(
                name = product.name,
                description = product.description,
                category = product.category,
                priceAmount = product.priceAmount,
                priceCurrency = product.priceCurrency,
                weightGrams = product.weightGrams,
                carbonFootprintKg = product.carbonFootprintKg
            )

            productService.createProduct(request)
                .onSuccess { created ->
                    logger.debug("Created worldview product: ${created.name}")
                }
                .onFailure { error ->
                    logger.warn("Failed to create worldview product ${product.name}: $error")
                }
        }
    }
}
