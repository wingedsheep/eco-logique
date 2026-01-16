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
    private val payments = mutableMapOf<String, PaymentRef>()
    private val warehouses = mutableMapOf<String, WarehouseRef>()

    fun storeProduct(name: String, ref: ProductRef) {
        products[name] = ref
    }

    fun getProduct(name: String): ProductRef? = products[name]

    fun storeOrder(id: String, ref: OrderRef) {
        orders[id] = ref
    }

    fun getLatestOrder(): OrderRef? = orders.values.lastOrNull()

    fun updateOrderStatus(id: String, newStatus: String) {
        orders[id]?.let { orders[id] = it.copy(status = newStatus) }
    }

    fun storePayment(orderId: String, ref: PaymentRef) {
        payments[orderId] = ref
    }

    fun getPaymentForOrder(orderId: String): PaymentRef? = payments[orderId]

    fun getLatestPayment(): PaymentRef? = payments.values.lastOrNull()

    fun storeWarehouse(name: String, ref: WarehouseRef) {
        warehouses[name] = ref
    }

    fun getWarehouse(name: String): WarehouseRef? = warehouses[name]

    fun getAllWarehouses(): List<WarehouseRef> = warehouses.values.toList()

    data class WarehouseRef(
        val id: String,
        val name: String,
        val countryCode: String
    )

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

    data class PaymentRef(
        val id: String,
        val orderId: String,
        val status: String,
        val amount: BigDecimal,
        val paymentMethodSummary: String
    )
}