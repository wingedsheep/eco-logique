package com.wingedsheep.ecologique.payment.api.event

import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.payment.api.PaymentId
import java.time.Instant

/**
 * Domain event published when a payment process has been initiated.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to PAYMENT_PENDING
 * - Starting payment timeout monitoring
 */
data class PaymentInitiated(
    val paymentId: PaymentId,
    val orderId: String,
    val amount: Money,
    val timestamp: Instant
)
