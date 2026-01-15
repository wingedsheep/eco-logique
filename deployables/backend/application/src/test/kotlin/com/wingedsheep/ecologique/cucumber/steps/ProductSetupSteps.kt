package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.ProductRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import java.math.BigDecimal

class ProductSetupSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient
) {
    @Given("the following products are available:")
    fun productsAreAvailable(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val name = row["name"]!!
            val price = BigDecimal(row["price"]!!)
            val category = ProductCategory.valueOf(row["category"]!!)

            ensureProductExists(name, price, category)
        }
    }

    @Given("a product {string} exists with price {double} EUR")
    fun productExistsWithPrice(name: String, price: Double) {
        ensureProductExists(name, BigDecimal.valueOf(price), ProductCategory.HOUSEHOLD)
    }

    private fun ensureProductExists(name: String, price: BigDecimal, category: ProductCategory) {
        findExistingProduct(name)?.let { existing ->
            context.storeProduct(name, ProductRef(existing.id, existing.name, price))
            return
        }

        val request = ProductCreateRequest(
            name = name,
            description = "Test product",
            category = category,
            priceAmount = price,
            priceCurrency = Currency.EUR,
            weightGrams = 100,
            carbonFootprintKg = BigDecimal("1.0")
        )

        val response = api.post("/api/v1/products", request)

        val productRef = when (response.statusCode) {
            201 -> ProductRef(
                id = response.jsonPath().getString("id"),
                name = name,
                price = price
            )
            409 -> findExistingProduct(name)
                ?: throw IllegalStateException("Product '$name' duplicate but not found")
            else -> throw IllegalStateException(
                "Failed to create product '$name': ${response.statusCode} - ${response.body.asString()}"
            )
        }

        context.storeProduct(name, productRef)
    }

    private fun findExistingProduct(name: String): ProductRef? {
        val response = api.get("/api/v1/products")
        if (response.statusCode != 200) return null

        val products = response.jsonPath().getList<Map<String, Any>>("")
        val product = products.find { it["name"] == name } ?: return null

        return ProductRef(
            id = product["id"] as String,
            name = product["name"] as String,
            price = BigDecimal(product["priceAmount"].toString())
        )
    }
}
