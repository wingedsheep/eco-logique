package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.payment.api.PaymentId
import java.math.BigDecimal
import java.time.Instant

internal data class Order(
    val id: OrderId,
    val userId: String,
    val status: OrderStatus,
    val lines: List<OrderLine>,
    val totals: TotalsSnapshot,
    val createdAt: Instant,
    val paymentId: PaymentId? = null
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

    fun withPaymentId(paymentId: PaymentId): Order = copy(paymentId = paymentId)

    fun isOwnedBy(userId: String): Boolean = this.userId == userId

    companion object {
        fun create(
            userId: String,
            lines: List<OrderLine>,
            currency: Currency,
            vatAmount: BigDecimal = BigDecimal.ZERO,
            vatRate: BigDecimal = BigDecimal.ZERO
        ): Order = Order(
            id = OrderId.generate(),
            userId = userId,
            status = OrderStatus.CREATED,
            lines = lines,
            totals = TotalsSnapshot.fromOrderLines(lines, currency, vatAmount, vatRate),
            createdAt = Instant.now()
        )
    }
}