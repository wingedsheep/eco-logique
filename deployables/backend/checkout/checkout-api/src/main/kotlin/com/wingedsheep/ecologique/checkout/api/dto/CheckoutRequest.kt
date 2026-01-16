package com.wingedsheep.ecologique.checkout.api.dto

import com.wingedsheep.ecologique.payment.api.dto.CardBrand

/**
 * Request to initiate checkout.
 */
data class CheckoutRequest(
    val cardToken: String,
    val cardLast4: String,
    val cardBrand: CardBrand
) {
    init {
        require(cardToken.isNotBlank()) { "Card token cannot be blank" }
        require(cardLast4.length == 4 && cardLast4.all { it.isDigit() }) {
            "Card last 4 digits must be exactly 4 numeric characters"
        }
    }
}
