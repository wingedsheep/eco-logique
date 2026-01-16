package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.money.Money
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.PaymentRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.payment.api.PaymentService
import com.wingedsheep.ecologique.payment.api.PaymentStatus
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.payment.api.dto.PaymentMethod
import com.wingedsheep.ecologique.payment.api.dto.PaymentRequest
import com.wingedsheep.ecologique.payment.impl.MockPaymentService
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID

class PaymentSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient,
    private val paymentService: PaymentService,
    private val orderService: OrderService,
) {
    private var lastPaymentError: String? = null

    @Before
    fun clearPayments() {
        if (paymentService is MockPaymentService) {
            paymentService.clearPayments()
        }
        lastPaymentError = null
    }

    @When("the order is ready for payment")
    fun orderIsReadyForPayment() {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        // Simulate what checkout would do before payment: reserve inventory
        // CREATED → RESERVED → PAYMENT_PENDING
        val orderId = OrderId(UUID.fromString(order.id))
        orderService.updateStatus(orderId, OrderStatus.RESERVED)
        orderService.updateStatus(orderId, OrderStatus.PAYMENT_PENDING)
    }

    @When("I pay for the order with a valid Visa card")
    fun payWithValidVisaCard() {
        processPaymentWithCard("tok_visa", "4242", CardBrand.VISA)
    }

    @When("I pay for the order with a valid Mastercard")
    fun payWithValidMastercard() {
        processPaymentWithCard("tok_mastercard", "5555", CardBrand.MASTERCARD)
    }

    @When("I pay for the order with a declined card")
    fun payWithDeclinedCard() {
        processPaymentWithCard("tok_declined", "0002", CardBrand.VISA)
    }

    @When("I pay for the order with a card with insufficient funds")
    fun payWithInsufficientFundsCard() {
        processPaymentWithCard("tok_insufficient_funds", "9995", CardBrand.VISA)
    }

    @When("I pay for the order with a card flagged for fraud")
    fun payWithFraudCard() {
        processPaymentWithCard("tok_fraud", "0000", CardBrand.VISA)
    }

    @Then("the payment should succeed")
    fun paymentShouldSucceed() {
        val payment = context.getLatestPayment()
            ?: throw IllegalStateException("No payment in context")
        assertThat(payment.status).isEqualTo(PaymentStatus.SUCCEEDED.name)
    }

    @Then("the payment should fail")
    fun paymentShouldFail() {
        assertThat(lastPaymentError).isNotNull()
    }

    @Then("the payment should fail with reason {string}")
    fun paymentShouldFailWithReason(expectedReason: String) {
        assertThat(lastPaymentError).isNotNull()
        assertThat(lastPaymentError).containsIgnoringCase(expectedReason)
    }

    @Then("the payment method should show {string}")
    fun paymentMethodShouldShow(expectedSummary: String) {
        val payment = context.getLatestPayment()
            ?: throw IllegalStateException("No payment in context")
        assertThat(payment.paymentMethodSummary).isEqualTo(expectedSummary)
    }

    @And("the order status should be updated to {string}")
    fun orderStatusShouldBeUpdatedTo(expectedStatus: String) {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders/${order.id}")
        assertThat(response.statusCode)
            .withFailMessage("Expected 200 but got ${response.statusCode}: ${response.bodyAsString()}")
            .isEqualTo(200)
        assertThat(response.getString("status")).isEqualTo(expectedStatus)
    }

    private fun processPaymentWithCard(token: String, last4: String, brand: CardBrand) {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context. Place an order first.")

        val paymentMethod = PaymentMethod.Card(
            token = token,
            last4 = last4,
            brand = brand
        )

        val request = PaymentRequest(
            orderId = order.id,
            amount = Money(order.grandTotal, Currency.EUR),
            paymentMethod = paymentMethod,
            description = "Payment for order ${order.id}"
        )

        val result = paymentService.processPayment(request)

        result.fold(
            onSuccess = { response ->
                context.storePayment(
                    orderId = order.id,
                    ref = PaymentRef(
                        id = response.id.value.toString(),
                        orderId = response.orderId,
                        status = response.status.name,
                        amount = response.amount.amount,
                        paymentMethodSummary = response.paymentMethodSummary
                    )
                )
                // Order status update is handled by PaymentEventListener via PaymentCompleted event
                lastPaymentError = null
            },
            onFailure = { error ->
                lastPaymentError = error.toString()
            }
        )
    }
}
