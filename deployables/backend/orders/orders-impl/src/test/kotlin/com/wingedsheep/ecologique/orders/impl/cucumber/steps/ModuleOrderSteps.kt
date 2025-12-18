package com.wingedsheep.ecologique.orders.impl.cucumber.steps

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.orders.api.error.OrderError
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
    private var createdOrderId: String? = null
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
            val productId = row["productId"]!!
            val productName = row["productName"]!!
            whenever(productService.getProduct(productId))
                .thenReturn(Result.ok(buildProductDto(id = productId, name = productName)))
        }
    }

    @Given("no products exist in the catalog")
    fun noProductsExist() {
        whenever(productService.getProduct(any()))
            .thenReturn(Result.err(ProductError.NotFound("unknown")))
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
            OrderLineCreateRequest(
                productId = row["productId"]!!,
                productName = row["productName"]!!,
                unitPrice = BigDecimal(row["unitPrice"]!!),
                quantity = row["quantity"]!!.toInt()
            )
        }

        val lineTotal = lines.sumOf { it.unitPrice.multiply(BigDecimal(it.quantity)) }

        val request = OrderCreateRequest(
            lines = lines,
            subtotal = lineTotal,
            grandTotal = lineTotal,
            currency = "EUR"
        )

        response = authenticatedRequest
            .body(request)
            .post()

        if (response!!.statusCode == 201) {
            createdOrderId = response!!.jsonPath().getString("id")
        }
    }

    @When("I try to create an order with no items")
    fun tryCreateOrderWithNoItems() {
        val request = mapOf(
            "lines" to emptyList<Any>(),
            "subtotal" to 0,
            "grandTotal" to 0,
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
        lastServiceResult = orderService.updateStatus(createdOrderId!!, newStatus)
        lastServiceResult?.onSuccess {
            response = authenticatedRequest.get("/$createdOrderId")
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
        assertThat(response!!.jsonPath().getString("id")).isEqualTo(createdOrderId)
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

    private fun createOrderForUser(userId: String) {
        whenever(productService.getProduct("PROD-TEST"))
            .thenReturn(Result.ok(buildProductDto(id = "PROD-TEST", name = "Test Product")))

        val request = OrderCreateRequest(
            lines = listOf(
                OrderLineCreateRequest(
                    productId = "PROD-TEST",
                    productName = "Test Product",
                    unitPrice = BigDecimal("19.99"),
                    quantity = 1
                )
            ),
            subtotal = BigDecimal("19.99"),
            grandTotal = BigDecimal("19.99"),
            currency = "EUR"
        )

        orderService.createOrder(userId, request).fold(
            onSuccess = { order -> createdOrderId = order.id },
            onFailure = { }
        )
    }

    private fun transitionToStatus(targetStatus: String) {
        val statusPath = when (targetStatus) {
            "RESERVED" -> listOf("RESERVED")
            "PAYMENT_PENDING" -> listOf("RESERVED", "PAYMENT_PENDING")
            "PAID" -> listOf("RESERVED", "PAYMENT_PENDING", "PAID")
            "SHIPPED" -> listOf("RESERVED", "PAYMENT_PENDING", "PAID", "SHIPPED")
            "DELIVERED" -> listOf("RESERVED", "PAYMENT_PENDING", "PAID", "SHIPPED", "DELIVERED")
            else -> emptyList()
        }
        statusPath.forEach { status ->
            orderService.updateStatus(createdOrderId!!, status)
        }
    }
}
