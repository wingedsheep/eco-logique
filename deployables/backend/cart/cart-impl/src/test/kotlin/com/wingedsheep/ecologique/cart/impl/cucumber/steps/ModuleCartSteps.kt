package com.wingedsheep.ecologique.cart.impl.cucumber.steps

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.common.result.Result
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

class ModuleCartSteps {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jwtDecoder: JwtDecoder

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var cartService: CartService

    private var response: Response? = null
    private var currentSubject: String = ""
    private lateinit var authenticatedRequest: RequestSpecification

    @Before
    fun setup() {
        RestAssured.port = port
        RestAssured.basePath = "/api/v1/cart"
        reset(productService)
    }

    @Given("the cart module is running")
    fun cartModuleIsRunning() {
        assertThat(port).isGreaterThan(0)
    }

    @Given("I am authenticated as cart user {string}")
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

    @Given("the following products are available for cart:")
    fun productsAvailableForCart(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val productId = UUID.fromString(row["productId"]!!)
            val productName = row["productName"]!!
            val price = BigDecimal(row["price"]!!)
            whenever(productService.getProduct(productId))
                .thenReturn(Result.ok(buildProductDto(id = productId, name = productName, priceAmount = price)))
        }
    }

    @Given("no products are available")
    fun noProductsAvailable() {
        whenever(productService.getProduct(any()))
            .thenReturn(Result.err(ProductError.NotFound(UUID.randomUUID())))
    }

    @Given("my cart is empty")
    fun cartIsEmpty() {
        cartService.clearCart(currentSubject)
    }

    @Given("my cart has the following items:")
    fun cartHasItems(dataTable: DataTable) {
        cartService.clearCart(currentSubject)
        dataTable.asMaps().forEach { row ->
            val productId = UUID.fromString(row["productId"]!!)
            val productName = row["productName"]!!
            val price = BigDecimal(row["price"]!!)
            val quantity = row["quantity"]!!.toInt()

            whenever(productService.getProduct(productId))
                .thenReturn(Result.ok(buildProductDto(id = productId, name = productName, priceAmount = price)))

            cartService.addItem(currentSubject, AddCartItemRequest(productId.toString(), quantity))
        }
    }

    @When("I view my cart")
    fun viewMyCart() {
        response = authenticatedRequest.get()
    }

    @When("I add {int} of product {string} to my cart")
    fun addProductToCart(quantity: Int, productId: String) {
        val request = AddCartItemRequest(productId, quantity)
        response = authenticatedRequest.body(request).post("/items")
    }

    @When("I update the quantity of {string} to {int}")
    fun updateQuantity(productId: String, quantity: Int) {
        val request = UpdateCartItemRequest(quantity)
        response = authenticatedRequest.body(request).put("/items/$productId")
    }

    @When("I remove {string} from my cart")
    fun removeFromCart(productId: String) {
        response = authenticatedRequest.delete("/items/$productId")
    }

    @When("I clear my cart")
    fun clearMyCart() {
        response = authenticatedRequest.delete()
    }

    @Then("my cart should be empty")
    fun cartShouldBeEmpty() {
        assertThat(response!!.statusCode).isIn(200, 204)
        if (response!!.statusCode == 200) {
            val items = response!!.jsonPath().getList<Map<String, Any>>("items")
            assertThat(items).isEmpty()
        }
    }

    @Then("my cart should have {int} item\\(s)")
    fun cartShouldHaveItems(expectedCount: Int) {
        ensureCartFetched()
        assertThat(response!!.statusCode).isEqualTo(200)
        val items = response!!.jsonPath().getList<Map<String, Any>>("items")
        assertThat(items).hasSize(expectedCount)
    }

    @Then("the cart subtotal should be {double}")
    fun cartSubtotalShouldBe(expectedSubtotal: Double) {
        ensureCartFetched()
        val subtotal = response!!.jsonPath().getDouble("subtotal")
        assertThat(subtotal).isEqualTo(expectedSubtotal)
    }

    @Then("the cart total items should be {int}")
    fun cartTotalItemsShouldBe(expectedTotal: Int) {
        ensureCartFetched()
        val totalItems = response!!.jsonPath().getInt("totalItems")
        assertThat(totalItems).isEqualTo(expectedTotal)
    }

    @Then("the item {string} should have quantity {int}")
    fun itemShouldHaveQuantity(productId: String, expectedQuantity: Int) {
        ensureCartFetched()
        val items = response!!.jsonPath().getList<Map<String, Any>>("items")
        val item = items.find { it["productId"] == productId }
        assertThat(item).isNotNull
        assertThat(item!!["quantity"]).isEqualTo(expectedQuantity)
    }

    private fun ensureCartFetched() {
        if (response!!.statusCode != 200) {
            response = authenticatedRequest.get()
        }
    }

    @Then("I should receive a product not found error for cart")
    fun shouldReceiveProductNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(400)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Product Not Found")
    }

    @Then("I should receive an item not found error")
    fun shouldReceiveItemNotFoundError() {
        assertThat(response!!.statusCode).isEqualTo(404)
        assertThat(response!!.jsonPath().getString("title")).isEqualTo("Item Not Found")
    }

    @Then("the item should be added successfully")
    fun itemAddedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(201)
    }

    @Then("the cart should be updated successfully")
    fun cartUpdatedSuccessfully() {
        assertThat(response!!.statusCode).isEqualTo(200)
    }
}
