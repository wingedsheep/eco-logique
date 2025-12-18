package com.wingedsheep.ecologique.cucumber

import io.cucumber.spring.ScenarioScope
import java.math.BigDecimal
import org.springframework.stereotype.Component

@Component
@ScenarioScope
class ScenarioContext {
    var authToken: String? = null
    var currentUserId: String? = null

    private val products = mutableMapOf<String, ProductRef>()
    private val orders = mutableMapOf<String, OrderRef>()

    fun storeProduct(name: String, ref: ProductRef) {
        products[name] = ref
    }

    fun getProduct(name: String): ProductRef? = products[name]

    fun storeOrder(id: String, ref: OrderRef) {
        orders[id] = ref
    }

    fun getLatestOrder(): OrderRef? = orders.values.lastOrNull()

    data class ProductRef(
        val id: String,
        val name: String,
        val price: BigDecimal
    )

    data class OrderRef(
        val id: String,
        val status: String,
        val grandTotal: BigDecimal
    )
}