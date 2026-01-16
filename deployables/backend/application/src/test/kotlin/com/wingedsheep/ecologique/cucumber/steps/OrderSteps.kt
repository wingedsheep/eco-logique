package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.OrderRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderLineCreateRequest
import com.wingedsheep.ecologique.products.api.ProductId
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.util.UUID

class OrderJourneySteps(
    private val context: ScenarioContext,
    private val api: TestApiClient
) {
    private var orderResponse: Response? = null
    private var ordersListResponse: Response? = null

    @When("I place an order with:")
    fun placeOrderWith(dataTable: DataTable) {
        val lines = dataTable.asMaps().map { row ->
            val productName = row["product"]!!
            val quantity = row["quantity"]!!.toInt()
            val product = context.getProduct(productName)
                ?: throw IllegalStateException("Product '$productName' not set up. Use 'the following products are available' first.")

            OrderLineCreateRequest(
                productId = ProductId(UUID.fromString(product.id)),
                productName = product.name,
                unitPrice = product.price,
                quantity = quantity
            )
        }

        val request = OrderCreateRequest(
            lines = lines,
            currency = Currency.EUR
        )

        orderResponse = api.post("/api/v1/orders", request)

        if (orderResponse!!.statusCode == 201) {
            context.storeOrder(
                id = orderResponse!!.jsonPath().getString("id"),
                ref = OrderRef(
                    id = orderResponse!!.jsonPath().getString("id"),
                    status = orderResponse!!.jsonPath().getString("status"),
                    grandTotal = BigDecimal(orderResponse!!.jsonPath().getString("grandTotal"))
                )
            )
        }
    }

    @Then("the order should be created with status {string}")
    fun orderCreatedWithStatus(expectedStatus: String) {
        assertThat(orderResponse!!.statusCode)
            .withFailMessage("Expected 201 but got ${orderResponse!!.statusCode}: ${orderResponse!!.body.asString()}")
            .isEqualTo(201)
        assertThat(orderResponse!!.jsonPath().getString("status")).isEqualTo(expectedStatus)
    }

    @Then("the order total should be {double} EUR")
    fun orderTotalShouldBe(expectedTotal: Double) {
        // Use orderResponse if available (from direct order API), otherwise use context
        val response = orderResponse ?: run {
            val order = context.getLatestOrder()
                ?: throw IllegalStateException("No order in context")
            api.get("/api/v1/orders/${order.id}")
        }
        // POST returns 201, GET returns 200
        assertThat(response.statusCode).isIn(200, 201)
        assertThat(response.jsonPath().getDouble("grandTotal")).isEqualTo(expectedTotal)
        assertThat(response.jsonPath().getString("currency")).isEqualTo("EUR")
    }

    @When("I view my order history")
    fun viewOrderHistory() {
        ordersListResponse = api.get("/api/v1/orders")
    }

    @Then("I should see {int} order(s) in my history")
    fun shouldSeeOrdersInHistory(expectedCount: Int) {
        assertThat(ordersListResponse!!.statusCode).isEqualTo(200)
        val orders = ordersListResponse!!.jsonPath().getList<Map<String, Any>>("")
        assertThat(orders).hasSizeGreaterThanOrEqualTo(expectedCount)
    }

    @Then("the order status should be {string}")
    fun orderStatusShouldBe(expectedStatus: String) {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders/${order.id}")
        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.jsonPath().getString("status")).isEqualTo(expectedStatus)
    }
}
