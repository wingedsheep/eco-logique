package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import java.time.Instant

internal data class Order(
    val id: OrderId,
    val userId: String,
    val status: OrderStatus,
    val lines: List<OrderLine>,
    val totals: TotalsSnapshot,
    val createdAt: Instant
) {
    init {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(lines.isNotEmpty()) { "Order must have at least one line" }
    }

    fun transitionTo(newStatus: OrderStatus): Order {
        require(status.canTransitionTo(newStatus)) {
            "Cannot transition from $status to $newStatus"
        }
        return copy(status = newStatus)
    }

    fun isOwnedBy(userId: String): Boolean = this.userId == userId

    companion object {
        fun create(
            userId: String,
            lines: List<OrderLine>,
            currency: Currency
        ): Order = Order(
            id = OrderId.generate(),
            userId = userId,
            status = OrderStatus.CREATED,
            lines = lines,
            totals = TotalsSnapshot.fromOrderLines(lines, currency),
            createdAt = Instant.now()
        )
    }
}