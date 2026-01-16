package com.wingedsheep.ecologique.payment.impl

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.payment.api.PaymentId
import com.wingedsheep.ecologique.payment.api.PaymentService
import com.wingedsheep.ecologique.payment.api.PaymentStatus
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.payment.api.dto.PaymentMethod
import com.wingedsheep.ecologique.payment.api.dto.PaymentRequest
import com.wingedsheep.ecologique.payment.api.dto.PaymentResponse
import com.wingedsheep.ecologique.payment.api.error.PaymentError
import com.wingedsheep.ecologique.payment.api.event.PaymentCompleted
import com.wingedsheep.ecologique.payment.api.event.PaymentFailed
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Mock implementation of [PaymentService] for development and testing.
 *
 * This implementation simulates payment processing without actually charging any
 * payment methods. It stores payments in memory and provides test utilities for
 * verification.
 *
 * ## Test Scenarios
 *
 * The mock supports special card tokens to simulate different scenarios:
 * - `tok_visa` / `tok_mastercard` - Successful payment
 * - `tok_declined` - Card declined
 * - `tok_insufficient_funds` - Insufficient funds
 * - `tok_fraud` - Fraud detected
 * - `tok_processing_error` - General processing error
 */
@Service
class MockPaymentService(
    private val eventPublisher: ApplicationEventPublisher
) : PaymentService {

    private val logger = Logger.getLogger(MockPaymentService::class.java.name)
    private val payments = ConcurrentHashMap<PaymentId, StoredPayment>()

    override fun processPayment(request: PaymentRequest): Result<PaymentResponse, PaymentError> {
        logger.info(
            """
            |
            |========================================
            | MOCK PAYMENT PROCESSING
            |========================================
            | Order: ${request.orderId}
            | Amount: ${request.amount.amount} ${request.amount.currency}
            | Method: ${request.paymentMethod.toSummary()}
            |========================================
            """.trimMargin()
        )

        val paymentId = PaymentId.generate()
        val now = Instant.now()
        val methodSummary = request.paymentMethod.toSummary()

        // Check for special test tokens that simulate failures
        val errorResult = checkForTestScenario(request.paymentMethod)
        if (errorResult != null) {
            logger.info("MOCK PAYMENT: Simulating failure - ${errorResult::class.simpleName}")

            // Store the failed payment
            val failedPayment = StoredPayment(
                id = paymentId,
                orderId = request.orderId,
                amount = request.amount,
                status = PaymentStatus.FAILED,
                paymentMethodSummary = methodSummary,
                failureReason = errorResult.toFailureReason(),
                createdAt = now,
                updatedAt = now,
            )
            payments[paymentId] = failedPayment

            // Publish failure event
            eventPublisher.publishEvent(
                PaymentFailed(
                    paymentId = paymentId,
                    orderId = request.orderId,
                    amount = request.amount,
                    failureReason = errorResult.toFailureReason(),
                    timestamp = now
                )
            )

            return Result.err(errorResult)
        }

        val storedPayment = StoredPayment(
            id = paymentId,
            orderId = request.orderId,
            amount = request.amount,
            status = PaymentStatus.SUCCEEDED,
            paymentMethodSummary = methodSummary,
            failureReason = null,
            createdAt = now,
            updatedAt = now,
        )

        payments[paymentId] = storedPayment

        logger.info("MOCK PAYMENT: Payment ${paymentId.value} succeeded")

        // Publish success event
        eventPublisher.publishEvent(
            PaymentCompleted(
                paymentId = paymentId,
                orderId = request.orderId,
                amount = request.amount,
                paymentMethodSummary = methodSummary,
                timestamp = now
            )
        )

        return Result.ok(storedPayment.toResponse())
    }

    private fun PaymentError.toFailureReason(): String = when (this) {
        is PaymentError.CardDeclined -> "Card declined: $reason"
        is PaymentError.InsufficientFunds -> "Insufficient funds"
        is PaymentError.FraudDetected -> "Payment flagged for fraud"
        is PaymentError.InvalidPaymentMethod -> "Invalid payment method: $reason"
        is PaymentError.ProcessingError -> "Processing error: $reason"
        is PaymentError.PaymentExpired -> "Payment expired"
        is PaymentError.ProviderUnavailable -> "Payment provider unavailable"
        is PaymentError.NotFound -> "Payment not found"
        is PaymentError.InvalidState -> "Invalid payment state"
    }

    override fun getPayment(paymentId: PaymentId): Result<PaymentResponse, PaymentError> {
        val payment = payments[paymentId]
            ?: return Result.err(PaymentError.NotFound(paymentId))

        return Result.ok(payment.toResponse())
    }

    override fun cancelPayment(paymentId: PaymentId): Result<PaymentResponse, PaymentError> {
        val payment = payments[paymentId]
            ?: return Result.err(PaymentError.NotFound(paymentId))

        if (payment.status !in listOf(PaymentStatus.PENDING, PaymentStatus.PROCESSING)) {
            return Result.err(
                PaymentError.InvalidState(
                    paymentId = paymentId,
                    currentState = payment.status.name,
                    requestedOperation = "cancel"
                )
            )
        }

        val updatedPayment = payment.copy(
            status = PaymentStatus.CANCELLED,
            updatedAt = Instant.now()
        )
        payments[paymentId] = updatedPayment

        logger.info("MOCK PAYMENT: Payment ${paymentId.value} cancelled")

        return Result.ok(updatedPayment.toResponse())
    }

    override fun refundPayment(paymentId: PaymentId): Result<PaymentResponse, PaymentError> {
        val payment = payments[paymentId]
            ?: return Result.err(PaymentError.NotFound(paymentId))

        if (payment.status != PaymentStatus.SUCCEEDED) {
            return Result.err(
                PaymentError.InvalidState(
                    paymentId = paymentId,
                    currentState = payment.status.name,
                    requestedOperation = "refund"
                )
            )
        }

        // For mock purposes, we mark as cancelled to indicate refunded
        val updatedPayment = payment.copy(
            status = PaymentStatus.CANCELLED,
            updatedAt = Instant.now()
        )
        payments[paymentId] = updatedPayment

        logger.info("MOCK PAYMENT: Payment ${paymentId.value} refunded")

        return Result.ok(updatedPayment.toResponse())
    }

    // ==================== Test Utilities ====================

    /**
     * Returns all payments processed through this mock service.
     * Useful for test assertions.
     */
    fun getProcessedPayments(): List<PaymentResponse> =
        payments.values.map { it.toResponse() }

    /**
     * Clears all stored payments.
     * Call this in test setup to ensure clean state.
     */
    fun clearPayments() {
        payments.clear()
    }

    /**
     * Finds payments for a specific order.
     */
    fun findPaymentsForOrder(orderId: String): List<PaymentResponse> =
        payments.values
            .filter { it.orderId == orderId }
            .map { it.toResponse() }

    /**
     * Returns the count of successful payments.
     */
    fun getSuccessfulPaymentCount(): Int =
        payments.values.count { it.status == PaymentStatus.SUCCEEDED }

    // ==================== Private Helpers ====================

    private fun checkForTestScenario(paymentMethod: PaymentMethod): PaymentError? {
        val token = when (paymentMethod) {
            is PaymentMethod.Card -> paymentMethod.token
            is PaymentMethod.BankTransfer -> paymentMethod.token
            is PaymentMethod.Wallet -> paymentMethod.token
        }

        return when (token) {
            "tok_declined" -> PaymentError.CardDeclined("Card was declined by the issuer")
            "tok_insufficient_funds" -> PaymentError.InsufficientFunds
            "tok_fraud" -> PaymentError.FraudDetected(PaymentId.generate())
            "tok_processing_error" -> PaymentError.ProcessingError("An error occurred while processing your card")
            "tok_expired" -> PaymentError.InvalidPaymentMethod("Card has expired")
            else -> null // Success case
        }
    }

    private fun PaymentMethod.toSummary(): String = when (this) {
        is PaymentMethod.Card -> "${brand.toDisplayName()} ending in $last4"
        is PaymentMethod.BankTransfer -> "$bankName ending in $last4"
        is PaymentMethod.Wallet -> walletType.toDisplayName()
    }

    private fun CardBrand.toDisplayName(): String = when (this) {
        CardBrand.VISA -> "Visa"
        CardBrand.MASTERCARD -> "Mastercard"
        CardBrand.AMEX -> "American Express"
        CardBrand.DISCOVER -> "Discover"
        CardBrand.OTHER -> "Card"
    }

    private fun com.wingedsheep.ecologique.payment.api.dto.WalletType.toDisplayName(): String = when (this) {
        com.wingedsheep.ecologique.payment.api.dto.WalletType.APPLE_PAY -> "Apple Pay"
        com.wingedsheep.ecologique.payment.api.dto.WalletType.GOOGLE_PAY -> "Google Pay"
        com.wingedsheep.ecologique.payment.api.dto.WalletType.PAYPAL -> "PayPal"
    }

    private fun StoredPayment.toResponse() = PaymentResponse(
        id = id,
        orderId = orderId,
        amount = amount,
        status = status,
        paymentMethodSummary = paymentMethodSummary,
        failureReason = failureReason,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

/**
 * Internal storage representation of a payment.
 */
private data class StoredPayment(
    val id: PaymentId,
    val orderId: String,
    val amount: com.wingedsheep.ecologique.common.money.Money,
    val status: PaymentStatus,
    val paymentMethodSummary: String,
    val failureReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
