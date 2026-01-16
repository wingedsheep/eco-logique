package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.WarehouseRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.inventory.api.dto.AddressDto
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.cucumber.TestResponse
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID

class InventoryManagementSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient
) {
    private var warehouseResponse: TestResponse? = null
    private var stockResponse: TestResponse? = null
    private var warehouseListResponse: TestResponse? = null
    private var inventoryCheckResponse: TestResponse? = null

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
            val id = warehouseResponse!!.getString("id")!!
            val name = warehouseResponse!!.getString("name")!!
            val countryCode = warehouseResponse!!.getString("countryCode")!!

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
            .withFailMessage("Expected 201 but got ${warehouseResponse!!.statusCode}: ${warehouseResponse!!.bodyAsString()}")
            .isEqualTo(201)
    }

    @Then("the warehouse {string} should exist")
    fun warehouseShouldExist(warehouseName: String) {
        val warehouse = context.getWarehouse(warehouseName)
            ?: throw IllegalStateException("Warehouse '$warehouseName' not found in context")

        val response = api.get("/api/v1/admin/inventory/warehouses/${warehouse.id}")
        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.getString("name")).isEqualTo(warehouseName)
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
    }

    @Then("the stock update should succeed")
    fun stockUpdateShouldSucceed() {
        assertThat(stockResponse!!.statusCode)
            .withFailMessage("Expected 200 but got ${stockResponse!!.statusCode}: ${stockResponse!!.bodyAsString()}")
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

        val stockLevels = response.getList<Map<String, Any>>("")
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
        val warehouses = warehouseListResponse!!.getList<Map<String, Any>>("")
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
            .withFailMessage("Expected 200 but got ${inventoryCheckResponse!!.statusCode}: ${inventoryCheckResponse!!.bodyAsString()}")
            .isEqualTo(200)

        val available = inventoryCheckResponse!!.getInt("available")
        assertThat(available).isEqualTo(expectedStock)
    }
}
