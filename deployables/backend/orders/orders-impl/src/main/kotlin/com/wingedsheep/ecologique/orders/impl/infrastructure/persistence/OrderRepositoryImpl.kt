package com.wingedsheep.ecologique.orders.impl.infrastructure.persistence

import com.wingedsheep.ecologique.orders.impl.domain.Order
import com.wingedsheep.ecologique.orders.impl.domain.OrderId
import com.wingedsheep.ecologique.orders.impl.domain.OrderRepository
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
internal class OrderRepositoryImpl(
    private val orderJdbc: OrderRepositoryJdbc,
    private val orderLineJdbc: OrderLineRepositoryJdbc
) : OrderRepository {

    override fun save(order: Order): Order {
        val entity = order.toEntity()
        if (orderJdbc.existsById(order.id.value)) {
            entity.markAsExisting()
            orderLineJdbc.deleteByOrderId(order.id.value)
        }

        val savedOrder = orderJdbc.save(entity)
        val savedLines = order.lines.map { line ->
            orderLineJdbc.save(line.toEntity(order.id.value))
        }

        return savedOrder.toOrder(savedLines)
    }

    override fun findById(id: OrderId): Order? {
        return orderJdbc.findById(id.value)
            .map { entity ->
                val lines = orderLineJdbc.findByOrderId(id.value)
                entity.toOrder(lines)
            }
            .orElse(null)
    }

    override fun findByUserId(userId: String): List<Order> {
        return orderJdbc.findByUserIdOrderByCreatedAtDesc(userId).map { entity ->
            val lines = orderLineJdbc.findByOrderId(entity.id)
            entity.toOrder(lines)
        }
    }

    override fun existsById(id: OrderId): Boolean {
        return orderJdbc.existsById(id.value)
    }
}

@Repository
internal interface OrderRepositoryJdbc : CrudRepository<OrderEntity, String> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<OrderEntity>
}

@Repository
internal interface OrderLineRepositoryJdbc : CrudRepository<OrderLineEntity, Long> {
    fun findByOrderId(orderId: String): List<OrderLineEntity>

    @Modifying
    @Query("DELETE FROM orders.order_lines WHERE order_id = :orderId")
    fun deleteByOrderId(orderId: String)
}
