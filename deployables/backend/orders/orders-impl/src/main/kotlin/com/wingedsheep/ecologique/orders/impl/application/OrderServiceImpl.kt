package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.error.OrderError
import com.wingedsheep.ecologique.orders.api.event.OrderCreated
import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderId
import com.wingedsheep.ecologique.orders.impl.domain.OrderLine
import com.wingedsheep.ecologique.orders.impl.domain.OrderRepository
import com.wingedsheep.ecologique.orders.impl.domain.OrderStatus
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
internal class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val productService: ProductService,
    private val eventPublisher: ApplicationEventPublisher
) : OrderService {

    @Transactional
    override fun createOrder(userId: String, request: OrderCreateRequest): Result<OrderDto, OrderError> {
        val currency = try {
            Currency.valueOf(request.currency)
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.ValidationFailed("Invalid currency: ${request.currency}"))
        }

        val productValidationError = validateProductsExist(request)
        if (productValidationError != null) {
            return Result.err(productValidationError)
        }

        val lines = try {
            request.lines.map { line ->
                OrderLine.create(
                    productId = line.productId,
                    productName = line.productName,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity
                )
            }
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.ValidationFailed(e.message ?: "Invalid order line"))
        }

        val order = try {
            Order.create(
                userId = userId,
                lines = lines,
                currency = currency
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.ValidationFailed(e.message ?: "Validation failed"))
        }

        val savedOrder = orderRepository.save(order)

        eventPublisher.publishEvent(
            OrderCreated(
                orderId = savedOrder.id.value.toString(),
                userId = savedOrder.userId,
                grandTotal = savedOrder.totals.grandTotal,
                currency = savedOrder.totals.currency.name,
                timestamp = Instant.now()
            )
        )

        return Result.ok(savedOrder.toDto())
    }

    @Transactional(readOnly = true)
    override fun getOrder(orderId: UUID, userId: String): Result<OrderDto, OrderError> {
        val id = OrderId(orderId)

        val order = orderRepository.findById(id)
            ?: return Result.err(OrderError.NotFound(orderId))

        if (!order.isOwnedBy(userId)) {
            return Result.err(OrderError.AccessDenied(orderId, userId))
        }

        return Result.ok(order.toDto())
    }

    @Transactional(readOnly = true)
    override fun findOrdersForUser(userId: String): Result<List<OrderDto>, OrderError> {
        val orders = orderRepository.findByUserId(userId)
        return Result.ok(orders.map { it.toDto() })
    }

    @Transactional
    override fun updateStatus(orderId: UUID, newStatus: String): Result<OrderDto, OrderError> {
        val id = OrderId(orderId)

        val targetStatus = OrderStatus.fromString(newStatus)
            ?: return Result.err(OrderError.ValidationFailed("Invalid status: $newStatus"))

        val order = orderRepository.findById(id)
            ?: return Result.err(OrderError.NotFound(orderId))

        val updatedOrder = try {
            order.transitionTo(targetStatus)
        } catch (e: IllegalArgumentException) {
            return Result.err(OrderError.InvalidStatus(order.status.name, newStatus))
        }

        val savedOrder = orderRepository.save(updatedOrder)
        return Result.ok(savedOrder.toDto())
    }

    private fun validateProductsExist(request: OrderCreateRequest): OrderError? {
        for (line in request.lines) {
            val productId = try {
                ProductId(UUID.fromString(line.productId))
            } catch (e: IllegalArgumentException) {
                return OrderError.ProductNotFound(line.productId)
            }
            val productResult = productService.getProduct(productId)
            if (productResult.isErr) {
                return OrderError.ProductNotFound(line.productId)
            }
        }
        return null
    }
}
