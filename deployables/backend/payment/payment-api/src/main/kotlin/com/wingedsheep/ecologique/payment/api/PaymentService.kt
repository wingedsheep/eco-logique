package com.wingedsheep.ecologique.payment.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.payment.api.dto.PaymentRequest
import com.wingedsheep.ecologique.payment.api.dto.PaymentResponse
import com.wingedsheep.ecologique.payment.api.error.PaymentError

/**
 * Service for processing payments.
 *
 * This interface defines the contract for payment processing. Implementations may
 * integrate with various payment service providers (Stripe, PayPal, Adyen, etc.) or
 * use a mock implementation for testing and development.
 *
 * The design follows Stripe's PaymentIntent pattern:
 * 1. Create a payment (captures intent to collect payment)
 * 2. Confirm/process the payment
 * 3. Handle success or failure
 */
interface PaymentService {

    /**
     * Creates and processes a payment.
     *
     * This is a synchronous operation that creates a payment intent and attempts
     * to process it immediately. For real integrations, you may want to split this
     * into create + confirm steps to handle 3D Secure and other async flows.
     *
     * @param request The payment request containing amount, payment method, etc.
     * @return [Result.Ok] with [PaymentResponse] on success,
     *         or [Result.Err] with [PaymentError] on failure.
     */
    fun processPayment(request: PaymentRequest): Result<PaymentResponse, PaymentError>

    /**
     * Retrieves a payment by its ID.
     *
     * @param paymentId The unique payment identifier.
     * @return [Result.Ok] with [PaymentResponse] if found,
     *         or [Result.Err] with [PaymentError.NotFound] if not found.
     */
    fun getPayment(paymentId: PaymentId): Result<PaymentResponse, PaymentError>

    /**
     * Cancels a pending payment.
     *
     * Only payments in PENDING or PROCESSING status can be cancelled.
     *
     * @param paymentId The payment to cancel.
     * @return [Result.Ok] with updated [PaymentResponse] on success,
     *         or [Result.Err] with [PaymentError] on failure.
     */
    fun cancelPayment(paymentId: PaymentId): Result<PaymentResponse, PaymentError>

    /**
     * Initiates a refund for a completed payment.
     *
     * Only payments in SUCCEEDED status can be refunded.
     *
     * @param paymentId The payment to refund.
     * @return [Result.Ok] with updated [PaymentResponse] on success,
     *         or [Result.Err] with [PaymentError] on failure.
     */
    fun refundPayment(paymentId: PaymentId): Result<PaymentResponse, PaymentError>
}
