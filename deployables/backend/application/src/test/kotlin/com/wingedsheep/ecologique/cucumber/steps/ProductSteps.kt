package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.cucumber.TestResponse
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.math.BigDecimal

class ProductSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient,
    private val jwtDecoder: JwtDecoder
) {
    // Admin user ID for test operations
    private val adminUserId = "22222222-2222-2222-2222-222222222222"

    private var response: TestResponse? = null
    private var createdProductId: String? = null

    @Given("the products database is empty")
    fun productsDbEmpty() {
        ensureAdminAuth()
        val productsResponse = api.get("/api/v1/products")
        val products = productsResponse.getList<Map<String, Any>>("")
        products.forEach { product ->
            api.delete("/api/v1/products/${product["id"]}")
        }
    }

    @When("I create a product with the following details:")
    fun createProductWithDetails(dataTable: DataTable) {
        ensureAdminAuth()
        val data = dataTable.asMap()
        val priceInfo = data["price"]!!.split(" ")
        val weightInfo = data["weight"]!!.split(" ")
        val carbonInfo = data["carbon"]!!.split(" ")

        val request = ProductCreateRequest(
            name = data["name"]!!,
            description = data["description"]!!,
            category = ProductCategory.valueOf(data["category"]!!),
            priceAmount = BigDecimal(priceInfo[0]),
            priceCurrency = Currency.valueOf(priceInfo[1]),
            weightGrams = weightInfo[0].toInt(),
            carbonFootprintKg = BigDecimal(carbonInfo[0])
        )

        response = api.post("/api/v1/products", request)

        if (response!!.statusCode == 201) {
            createdProductId = response!!.getString("id")
        }
    }

    @Then("the product should be created successfully")
    fun productCreatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
        assertThat(createdProductId).isNotNull()
    }

    @When("I retrieve the product by ID")
    fun retrieveProductById() {
        ensureAdminAuth()
        response = api.get("/api/v1/products/$createdProductId")
    }

    @Then("I should receive the product details")
    fun receiveProductDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.getString("id")).isEqualTo(createdProductId)
    }

    @Then("the product name should be {string}")
    fun productNameShouldBe(expectedName: String) {
        assertThat(response!!.getString("name")).isEqualTo(expectedName)
    }

    private fun ensureAdminAuth() {
        if (context.authToken == null) {
            val tokenValue = "test-token-admin-$adminUserId"
            val jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "none")
                .claim("sub", adminUserId)
                .claim("realm_access", mapOf("roles" to listOf("ROLE_ADMIN", "ROLE_CUSTOMER")))
                .build()

            whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)

            context.authToken = tokenValue
            context.currentUserId = adminUserId
        }
    }
}
