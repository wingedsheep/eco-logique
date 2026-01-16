package com.wingedsheep.ecologique.payment.api.event

import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.payment.api.PaymentId
import java.time.Instant

/**
 * Domain event published when a payment has been successfully completed.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to PAID
 * - Initiating shipment creation
 * - Sending confirmation emails
 */
data class PaymentCompleted(
    val paymentId: PaymentId,
    val orderId: String,
    val amount: Money,
    val paymentMethodSummary: String,
    val timestamp: Instant
)
