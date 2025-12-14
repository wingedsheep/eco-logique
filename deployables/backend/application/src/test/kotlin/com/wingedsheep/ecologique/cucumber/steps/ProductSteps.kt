package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.math.BigDecimal

class ProductSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var jwtDecoder: JwtDecoder

    private var response: Response? = null
    private var createdProductId: String? = null

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/products"

        val tokenValue = "fake-jwt-token"
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", "jane")
            .claim("realm_access", mapOf("roles" to listOf("ROLE_ADMIN")))
            .build()

        whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)

        RestAssured.requestSpecification = RestAssured.given()
            .header("Authorization", "Bearer $tokenValue")
    }

    @Given("the products database is empty")
    fun productsDbEmpty() {
        val products = RestAssured.get().then().extract().body().jsonPath().getList<Map<String, Any>>("")
        products.forEach { product ->
            RestAssured.delete("/${product["id"]}")
        }
    }

    @When("I create a product with the following details:")
    fun createProductWithDetails(dataTable: DataTable) {
        val data = dataTable.asMap()
        val priceInfo = data["price"]!!.split(" ")
        val weightInfo = data["weight"]!!.split(" ")
        val carbonInfo = data["carbon"]!!.split(" ")

        val request = ProductCreateRequest(
            name = data["name"]!!,
            description = data["description"]!!,
            category = data["category"]!!,
            priceAmount = BigDecimal(priceInfo[0]),
            priceCurrency = priceInfo[1],
            weightGrams = weightInfo[0].toInt(),
            carbonFootprintKg = BigDecimal(carbonInfo[0])
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        if (response!!.statusCode == 201) {
            createdProductId = response!!.jsonPath().getString("id")
        }
    }

    @Then("the product should be created successfully")
    fun productCreatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
        assertThat(createdProductId).isNotNull()
    }

    @When("I retrieve the product by ID")
    fun retrieveProductById() {
        response = RestAssured.get("/$createdProductId")
    }

    @Then("I should receive the product details")
    fun receiveProductDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.jsonPath().getString("id")).isEqualTo(createdProductId)
    }

    @Then("the product name should be {string}")
    fun productNameShouldBe(expectedName: String) {
        assertThat(response!!.jsonPath().getString("name")).isEqualTo(expectedName)
    }
}
