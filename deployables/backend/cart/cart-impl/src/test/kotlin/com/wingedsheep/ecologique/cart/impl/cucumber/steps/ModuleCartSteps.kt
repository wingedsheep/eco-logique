package com.wingedsheep.ecologique.cart.impl.cucumber.steps

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.buildProductDto
import com.wingedsheep.ecologique.products.api.error.ProductError
import com.wingedsheep.ecologique.users.api.UserId
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

class ModuleCartSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jwtDecoder: JwtDecoder

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var cartService: CartService

    private lateinit var client: WebTestClient
    private val objectMapper = jacksonObjectMapper()

    private var response: TestResponse? = null
    private var currentUserId: UserId = UserId.generate()
    private var authToken: String? = null

    @Before
    fun setup() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port/api/v1/cart")
            .responseTimeout(Duration.ofSeconds(30))
            .build()
        reset(productService)
    }

    @Given("the cart module is running")
    fun cartModuleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @Given("I am authenticated as cart user {string}")
    fun authenticatedAs(subject: String) {
        currentUserId = UserId(UUID.fromString(subject))
        val tokenValue = "test-token-$subject"
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", subject)
            .build()

        whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)
        authToken = tokenValue
    }

    @Given("the following products are available for cart:")
    fun productsAvailableForCart(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val productId = ProductId(UUID.fromString(row["productId"]!!))
            val productName = row["productName"]!!
            val price = BigDecimal(row["price"]!!)
            whenever(productService.getProduct(productId))
                .thenReturn(Result.ok(buildProductDto(id = productId, name = productName, priceAmount = price)))
        }
    }

    @Given("no products are available")
    fun noProductsAvailable() {
        whenever(productService.getProduct(any()))
            .thenReturn(Result.err(ProductError.NotFound(ProductId.generate())))
    }

    @Given("my cart is empty")
    fun cartIsEmpty() {
        cartService.clearCart(currentUserId)
    }

    @Given("my cart has the following items:")
    fun cartHasItems(dataTable: DataTable) {
        cartService.clearCart(currentUserId)
        dataTable.asMaps().forEach { row ->
            val productUuid = UUID.fromString(row["productId"]!!)
            val productId = ProductId(productUuid)
            val productName = row["productName"]!!
            val price = BigDecimal(row["price"]!!)
            val quantity = row["quantity"]!!.toInt()

            whenever(productService.getProduct(productId))
                .thenReturn(Result.ok(buildProductDto(id = productId, name = productName, priceAmount = price)))

            cartService.addItem(currentUserId, AddCartItemRequest(productId, quantity))
        }
    }

    @When("I view my cart")
    fun viewMyCart() {
        response = get("")
    }

    @When("I add {int} of product {string} to my cart")
    fun addProductToCart(quantity: Int, productIdStr: String) {
        val productId = ProductId(UUID.fromString(productIdStr))
        val request = AddCartItemRequest(productId, quantity)
        response = post("/items", request)
    }

    @When("I update the quantity of {string} to {int}")
    fun updateQuantity(productId: String, quantity: Int) {
        val request = UpdateCartItemRequest(quantity)
        response = put("/items/$productId", request)
    }

    @When("I remove {string} from my cart")
    fun removeFromCart(productId: String) {
        response = delete("/items/$productId")
    }

    @When("I clear my cart")
    fun clearMyCart() {
        response = delete("")
    }

    @Then("my cart should be empty")
    fun cartShouldBeEmpty() {
        assertThat(response!!.statusCode).isIn(200, 204)
        if (response!!.statusCode == 200) {
            val items = response!!.getList<Map<String, Any>>("items")
            assertThat(items).isEmpty()
        }
    }

    @Then("my cart should have {int} item\\(s)")
    fun cartShouldHaveItems(expectedCount: Int) {
        ensureCartFetched()
        assertThat(response!!.statusCode).isEqualTo(200)
        val items = response!!.getList<Map<String, Any>>("items")
        assertThat(items).hasSize(expectedCount)
    }

    @Then("the cart subtotal should be {double}")
    fun cartSubtotalShouldBe(expectedSubtotal: Double) {
        ensureCartFetched()
        val subtotal = response!!.getDouble("subtotal")
        assertThat(subtotal).isEqualTo(expectedSubtotal)
    }

    @Then("the cart total items should be {int}")
    fun cartTotalItemsShouldBe(expectedTotal: Int) {
        ensureCartFetched()
        val totalItems = response!!.getInt("totalItems")
        assertThat(totalItems).isEqualTo(expectedTotal)
    }

    @Then("the item {string} should have quantity {int}")
    fun itemShouldHaveQuantity(productId: String, expectedQuantity: Int) {
        ensureCartFetched()
        val items = response!!.getList<Map<String, Any>>("items")
        val item = items.find { it["productId"] == productId }
        assertThat(item).isNotNull
        assertThat(item!!["quantity"]).isEqualTo(expectedQuantity)
    }

    private fun ensureCartFetched() {
        if (response!!.statusCode != 200) {
            response = get("")
        }
    }

    @Then("I should receive a product not found error for cart")
    fun shouldReceiveProductNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.getString("title")).isEqualTo("Product Not Found")
    }

    @Then("I should receive an item not found error")
    fun shouldReceiveItemNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
        assertThat(response!!.getString("title")).isEqualTo("Item Not Found")
    }

    @Then("the item should be added successfully")
    fun itemAddedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
    }

    @Then("the cart should be updated successfully")
    fun cartUpdatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(200)
    }

    // Helper methods
    private fun get(path: String): TestResponse {
        val result = client.get()
            .uri(path)
            .headers { headers -> authToken?.let { headers.setBearerAuth(it) } }
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun post(path: String, body: Any): TestResponse {
        val result = client.post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .headers { headers -> authToken?.let { headers.setBearerAuth(it) } }
            .bodyValue(body)
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun put(path: String, body: Any): TestResponse {
        val result = client.put()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .headers { headers -> authToken?.let { headers.setBearerAuth(it) } }
            .bodyValue(body)
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun delete(path: String): TestResponse {
        val result = client.delete()
            .uri(path)
            .headers { headers -> authToken?.let { headers.setBearerAuth(it) } }
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    class TestResponse(
        val statusCode: Int,
        private val body: String,
        private val objectMapper: ObjectMapper
    ) {
        fun getString(path: String): String? = getValueAtPath(path) as? String
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
