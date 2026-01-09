package com.wingedsheep.ecologique.orders.api.error

import java.util.UUID

sealed class OrderError {
    data class NotFound(val id: UUID) : OrderError()
    data class AccessDenied(val orderId: UUID, val userId: String) : OrderError()
    data class InvalidStatus(val currentStatus: String, val requestedStatus: String) : OrderError()
    data class ProductNotFound(val productId: String) : OrderError()
    data class ValidationFailed(val reason: String) : OrderError()
    data class Unexpected(val message: String) : OrderError()
}
