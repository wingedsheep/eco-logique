package com.wingedsheep.ecologique.orders.api.error

sealed class OrderError {
    data class NotFound(val id: String) : OrderError()
    data class AccessDenied(val orderId: String, val userId: String) : OrderError()
    data class InvalidStatus(val currentStatus: String, val requestedStatus: String) : OrderError()
    data class ProductNotFound(val productId: String) : OrderError()
    data class ValidationFailed(val reason: String) : OrderError()
    data class Unexpected(val message: String) : OrderError()
}
