package com.wingedsheep.ecologique.orders.impl.infrastructure.web

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.error.OrderError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
    ): ResponseEntity<OrderDto> {
        return orderService.createOrder(jwt.subject, request).fold(
            onSuccess = { order ->
                ResponseEntity.status(HttpStatus.CREATED).body(order)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID (ownership check applies)")
    fun getOrder(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID
    ): ResponseEntity<OrderDto> {
        return orderService.getOrder(OrderId(id), jwt.subject).fold(
            onSuccess = { order ->
                ResponseEntity.ok(order)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @GetMapping
    @Operation(summary = "List orders", description = "Lists all orders for the authenticated user")
    fun getOrders(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<OrderDto>> {
        return orderService.findOrdersForUser(jwt.subject).fold(
            onSuccess = { orders ->
                ResponseEntity.ok(orders)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }
}

private fun OrderError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is OrderError.NotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Order Not Found",
            "Order not found: $id"
        )
        is OrderError.AccessDenied -> Triple(
            HttpStatus.FORBIDDEN,
            "Access Denied",
            "Access denied to order: $orderId"
        )
        is OrderError.InvalidStatus -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Status",
            "Cannot transition from '$currentStatus' to '$requestedStatus'"
        )
        is OrderError.ProductNotFound -> Triple(
            HttpStatus.BAD_REQUEST,
            "Product Not Found",
            "Product not found: $productId"
        )
        is OrderError.ValidationFailed -> Triple(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            reason
        )
        is OrderError.Unexpected -> Triple(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            message
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}
