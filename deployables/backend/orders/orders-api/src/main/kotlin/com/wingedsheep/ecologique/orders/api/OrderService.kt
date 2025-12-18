package com.wingedsheep.ecologique.orders.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.dto.OrderCreateRequest
import com.wingedsheep.ecologique.orders.api.dto.OrderDto
import com.wingedsheep.ecologique.orders.api.error.OrderError

interface OrderService {
    fun createOrder(userId: String, request: OrderCreateRequest): Result<OrderDto, OrderError>
    fun getOrder(orderId: String, userId: String): Result<OrderDto, OrderError>
    fun findOrdersForUser(userId: String): Result<List<OrderDto>, OrderError>
    fun updateStatus(orderId: String, newStatus: String): Result<OrderDto, OrderError>
}