package com.wingedsheep.ecologique.cucumber.steps

import com.wingedsheep.ecologique.shipping.api.messaging.DeliveryStatusMessage
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutRequest
import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.cucumber.ScenarioContext
import com.wingedsheep.ecologique.cucumber.ScenarioContext.OrderRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.PaymentRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.ProductRef
import com.wingedsheep.ecologique.cucumber.ScenarioContext.ShipmentRef
import com.wingedsheep.ecologique.cucumber.TestApiClient
import com.wingedsheep.ecologique.cucumber.TestResponse
import com.wingedsheep.ecologique.payment.api.dto.CardBrand
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.users.api.RegistrationService
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.dto.AddressDto
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.whenever
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class CompleteOrderJourneySteps(
    private val context: ScenarioContext,
    private val api: TestApiClient,
    private val rabbitTemplate: RabbitTemplate,
    private val registrationService: RegistrationService,
    private val jwtDecoder: JwtDecoder
) {
    private var productResponse: TestResponse? = null
    private var cartResponse: TestResponse? = null
    private var checkoutResponse: TestResponse? = null

    // Track registered customers to avoid duplicate registration
    private val registeredCustomers = mutableMapOf<String, String>()  // email -> userId

    @Given("I register as a new customer with email {string}")
    fun registerAsNewCustomer(email: String) {
        val password = "TestPassword123!"
        val userId = UUID.randomUUID().toString()

        // Create the test user in the registration service
        registrationService.createTestUser(
            userId = UserId(UUID.fromString(userId)),
            externalSubject = userId,
            email = email,
            password = password
        )

        // Set up authentication for this user
        authenticateAsCustomer(userId)
        registeredCustomers[email] = userId
    }

    @Given("I am logged in as customer {string}")
    fun loginAsRegisteredCustomer(email: String) {
        val userId = registeredCustomers[email]
            ?: throw IllegalStateException("Customer '$email' not registered. Use 'I register as a new customer' first.")

        authenticateAsCustomer(userId)
    }

    private fun authenticateAsCustomer(userId: String) {
        val tokenValue = "test-token-$userId"
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "none")
            .claim("sub", userId)
            .claim("realm_access", mapOf("roles" to listOf("ROLE_CUSTOMER")))
            .build()

        whenever(jwtDecoder.decode(tokenValue)).thenReturn(jwt)

        context.authToken = tokenValue
        context.currentUserId = userId
    }

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
                    id = productResponse!!.getString("id")!!,
                    name = data["name"]!!,
                    price = BigDecimal(data["price"]!!)
                )
            )
        }
    }

    @Then("the product creation should succeed")
    fun productShouldBeCreated() {
        assertThat(productResponse!!.statusCode)
            .withFailMessage("Expected 201 but got ${productResponse!!.statusCode}: ${productResponse!!.bodyAsString()}")
            .isEqualTo(201)
    }

    @Then("I can retrieve the product by name {string}")
    fun canRetrieveProductByName(productName: String) {
        val response = api.get("/api/v1/products")
        assertThat(response.statusCode).isEqualTo(200)

        val products = response.getList<Map<String, Any>>("")
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
            .withFailMessage("Failed to add item to cart: ${cartResponse!!.bodyAsString()}")
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
            .withFailMessage("Failed to add item to cart: ${cartResponse!!.bodyAsString()}")
            .isEqualTo(201)
    }

    @Then("my cart should contain {int} of {string}")
    fun cartShouldContain(expectedQuantity: Int, productName: String) {
        val product = context.getProduct(productName)
            ?: throw IllegalStateException("Product '$productName' not found in context")

        val response = api.get("/api/v1/cart")
        assertThat(response.statusCode).isEqualTo(200)

        val items = response.getList<Map<String, Any>>("items")
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

        val items = response.getList<Map<String, Any>>("items")
        val totalQuantity = items.sumOf { (it["quantity"] as Number).toInt() }
        assertThat(totalQuantity).isEqualTo(expectedTotal)
    }

    @Then("the cart total should be {double} EUR")
    fun cartTotalShouldBe(expectedTotal: Double) {
        val response = api.get("/api/v1/cart")
        assertThat(response.statusCode).isEqualTo(200)

        val subtotal = response.getDouble("subtotal")
        assertThat(subtotal).isEqualTo(expectedTotal)
    }

    @When("I set up my profile with a shipping address in {string}")
    fun setUpProfileWithAddress(country: String) {
        val (city, countryCode) = when (country.lowercase()) {
            "netherlands", "nl" -> "Amsterdam" to "NL"
            "germany", "de" -> "Berlin" to "DE"
            "belgium", "be" -> "Brussels" to "BE"
            "france", "fr" -> "Paris" to "FR"
            "portugal", "pt" -> "Lisbon" to "PT"
            else -> country to country.take(2).uppercase()
        }

        val request = UserCreateRequest(
            name = "Test Customer",
            email = "test.customer.${System.currentTimeMillis()}@demo.com",
            address = AddressDto(
                street = "Test Street",
                houseNumber = "123",
                postalCode = "12345",
                city = city,
                countryCode = countryCode
            )
        )

        val response = api.post("/api/v1/users", request)
        assertThat(response.statusCode)
            .withFailMessage("Failed to create user profile: ${response.bodyAsString()}")
            .isIn(200, 201)
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

        val orders = response.getList<Map<String, Any>>("")
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

        val lines = response.getList<Map<String, Any>>("lines")
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

    @Then("the order VAT should be {double} EUR at {double}% rate")
    fun orderVatShouldBe(expectedVat: Double, expectedRate: Double) {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders/${order.id}")
        assertThat(response.statusCode).isEqualTo(200)

        val vatAmount = response.getDouble("vatAmount")
        val vatRate = response.getDouble("vatRate")

        assertThat(vatAmount)
            .withFailMessage("Expected VAT amount $expectedVat but was $vatAmount")
            .isEqualTo(expectedVat)

        // VAT rate is stored as decimal (0.20 for 20%), convert expected to match
        val expectedRateDecimal = expectedRate / 100.0
        assertThat(vatRate)
            .withFailMessage("Expected VAT rate ${expectedRateDecimal} (${expectedRate}%) but was $vatRate")
            .isEqualTo(expectedRateDecimal)
    }

    private fun performCheckout(token: String, last4: String, brand: CardBrand) {
        val request = CheckoutRequest(
            cardToken = token,
            cardLast4 = last4,
            cardBrand = brand
        )

        checkoutResponse = api.post("/api/v1/checkout", request)

        if (checkoutResponse!!.statusCode == 201) {
            val orderId = checkoutResponse!!.getString("orderId")!!
            val orderStatus = checkoutResponse!!.getString("orderStatus") ?: "UNKNOWN"
            val paymentId = checkoutResponse!!.getString("paymentId")
            val paymentStatus = checkoutResponse!!.getString("paymentStatus") ?: "UNKNOWN"

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
                        paymentMethodSummary = "$brand ending in $last4"
                    )
                )
            }
        }
    }

    // ==================== Shipping Steps ====================

    @When("I check the shipment for my order")
    fun checkShipmentForOrder() {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/shipments?orderId=${order.id}")

        if (response.statusCode == 200) {
            context.storeShipment(
                orderId = order.id,
                ref = ShipmentRef(
                    id = response.getString("id")!!,
                    orderId = response.getString("orderId")!!,
                    trackingNumber = response.getString("trackingNumber")!!,
                    status = response.getString("status")!!,
                    warehouseId = response.getString("warehouseId")!!
                )
            )
        }
    }

    @Then("a shipment should be created for the order")
    fun shipmentShouldBeCreatedForOrder() {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/shipments?orderId=${order.id}")
        assertThat(response.statusCode)
            .withFailMessage("Expected shipment for order ${order.id} but got ${response.statusCode}: ${response.bodyAsString()}")
            .isEqualTo(200)

        val trackingNumber = response.getString("trackingNumber")
        assertThat(trackingNumber)
            .withFailMessage("Shipment should have a tracking number")
            .isNotNull
            .startsWith("ECO-")

        // Store shipment in context
        context.storeShipment(
            orderId = order.id,
            ref = ShipmentRef(
                id = response.getString("id")!!,
                orderId = response.getString("orderId")!!,
                trackingNumber = trackingNumber!!,
                status = response.getString("status")!!,
                warehouseId = response.getString("warehouseId")!!
            )
        )
    }

    @Then("the shipment status should be {string}")
    fun shipmentStatusShouldBe(expectedStatus: String) {
        val shipment = context.getLatestShipment()
            ?: throw IllegalStateException("No shipment in context")

        assertThat(shipment.status).isEqualTo(expectedStatus)
    }

    @Then("the shipment should have a tracking number starting with {string}")
    fun shipmentShouldHaveTrackingNumber(prefix: String) {
        val shipment = context.getLatestShipment()
            ?: throw IllegalStateException("No shipment in context")

        assertThat(shipment.trackingNumber)
            .withFailMessage("Expected tracking number to start with '$prefix' but was '${shipment.trackingNumber}'")
            .startsWith(prefix)
    }

    @Then("the order status should now be {string}")
    fun orderStatusShouldNowBe(expectedStatus: String) {
        val order = context.getLatestOrder()
            ?: throw IllegalStateException("No order in context")

        val response = api.get("/api/v1/orders/${order.id}")
        assertThat(response.statusCode).isEqualTo(200)

        val status = response.getString("status")
        assertThat(status)
            .withFailMessage("Expected order status '$expectedStatus' but was '$status'")
            .isEqualTo(expectedStatus)

        // Update order in context
        context.updateOrderStatus(order.id, status!!)
    }

    @Then("the shipment should be assigned to warehouse {string}")
    fun shipmentShouldBeAssignedToWarehouse(warehouseName: String) {
        val shipment = context.getLatestShipment()
            ?: throw IllegalStateException("No shipment in context")

        val warehouse = context.getWarehouse(warehouseName)
            ?: throw IllegalStateException("Warehouse '$warehouseName' not found in context")

        assertThat(shipment.warehouseId)
            .withFailMessage("Expected shipment to be assigned to warehouse '$warehouseName' (${warehouse.id}) but was assigned to ${shipment.warehouseId}")
            .isEqualTo(warehouse.id)
    }

    // ==================== Warehouse Processing Steps ====================

    @When("the warehouse marks the shipment as processing")
    fun warehouseMarksShipmentAsProcessing() {
        updateShipmentStatus("PROCESSING")
    }

    @When("the warehouse marks the shipment as shipped")
    fun warehouseMarksShipmentAsShipped() {
        updateShipmentStatus("SHIPPED")
    }

    private fun updateShipmentStatus(status: String) {
        val shipment = context.getLatestShipment()
            ?: throw IllegalStateException("No shipment in context")

        val response = api.put("/api/v1/shipments/${shipment.id}/status", mapOf("status" to status))
        assertThat(response.statusCode)
            .withFailMessage("Failed to update shipment status to $status: ${response.bodyAsString()}")
            .isEqualTo(200)

        // Update shipment in context
        context.storeShipment(
            orderId = shipment.orderId,
            ref = shipment.copy(status = status)
        )
    }

    // ==================== Driver Delivery Steps ====================

    @When("the driver marks the shipment as {string}")
    fun driverMarksShipmentAs(status: String) {
        val shipment = context.getLatestShipment()
            ?: throw IllegalStateException("No shipment in context")

        val message = DeliveryStatusMessage(
            trackingNumber = shipment.trackingNumber,
            newStatus = status,
            timestamp = Instant.now(),
            driverNotes = null
        )

        rabbitTemplate.convertAndSend(DeliveryStatusMessage.QUEUE_NAME, message)

        // Wait for the listener to process the message with retry
        waitForShipmentStatus(shipment.orderId, status)
    }

    @When("the driver marks the shipment as {string} with notes {string}")
    fun driverMarksShipmentAsWithNotes(status: String, notes: String) {
        val shipment = context.getLatestShipment()
            ?: throw IllegalStateException("No shipment in context")

        val message = DeliveryStatusMessage(
            trackingNumber = shipment.trackingNumber,
            newStatus = status,
            timestamp = Instant.now(),
            driverNotes = notes
        )

        rabbitTemplate.convertAndSend(DeliveryStatusMessage.QUEUE_NAME, message)

        // Wait for the listener to process the message with retry
        waitForShipmentStatus(shipment.orderId, status)
    }

    private fun waitForShipmentStatus(orderId: String, expectedStatus: String) {
        val maxAttempts = 10
        val delayMs = 200L

        for (attempt in 1..maxAttempts) {
            Thread.sleep(delayMs)

            val response = api.get("/api/v1/shipments?orderId=$orderId")
            if (response.statusCode == 200) {
                val currentStatus = response.getString("status")
                if (currentStatus == expectedStatus) {
                    context.storeShipment(
                        orderId = orderId,
                        ref = ShipmentRef(
                            id = response.getString("id")!!,
                            orderId = response.getString("orderId")!!,
                            trackingNumber = response.getString("trackingNumber")!!,
                            status = currentStatus!!,
                            warehouseId = response.getString("warehouseId")!!
                        )
                    )
                    return
                }
            }
        }

        // Final fetch to update context even if status didn't change
        val response = api.get("/api/v1/shipments?orderId=$orderId")
        if (response.statusCode == 200) {
            context.storeShipment(
                orderId = orderId,
                ref = ShipmentRef(
                    id = response.getString("id")!!,
                    orderId = response.getString("orderId")!!,
                    trackingNumber = response.getString("trackingNumber")!!,
                    status = response.getString("status")!!,
                    warehouseId = response.getString("warehouseId")!!
                )
            )
        }
    }
}
