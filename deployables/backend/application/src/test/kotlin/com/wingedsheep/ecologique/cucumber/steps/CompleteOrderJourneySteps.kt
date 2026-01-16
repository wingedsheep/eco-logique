package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutRequest
import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.OrderRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.PaymentRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.ProductRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.util.UUID

class CompleteOrderJourneySteps(
    private val context: ScenarioContext,
    private val api: TestApiClient
) {
    private var productResponse: Response? = null
    private var cartResponse: Response? = null
    private var checkoutResponse: Response? = null

    @When("as admin I create a product with:")
    fun adminCreateProductWithDetails(dataTable: DataTable) {
        val data = dataTable.asMap()

        val request = ProductCreateRequest(
            name = data["name"]!!,
            description = data["description"]!!,
            category = ProductCategory.valueOf(data["category"]!!),
            priceAmount = BigDecimal(data["price"]!!),
            priceCurrency = Currency.EUR,
            weightGrams = data["weight"]!!.toInt(),
            carbonFootprintKg = BigDecimal(data["carbon"]!!)
        )

        productResponse = api.post("/api/v1/products", request)

        if (productResponse!!.statusCode == 201) {
            context.storeProduct(
                name = data["name"]!!,
                ref = ProductRef(
                    id = productResponse!!.jsonPath().getString("id"),
                    name = data["name"]!!,
                    price = BigDecimal(data["price"]!!)
                )
            )
        }
    }

    @Then("the product creation should succeed")
    fun productShouldBeCreated() {
        assertThat(productResponse!!.statusCode)
            .withFailMessage("Expected 201 but got ${productResponse!!.statusCode}: ${productResponse!!.body.asString()}")
            .isEqualTo(201)
    }

    @Then("I can retrieve the product by name {string}")
    fun canRetrieveProductByName(productName: String) {
        val response = api.get("/api/v1/products")
        assertThat(response.statusCode).isEqualTo(200)

        val products = response.jsonPath().getList<Map<String, Any>>("")
        val product = products.find { it["name"] == productName }
        assertThat(product)
            .withFailMessage("Product '$productName' not found in product list")
            .isNotNull

        // Store the product in context if not already there
        if (context.getProduct(productName) == null) {
            context.storeProduct(
                name = productName,
                ref = ProductRef(
                    id = product!!["id"] as String,
                    name = productName,
                    price = BigDecimal(product["priceAmount"].toString())
                )
            )
        }
    }

    @When("I add {int} of {string} to my cart")
    fun addProductToCart(quantity: Int, productName: String) {
        // First ensure cart is clean for this customer
        api.delete("/api/v1/cart")

        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context. Make sure the product was created first.")

        val request = AddCartItemRequest(
            productId = ProductId(UUID.fromString(product.id)),
            quantity = quantity
        )

        cartResponse = api.post("/api/v1/cart/items", request)
        assertThat(cartResponse!!.statusCode)
            .withFailMessage("Failed to add item to cart: ${cartResponse!!.body.asString()}")
            .isEqualTo(201)
    }

    @When("I also add {int} of {string} to my cart")
    fun addMoreProductToCart(quantity: Int, productName: String) {
        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context")

        val request = AddCartItemRequest(
            productId = ProductId(UUID.fromString(product.id)),
            quantity = quantity
        )

        cartResponse = api.post("/api/v1/cart/items", request)
        assertThat(cartResponse!!.statusCode)
            .withFailMessage("Failed to add item to cart: ${cartResponse!!.body.asString()}")
            .isEqualTo(201)
    }

    @Then("my cart should contain {int} of {string}")
    fun cartShouldContain(expectedQuantity: Int, productName: String) {
        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context")

        val response = api.get("/api/v1/cart")
        assertThat(response.statusCode).isEqualTo(200)

        val items = response.jsonPath().getList<Map<String, Any>>("items")
        val cartItem = items.find { it["productId"].toString() == product.id }

        assertThat(cartItem)
            .withFailMessage("Product '$productName' not found in cart")
            .isNotNull

        val quantity = (cartItem!!["quantity"] as Number).toInt()
        assertThat(quantity).isEqualTo(expectedQuantity)
    }

    @Then("my cart should contain {int} items total")
    fun cartShouldContainTotalItems(expectedTotal: Int) {
        val response = api.get("/api/v1/cart")
        assertThat(response.statusCode).isEqualTo(200)

        val items = response.jsonPath().getList<Map<String, Any>>("items")
        val totalQuantity = items.sumOf { (it["quantity"] as Number).toInt() }
        assertThat(totalQuantity).isEqualTo(expectedTotal)
    }

    @Then("the cart total should be {double} EUR")
    fun cartTotalShouldBe(expectedTotal: Double) {
        val response = api.get("/api/v1/cart")
        assertThat(response.statusCode).isEqualTo(200)

        val subtotal = response.jsonPath().getDouble("subtotal")
        assertThat(subtotal).isEqualTo(expectedTotal)
    }

    @When("I checkout with a valid Mastercard")
    fun checkoutWithValidMastercard() {
        performCheckout("tok_mastercard", "5555", CardBrand.MASTERCARD)
    }

    @Then("the order history should contain the recent order")
    fun orderHistoryShouldContainRecentOrder() {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders")
        assertThat(response.statusCode).isEqualTo(200)

        val orders = response.jsonPath().getList<Map<String, Any>>("")
        val found = orders.any { it["id"] == order.id }
        assertThat(found)
            .withFailMessage("Order ${order.id} not found in order history")
            .isTrue()
    }

    @When("I view the order details")
    fun viewOrderDetails() {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders/${order.id}")
        assertThat(response.statusCode).isEqualTo(200)
    }

    @Then("the order should contain {string} with quantity {int}")
    fun orderShouldContainProductWithQuantity(productName: String, expectedQuantity: Int) {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders/${order.id}")
        assertThat(response.statusCode).isEqualTo(200)

        val lines = response.jsonPath().getList<Map<String, Any>>("lines")
        val line = lines.find { it["productName"] == productName }

        assertThat(line)
            .withFailMessage("Product '$productName' not found in order")
            .isNotNull

        val quantity = (line!!["quantity"] as Number).toInt()
        assertThat(quantity).isEqualTo(expectedQuantity)
    }

    @Then("the order payment status should be {string}")
    fun orderPaymentStatusShouldBe(expectedStatus: String) {
        val payment = context.getLatestPayment()
            ?: throw IllegalStateException("No payment in context")

        assertThat(payment.status).isEqualTo(expectedStatus)
    }

    private fun performCheckout(token: String, last4: String, brand: CardBrand) {
        val request = CheckoutRequest(
            cardToken = token,
            cardLast4 = last4,
            cardBrand = brand
        )

        checkoutResponse = api.post("/api/v1/checkout", request)

        if (checkoutResponse!!.statusCode == 201) {
            val orderId = checkoutResponse!!.jsonPath().getString("orderId")
            val orderStatus = checkoutResponse!!.jsonPath().getString("orderStatus")
            val paymentId = checkoutResponse!!.jsonPath().getString("paymentId")
            val paymentStatus = checkoutResponse!!.jsonPath().getString("paymentStatus")

            // Fetch order details to get the total
            val orderResponse = api.get("/api/v1/orders/$orderId")
            val grandTotal = if (orderResponse.statusCode == 200) {
                BigDecimal(orderResponse.jsonPath().getString("grandTotal"))
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
                        paymentMethodSummary = "$brand ending in $last4"
                    )
                )
            }
        }
    }
}
