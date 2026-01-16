package com.wingedsheep.ecologique.payment.api.dto

import com.wingedsheep.ecologique.common.money.Money

/**
 * Request to create a payment.
 *
 * Similar to Stripe's PaymentIntent creation, this captures the intent to collect
 * a payment for a specific amount.
 *
 * @param orderId Reference to the order this payment is for.
 * @param amount The amount to charge.
 * @param paymentMethod The payment method to use.
 * @param description Optional description for the payment (appears on statements).
 * @param metadata Optional key-value pairs for additional context.
 */
data class PaymentRequest(
    val orderId: String,
    val amount: Money,
    val paymentMethod: PaymentMethod,
    val description: String? = null,
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(orderId.isNotBlank()) { "Order ID cannot be blank" }
        require(amount.amount > java.math.BigDecimal.ZERO) { "Payment amount must be positive" }
    }
}
