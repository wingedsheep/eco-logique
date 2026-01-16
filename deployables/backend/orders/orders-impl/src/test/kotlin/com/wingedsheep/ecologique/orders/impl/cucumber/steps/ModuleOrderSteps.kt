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
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.math.BigDecimal
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

    private var response: Response? = null
    private var currentSubject: String = ""
    private var createdOrderId: UUID? = null
    private var lastServiceResult: Result<*, *>? = null
    private lateinit var authenticatedRequest: RequestSpecification

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/orders"
        reset(productService)
        lastServiceResult = null
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

        authenticatedRequest = RestAssured.given()
            .header("Authorization", "Bearer $tokenValue")
            .contentType(ContentType.JSON)
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

        response = authenticatedRequest
            .body(request)
            .post()

        val resp = response
        if (resp != null && resp.statusCode == 201) {
            createdOrderId = UUID.fromString(resp.jsonPath().getString("id"))
        }
    }

    @When("I try to create an order with no items")
    fun tryCreateOrderWithNoItems() {
        val request = mapOf(
            "lines" to emptyList<Any>(),
            "currency" to "EUR"
        )

        response = authenticatedRequest
            .body(request)
            .post()
    }

    @When("I retrieve the order by ID")
    fun retrieveOrderById() {
        response = authenticatedRequest.get("/$createdOrderId")
    }

    @When("I try to retrieve the order by ID")
    fun tryRetrieveOrderById() {
        response = authenticatedRequest.get("/$createdOrderId")
    }

    @When("I retrieve order with ID {string}")
    fun retrieveOrderWithId(orderId: String) {
        response = authenticatedRequest.get("/$orderId")
    }

    @When("I list my orders")
    fun listMyOrders() {
        response = authenticatedRequest.get()
    }

    @When("the order status is updated to {string}")
    fun updateOrderStatus(newStatus: String) {
        val orderId = createdOrderId ?: error("No order created yet")
        lastServiceResult = orderService.updateStatus(OrderId(orderId), OrderStatus.valueOf(newStatus))
        lastServiceResult?.onSuccess {
            response = authenticatedRequest.get("/$orderId")
        }
    }

    @Then("the order should be created successfully")
    fun orderCreatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
        assertThat(createdOrderId).isNotNull()
    }

    @Then("the order status should be {string}")
    fun orderStatusShouldBe(expectedStatus: String) {
        val status = response!!.jsonPath().getString("status")
        assertThat(status).isEqualTo(expectedStatus)
    }

    @Then("the order grand total should be {double} EUR")
    fun orderGrandTotalShouldBe(expectedTotal: Double) {
        val grandTotal = response!!.jsonPath().getDouble("grandTotal")
        assertThat(grandTotal).isEqualTo(expectedTotal)
    }

    @Then("the order should have {int} lines")
    fun orderShouldHaveLines(expectedCount: Int) {
        val lines = response!!.jsonPath().getList<Map<String, Any>>("lines")
        assertThat(lines).hasSize(expectedCount)
    }

    @Then("I should receive the order details")
    fun receiveOrderDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.jsonPath().getString("id")).isEqualTo(createdOrderId.toString())
    }

    @Then("the order user should match")
    fun orderUserShouldMatch() {
        assertThat(response!!.jsonPath().getString("userId")).isEqualTo(currentSubject)
    }

    @Then("I should receive {int} orders")
    fun shouldReceiveOrders(expectedCount: Int) {
        assertThat(response!!.statusCode).isEqualTo(200)
        val orders = response!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(orders).hasSize(expectedCount)
    }

    @Then("I should receive an access denied error")
    fun shouldReceiveAccessDeniedError() {
        assertThat(response!!.statusCode).isEqualTo(403)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Access Denied")
    }

    @Then("I should receive a validation error")
    fun shouldReceiveValidationError() {
        assertThat(response!!.statusCode).isEqualTo(400)
    }

    @Then("I should receive a product not found error")
    fun shouldReceiveProductNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Product Not Found")
    }

    @Then("I should receive a not found error")
    fun shouldReceiveNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Order Not Found")
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
        val lines = response!!.jsonPath().getList<Map<String, Any>>("lines")
        assertThat(lines).isNotEmpty()
        val firstLineUnitPrice = response!!.jsonPath().getDouble("lines[0].unitPrice")
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
}
