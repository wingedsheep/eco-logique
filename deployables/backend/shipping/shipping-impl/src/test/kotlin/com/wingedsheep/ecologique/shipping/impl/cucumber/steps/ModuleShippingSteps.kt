package com.wingedsheep.ecologique.shipping.impl.cucumber.steps

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.api.WarehouseService
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseDto
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.dto.CreateShipmentRequest
import com.wingedsheep.ecologique.shipping.api.dto.ShipmentDto
import com.wingedsheep.ecologique.shipping.api.dto.ShippingAddressDto
import com.wingedsheep.ecologique.shipping.api.error.ShippingError
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

class ModuleShippingSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var warehouseService: WarehouseService

    @Autowired
    private lateinit var shippingService: ShippingService

    private lateinit var client: WebTestClient
    private val objectMapper = ObjectMapper()

    private var response: TestResponse? = null
    private var createdShipmentId: UUID? = null
    private var createdTrackingNumber: String? = null
    private var lastServiceResult: Result<ShipmentDto, ShippingError>? = null

    @Before
    fun setup() {
        client = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port/api/v1/shipments")
            .responseTimeout(Duration.ofSeconds(10))
            .build()
        reset(warehouseService)
        lastServiceResult = null
        createdShipmentId = null
        createdTrackingNumber = null
    }

    @Given("the module is running")
    fun moduleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @Given("a warehouse exists for country {string}")
    fun warehouseExistsForCountry(countryCode: String) {
        val warehouseId = WarehouseId(UUID.randomUUID())
        val warehouse = WarehouseDto(
            id = warehouseId,
            name = "Test Warehouse $countryCode",
            countryCode = countryCode
        )
        whenever(warehouseService.getAllWarehouses())
            .thenReturn(Result.ok(listOf(warehouse)))
    }

    @Given("no warehouses exist")
    fun noWarehousesExist() {
        whenever(warehouseService.getAllWarehouses())
            .thenReturn(Result.ok(emptyList()))
    }

    @Given("a shipment exists for order {string}")
    fun shipmentExistsForOrder(orderId: String) {
        createShipmentForOrder(UUID.fromString(orderId), null)
    }

    @Given("a shipment exists for order {string} with status {string}")
    fun shipmentExistsWithStatus(orderId: String, status: String) {
        createShipmentForOrder(UUID.fromString(orderId), null)
        if (status != "CREATED") {
            transitionToStatus(status)
        }
    }

    @When("I create a shipment for order {string} with address:")
    fun createShipmentWithAddress(orderId: String, dataTable: DataTable) {
        val data = dataTable.asMap()
        val request = mapOf(
            "orderId" to orderId,
            "shippingAddress" to mapOf(
                "recipientName" to data["recipientName"],
                "street" to data["street"],
                "houseNumber" to data["houseNumber"],
                "postalCode" to data["postalCode"],
                "city" to data["city"],
                "countryCode" to data["countryCode"]
            )
        )

        // Use service directly since there's no POST endpoint for creating shipments via API
        val createRequest = CreateShipmentRequest(
            orderId = OrderId(UUID.fromString(orderId)),
            shippingAddress = ShippingAddressDto(
                recipientName = data["recipientName"]!!,
                street = data["street"]!!,
                houseNumber = data["houseNumber"]!!,
                postalCode = data["postalCode"]!!,
                city = data["city"]!!,
                countryCode = data["countryCode"]!!
            )
        )

        lastServiceResult = shippingService.createShipment(createRequest)
        lastServiceResult?.onSuccess { shipment ->
            createdShipmentId = shipment.id.value
            createdTrackingNumber = shipment.trackingNumber
            response = get("/$createdShipmentId")
        }
    }

    @When("I create a shipment for order {string} with weight {double} kg and address:")
    fun createShipmentWithWeightAndAddress(orderId: String, weight: Double, dataTable: DataTable) {
        val data = dataTable.asMap()

        val createRequest = CreateShipmentRequest(
            orderId = OrderId(UUID.fromString(orderId)),
            shippingAddress = ShippingAddressDto(
                recipientName = data["recipientName"]!!,
                street = data["street"]!!,
                houseNumber = data["houseNumber"]!!,
                postalCode = data["postalCode"]!!,
                city = data["city"]!!,
                countryCode = data["countryCode"]!!
            ),
            weightKg = BigDecimal.valueOf(weight)
        )

        lastServiceResult = shippingService.createShipment(createRequest)
        lastServiceResult?.onSuccess { shipment ->
            createdShipmentId = shipment.id.value
            createdTrackingNumber = shipment.trackingNumber
            response = get("/$createdShipmentId")
        }
    }

    @When("I retrieve the shipment by ID")
    fun retrieveShipmentById() {
        response = get("/$createdShipmentId")
    }

    @When("I retrieve shipment with ID {string}")
    fun retrieveShipmentWithId(shipmentId: String) {
        response = get("/$shipmentId")
    }

    @When("I retrieve the shipment by tracking number")
    fun retrieveShipmentByTrackingNumber() {
        response = get("/tracking/$createdTrackingNumber")
    }

    @When("I retrieve the shipment for order {string}")
    fun retrieveShipmentForOrder(orderId: String) {
        response = get("?orderId=$orderId")
    }

    @When("I update the shipment status to {string}")
    fun updateShipmentStatus(newStatus: String) {
        val shipmentId = createdShipmentId ?: error("No shipment created yet")
        response = put("/$shipmentId/status", mapOf("status" to newStatus))
    }

    @Then("the shipment should be created successfully")
    fun shipmentCreatedSuccessfully() {
        assertThat(lastServiceResult?.isOk).isTrue()
        assertThat(createdShipmentId).isNotNull()
    }

    @Then("the shipment status should be {string}")
    fun shipmentStatusShouldBe(expectedStatus: String) {
        val status = response!!.getString("status")
        assertThat(status).isEqualTo(expectedStatus)
    }

    @Then("the shipment should have a tracking number")
    fun shipmentShouldHaveTrackingNumber() {
        val trackingNumber = response!!.getString("trackingNumber")
        assertThat(trackingNumber).isNotBlank()
    }

    @Then("I should receive the shipment details")
    fun receiveShipmentDetails() {
        assertThat(response!!.statusCode).isEqualTo(200)
        assertThat(response!!.getString("id")).isNotBlank()
    }

    @Then("I should receive a not found error")
    fun shouldReceiveNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
    }

    @Then("I should receive a not found for order error")
    fun shouldReceiveNotFoundForOrderError() {
        assertThat(response!!.statusCode).isEqualTo(404)
    }

    @Then("I should receive a duplicate shipment error")
    fun shouldReceiveDuplicateShipmentError() {
        assertThat(lastServiceResult?.isErr).isTrue()
        lastServiceResult?.onFailure { error ->
            assertThat(error).isInstanceOf(ShippingError.DuplicateShipment::class.java)
        }
    }

    @Then("I should receive a no warehouse error")
    fun shouldReceiveNoWarehouseError() {
        assertThat(lastServiceResult?.isErr).isTrue()
        lastServiceResult?.onFailure { error ->
            assertThat(error).isInstanceOf(ShippingError.NoWarehouseForCountry::class.java)
        }
    }

    @Then("I should receive an invalid status transition error")
    fun shouldReceiveInvalidStatusTransitionError() {
        assertThat(response!!.statusCode).isEqualTo(400)
    }

    @Then("the shipment weight should be {double} kg")
    fun shipmentWeightShouldBe(expectedWeight: Double) {
        val weight = response!!.getDouble("weightKg")
        assertThat(weight).isEqualTo(expectedWeight)
    }

    private fun createShipmentForOrder(orderId: UUID, weightKg: BigDecimal?) {
        val request = CreateShipmentRequest(
            orderId = OrderId(orderId),
            shippingAddress = ShippingAddressDto(
                recipientName = "Test User",
                street = "Test Street",
                houseNumber = "1",
                postalCode = "1234 AB",
                city = "Test City",
                countryCode = "NL"
            ),
            weightKg = weightKg
        )

        shippingService.createShipment(request).fold(
            onSuccess = { shipment ->
                createdShipmentId = shipment.id.value
                createdTrackingNumber = shipment.trackingNumber
            },
            onFailure = { }
        )
    }

    private fun transitionToStatus(targetStatus: String) {
        val shipmentId = createdShipmentId ?: error("No shipment created yet")
        val statusPath = when (targetStatus) {
            "PROCESSING" -> listOf(ShipmentStatus.PROCESSING)
            "SHIPPED" -> listOf(ShipmentStatus.PROCESSING, ShipmentStatus.SHIPPED)
            "IN_TRANSIT" -> listOf(ShipmentStatus.PROCESSING, ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT)
            "DELIVERED" -> listOf(ShipmentStatus.PROCESSING, ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED)
            "CANCELLED" -> listOf(ShipmentStatus.CANCELLED)
            "RETURNED" -> listOf(ShipmentStatus.PROCESSING, ShipmentStatus.SHIPPED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.RETURNED)
            else -> emptyList()
        }
        statusPath.forEach { status ->
            shippingService.updateStatus(
                com.wingedsheep.ecologique.shipping.api.ShipmentId(shipmentId),
                status
            )
        }
    }

    private fun get(path: String): TestResponse {
        val result = client.get()
            .uri(path)
            .exchange()
            .returnResult<String>()
        return TestResponse(result.status.value(), result.responseBody.blockFirst() ?: "", objectMapper)
    }

    private fun put(path: String, body: Any): TestResponse {
        val result = client.put()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
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
        fun getDouble(path: String): Double? = (getValueAtPath(path) as? Number)?.toDouble()

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
