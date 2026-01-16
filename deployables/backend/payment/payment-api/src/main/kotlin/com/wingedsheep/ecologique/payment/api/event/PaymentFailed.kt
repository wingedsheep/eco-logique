package com.wingedsheep.ecologique.payment.api.event

import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.payment.api.PaymentId
import java.time.Instant

/**
 * Domain event published when a payment has failed.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Notifying the customer of the failure
 * - Releasing reserved inventory
 * - Logging for analytics
 */
data class PaymentFailed(
    val paymentId: PaymentId,
    val orderId: String,
    val amount: Money,
    val failureReason: String,
    val timestamp: Instant
)
