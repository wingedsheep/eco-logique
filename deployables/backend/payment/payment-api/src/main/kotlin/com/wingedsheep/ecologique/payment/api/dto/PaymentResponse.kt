package com.wingedsheep.ecologique.payment.api.dto

import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.payment.api.PaymentId
import com.wingedsheep.ecologique.payment.api.PaymentStatus
import java.time.Instant

/**
 * Response containing payment details.
 *
 * @param id Unique payment identifier.
 * @param orderId Reference to the associated order.
 * @param amount The payment amount.
 * @param status Current status of the payment.
 * @param paymentMethodSummary A summary of the payment method used (e.g., "VISA ending in 4242").
 * @param failureReason If status is FAILED, the reason for failure.
 * @param createdAt When the payment was created.
 * @param updatedAt When the payment was last updated.
 */
data class PaymentResponse(
    val id: PaymentId,
    val orderId: String,
    val amount: Money,
    val status: PaymentStatus,
    val paymentMethodSummary: String,
    val failureReason: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
