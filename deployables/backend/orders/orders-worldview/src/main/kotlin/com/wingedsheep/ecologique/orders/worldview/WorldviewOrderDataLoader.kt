package com.wingedsheep.ecologique.orders.worldview

import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineDto
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(3)
class WorldviewOrderDataLoader(
    private val orderService: OrderService,
    private val productService: ProductService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        if (activeProfile.contains("prod") || activeProfile.contains("test")) {
            logger.info("Skipping worldview order data for profile: $activeProfile")
            return
        }

        logger.info("Loading worldview order data...")
        loadOrders()
        logger.info("Worldview order data loaded successfully")
    }

    private fun loadOrders() {
        val productsByName = productService.findAllProducts()
            .getOrNull()
            ?.associateBy { it.name }
            ?: emptyMap()

        if (productsByName.isEmpty()) {
            logger.warn("No products found - skipping worldview order creation")
            return
        }

        val existingOrdersByUser = WorldviewOrder.allOrders
            .map { it.userId }
            .distinct()
            .associateWith { userId ->
                orderService.findOrdersForUser(userId).getOrNull()?.isNotEmpty() ?: false
            }

        WorldviewOrder.allOrders.forEach { dto ->
            if (existingOrdersByUser[dto.userId] == true) {
                logger.debug("User ${dto.userId} already has orders - skipping worldview order creation")
                return@forEach
            }
            val resolvedLines = resolveProductIds(dto.lines, productsByName)
            if (resolvedLines.isEmpty()) {
                logger.warn("Could not resolve products for order ${dto.id} - skipping")
                return@forEach
            }

            val request = OrderCreateRequest(
                lines = resolvedLines,
                subtotal = dto.subtotal,
                grandTotal = dto.grandTotal,
                currency = dto.currency
            )

            orderService.createOrder(dto.userId, request)
                .onSuccess { created ->
                    updateStatusIfNeeded(created.id, dto.status)
                    logger.debug("Created worldview order: ${created.id}")
                }
                .onFailure { error ->
                    logger.warn("Failed to create worldview order ${dto.id}: $error")
                }
        }
    }

    private fun resolveProductIds(
        lines: List<OrderLineDto>,
        productsByName: Map<String, ProductDto>
    ): List<OrderLineCreateRequest> {
        return lines.mapNotNull { line ->
            val product = productsByName[line.productName]
            if (product == null) {
                logger.warn("Product not found by name: ${line.productName}")
                null
            } else {
                OrderLineCreateRequest(
                    productId = product.id,
                    productName = line.productName,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity
                )
            }
        }
    }

    private fun updateStatusIfNeeded(orderId: String, targetStatus: String) {
        val statusPath = getStatusPath(targetStatus)
        statusPath.forEach { status ->
            orderService.updateStatus(orderId, status)
        }
    }

    private fun getStatusPath(targetStatus: String): List<String> = when (targetStatus) {
        "CREATED" -> emptyList()
        "RESERVED" -> listOf("RESERVED")
        "PAYMENT_PENDING" -> listOf("RESERVED", "PAYMENT_PENDING")
        "PAID" -> listOf("RESERVED", "PAYMENT_PENDING", "PAID")
        "SHIPPED" -> listOf("RESERVED", "PAYMENT_PENDING", "PAID", "SHIPPED")
        "DELIVERED" -> listOf("RESERVED", "PAYMENT_PENDING", "PAID", "SHIPPED", "DELIVERED")
        else -> emptyList()
    }
}
