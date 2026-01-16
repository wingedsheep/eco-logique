package com.wingedsheep.ecologique.payment.worldview

import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.payment.api.dto.PaymentMethod
import com.wingedsheep.ecologique.payment.api.dto.WalletType

/**
 * Predefined payment methods for testing and development.
 *
 * These payment methods can be used in tests and for seeding demo data.
 * The token values are special test tokens recognized by the MockPaymentService.
 */
object WorldviewPayment {

    // ==================== Successful Payment Methods ====================

    /**
     * A valid Visa card that will always succeed.
     */
    val validVisaCard = PaymentMethod.Card(
        token = "tok_visa",
        last4 = "4242",
        brand = CardBrand.VISA
    )

    /**
     * A valid Mastercard that will always succeed.
     */
    val validMastercard = PaymentMethod.Card(
        token = "tok_mastercard",
        last4 = "5555",
        brand = CardBrand.MASTERCARD
    )

    /**
     * A valid American Express card that will always succeed.
     */
    val validAmex = PaymentMethod.Card(
        token = "tok_amex",
        last4 = "0005",
        brand = CardBrand.AMEX
    )

    /**
     * Apple Pay that will always succeed.
     */
    val validApplePay = PaymentMethod.Wallet(
        token = "tok_apple_pay",
        walletType = WalletType.APPLE_PAY
    )

    /**
     * Google Pay that will always succeed.
     */
    val validGooglePay = PaymentMethod.Wallet(
        token = "tok_google_pay",
        walletType = WalletType.GOOGLE_PAY
    )

    // ==================== Failing Payment Methods ====================

    /**
     * A card that will be declined by the issuer.
     */
    val declinedCard = PaymentMethod.Card(
        token = "tok_declined",
        last4 = "0002",
        brand = CardBrand.VISA
    )

    /**
     * A card that will fail due to insufficient funds.
     */
    val insufficientFundsCard = PaymentMethod.Card(
        token = "tok_insufficient_funds",
        last4 = "9995",
        brand = CardBrand.VISA
    )

    /**
     * A card that will be flagged for fraud.
     */
    val fraudCard = PaymentMethod.Card(
        token = "tok_fraud",
        last4 = "0000",
        brand = CardBrand.VISA
    )

    /**
     * An expired card that will fail validation.
     */
    val expiredCard = PaymentMethod.Card(
        token = "tok_expired",
        last4 = "1234",
        brand = CardBrand.VISA
    )

    /**
     * A card that will cause a processing error.
     */
    val processingErrorCard = PaymentMethod.Card(
        token = "tok_processing_error",
        last4 = "9999",
        brand = CardBrand.VISA
    )

    // ==================== Collections ====================

    /**
     * All payment methods that should succeed.
     */
    val successfulPaymentMethods = listOf(
        validVisaCard,
        validMastercard,
        validAmex,
        validApplePay,
        validGooglePay
    )

    /**
     * All payment methods that should fail.
     */
    val failingPaymentMethods = listOf(
        declinedCard,
        insufficientFundsCard,
        fraudCard,
        expiredCard,
        processingErrorCard
    )
}
