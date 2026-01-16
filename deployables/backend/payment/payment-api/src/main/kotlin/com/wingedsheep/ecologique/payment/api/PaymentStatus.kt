package com.wingedsheep.ecologique.payment.api

/**
 * Status of a payment.
 *
 * Follows a simplified version of Stripe's PaymentIntent status lifecycle.
 */
enum class PaymentStatus {
    /**
     * Payment has been created and is awaiting processing.
     */
    PENDING,

    /**
     * Payment is currently being processed by the payment provider.
     */
    PROCESSING,

    /**
     * Payment has been successfully completed.
     */
    SUCCEEDED,

    /**
     * Payment has failed (e.g., card declined, insufficient funds).
     */
    FAILED,

    /**
     * Payment was cancelled before completion.
     */
    CANCELLED
}
