package com.wingedsheep.ecologique.products.impl.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.time.Duration

class ModuleProductSteps {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var client: WebTestClient
    private val objectMapper = jacksonObjectMapper()

    private var response: TestResponse? = null
    private var createdProductId: String? = null

    @Before
    fun setup() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port/api/v1/products")
            .responseTimeout(Duration.ofSeconds(30))
            .build()
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @Given("the products database is empty")
    fun productsDbEmpty() {
        val products = get("").getList<Map<String, Any>>("")
        products.forEach { product ->
            delete("/${product["id"]}")
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
            category = ProductCategory.valueOf(data["category"]!!),
            priceAmount = BigDecimal(priceInfo[0]),
            priceCurrency = Currency.valueOf(priceInfo[1]),
            weightGrams = weightInfo[0].toInt(),
            carbonFootprintKg = BigDecimal(carbonInfo[0])
        )

        response = post("", request)

        if (response!!.statusCode == 201) {
            createdProductId = response!!.getString("id")
        }
    }

    @Then("the product should be created successfully")
    fun productCreatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
        assertThat(createdProductId).isNotNull()
    }

    @Then("the product should have a sustainability rating")
    fun productHasSustainabilityRating() {
        val rating = response!!.getString("sustainabilityRating")
        assertThat(rating).isNotNull()
        assertThat(rating).isIn("A_PLUS", "A", "B", "C", "D")
    }

    @Given("a product {string} exists")
    fun productExists(name: String) {
        val request = ProductCreateRequest(
            name = name,
            description = "Test product description",
            category = ProductCategory.HOUSEHOLD,
            priceAmount = BigDecimal("19.99"),
            priceCurrency = Currency.EUR,
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("1.5")
        )

        response = post("", request)
        createdProductId = response!!.getString("id")
    }

    @Given("a product {string} exists with price {double} EUR")
    fun productExistsWithPrice(name: String, price: Double) {
        val request = ProductCreateRequest(
            name = name,
            description = "Test product description",
            category = ProductCategory.ELECTRONICS,
            priceAmount = BigDecimal.valueOf(price),
            priceCurrency = Currency.EUR,
            weightGrams = 300,
            carbonFootprintKg = BigDecimal("3.0")
        )

        response = post("", request)
        createdProductId = response!!.getString("id")
    }

    @When("I retrieve the product by ID")
    fun retrieveProductById() {
        response = get("/$createdProductId")
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

    @When("I update the product price to {double} EUR")
    fun updateProductPrice(newPrice: Double) {
        val request = ProductUpdatePriceRequest(
            priceAmount = BigDecimal.valueOf(newPrice),
            priceCurrency = Currency.EUR
        )

        response = put("/$createdProductId/price", request)
    }

    @Then("the product price should be {double} EUR")
    fun productPriceShouldBe(expectedPrice: Double) {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.getDouble("priceAmount")).isEqualTo(expectedPrice)
    }

    @Given("the following products exist:")
    fun followingProductsExist(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val request = ProductCreateRequest(
                name = row["name"]!!,
                description = "Test description",
                category = ProductCategory.valueOf(row["category"]!!),
                priceAmount = BigDecimal("19.99"),
                priceCurrency = Currency.EUR,
                weightGrams = 100,
                carbonFootprintKg = BigDecimal("1.5")
            )
            post("", request)
        }
    }

    @When("I list products in category {string}")
    fun listProductsInCategory(category: String) {
        response = get("?category=$category")
    }

    @Then("I should receive {int} product")
    fun shouldReceiveProducts(count: Int) {
        assertThat(response!!.statusCode).isEqualTo(200)
        val products = response!!.getList<Map<String, Any>>("")
        assertThat(products).hasSize(count)
    }

    @Then("the product should be {string}")
    fun productShouldBe(expectedName: String) {
        val products = response!!.getList<Map<String, Any>>("")
        assertThat(products[0]["name"]).isEqualTo(expectedName)
    }

    @When("I delete the product")
    fun deleteProduct() {
        response = delete("/$createdProductId")
    }

    @Then("the product should no longer exist")
    fun productShouldNoLongerExist() {
        assertThat(response!!.statusCode).isEqualTo(204)
        val getResponse = get("/$createdProductId")
        assertThat(getResponse.statusCode).isEqualTo(404)
    }

    @When("I try to create another product with name {string}")
    fun tryCreateDuplicateProduct(name: String) {
        val request = ProductCreateRequest(
            name = name,
            description = "Duplicate product",
            category = ProductCategory.HOUSEHOLD,
            priceAmount = BigDecimal("29.99"),
            priceCurrency = Currency.EUR,
            weightGrams = 200,
            carbonFootprintKg = BigDecimal("2.0")
        )

        response = post("", request)
    }

    @Then("I should receive a duplicate name error")
    fun shouldReceiveDuplicateNameError() {
        assertThat(response!!.statusCode).isEqualTo(409)
        assertThat(response!!.getString("title")).isEqualTo("Duplicate Product Name")
    }

    // Helper methods
    private fun get(path: String): TestResponse {
        val result = client.get().uri(path).exchange().returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun post(path: String, body: Any): TestResponse {
        val result = client.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun put(path: String, body: Any): TestResponse {
        val result = client.put()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun delete(path: String): TestResponse {
        val result = client.delete().uri(path).exchange().returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    class TestResponse(
        val statusCode: Int,
        private val body: String,
        private val objectMapper: ObjectMapper
    ) {
        fun getString(path: String): String? = getValueAtPath(path) as? String
        fun getDouble(path: String): Double? = (getValueAtPath(path) as? Number)?.toDouble()

        fun <T> getList(path: String): List<T> {
            val value = if (path.isEmpty()) parseBody() else getValueAtPath(path)
            @Suppress("UNCHECKED_CAST")
            return value as? List<T> ?: emptyList()
        }

        private fun getValueAtPath(path: String): Any? {
            if (body.isBlank()) return null
            val parsed = parseBody() ?: return null
            if (path.isEmpty()) return parsed
            var current: Any? = parsed
            for (segment in path.split(".")) {
                current = when (current) {
                    is Map<*, *> -> current[segment]
                    is List<*> -> current.getOrNull(segment.toIntOrNull() ?: return null)
                    else -> return null
                }
            }
            return current
        }

        private fun parseBody(): Any? {
            if (body.isBlank()) return null
            return try { objectMapper.readValue<Any>(body) } catch (e: Exception) { null }
        }
    }
}
