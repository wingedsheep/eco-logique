package com.wingedsheep.ecologique.orders.impl.application

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.outbox.OutboxEvent
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.event.OrderCreated
import java.math.BigDecimal
import java.time.Instant

/**
 * Outbox-enabled wrapper for [OrderCreated] event.
 * This wrapper implements [OutboxEvent] to ensure reliable delivery through the transactional outbox.
 */
internal data class OrderCreatedOutboxEvent(
    val orderId: OrderId,
    val userId: String,
    val grandTotal: BigDecimal,
    val currency: Currency,
    val timestamp: Instant
) : OutboxEvent {
    override val aggregateType: String = "Order"
    override val aggregateId: String = orderId.value.toString()

    companion object {
        fun from(event: OrderCreated): OrderCreatedOutboxEvent = OrderCreatedOutboxEvent(
            orderId = event.orderId,
            userId = event.userId,
            grandTotal = event.grandTotal,
            currency = event.currency,
            timestamp = event.timestamp
        )
    }
}
