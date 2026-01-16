package com.wingedsheep.ecologique.inventory.impl.cucumber.steps

import com.wingedsheep.ecologique.inventory.api.dto.AddressDto
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseUpdateRequest
import com.wingedsheep.ecologique.products.api.ProductId
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
import java.time.Duration
import java.util.UUID

class ModuleInventorySteps {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var client: WebTestClient
    private val objectMapper = jacksonObjectMapper()

    private var response: TestResponse? = null
    private var createdWarehouseId: String? = null
    private var testProductId: UUID = UUID.randomUUID()

    @Before
    fun setup() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .responseTimeout(Duration.ofSeconds(30))
            .build()
        testProductId = UUID.randomUUID()
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @When("I create a warehouse with the following details:")
    fun createWarehouseWithDetails(dataTable: DataTable) {
        val data = dataTable.asMap()

        val address = if (data.containsKey("street")) {
            AddressDto(
                street = data["street"]!!,
                houseNumber = data["houseNumber"]!!,
                postalCode = data["postalCode"]!!,
                city = data["city"]!!,
                countryCode = data["countryCode"]!!
            )
        } else {
            null
        }

        val request = WarehouseCreateRequest(
            name = data["name"]!!,
            countryCode = data["countryCode"]!!,
            address = address
        )

        response = post("/api/v1/admin/inventory/warehouses", request)

        if (response!!.statusCode == 201) {
            createdWarehouseId = response!!.getString("id")
        }
    }

    @Then("the warehouse should be created successfully")
    fun warehouseCreatedSuccessfully() {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 201 but got ${response!!.statusCode}: ${response!!.body}")
            .isEqualTo(201)
        assertThat(createdWarehouseId).isNotNull()
    }

    @Then("the warehouse name should be {string}")
    fun warehouseNameShouldBe(expectedName: String) {
        assertThat(response!!.getString("name")).isEqualTo(expectedName)
    }

    @Then("the warehouse country code should be {string}")
    fun warehouseCountryCodeShouldBe(expectedCode: String) {
        assertThat(response!!.getString("countryCode")).isEqualTo(expectedCode)
    }

    @Then("the warehouse should have an address")
    fun warehouseShouldHaveAddress() {
        assertThat(response!!.getString("address")).isNotNull()
        assertThat(response!!.getString("address.street")).isNotNull()
    }

    @Given("a warehouse {string} exists in country {string}")
    fun warehouseExistsInCountry(name: String, countryCode: String) {
        val request = WarehouseCreateRequest(
            name = name,
            countryCode = countryCode,
            address = null
        )

        response = post("/api/v1/admin/inventory/warehouses", request)
        createdWarehouseId = response!!.getString("id")
    }

