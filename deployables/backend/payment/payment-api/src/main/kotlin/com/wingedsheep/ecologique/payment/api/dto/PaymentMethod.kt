package com.wingedsheep.ecologique.payment.api.dto

/**
 * Represents a payment method.
 *
 * This is an abstraction over different payment methods supported by payment providers.
 * In a real integration with Stripe, this would be a tokenized reference to the actual
 * payment method stored securely by Stripe (never raw card numbers).
 */
sealed class PaymentMethod {

    /**
     * Credit or debit card payment.
     *
     * @param token A tokenized reference to the card (e.g., Stripe's `pm_xxx` token).
     *              The actual card details are never stored in our system.
     * @param last4 Last 4 digits of the card for display purposes.
     * @param brand The card brand (VISA, MASTERCARD, etc.).
     */
    data class Card(
        val token: String,
        val last4: String,
        val brand: CardBrand,
    ) : PaymentMethod() {
        init {
            require(token.isNotBlank()) { "Card token cannot be blank" }
            require(last4.length == 4 && last4.all { it.isDigit() }) {
                "Last 4 digits must be exactly 4 numeric characters"
            }
        }
    }

    /**
     * Bank transfer payment (e.g., SEPA, ACH).
     *
     * @param token A tokenized reference to the bank account.
     * @param bankName Name of the bank for display purposes.
     * @param last4 Last 4 digits of the account number.
     */
    data class BankTransfer(
        val token: String,
        val bankName: String,
        val last4: String,
    ) : PaymentMethod() {
        init {
            require(token.isNotBlank()) { "Bank transfer token cannot be blank" }
            require(bankName.isNotBlank()) { "Bank name cannot be blank" }
            require(last4.length == 4 && last4.all { it.isDigit() }) {
                "Last 4 digits must be exactly 4 numeric characters"
            }
        }
    }

    /**
     * Digital wallet payment (e.g., Apple Pay, Google Pay).
     *
     * @param token A tokenized reference to the wallet payment.
     * @param walletType Type of digital wallet.
     */
    data class Wallet(
        val token: String,
        val walletType: WalletType,
    ) : PaymentMethod() {
        init {
            require(token.isNotBlank()) { "Wallet token cannot be blank" }
        }
    }
}

enum class CardBrand {
    VISA,
    MASTERCARD,
    AMEX,
    DISCOVER,
    OTHER
}

enum class WalletType {
    APPLE_PAY,
    GOOGLE_PAY,
    PAYPAL
}
