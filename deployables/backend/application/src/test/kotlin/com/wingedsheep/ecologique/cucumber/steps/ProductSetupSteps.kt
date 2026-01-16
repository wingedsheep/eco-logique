package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.ProductRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.math.BigDecimal

class ProductSetupSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient,
    private val jwtDecoder: JwtDecoder
) {
    // Admin user ID for creating test fixtures
    private val adminUserId = "22222222-2222-2222-2222-222222222222"

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

        // Temporarily switch to admin auth for creating products
        val originalToken = context.authToken
        val originalUserId = context.currentUserId

        try {
            authenticateAsAdmin()

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
                    id = response.getString("id")!!,
                    name = name,
                    price = price
                )
                409 -> findExistingProduct(name)
                    ?: throw IllegalStateException("Product '$name' duplicate but not found")
                else -> throw IllegalStateException(
                    "Failed to create product '$name': ${response.statusCode} - ${response.bodyAsString()}"
                )
            }

            context.storeProduct(name, productRef)
        } finally {
            // Restore original auth
            context.authToken = originalToken
            context.currentUserId = originalUserId
        }
    }

    private fun authenticateAsAdmin() {
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

    private fun findExistingProduct(name: String): ProductRef? {
        // Use admin auth to list products too
        val originalToken = context.authToken
        val originalUserId = context.currentUserId

        try {
            authenticateAsAdmin()

            val response = api.get("/api/v1/products")
            if (response.statusCode != 200) return null

            val products = response.getList<Map<String, Any>>("")
            val product = products.find { it["name"] == name } ?: return null

            return ProductRef(
                id = product["id"] as String,
                name = product["name"] as String,
                price = BigDecimal(product["priceAmount"].toString())
            )
        } finally {
            context.authToken = originalToken
            context.currentUserId = originalUserId
        }
    }
}