    @When("I retrieve the warehouse by ID")
    fun retrieveWarehouseById() {
        response = get("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
    }

    @Then("I should receive the warehouse details")
    fun shouldReceiveWarehouseDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.getString("id")).isEqualTo(createdWarehouseId)
    }

    @When("I update the warehouse name to {string}")
    fun updateWarehouseName(newName: String) {
        val request = WarehouseUpdateRequest(
            name = newName,
            countryCode = null,
            address = null
        )

        response = put("/api/v1/admin/inventory/warehouses/$createdWarehouseId", request)
    }

    @Then("the warehouse update should succeed")
    fun warehouseUpdateShouldSucceed() {
        assertThat(response!!.statusCode).isEqualTo(200)
    }

    @When("I delete the warehouse")
    fun deleteWarehouse() {
        response = delete("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
    }

    @Then("the warehouse should be deleted successfully")
    fun warehouseDeletedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(204)
    }

    @Then("the warehouse should no longer exist")
    fun warehouseShouldNoLongerExist() {
        val getResponse = get("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
        assertThat(getResponse.statusCode).isEqualTo(404)
    }

    @When("I list all warehouses")
    fun listAllWarehouses() {
        response = get("/api/v1/admin/inventory/warehouses")
    }

    @Then("I should receive a list of warehouses")
    fun shouldReceiveWarehouseList() {
        assertThat(response!!.statusCode).isEqualTo(200)
        val warehouses = response!!.getList<Map<String, Any>>("")
        assertThat(warehouses).isNotNull()
    }

    @Then("the list should contain at least {int} warehouse")
    fun listShouldContainAtLeastWarehouses(minCount: Int) {
        val warehouses = response!!.getList<Map<String, Any>>("")
        assertThat(warehouses.size).isGreaterThanOrEqualTo(minCount)
    }

    @When("I try to create another warehouse with name {string}")
    fun tryCreateDuplicateWarehouse(name: String) {
        val request = WarehouseCreateRequest(
            name = name,
            countryCode = "NL",
            address = null
        )

        response = post("/api/v1/admin/inventory/warehouses", request)
    }

    @Then("I should receive a duplicate name error")
    fun shouldReceiveDuplicateNameError() {
        assertThat(response!!.statusCode).isEqualTo(409)
        assertThat(response!!.getString("title")).isEqualTo("Duplicate Warehouse Name")
    }

    @When("I try to create a warehouse with invalid country code {string}")
    fun tryCreateWarehouseWithInvalidCountryCode(countryCode: String) {
        val request = WarehouseCreateRequest(
            name = "Invalid Country Warehouse",
            countryCode = countryCode,
            address = null
        )

        response = post("/api/v1/admin/inventory/warehouses", request)
    }

    @Then("I should receive an invalid country code error")
    fun shouldReceiveInvalidCountryCodeError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.getString("title")).isEqualTo("Invalid Country Code")
    }

    @When("I add stock for a product with quantity {int}")
    fun addStockForProduct(quantity: Int) {
        val request = StockUpdateRequest(
            productId = ProductId(testProductId),
            quantityOnHand = quantity
        )

        response = put("/api/v1/admin/inventory/warehouses/$createdWarehouseId/stock", request)
    }

    @Then("the stock update should succeed")
    fun stockUpdateShouldSucceed() {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 200 but got ${response!!.statusCode}: ${response!!.body}")
            .isEqualTo(200)
    }

    @Then("the stock level should show quantity {int}")
    fun stockLevelShouldShow(expectedQuantity: Int) {
        val available = response!!.getInt("available")
        assertThat(available).isEqualTo(expectedQuantity)
    }

    @When("I get the warehouse stock")
    fun getWarehouseStock() {
        response = get("/api/v1/admin/inventory/warehouses/$createdWarehouseId/stock")
    }

    @Then("I should receive a list of stock levels")
    fun shouldReceiveStockLevelsList() {
        assertThat(response!!.statusCode).isEqualTo(200)
        val stockLevels = response!!.getList<Map<String, Any>>("")
        assertThat(stockLevels).isNotNull()
    }

    @When("I check stock for the test product")
    fun checkStockForTestProduct() {
        response = get("/api/v1/inventory/$testProductId")
    }

    @Then("the total available stock should be {int}")
    fun totalAvailableStockShouldBe(expectedStock: Int) {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 200 but got ${response!!.statusCode}: ${response!!.body}")
            .isEqualTo(200)
        val available = response!!.getInt("available")
        assertThat(available).isEqualTo(expectedStock)
    }

    @When("I update stock quantity to {int}")
    fun updateStockQuantity(quantity: Int) {
        val request = StockUpdateRequest(
            productId = ProductId(testProductId),
            quantityOnHand = quantity
        )

        response = put("/api/v1/admin/inventory/warehouses/$createdWarehouseId/stock", request)
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
        val body: String,
        private val objectMapper: ObjectMapper
    ) {
        fun getString(path: String): String? {
            val value = getValueAtPath(path)
            return when (value) {
                is String -> value
                null -> null
                else -> value.toString()
            }
        }
        fun getInt(path: String): Int? = (getValueAtPath(path) as? Number)?.toInt()
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
