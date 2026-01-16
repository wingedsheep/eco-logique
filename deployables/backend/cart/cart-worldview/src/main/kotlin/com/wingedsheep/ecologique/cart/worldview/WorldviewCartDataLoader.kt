package com.wingedsheep.ecologique.cart.worldview

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(4)
class WorldviewCartDataLoader(
    private val cartService: CartService,
    private val productService: ProductService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (activeProfile.contains("prod") || activeProfile.contains("test")) {
            logger.info("Skipping worldview cart data for profile: $activeProfile")
            return
        }

        logger.info("Loading worldview cart data...")
        loadCarts()
        logger.info("Worldview cart data loaded successfully")
    }

    private fun loadCarts() {
        val productsByName = productService.findAllProducts()
            .getOrNull()
            ?.associateBy { it.name }
            ?: emptyMap()

        if (productsByName.isEmpty()) {
            logger.warn("No products found - skipping worldview cart creation")
            return
        }

        WorldviewCart.allCarts.forEach carts@{ cartDto ->
            val existingCart = cartService.getCart(cartDto.userId)
            if (existingCart.isOk && existingCart.getOrNull()?.items?.isNotEmpty() == true) {
                logger.debug("User ${cartDto.userId} already has cart items - skipping")
                return@carts
            }

            cartDto.items.forEach items@{ item ->
                val product = productsByName[item.productName]
                if (product == null) {
                    logger.warn("Product not found by name: ${item.productName} - skipping item")
                    return@items
                }

                cartService.addItem(
                    cartDto.userId,
                    AddCartItemRequest(product.id, item.quantity)
                ).onSuccess {
                    logger.debug("Added ${item.quantity}x ${item.productName} to cart for ${cartDto.userId}")
                }.onFailure { error ->
                    logger.warn("Failed to add item to cart: $error")
                }
            }
        }
    }
}
