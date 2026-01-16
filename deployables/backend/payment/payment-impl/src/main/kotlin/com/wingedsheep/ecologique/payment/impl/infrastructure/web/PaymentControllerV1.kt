package com.wingedsheep.ecologique.payment.impl.infrastructure.web

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.payment.api.PaymentId
import com.wingedsheep.ecologique.payment.api.PaymentService
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.payment.api.dto.PaymentMethod
import com.wingedsheep.ecologique.payment.api.dto.PaymentRequest
import com.wingedsheep.ecologique.payment.api.dto.PaymentResponse
import com.wingedsheep.ecologique.payment.api.error.PaymentError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment processing")
internal class PaymentControllerV1(
    private val paymentService: PaymentService
) {

    @PostMapping
    @Operation(summary = "Process a payment", description = "Processes a payment for an order")
    fun processPayment(
        @RequestBody request: ProcessPaymentRequest
    ): ResponseEntity<PaymentResponse> {
        val paymentRequest = request.toPaymentRequest()

        return paymentService.processPayment(paymentRequest).fold(
            onSuccess = { payment ->
                ResponseEntity.status(HttpStatus.CREATED).body(payment)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves a payment by its ID")
    fun getPayment(
        @PathVariable id: UUID
    ): ResponseEntity<PaymentResponse> {
        return paymentService.getPayment(PaymentId(id)).fold(
            onSuccess = { payment ->
                ResponseEntity.ok(payment)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }
}

/**
 * REST request for processing a payment.
 */
data class ProcessPaymentRequest(
    val orderId: String,
    val amount: BigDecimal,
    val currency: Currency,
    val cardToken: String,
    val cardLast4: String,
    val cardBrand: CardBrand,
    val description: String? = null,
) {
    init {
        require(orderId.isNotBlank()) { "Order ID cannot be blank" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
        require(cardToken.isNotBlank()) { "Card token cannot be blank" }
        require(cardLast4.length == 4 && cardLast4.all { it.isDigit() }) {
            "Card last 4 digits must be exactly 4 numeric characters"
        }
    }

    fun toPaymentRequest() = PaymentRequest(
        orderId = orderId,
        amount = Money(amount, currency),
        paymentMethod = PaymentMethod.Card(
            token = cardToken,
            last4 = cardLast4,
            brand = cardBrand
        ),
        description = description
    )
}

private fun PaymentError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is PaymentError.NotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Payment Not Found",
            "Payment not found: ${paymentId.value}"
        )
        is PaymentError.CardDeclined -> Triple(
            HttpStatus.PAYMENT_REQUIRED,
            "Card Declined",
            "Card was declined: $reason"
        )
        is PaymentError.InsufficientFunds -> Triple(
            HttpStatus.PAYMENT_REQUIRED,
            "Insufficient Funds",
            "Insufficient funds in the account"
        )
        is PaymentError.InvalidPaymentMethod -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Payment Method",
            "Invalid payment method: $reason"
        )
        is PaymentError.PaymentExpired -> Triple(
            HttpStatus.GONE,
            "Payment Expired",
            "Payment has expired: ${paymentId.value}"
        )
        is PaymentError.ProviderUnavailable -> Triple(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "Payment provider is currently unavailable"
        )
        is PaymentError.FraudDetected -> Triple(
            HttpStatus.FORBIDDEN,
            "Payment Rejected",
            "Payment was rejected due to suspicious activity"
        )
        is PaymentError.ProcessingError -> Triple(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Processing Error",
            "An error occurred while processing the payment: $reason"
        )
        is PaymentError.InvalidState -> Triple(
            HttpStatus.CONFLICT,
            "Invalid State",
            "Cannot $requestedOperation payment in state: $currentState"
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}
