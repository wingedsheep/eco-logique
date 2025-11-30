package com.wingedsheep.ecologique.products.impl.cucumber.steps

import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.web.server.LocalServerPort
import java.math.BigDecimal

class ModuleProductSteps {

    @LocalServerPort
    private var port: Int = 0

    private var response: Response? = null
    private var createdProductId: String? = null

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/products"
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        // No-op, just to verify context loaded
        assertThat(port).isGreaterThan(0)
    }

    @When("I create a product {string} with price {double} EUR")
    fun createProduct(name: String, price: Double) {
        val request = ProductCreateRequest(
            name = name,
            description = "Module test description",
            category = "HOUSEHOLD",
            priceAmount = BigDecimal.valueOf(price),
            priceCurrency = "EUR",
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("1.0")
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        if (response!!.statusCode == 201) {
            createdProductId = response!!.jsonPath().getString("id")
        }
    }

    @Then("the product should be retrievable by ID")
    fun retrieveProduct() {
        assertThat(createdProductId).isNotNull
        response = RestAssured.get("/$createdProductId")
        assertThat(response!!.statusCode).isEqualTo(200)
    }

    @Then("the product name should be {string}")
    fun verifyProductName(name: String) {
        assertThat(response!!.jsonPath().getString("name")).isEqualTo(name)
    }
}
