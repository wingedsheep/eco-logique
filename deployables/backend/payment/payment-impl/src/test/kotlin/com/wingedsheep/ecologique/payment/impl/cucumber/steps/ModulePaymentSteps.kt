package com.wingedsheep.ecologique.payment.impl.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.payment.impl.MockPaymentService
import com.wingedsheep.ecologique.payment.impl.infrastructure.web.ProcessPaymentRequest
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import java.math.BigDecimal
import java.util.UUID

class ModulePaymentSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockPaymentService: MockPaymentService

    private var response: Response? = null
    private var createdPaymentId: String? = null
    private var currentOrderId: String = UUID.randomUUID().toString()

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/payments"
        mockPaymentService.clearPayments()
        currentOrderId = UUID.randomUUID().toString()
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @When("I process a payment with a valid Visa card")
    fun processPaymentWithValidVisa() {
        processPayment("tok_visa", "4242", CardBrand.VISA)
    }

    @When("I process a payment with a valid Mastercard")
    fun processPaymentWithValidMastercard() {
        processPayment("tok_mastercard", "5555", CardBrand.MASTERCARD)
    }

    @When("I process a payment with a declined card")
    fun processPaymentWithDeclinedCard() {
        processPayment("tok_declined", "0000", CardBrand.VISA)
    }

    @When("I process a payment with insufficient funds")
    fun processPaymentWithInsufficientFunds() {
        processPayment("tok_insufficient_funds", "1234", CardBrand.MASTERCARD)
    }

    @When("I process a payment that triggers fraud detection")
    fun processPaymentWithFraudDetection() {
        processPayment("tok_fraud", "9999", CardBrand.VISA)
    }

    @When("I process a payment that causes a processing error")
    fun processPaymentWithProcessingError() {
        processPayment("tok_processing_error", "8888", CardBrand.VISA)
    }

    @When("I process a payment with an expired card")
    fun processPaymentWithExpiredCard() {
        processPayment("tok_expired", "7777", CardBrand.VISA)
    }

    private fun processPayment(token: String, last4: String, brand: CardBrand) {
        val request = ProcessPaymentRequest(
            orderId = currentOrderId,
            amount = BigDecimal("99.99"),
            currency = Currency.EUR,
            cardToken = token,
            cardLast4 = last4,
            cardBrand = brand,
            description = "Test payment"
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        if (response!!.statusCode == 201) {
            createdPaymentId = response!!.jsonPath().getString("id")
        }
    }

    @Then("the payment should succeed")
    fun paymentShouldSucceed() {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 201 but got ${response!!.statusCode}: ${response!!.body.asString()}")
            .isEqualTo(201)
        assertThat(createdPaymentId).isNotNull()
    }

    @Then("the payment status should be {string}")
    fun paymentStatusShouldBe(expectedStatus: String) {
        assertThat(response!!.jsonPath().getString("status")).isEqualTo(expectedStatus)
    }

    @Then("the payment method summary should contain {string}")
    fun paymentMethodSummaryShouldContain(expectedText: String) {
        assertThat(response!!.jsonPath().getString("paymentMethodSummary")).contains(expectedText)
    }

    @Then("the payment should fail with status code {int}")
    fun paymentShouldFailWithStatusCode(expectedStatusCode: Int) {
        assertThat(response!!.statusCode).isEqualTo(expectedStatusCode)
    }

    @Then("the error title should be {string}")
    fun errorTitleShouldBe(expectedTitle: String) {
        assertThat(response!!.jsonPath().getString("title")).isEqualTo(expectedTitle)
    }

    @When("I retrieve the payment by ID")
    fun retrievePaymentById() {
        response = RestAssured.get("/$createdPaymentId")
    }

    @Then("I should receive the payment details")
    fun shouldReceivePaymentDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.jsonPath().getString("id")).isEqualTo(createdPaymentId)
    }

    @When("I retrieve a non-existent payment")
    fun retrieveNonExistentPayment() {
        response = RestAssured.get("/${UUID.randomUUID()}")
    }

    @Then("I should receive a not found error")
    fun shouldReceiveNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Payment Not Found")
    }

    @Given("a successful payment exists")
    fun successfulPaymentExists() {
        processPayment("tok_visa", "4242", CardBrand.VISA)
        assertThat(response!!.statusCode).isEqualTo(201)
    }

    @When("I process another payment for the same order")
    fun processAnotherPaymentForSameOrder() {
        val request = ProcessPaymentRequest(
            orderId = currentOrderId,
            amount = BigDecimal("50.00"),
            currency = Currency.EUR,
            cardToken = "tok_mastercard",
            cardLast4 = "5555",
            cardBrand = CardBrand.MASTERCARD,
            description = "Additional payment"
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        if (response!!.statusCode == 201) {
            createdPaymentId = response!!.jsonPath().getString("id")
        }
    }

    @Then("both payments should be recorded")
    fun bothPaymentsShouldBeRecorded() {
        val payments = mockPaymentService.findPaymentsForOrder(currentOrderId)
        assertThat(payments).hasSize(2)
    }

    @When("I process a payment with amount {double} EUR")
    fun processPaymentWithAmount(amount: Double) {
        val request = ProcessPaymentRequest(
            orderId = currentOrderId,
            amount = BigDecimal.valueOf(amount),
            currency = Currency.EUR,
            cardToken = "tok_visa",
            cardLast4 = "4242",
            cardBrand = CardBrand.VISA,
            description = "Test payment"
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        if (response!!.statusCode == 201) {
            createdPaymentId = response!!.jsonPath().getString("id")
        }
    }

    @Then("the payment amount should be {double}")
    fun paymentAmountShouldBe(expectedAmount: Double) {
        assertThat(response!!.jsonPath().getDouble("amount.amount")).isEqualTo(expectedAmount)
    }
}
