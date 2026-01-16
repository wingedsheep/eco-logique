package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.orders.api.OrderId

internal interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: OrderId): Order?
    fun findByUserId(userId: String): List<Order>
    fun existsById(id: OrderId): Boolean
}