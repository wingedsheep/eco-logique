package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import io.cucumber.datatable.DataTable
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

class ProductSteps {

    @LocalServerPort
    private var port: Int = 0

    private var response: Response? = null
    private var createdProductId: String? = null

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/products"
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

    @Then("the product should have a sustainability rating")
    fun productHasSustainabilityRating() {
        val rating = response!!.jsonPath().getString("sustainabilityRating")
        assertThat(rating).isNotNull()
        assertThat(rating).isIn("A_PLUS", "A", "B", "C", "D")
    }

    @Given("a product {string} exists")
    fun productExists(name: String) {
        val request = ProductCreateRequest(
            name = name,
            description = "Test product description",
            category = "HOUSEHOLD",
            priceAmount = BigDecimal("19.99"),
            priceCurrency = "EUR",
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("1.5")
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        createdProductId = response!!.jsonPath().getString("id")
    }

    @Given("a product {string} exists with price {double} EUR")
    fun productExistsWithPrice(name: String, price: Double) {
        val request = ProductCreateRequest(
            name = name,
            description = "Test product description",
            category = "ELECTRONICS",
            priceAmount = BigDecimal.valueOf(price),
            priceCurrency = "EUR",
            weightGrams = 300,
            carbonFootprintKg = BigDecimal("3.0")
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()

        createdProductId = response!!.jsonPath().getString("id")
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

    @When("I update the product price to {double} EUR")
    fun updateProductPrice(newPrice: Double) {
        val request = ProductUpdatePriceRequest(
            priceAmount = BigDecimal.valueOf(newPrice),
            priceCurrency = "EUR"
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .put("/$createdProductId/price")
    }

    @Then("the product price should be {double} EUR")
    fun productPriceShouldBe(expectedPrice: Double) {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.jsonPath().getDouble("priceAmount")).isEqualTo(expectedPrice)
    }

    @Given("the following products exist:")
    fun followingProductsExist(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val request = ProductCreateRequest(
                name = row["name"]!!,
                description = "Test description",
                category = row["category"]!!,
                priceAmount = BigDecimal("19.99"),
                priceCurrency = "EUR",
                weightGrams = 100,
                carbonFootprintKg = BigDecimal("1.5")
            )

            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .post()
        }
    }

    @When("I list products in category {string}")
    fun listProductsInCategory(category: String) {
        response = RestAssured.get("?category=$category")
    }

    @Then("I should receive {int} product")
    fun shouldReceiveProducts(count: Int) {
        assertThat(response!!.statusCode).isEqualTo(200)
        val products = response!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(products).hasSize(count)
    }

    @Then("the product should be {string}")
    fun productShouldBe(expectedName: String) {
        val products = response!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(products[0]["name"]).isEqualTo(expectedName)
    }

    @When("I delete the product")
    fun deleteProduct() {
        response = RestAssured.delete("/$createdProductId")
    }

    @Then("the product should no longer exist")
    fun productShouldNoLongerExist() {
        assertThat(response!!.statusCode).isEqualTo(204)
        val getResponse = RestAssured.get("/$createdProductId")
        assertThat(getResponse.statusCode).isEqualTo(404)
    }

    @When("I try to create another product with name {string}")
    fun tryCreateDuplicateProduct(name: String) {
        val request = ProductCreateRequest(
            name = name,
            description = "Duplicate product",
            category = "HOUSEHOLD",
            priceAmount = BigDecimal("29.99"),
            priceCurrency = "EUR",
            weightGrams = 200,
            carbonFootprintKg = BigDecimal("2.0")
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post()
    }

    @Then("I should receive a duplicate name error")
    fun shouldReceiveDuplicateNameError() {
        assertThat(response!!.statusCode).isEqualTo(409)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Duplicate Product Name")
    }
}
