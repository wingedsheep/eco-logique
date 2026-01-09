package com.wingedsheep.ecologique.cart.impl.infrastructure.web

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management")
class CartControllerV1(
    private val cartService: CartService
) {

    @GetMapping
    @Operation(summary = "Get cart", description = "Retrieves the cart for the authenticated user")
    fun getCart(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        return cartService.getCart(jwt.subject).fold(
            onSuccess = { cart ->
                ResponseEntity.ok(cart)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product to the cart")
    fun addItem(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: AddCartItemRequest
    ): ResponseEntity<Any> {
        return cartService.addItem(jwt.subject, request).fold(
            onSuccess = { cart ->
                ResponseEntity.status(HttpStatus.CREATED).body(cart)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of a product in the cart")
    fun updateItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable productId: String,
        @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<Any> {
        return cartService.updateItem(jwt.subject, productId, request).fold(
            onSuccess = { cart ->
                ResponseEntity.ok(cart)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Removes a product from the cart")
    fun removeItem(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable productId: String
    ): ResponseEntity<Any> {
        return cartService.removeItem(jwt.subject, productId).fold(
            onSuccess = { cart ->
                ResponseEntity.ok(cart)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    fun clearCart(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        return cartService.clearCart(jwt.subject).fold(
            onSuccess = {
                ResponseEntity.noContent().build()
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    private fun CartError.toHttpStatus(): HttpStatus = when (this) {
        is CartError.ProductNotFound -> HttpStatus.BAD_REQUEST
        is CartError.ItemNotFound -> HttpStatus.NOT_FOUND
        is CartError.InvalidQuantity -> HttpStatus.BAD_REQUEST
        is CartError.ValidationFailed -> HttpStatus.BAD_REQUEST
    }

    private fun CartError.toProblemDetail(): ProblemDetail = when (this) {
        is CartError.ProductNotFound -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Product not found: $productId"
        ).apply {
            type = URI.create("urn:problem:cart:product-not-found")
            title = "Product Not Found"
        }
        is CartError.ItemNotFound -> ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Item not in cart: $productId"
        ).apply {
            type = URI.create("urn:problem:cart:item-not-found")
            title = "Item Not Found"
        }
        is CartError.InvalidQuantity -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Invalid quantity: $quantity"
        ).apply {
            type = URI.create("urn:problem:cart:invalid-quantity")
            title = "Invalid Quantity"
        }
        is CartError.ValidationFailed -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            reason
        ).apply {
            type = URI.create("urn:problem:cart:validation-failed")
            title = "Validation Failed"
        }
    }
}
