package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutRequest
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.OrderRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.PaymentRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.WarehouseRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.cucumber.TestResponse
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.math.BigDecimal
import java.util.UUID

class CheckoutSteps(
    private val context: ScenarioContext,
    private val api: TestApiClient,
    private val jwtDecoder: JwtDecoder
) {
    private var checkoutResponse: TestResponse? = null
    private var checkoutError: String? = null

    // Admin user ID for inventory management
    private val adminUserId = "22222222-2222-2222-2222-222222222222"

    // Default stock level for products (high enough for most test scenarios)
    private val defaultStockLevel = 1000

    // Test warehouse name
    private val testWarehouseName = "Test Warehouse"

    @Given("I have the following items in my cart:")
    fun addItemsToCart(dataTable: DataTable) {
        // First clear any existing cart
        api.delete("/api/v1/cart")

        dataTable.asMaps().forEach { row ->
            val productName = requireNotNull(row["product"]) { "product column required" }
            val quantity = requireNotNull(row["quantity"]) { "quantity column required" }.toInt()
            val product = context.getProduct(productName)
                ?: throw IllegalStateException("Product '$productName' not set up. Use 'the following products are available' first.")

            // Ensure stock exists for this product (with default high level)
            ensureStockExists(product.id, defaultStockLevel)

            val request = AddCartItemRequest(
                productId = ProductId(UUID.fromString(product.id)),
                quantity = quantity
            )

            val response = api.post("/api/v1/cart/items", request)
            assertThat(response.statusCode)
                .withFailMessage("Failed to add item to cart: ${response.bodyAsString()}")
                .isEqualTo(201)
        }
    }

    @Given("my cart is empty")
    fun ensureCartIsEmpty() {
        api.delete("/api/v1/cart")
    }

    @Given("the stock level for {string} is {int}")
    fun setStockLevelForProduct(productName: String, stockLevel: Int) {
        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not set up. Use 'the following products are available' first.")

        ensureStockExists(product.id, stockLevel)
    }

    @When("I checkout with a valid Visa card")
    fun checkoutWithValidVisa() {
        performCheckout("tok_visa", "4242", CardBrand.VISA)
    }

    @When("I checkout with a declined card")
    fun checkoutWithDeclinedCard() {
        performCheckout("tok_declined", "0002", CardBrand.VISA)
    }

    @When("I attempt to checkout with a valid Visa card")
    fun attemptCheckoutWithValidVisa() {
        performCheckout("tok_visa", "4242", CardBrand.VISA)
    }

    @Then("the checkout should succeed")
    fun checkoutShouldSucceed() {
        val response = requireResponse()
        assertThat(response.statusCode)
            .withFailMessage("Expected 201 but got ${response.statusCode}: ${response.bodyAsString()}")
            .isEqualTo(201)
    }

    @Then("the checkout should fail with {string}")
    fun checkoutShouldFailWith(expectedError: String) {
        val response = requireResponse()
        assertThat(response.statusCode)
            .withFailMessage("Expected error status but got ${response.statusCode}")
            .isNotEqualTo(201)
        assertThat(response.bodyAsString()).containsIgnoringCase(expectedError)
    }

    @Then("an order should be created with status {string}")
    fun orderCreatedWithStatus(expectedStatus: String) {
        val response = requireResponse()
        if (response.statusCode == 201) {
            val orderId = response.getString("orderId")
            assertThat(orderId).isNotNull()

            // Verify order status via orders API
            val orderResponse = api.get("/api/v1/orders/$orderId")
            assertThat(orderResponse.statusCode).isEqualTo(200)
            assertThat(orderResponse.getString("status")).isEqualTo(expectedStatus)
        }
    }

    @Then("an order should exist with status {string}")
    fun orderExistsWithStatus(expectedStatus: String) {
        // Try to get order ID from response or context
        val orderId = checkoutResponse?.getString("orderId")
            ?: context.getLatestOrder()?.id
            ?: throw IllegalStateException("No order found")

        val orderResponse = api.get("/api/v1/orders/$orderId")
        assertThat(orderResponse.statusCode).isEqualTo(200)
        assertThat(orderResponse.getString("status")).isEqualTo(expectedStatus)
    }

    @And("my cart should be empty")
    fun cartShouldBeEmpty() {
        val cartResponse = api.get("/api/v1/cart")
        assertThat(cartResponse.statusCode).isEqualTo(200)
        val items = cartResponse.getList<Map<String, Any>>("items")
        assertThat(items).isEmpty()
    }

    private fun requireResponse(): TestResponse =
        checkoutResponse ?: throw IllegalStateException("No checkout response - did you call a checkout step?")

    private fun performCheckout(token: String, last4: String, brand: CardBrand) {
        val request = CheckoutRequest(
            cardToken = token,
            cardLast4 = last4,
            cardBrand = brand
        )

        val response = api.post("/api/v1/checkout", request)
        checkoutResponse = response

        val responseBody = response.bodyAsString()

        if (response.statusCode == 201) {
            // Value classes are serialized as plain strings, not objects
            val orderId = response.getString("orderId")
                ?: throw IllegalStateException("Checkout succeeded but orderId is null. Response: $responseBody")
            val orderStatus = response.getString("orderStatus") ?: "UNKNOWN"
            val paymentId = response.getString("paymentId")
            val paymentStatus = response.getString("paymentStatus") ?: "UNKNOWN"

            // Fetch order details to get the total
            val orderResponse = api.get("/api/v1/orders/$orderId")
            val grandTotal = if (orderResponse.statusCode == 200) {
                BigDecimal(orderResponse.getString("grandTotal")!!)
            } else {
                BigDecimal.ZERO
            }

            context.storeOrder(
                id = orderId,
                ref = OrderRef(
                    id = orderId,
                    status = orderStatus,
                    grandTotal = grandTotal
                )
            )

            if (paymentId != null) {
                context.storePayment(
                    orderId = orderId,
                    ref = PaymentRef(
                        id = paymentId,
                        orderId = orderId,
                        status = paymentStatus,
                        amount = grandTotal,
                        paymentMethodSummary = ""
                    )
                )
            }
        } else {
            checkoutError = responseBody

            // For payment failures, the order was created - extract orderId from error response
            val orderId = response.getString("orderId")
            if (orderId != null) {
                val orderResponse = api.get("/api/v1/orders/$orderId")
                if (orderResponse.statusCode == 200) {
                    context.storeOrder(
                        id = orderId,
                        ref = OrderRef(
                            id = orderId,
                            status = orderResponse.getString("status") ?: "UNKNOWN",
                            grandTotal = BigDecimal(orderResponse.getString("grandTotal")!!)
                        )
                    )
                }
            }
        }
    }

    private fun ensureStockExists(productId: String, quantity: Int) {
        val originalToken = context.authToken
        val originalUserId = context.currentUserId

        try {
            authenticateAsAdmin()

            val warehouse = ensureTestWarehouseExists()

            val request = StockUpdateRequest(
                productId = ProductId(UUID.fromString(productId)),
                quantityOnHand = quantity
            )

            val response = api.put("/api/v1/admin/inventory/warehouses/${warehouse.id}/stock", request)
            assertThat(response.statusCode)
                .withFailMessage("Failed to update stock: ${response.bodyAsString()}")
                .isEqualTo(200)
        } finally {
            context.authToken = originalToken
            context.currentUserId = originalUserId
        }
    }

    private fun ensureTestWarehouseExists(): WarehouseRef {
        // Check if we already have a test warehouse in context
        context.getWarehouse(testWarehouseName)?.let { return it }

        // Try to find existing warehouse
        val listResponse = api.get("/api/v1/admin/inventory/warehouses")
        if (listResponse.statusCode == 200) {
            val warehouses = listResponse.getList<Map<String, Any>>("")
            val existing = warehouses.find { it["name"] == testWarehouseName }
            if (existing != null) {
                val ref = WarehouseRef(
                    id = existing["id"] as String,
                    name = testWarehouseName,
                    countryCode = existing["countryCode"] as String
                )
                context.storeWarehouse(testWarehouseName, ref)
                return ref
            }
        }

        // Create new warehouse
        val request = WarehouseCreateRequest(
            name = testWarehouseName,
            countryCode = "NL",
            address = null
        )

        val response = api.post("/api/v1/admin/inventory/warehouses", request)
        assertThat(response.statusCode)
            .withFailMessage("Failed to create test warehouse: ${response.bodyAsString()}")
            .isEqualTo(201)

        val ref = WarehouseRef(
            id = response.getString("id")!!,
            name = testWarehouseName,
            countryCode = "NL"
        )
        context.storeWarehouse(testWarehouseName, ref)
        return ref
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
}
