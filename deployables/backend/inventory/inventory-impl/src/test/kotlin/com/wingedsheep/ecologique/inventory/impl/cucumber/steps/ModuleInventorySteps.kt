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
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.web.server.LocalServerPort
import java.util.UUID

class ModuleInventorySteps {

    @LocalServerPort
    private var port: Int = 0

    private var response: Response? = null
    private var createdWarehouseId: String? = null
    private var testProductId: UUID = UUID.randomUUID()

    @Before
    fun setup() {
        RestAssured.port = port
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

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/admin/inventory/warehouses")

        if (response!!.statusCode == 201) {
            createdWarehouseId = response!!.jsonPath().getString("id")
        }
    }

    @Then("the warehouse should be created successfully")
    fun warehouseCreatedSuccessfully() {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 201 but got ${response!!.statusCode}: ${response!!.body.asString()}")
            .isEqualTo(201)
        assertThat(createdWarehouseId).isNotNull()
    }

    @Then("the warehouse name should be {string}")
    fun warehouseNameShouldBe(expectedName: String) {
        assertThat(response!!.jsonPath().getString("name")).isEqualTo(expectedName)
    }

    @Then("the warehouse country code should be {string}")
    fun warehouseCountryCodeShouldBe(expectedCode: String) {
        assertThat(response!!.jsonPath().getString("countryCode")).isEqualTo(expectedCode)
    }

    @Then("the warehouse should have an address")
    fun warehouseShouldHaveAddress() {
        assertThat(response!!.jsonPath().getString("address")).isNotNull()
        assertThat(response!!.jsonPath().getString("address.street")).isNotNull()
    }

    @Given("a warehouse {string} exists in country {string}")
    fun warehouseExistsInCountry(name: String, countryCode: String) {
        val request = WarehouseCreateRequest(
            name = name,
            countryCode = countryCode,
            address = null
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/admin/inventory/warehouses")

        createdWarehouseId = response!!.jsonPath().getString("id")
    }

    @When("I retrieve the warehouse by ID")
    fun retrieveWarehouseById() {
        response = RestAssured.get("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
    }

    @Then("I should receive the warehouse details")
    fun shouldReceiveWarehouseDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.jsonPath().getString("id")).isEqualTo(createdWarehouseId)
    }

    @When("I update the warehouse name to {string}")
    fun updateWarehouseName(newName: String) {
        val request = WarehouseUpdateRequest(
            name = newName,
            countryCode = null,
            address = null
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .put("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
    }

    @Then("the warehouse update should succeed")
    fun warehouseUpdateShouldSucceed() {
        assertThat(response!!.statusCode).isEqualTo(200)
    }

    @When("I delete the warehouse")
    fun deleteWarehouse() {
        response = RestAssured.delete("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
    }

    @Then("the warehouse should be deleted successfully")
    fun warehouseDeletedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(204)
    }

    @Then("the warehouse should no longer exist")
    fun warehouseShouldNoLongerExist() {
        val getResponse = RestAssured.get("/api/v1/admin/inventory/warehouses/$createdWarehouseId")
        assertThat(getResponse.statusCode).isEqualTo(404)
    }

    @When("I list all warehouses")
    fun listAllWarehouses() {
        response = RestAssured.get("/api/v1/admin/inventory/warehouses")
    }

    @Then("I should receive a list of warehouses")
    fun shouldReceiveWarehouseList() {
        assertThat(response!!.statusCode).isEqualTo(200)
        val warehouses = response!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(warehouses).isNotNull()
    }

    @Then("the list should contain at least {int} warehouse")
    fun listShouldContainAtLeastWarehouses(minCount: Int) {
        val warehouses = response!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(warehouses.size).isGreaterThanOrEqualTo(minCount)
    }

    @When("I try to create another warehouse with name {string}")
    fun tryCreateDuplicateWarehouse(name: String) {
        val request = WarehouseCreateRequest(
            name = name,
            countryCode = "NL",
            address = null
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/admin/inventory/warehouses")
    }

    @Then("I should receive a duplicate name error")
    fun shouldReceiveDuplicateNameError() {
        assertThat(response!!.statusCode).isEqualTo(409)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Duplicate Warehouse Name")
    }

    @When("I try to create a warehouse with invalid country code {string}")
    fun tryCreateWarehouseWithInvalidCountryCode(countryCode: String) {
        val request = WarehouseCreateRequest(
            name = "Invalid Country Warehouse",
            countryCode = countryCode,
            address = null
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v1/admin/inventory/warehouses")
    }

    @Then("I should receive an invalid country code error")
    fun shouldReceiveInvalidCountryCodeError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Invalid Country Code")
    }

    @When("I add stock for a product with quantity {int}")
    fun addStockForProduct(quantity: Int) {
        val request = StockUpdateRequest(
            productId = ProductId(testProductId),
            quantityOnHand = quantity
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .put("/api/v1/admin/inventory/warehouses/$createdWarehouseId/stock")
    }

    @Then("the stock update should succeed")
    fun stockUpdateShouldSucceed() {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 200 but got ${response!!.statusCode}: ${response!!.body.asString()}")
            .isEqualTo(200)
    }

    @Then("the stock level should show quantity {int}")
    fun stockLevelShouldShow(expectedQuantity: Int) {
        val available = response!!.jsonPath().getInt("available")
        assertThat(available).isEqualTo(expectedQuantity)
    }

    @When("I get the warehouse stock")
    fun getWarehouseStock() {
        response = RestAssured.get("/api/v1/admin/inventory/warehouses/$createdWarehouseId/stock")
    }

    @Then("I should receive a list of stock levels")
    fun shouldReceiveStockLevelsList() {
        assertThat(response!!.statusCode).isEqualTo(200)
        val stockLevels = response!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(stockLevels).isNotNull()
    }

    @When("I check stock for the test product")
    fun checkStockForTestProduct() {
        response = RestAssured.get("/api/v1/inventory/$testProductId")
    }

    @Then("the total available stock should be {int}")
    fun totalAvailableStockShouldBe(expectedStock: Int) {
        assertThat(response!!.statusCode)
            .withFailMessage("Expected 200 but got ${response!!.statusCode}: ${response!!.body.asString()}")
            .isEqualTo(200)
        val available = response!!.jsonPath().getInt("available")
        assertThat(available).isEqualTo(expectedStock)
    }

    @When("I update stock quantity to {int}")
    fun updateStockQuantity(quantity: Int) {
        val request = StockUpdateRequest(
            productId = ProductId(testProductId),
            quantityOnHand = quantity
        )

        response = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(request)
            .put("/api/v1/admin/inventory/warehouses/$createdWarehouseId/stock")
    }
}
