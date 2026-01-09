package com.wingedsheep.ecologique.cart.impl.infrastructure.web

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management")
class CartControllerV1(
    private val cartService: CartService
) {

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves the cart for the authenticated user")
    fun getCart(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<CartDto> {
        return cartService.getCart(jwt.subject).fold(
            onSuccess = { cart ->
                ResponseEntity.ok(cart)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product to the cart")
    fun addItem(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: AddCartItemRequest
    ): ResponseEntity<CartDto> {
        return cartService.addItem(jwt.subject, request).fold(
            onSuccess = { cart ->
                ResponseEntity.status(HttpStatus.CREATED).body(cart)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a product in the cart")
    fun updateItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable productId: String,
        @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<CartDto> {
        return cartService.updateItem(jwt.subject, productId, request).fold(
            onSuccess = { cart ->
                ResponseEntity.ok(cart)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Removes a product from the cart")
    fun removeItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable productId: String
    ): ResponseEntity<CartDto> {
        return cartService.removeItem(jwt.subject, productId).fold(
            onSuccess = { cart ->
                ResponseEntity.ok(cart)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    fun clearCart(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Unit> {
        return cartService.clearCart(jwt.subject).fold(
            onSuccess = {
                ResponseEntity.noContent().build()
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }
}

private fun CartError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is CartError.ProductNotFound -> Triple(
            HttpStatus.BAD_REQUEST,
            "Product Not Found",
            "Product not found: $productId"
        )
        is CartError.ItemNotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Item Not Found",
            "Item not in cart: $productId"
        )
        is CartError.InvalidQuantity -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Quantity",
            "Invalid quantity: $quantity"
        )
        is CartError.ValidationFailed -> Triple(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            reason
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}
