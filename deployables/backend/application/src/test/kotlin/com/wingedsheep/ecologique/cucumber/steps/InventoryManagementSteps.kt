package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.WarehouseRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.inventory.api.dto.AddressDto
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.inventory.impl.MockInventoryService
import com.wingedsheep.ecologique.products.api.ProductId
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID

class InventoryManagementSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient,
    private val mockInventoryService: MockInventoryService
) {
    private var warehouseResponse: Response? = null
    private var stockResponse: Response? = null
    private var warehouseListResponse: Response? = null
    private var inventoryCheckResponse: Response? = null

    @When("I create a warehouse with the following details:")
    fun createWarehouse(dataTable: DataTable) {
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

        warehouseResponse = api.post("/api/v1/admin/inventory/warehouses", request)

        if (warehouseResponse!!.statusCode == 201) {
            val id = warehouseResponse!!.jsonPath().getString("id")
            val name = warehouseResponse!!.jsonPath().getString("name")
            val countryCode = warehouseResponse!!.jsonPath().getString("countryCode")

            context.storeWarehouse(
                name = name,
                ref = WarehouseRef(
                    id = id,
                    name = name,
                    countryCode = countryCode
                )
            )
        }
    }

    @Then("the warehouse should be created successfully")
    fun warehouseShouldBeCreated() {
        assertThat(warehouseResponse!!.statusCode)
            .withFailMessage("Expected 201 but got ${warehouseResponse!!.statusCode}: ${warehouseResponse!!.body.asString()}")
            .isEqualTo(201)
    }

    @Then("the warehouse {string} should exist")
    fun warehouseShouldExist(warehouseName: String) {
        val warehouse = context.getWarehouse(warehouseName)
            ?: throw IllegalStateException("Warehouse '$warehouseName' not found in context")

        val response = api.get("/api/v1/admin/inventory/warehouses/${warehouse.id}")
        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.jsonPath().getString("name")).isEqualTo(warehouseName)
    }

    @When("I add stock for product {string} to warehouse {string} with quantity {int}")
    fun addStockToWarehouse(productName: String, warehouseName: String, quantity: Int) {
        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context")
        val warehouse = context.getWarehouse(warehouseName)
            ?: throw IllegalStateException("Warehouse '$warehouseName' not found in context")

        val request = StockUpdateRequest(
            productId = ProductId(UUID.fromString(product.id)),
            quantityOnHand = quantity
        )

        stockResponse = api.put("/api/v1/admin/inventory/warehouses/${warehouse.id}/stock", request)

        // Also update the mock inventory service so checkout works
        if (stockResponse!!.statusCode == 200) {
            mockInventoryService.setStockLevel(
                ProductId(UUID.fromString(product.id)),
                quantity
            )
        }
    }

    @Then("the stock update should succeed")
    fun stockUpdateShouldSucceed() {
        assertThat(stockResponse!!.statusCode)
            .withFailMessage("Expected 200 but got ${stockResponse!!.statusCode}: ${stockResponse!!.body.asString()}")
            .isEqualTo(200)
    }

    @Then("the stock level for {string} in {string} should be {int}")
    fun stockLevelShouldBe(productName: String, warehouseName: String, expectedQuantity: Int) {
        val warehouse = context.getWarehouse(warehouseName)
            ?: throw IllegalStateException("Warehouse '$warehouseName' not found in context")

        val response = api.get("/api/v1/admin/inventory/warehouses/${warehouse.id}/stock")
        assertThat(response.statusCode).isEqualTo(200)

        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context")

        val stockLevels = response.jsonPath().getList<Map<String, Any>>("")
        val productStock = stockLevels.find {
            it["productId"].toString() == product.id
        }

        assertThat(productStock)
            .withFailMessage("No stock entry found for product '$productName' in warehouse '$warehouseName'")
            .isNotNull

        val available = (productStock!!["available"] as Number).toInt()
        assertThat(available).isEqualTo(expectedQuantity)
    }

    @When("I list all warehouses")
    fun listAllWarehouses() {
        warehouseListResponse = api.get("/api/v1/admin/inventory/warehouses")
    }

    @Then("I should see at least {int} warehouse(s)")
    fun shouldSeeAtLeastWarehouses(minCount: Int) {
        assertThat(warehouseListResponse!!.statusCode).isEqualTo(200)
        val warehouses = warehouseListResponse!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(warehouses.size).isGreaterThanOrEqualTo(minCount)
    }

    @When("I check the stock availability for {string}")
    fun checkStockAvailability(productName: String) {
        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context")

        inventoryCheckResponse = api.get("/api/v1/inventory/${product.id}")
    }

    @Then("the total available stock should be {int}")
    fun totalAvailableStockShouldBe(expectedStock: Int) {
        assertThat(inventoryCheckResponse!!.statusCode)
            .withFailMessage("Expected 200 but got ${inventoryCheckResponse!!.statusCode}: ${inventoryCheckResponse!!.body.asString()}")
            .isEqualTo(200)

        val available = inventoryCheckResponse!!.jsonPath().getInt("available")
        assertThat(available).isEqualTo(expectedStock)
    }
}
