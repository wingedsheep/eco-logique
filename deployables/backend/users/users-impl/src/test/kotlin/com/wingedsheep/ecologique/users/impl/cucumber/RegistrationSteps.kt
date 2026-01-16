package com.wingedsheep.ecologique.users.impl.cucumber

import com.wingedsheep.ecologique.users.api.dto.RegistrationRequest
import com.wingedsheep.ecologique.users.api.dto.VerifyEmailRequest
import com.wingedsheep.ecologique.users.impl.application.RegistrationServiceImpl
import com.wingedsheep.ecologique.users.impl.infrastructure.identity.MockIdentityProvider
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort

class RegistrationSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mockIdentityProvider: MockIdentityProvider

    @Autowired
    private lateinit var registrationService: RegistrationServiceImpl

    @Autowired
    private lateinit var mockEmailService: MockEmailService

    private var response: Response? = null
    private var currentEmail: String = ""
    private var verificationCode: String? = null

    @Before
    fun setupRegistration() {
        mockIdentityProvider.clearUsers()
        registrationService.clearPendingVerifications()
        mockEmailService.clearSentEmails()
    }

    private fun authRequest(): RequestSpecification {
        return RestAssured.given()
            .port(port)
            .basePath("")
            .contentType(ContentType.JSON)
    }

    // Given steps

    @Given("I have registered with email {string} and password {string}")
    fun registerWithEmailAndPassword(email: String, password: String) {
        currentEmail = email
        val request = RegistrationRequest(email = email, password = password)
        response = authRequest()
            .body(request)
            .post("/api/v1/auth/register")

        // Store the verification code for later use
        val pending = registrationService.getPendingVerification(email)
        verificationCode = pending?.verificationCode
    }

    @Given("I have verified my email for {string}")
    fun verifyEmailFor(email: String) {
        val pending = registrationService.getPendingVerification(email)
        if (pending != null) {
            val request = VerifyEmailRequest(email = email, verificationCode = pending.verificationCode)
            authRequest()
                .body(request)
                .post("/api/v1/auth/verify-email")
        }
    }

    // When steps

    @When("I register with email {string} and password {string}")
    fun iRegisterWithEmailAndPassword(email: String, password: String) {
        currentEmail = email
        // Use raw JSON to allow testing edge cases where DTO validation would fail
        val rawJson = """{"email":"$email","password":"$password"}"""
        response = authRequest()
            .body(rawJson)
            .post("/api/v1/auth/register")

        // Store the verification code for later use if registration succeeded
        if (response?.statusCode == 201) {
            val pending = registrationService.getPendingVerification(email)
            verificationCode = pending?.verificationCode
        }
    }

    @When("I verify my email with the correct verification code")
    fun verifyWithCorrectCode() {
        val pending = registrationService.getPendingVerification(currentEmail)
        val code = pending?.verificationCode ?: verificationCode ?: "000000"
        val request = VerifyEmailRequest(email = currentEmail, verificationCode = code)
        response = authRequest()
            .body(request)
            .post("/api/v1/auth/verify-email")
    }

    @When("I verify my email with an incorrect verification code for {string}")
    fun verifyWithIncorrectCode(email: String) {
        val request = VerifyEmailRequest(email = email, verificationCode = "000000")
        response = authRequest()
            .body(request)
            .post("/api/v1/auth/verify-email")
    }

    @When("I request to resend the verification email for {string}")
    fun requestResendVerification(email: String) {
        currentEmail = email
        mockEmailService.clearSentEmails()
        response = authRequest()
            .queryParam("email", email)
            .post("/api/v1/auth/resend-verification")
    }

    // Then steps

    @Then("the registration should be initiated successfully")
    fun registrationInitiatedSuccessfully() {
        assertThat(response?.statusCode).isEqualTo(201)
        assertThat(response?.jsonPath()?.getString("email")).isEqualTo(currentEmail)
    }

    @Then("a verification email should be sent to {string}")
    fun verificationEmailSentTo(email: String) {
        val sentEmails = mockEmailService.getSentEmails()
        assertThat(sentEmails).anyMatch { it.to.value == email }
    }

    @Then("a new verification email should be sent to {string}")
    fun newVerificationEmailSentTo(email: String) {
        assertThat(response?.statusCode).isEqualTo(200)
        val sentEmails = mockEmailService.getSentEmails()
        assertThat(sentEmails).anyMatch { it.to.value == email }
    }

    @Then("my account should be verified")
    fun accountVerified() {
        assertThat(response?.statusCode).isEqualTo(200)
        assertThat(response?.jsonPath()?.getString("message")).contains("verified")
    }

    @Then("the registration should fail with an invalid email error")
    fun registrationFailsWithInvalidEmail() {
        assertThat(response?.statusCode).isEqualTo(400)
        assertThat(response?.jsonPath()?.getString("title")).isEqualTo("Invalid Email")
    }

    @Then("the registration should fail with an invalid password error")
    fun registrationFailsWithInvalidPassword() {
        assertThat(response?.statusCode).isEqualTo(400)
        assertThat(response?.jsonPath()?.getString("title")).isEqualTo("Invalid Password")
    }

    @Then("the registration should fail with an email already exists error")
    fun registrationFailsWithEmailAlreadyExists() {
        assertThat(response?.statusCode).isEqualTo(409)
        assertThat(response?.jsonPath()?.getString("title")).isEqualTo("Email Already Exists")
    }

    @Then("the verification should fail with an invalid code error")
    fun verificationFailsWithInvalidCode() {
        assertThat(response?.statusCode).isEqualTo(400)
        assertThat(response?.jsonPath()?.getString("title")).isEqualTo("Invalid Verification Code")
    }

    @Then("the error should mention {string}")
    fun errorShouldMention(text: String) {
        val detail = response?.jsonPath()?.getString("detail") ?: ""
        assertThat(detail.lowercase()).contains(text.lowercase())
    }
}
