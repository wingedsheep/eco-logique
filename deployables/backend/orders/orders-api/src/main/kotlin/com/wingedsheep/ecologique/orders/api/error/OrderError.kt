package com.wingedsheep.ecologique.orders.api.error

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.products.api.ProductId

sealed class OrderError {
    data class NotFound(val id: OrderId) : OrderError()
    data class AccessDenied(val orderId: OrderId, val userId: String) : OrderError()
    data class InvalidStatus(val currentStatus: OrderStatus, val requestedStatus: OrderStatus) : OrderError()
    data class ProductNotFound(val productId: ProductId) : OrderError()
    data class ValidationFailed(val reason: String) : OrderError()
    data class Unexpected(val message: String) : OrderError()
}
