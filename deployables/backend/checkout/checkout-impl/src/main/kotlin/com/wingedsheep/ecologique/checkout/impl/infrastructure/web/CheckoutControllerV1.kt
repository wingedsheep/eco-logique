package com.wingedsheep.ecologique.checkout.impl.infrastructure.web

import com.wingedsheep.ecologique.checkout.api.CheckoutService
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutRequest
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutResult
import com.wingedsheep.ecologique.checkout.api.error.CheckoutError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "Checkout orchestration")
internal class CheckoutControllerV1(
    private val checkoutService: CheckoutService
) {

    @PostMapping
    @Operation(
        summary = "Process checkout",
        description = "Processes checkout: creates order from cart, reserves inventory, and processes payment"
    )
    fun checkout(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CheckoutRequest
    ): ResponseEntity<CheckoutResult> {
        return checkoutService.checkout(jwt.subject, request).fold(
            onSuccess = { result ->
                ResponseEntity.status(HttpStatus.CREATED).body(result)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }
}

private fun CheckoutError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is CheckoutError.EmptyCart -> Triple(
            HttpStatus.BAD_REQUEST,
            "Empty Cart",
            "Cannot checkout with an empty cart"
        )
        is CheckoutError.ProductNotFound -> Triple(
            HttpStatus.BAD_REQUEST,
            "Product Not Found",
            "Product not found: ${productId.value}"
        )
        is CheckoutError.InsufficientStock -> Triple(
            HttpStatus.CONFLICT,
            "Insufficient Stock",
            "Insufficient stock for product ${productId.value}: requested $requested, available $available"
        )
        is CheckoutError.OrderCreationFailed -> Triple(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Order Creation Failed",
            reason
        )
        is CheckoutError.PaymentFailed -> Triple(
            HttpStatus.PAYMENT_REQUIRED,
            "Payment Failed",
            "Payment failed for order ${orderId.value}: $reason"
        )
        is CheckoutError.CheckoutUnavailable -> Triple(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Checkout Unavailable",
            reason
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title

    // Add orderId to response when available (for payment failures, order was created)
    if (this is CheckoutError.PaymentFailed) {
        problemDetail.setProperty("orderId", orderId.value.toString())
    }

    return ErrorResponseException(status, problemDetail, null)
}
