package com.wingedsheep.ecologique.payment.api

import java.util.UUID

/**
 * Unique identifier for a payment.
 *
 * Similar to Stripe's PaymentIntent ID, this identifies a single payment attempt.
 */
@JvmInline
value class PaymentId(val value: UUID) {
    companion object {
        fun generate(): PaymentId = PaymentId(UUID.randomUUID())
    }
}
