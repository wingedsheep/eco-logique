package com.wingedsheep.ecologique.products.worldview

import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(1)
class WorldviewProductDataLoader(
    private val productService: ProductService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (activeProfile.contains("prod") || activeProfile.contains("test")) {
            logger.info("Skipping worldview product data for profile: $activeProfile")
            return
        }

        logger.info("Loading worldview product data...")
        loadProducts()
        logger.info("Worldview product data loaded successfully")
    }

    private fun loadProducts() {
        val existingProducts = productService.findAllProducts()
            .getOrNull()
            ?.associateBy { it.name }
            ?: emptyMap()

        WorldviewProduct.allProducts.forEach { product ->
            if (existingProducts.containsKey(product.name)) {
                logger.debug("Worldview product already exists: ${product.name}")
                return@forEach
            }

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
                    logger.debug("Created worldview product: ${created.name} (${created.id})")
                }
                .onFailure { error ->
                    logger.warn("Failed to create worldview product ${product.name}: $error")
                }
        }
    }
}
