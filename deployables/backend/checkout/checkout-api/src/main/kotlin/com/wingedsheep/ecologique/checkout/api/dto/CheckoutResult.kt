package com.wingedsheep.ecologique.checkout.api.dto

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.payment.api.PaymentId
import com.wingedsheep.ecologique.payment.api.PaymentStatus

/**
 * Result of a successful checkout.
 */
data class CheckoutResult(
    val orderId: OrderId,
    val orderStatus: OrderStatus,
    val paymentId: PaymentId,
    val paymentStatus: PaymentStatus
)
