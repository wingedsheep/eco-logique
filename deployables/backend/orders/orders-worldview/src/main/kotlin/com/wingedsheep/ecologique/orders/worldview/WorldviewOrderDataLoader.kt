package com.wingedsheep.ecologique.orders.worldview

import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WorldviewOrderDataLoader(
    private val orderService: OrderService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun loadWorldviewData() {
        if (activeProfile.contains("prod") || activeProfile.contains("test")) {
            logger.info("Skipping worldview order data for profile: $activeProfile")
            return
        }

        logger.info("Loading worldview order data...")
        loadOrders()
        logger.info("Worldview order data loaded successfully")
    }

    private fun loadOrders() {
        WorldviewOrder.allOrders.forEach { dto ->
            val request = dto.toCreateRequest()

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

    private fun OrderDto.toCreateRequest(): OrderCreateRequest = OrderCreateRequest(
        lines = lines.map { line ->
            OrderLineCreateRequest(
                productId = line.productId,
                productName = line.productName,
                unitPrice = line.unitPrice,
                quantity = line.quantity
            )
        },
        subtotal = subtotal,
        grandTotal = grandTotal,
        currency = currency
    )
}