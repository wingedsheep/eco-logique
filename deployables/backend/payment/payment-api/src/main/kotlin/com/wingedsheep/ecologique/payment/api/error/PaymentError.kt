package com.wingedsheep.ecologique.payment.api.error

import com.wingedsheep.ecologique.payment.api.PaymentId

/**
 * Errors that can occur during payment processing.
 *
 * Based on common Stripe error scenarios.
 */
sealed class PaymentError {

    /**
     * Payment was not found.
     */
    data class NotFound(val paymentId: PaymentId) : PaymentError()

    /**
     * Card was declined by the issuing bank.
     *
     * @param reason The decline reason (e.g., "insufficient_funds", "card_declined").
     */
    data class CardDeclined(val reason: String) : PaymentError()

    /**
     * The payment method provided is invalid or expired.
     */
    data class InvalidPaymentMethod(val reason: String) : PaymentError()

    /**
     * Insufficient funds in the account.
     */
    data object InsufficientFunds : PaymentError()

    /**
     * Payment has expired (e.g., customer took too long to complete).
     */
    data class PaymentExpired(val paymentId: PaymentId) : PaymentError()

    /**
     * Payment provider is currently unavailable.
     */
    data object ProviderUnavailable : PaymentError()

    /**
     * Payment cannot be processed due to fraud detection.
     */
    data class FraudDetected(val paymentId: PaymentId) : PaymentError()

    /**
     * General processing error from the payment provider.
     */
    data class ProcessingError(val reason: String) : PaymentError()

    /**
     * Payment is in an invalid state for the requested operation.
     */
    data class InvalidState(
        val paymentId: PaymentId,
        val currentState: String,
        val requestedOperation: String,
    ) : PaymentError()
}
