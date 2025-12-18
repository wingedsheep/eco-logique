package com.wingedsheep.ecologique.orders.impl.infrastructure.web

import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.error.OrderError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management")
class OrderControllerV1(
    private val orderService: OrderService
) {

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order for the authenticated user")
    fun createOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: OrderCreateRequest
    ): ResponseEntity<Any> {
        return orderService.createOrder(jwt.subject, request).fold(
            onSuccess = { order ->
                ResponseEntity.status(HttpStatus.CREATED).body(order)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID (ownership check applies)")
    fun getOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return orderService.getOrder(id, jwt.subject).fold(
            onSuccess = { order ->
                ResponseEntity.ok(order)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @GetMapping
    @Operation(summary = "List orders", description = "Lists all orders for the authenticated user")
    fun getOrders(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        return orderService.findOrdersForUser(jwt.subject).fold(
            onSuccess = { orders ->
                ResponseEntity.ok(orders)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    private fun OrderError.toHttpStatus(): HttpStatus = when (this) {
        is OrderError.NotFound -> HttpStatus.NOT_FOUND
        is OrderError.AccessDenied -> HttpStatus.FORBIDDEN
        is OrderError.InvalidStatus -> HttpStatus.BAD_REQUEST
        is OrderError.ProductNotFound -> HttpStatus.BAD_REQUEST
        is OrderError.ValidationFailed -> HttpStatus.BAD_REQUEST
        is OrderError.Unexpected -> HttpStatus.INTERNAL_SERVER_ERROR
    }

    private fun OrderError.toProblemDetail(): ProblemDetail = when (this) {
        is OrderError.NotFound -> ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Order not found with id: $id"
        ).apply {
            type = URI.create("urn:problem:order:not-found")
            title = "Order Not Found"
        }
        is OrderError.AccessDenied -> ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "Access denied to order: $orderId"
        ).apply {
            type = URI.create("urn:problem:order:access-denied")
            title = "Access Denied"
        }
        is OrderError.InvalidStatus -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Cannot transition from '$currentStatus' to '$requestedStatus'"
        ).apply {
            type = URI.create("urn:problem:order:invalid-status-transition")
            title = "Invalid Status Transition"
        }
        is OrderError.ProductNotFound -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Product not found: $productId"
        ).apply {
            type = URI.create("urn:problem:order:product-not-found")
            title = "Product Not Found"
        }
        is OrderError.ValidationFailed -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            reason
        ).apply {
            type = URI.create("urn:problem:order:validation-failed")
            title = "Validation Failed"
        }
        is OrderError.Unexpected -> ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        ).apply {
            type = URI.create("urn:problem:order:unexpected")
            title = "Unexpected Error"
        }
    }
}
