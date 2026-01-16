package com.wingedsheep.ecologique.users.impl.cucumber

import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserUpdateAddressRequest
import com.wingedsheep.ecologique.users.impl.infrastructure.identity.MockIdentityProvider
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean

class ModuleUserSteps {

    @LocalServerPort
    private var port: Int = 0

    @MockitoBean
    @Autowired
    private lateinit var jwtDecoder: JwtDecoder

    @Autowired
    private lateinit var mockIdentityProvider: MockIdentityProvider

    private var response: Response? = null
    private var currentSubject: String = ""
    private var currentUserId: UserId? = null
    private lateinit var authenticatedRequest: RequestSpecification

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/users"
        mockIdentityProvider.clearUsers()
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @Given("I am authenticated as {string}")
    fun authenticatedAs(subject: String) {
        currentSubject = subject
        val tokenValue = "test-token-$subject"
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", subject)
            .build()

        whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)

        // Create a demo user in the identity provider so UserContext can resolve the user
        // The JWT subject must match the identity provider's external subject
        currentUserId = UserId.generate()
        mockIdentityProvider.createUserWithSubject(
            userId = currentUserId!!,
            externalSubject = subject,
            email = "$subject@test.com",
            password = "TestPassword123!"
        )

        authenticatedRequest = RestAssured.given()
            .header("Authorization", "Bearer $tokenValue")
            .contentType(ContentType.JSON)
    }

    @Given("a profile exists for the current user with name {string}")
    fun profileExistsWithName(name: String) {
        val request = UserCreateRequest(
            name = name,
            email = "$currentSubject@example.com",
            address = null
        )

        authenticatedRequest
            .body(request)
            .post()
    }

    @Given("a profile exists for the current user with email {string}")
    fun profileExistsWithEmail(email: String) {
        val request = UserCreateRequest(
            name = "Test User",
            email = email,
            address = null
        )

        authenticatedRequest
            .body(request)
            .post()
    }

    @When("I create a profile with the following details:")
    fun createProfileWithDetails(dataTable: DataTable) {
        val data = dataTable.asMap()
        val request = UserCreateRequest(
            name = data["name"]!!,
            email = data["email"]!!,
            address = null
        )

        response = authenticatedRequest
            .body(request)
            .post()
    }

    @When("I create a profile with address:")
    fun createProfileWithAddress(dataTable: DataTable) {
        val data = dataTable.asMap()
        val request = UserCreateRequest(
            name = data["name"]!!,
            email = data["email"]!!,
            address = AddressDto(
                street = data["street"]!!,
                houseNumber = data["houseNumber"]!!,
                postalCode = data["postalCode"]!!,
                city = data["city"]!!,
                countryCode = data["countryCode"]!!
            )
        )

        response = authenticatedRequest
            .body(request)
            .post()
    }

    @When("I retrieve my profile")
    fun retrieveProfile() {
        response = authenticatedRequest.get()
    }

    @When("I update my address to:")
    fun updateAddress(dataTable: DataTable) {
        val data = dataTable.asMap()
        val request = UserUpdateAddressRequest(
            street = data["street"]!!,
            houseNumber = data["houseNumber"]!!,
            postalCode = data["postalCode"]!!,
            city = data["city"]!!,
            countryCode = data["countryCode"]!!
        )

        response = authenticatedRequest
            .body(request)
            .put("/address")
    }

    @When("I try to create another profile")
    fun tryCreateAnotherProfile() {
        val request = UserCreateRequest(
            name = "Another User",
            email = "another-$currentSubject@example.com",
            address = null
        )

        response = authenticatedRequest
            .body(request)
            .post()
    }

    @When("I try to create a profile with email {string}")
    fun tryCreateProfileWithEmail(email: String) {
        val request = UserCreateRequest(
            name = "Test User",
            email = email,
            address = null
        )

        response = authenticatedRequest
            .body(request)
            .post()
    }

    @When("I try to create a profile with country {string}")
    fun tryCreateProfileWithCountry(countryCode: String) {
        val request = UserCreateRequest(
            name = "Test User",
            email = "$currentSubject@example.com",
            address = AddressDto(
                street = "Test Street",
                houseNumber = "1",
                postalCode = "12345",
                city = "Test City",
                countryCode = countryCode
            )
        )

        response = authenticatedRequest
            .body(request)
            .post()
    }

    @Then("the profile should be created successfully")
    fun profileCreatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
        assertThat(response!!.jsonPath().getString("id")).isNotNull()
    }

    @Then("the profile should have name {string}")
    fun profileHasName(expectedName: String) {
        assertThat(response!!.jsonPath().getString("name")).isEqualTo(expectedName)
    }

    @Then("the profile should have email {string}")
    fun profileHasEmail(expectedEmail: String) {
        assertThat(response!!.jsonPath().getString("email")).isEqualTo(expectedEmail)
    }

    @Then("the profile should have an address in {string}")
    fun profileHasAddressInCity(expectedCity: String) {
        assertThat(response!!.jsonPath().getString("defaultAddress.city")).isEqualTo(expectedCity)
    }

    @Then("I should receive the profile details")
    fun receiveProfileDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.jsonPath().getString("id")).isNotNull()
    }

    @Then("the profile name should be {string}")
    fun profileNameShouldBe(expectedName: String) {
        assertThat(response!!.jsonPath().getString("name")).isEqualTo(expectedName)
    }

    @Then("the address should be updated successfully")
    fun addressUpdatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(200)
    }

    @Then("I should receive an already exists error")
    fun shouldReceiveAlreadyExistsError() {
        assertThat(response!!.statusCode).isEqualTo(409)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("User Already Exists")
    }

    @Then("I should receive an email already exists error")
    fun shouldReceiveEmailAlreadyExistsError() {
        assertThat(response!!.statusCode).isEqualTo(409)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Email Already Exists")
    }

    @Then("I should receive a validation error")
    fun shouldReceiveValidationError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Validation Failed")
    }

    @Then("I should receive an invalid country error")
    fun shouldReceiveInvalidCountryError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Invalid Country")
    }

    @Then("I should receive a not found error")
    fun shouldReceiveNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("User Not Found")
    }
}