package com.wingedsheep.ecologique.orders.impl.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.error.OrderError
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.buildProductDto
import com.wingedsheep.ecologique.products.api.error.ProductError
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
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

class ModuleOrderSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jwtDecoder: JwtDecoder

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var orderService: OrderService

    private lateinit var client: WebTestClient
    private val objectMapper = ObjectMapper()

    private var response: TestResponse? = null
    private var currentSubject: String = ""
    private var createdOrderId: UUID? = null
    private var lastServiceResult: Result<*, *>? = null
    private var authToken: String? = null

    @Before
    fun setup() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port/api/v1/orders")
            .responseTimeout(Duration.ofSeconds(10))
            .build()
        reset(productService)
        lastServiceResult = null
        authToken = null
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @Given("I am authenticated as {string}")
    fun authenticatedAs(subject: String) {
        currentSubject = subject
        val tokenValue = "test-token-$subject"
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", subject)
            .build()

        whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)

        authToken = tokenValue
    }

    @Given("the following products exist in the catalog:")
    fun productsExistInCatalog(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val productId = ProductId(UUID.fromString(row["productId"]!!))
            val productName = row["productName"]!!
            whenever(productService.getProduct(productId))
                .thenReturn(Result.ok(buildProductDto(id = productId, name = productName)))
        }
    }

    @Given("no products exist in the catalog")
    fun noProductsExist() {
        whenever(productService.getProduct(any()))
            .thenReturn(Result.err(ProductError.NotFound(ProductId.generate())))
    }

    @Given("an order exists for the current user")
    fun orderExistsForCurrentUser() {
        createOrderForUser(currentSubject)
    }

    @Given("another order exists for the current user")
    fun anotherOrderExistsForCurrentUser() {
        createOrderForUser(currentSubject)
    }

    @Given("an order exists for the current user with status {string}")
    fun orderExistsWithStatus(status: String) {
        createOrderForUser(currentSubject)
        if (status != "CREATED") {
            transitionToStatus(status)
        }
    }

    @When("I create an order with the following items:")
    fun createOrderWithItems(dataTable: DataTable) {
        val lines = dataTable.asMaps().map { row ->
            mapOf(
                "productId" to row["productId"],
                "productName" to row["productName"],
                "unitPrice" to row["unitPrice"],
                "quantity" to row["quantity"]?.toInt()
            )
        }

        val request = mapOf(
            "lines" to lines,
            "currency" to "EUR"
        )

        response = post("", request)

        val resp = response
        if (resp != null && resp.statusCode == 201) {
            createdOrderId = UUID.fromString(resp.getString("id"))
        }
    }

    @When("I try to create an order with no items")
    fun tryCreateOrderWithNoItems() {
        val request = mapOf(
            "lines" to emptyList<Any>(),
            "currency" to "EUR"
        )

        response = post("", request)
    }

    @When("I retrieve the order by ID")
    fun retrieveOrderById() {
        response = get("/$createdOrderId")
    }

    @When("I try to retrieve the order by ID")
    fun tryRetrieveOrderById() {
        response = get("/$createdOrderId")
    }

    @When("I retrieve order with ID {string}")
    fun retrieveOrderWithId(orderId: String) {
        response = get("/$orderId")
    }

    @When("I list my orders")
    fun listMyOrders() {
        response = get("")
    }

    @When("the order status is updated to {string}")
    fun updateOrderStatus(newStatus: String) {
        val orderId = createdOrderId ?: error("No order created yet")
        lastServiceResult = orderService.updateStatus(OrderId(orderId), OrderStatus.valueOf(newStatus))
        lastServiceResult?.onSuccess {
            response = get("/$orderId")
        }
    }

    @Then("the order should be created successfully")
    fun orderCreatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
        assertThat(createdOrderId).isNotNull()
    }

    @Then("the order status should be {string}")
    fun orderStatusShouldBe(expectedStatus: String) {
        val status = response!!.getString("status")
        assertThat(status).isEqualTo(expectedStatus)
    }

    @Then("the order grand total should be {double} EUR")
    fun orderGrandTotalShouldBe(expectedTotal: Double) {
        val grandTotal = response!!.getDouble("grandTotal")
        assertThat(grandTotal).isEqualTo(expectedTotal)
    }

    @Then("the order should have {int} lines")
    fun orderShouldHaveLines(expectedCount: Int) {
        val lines = response!!.getList<Map<String, Any>>("lines")
        assertThat(lines).hasSize(expectedCount)
    }

    @Then("I should receive the order details")
    fun receiveOrderDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.getString("id")).isEqualTo(createdOrderId.toString())
    }

    @Then("the order user should match")
    fun orderUserShouldMatch() {
        assertThat(response!!.getString("userId")).isEqualTo(currentSubject)
    }

    @Then("I should receive {int} orders")
    fun shouldReceiveOrders(expectedCount: Int) {
        assertThat(response!!.statusCode).isEqualTo(200)
        val orders = response!!.getList<Map<String, Any>>("")
        assertThat(orders).hasSize(expectedCount)
    }

    @Then("I should receive an access denied error")
    fun shouldReceiveAccessDeniedError() {
        assertThat(response!!.statusCode).isEqualTo(403)
        assertThat(response!!.getString("title")).isEqualTo("Access Denied")
    }

    @Then("I should receive a validation error")
    fun shouldReceiveValidationError() {
        assertThat(response!!.statusCode).isEqualTo(400)
    }

    @Then("I should receive a product not found error")
    fun shouldReceiveProductNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.getString("title")).isEqualTo("Product Not Found")
    }

    @Then("I should receive a not found error")
    fun shouldReceiveNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
        assertThat(response!!.getString("title")).isEqualTo("Order Not Found")
    }

    @Then("I should receive an invalid status error")
    fun shouldReceiveInvalidStatusError() {
        assertThat(lastServiceResult?.isErr).isTrue()
        lastServiceResult?.onFailure { error ->
            assertThat(error).isInstanceOf(OrderError.InvalidStatus::class.java)
        }
    }

    @Then("the order line should have unit price {double}")
    fun orderLineShouldHaveUnitPrice(expectedUnitPrice: Double) {
        val lines = response!!.getList<Map<String, Any>>("lines")
        assertThat(lines).isNotEmpty()
        val firstLineUnitPrice = response!!.getDouble("lines[0].unitPrice")
        assertThat(firstLineUnitPrice).isEqualTo(expectedUnitPrice)
    }

    private fun createOrderForUser(userId: String) {
        // Use the same fixed UUID that's set up in the feature file's product catalog
        val testProductId = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000099"))
        // Note: productService mock is already set up by the @Given step for products

        val request = OrderCreateRequest(
            lines = listOf(
                OrderLineCreateRequest(
                    productId = testProductId,
                    productName = "Test Product",
                    unitPrice = BigDecimal("19.99"),
                    quantity = 1
                )
            ),
            currency = Currency.EUR
        )

        orderService.createOrder(userId, request).fold(
            onSuccess = { order -> createdOrderId = order.id.value },
            onFailure = { }
        )
    }

    private fun transitionToStatus(targetStatus: String) {
        val orderId = createdOrderId ?: error("No order created yet")
        val statusPath = when (targetStatus) {
            "RESERVED" -> listOf(OrderStatus.RESERVED)
            "PAYMENT_PENDING" -> listOf(OrderStatus.RESERVED, OrderStatus.PAYMENT_PENDING)
            "PAID" -> listOf(OrderStatus.RESERVED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAID)
            "SHIPPED" -> listOf(OrderStatus.RESERVED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, OrderStatus.SHIPPED)
            "DELIVERED" -> listOf(OrderStatus.RESERVED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED)
            else -> emptyList()
        }
        statusPath.forEach { status ->
            orderService.updateStatus(OrderId(orderId), status)
        }
    }

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
                // Handle array index notation like "lines[0]"
                val arrayMatch = Regex("""(\w+)\[(\d+)]""").matchEntire(segment)
                if (arrayMatch != null) {
                    val key = arrayMatch.groupValues[1]
                    val index = arrayMatch.groupValues[2].toInt()
                    current = when (current) {
                        is Map<*, *> -> (current[key] as? List<*>)?.getOrNull(index)
                        else -> return null
                    }
                } else {
                    current = when (current) {
                        is Map<*, *> -> current[segment]
                        is List<*> -> current.getOrNull(segment.toIntOrNull() ?: return null)
                        else -> return null
                    }
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
