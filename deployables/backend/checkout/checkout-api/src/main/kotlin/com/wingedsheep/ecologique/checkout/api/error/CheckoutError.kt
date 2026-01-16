package com.wingedsheep.ecologique.checkout.api.error

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.products.api.ProductId

/**
 * Errors that can occur during checkout.
 */
sealed class CheckoutError {

    /**
     * Cart is empty, cannot checkout.
     */
    data object EmptyCart : CheckoutError()

    /**
     * Product in cart not found.
     */
    data class ProductNotFound(val productId: ProductId) : CheckoutError()

    /**
     * Insufficient stock for a product.
     */
    data class InsufficientStock(
        val productId: ProductId,
        val requested: Int,
        val available: Int
    ) : CheckoutError()

    /**
     * Failed to create order.
     */
    data class OrderCreationFailed(val reason: String) : CheckoutError()

    /**
     * Payment was declined or failed.
     */
    data class PaymentFailed(
        val orderId: OrderId,
        val reason: String
    ) : CheckoutError()

    /**
     * General checkout error.
     */
    data class CheckoutUnavailable(val reason: String) : CheckoutError()
}
