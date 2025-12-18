package com.wingedsheep.ecologique.orders.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table("orders", schema = "orders")
internal class OrderEntity(
    @Id private val id: String,
    val userId: String,
    val status: String,
    val subtotal: BigDecimal,
    val grandTotal: BigDecimal,
    val currency: String,
    val createdAt: Instant
) : Persistable<String> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): String = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): OrderEntity {
        isNewEntity = false
        return this
    }
}

@Table("order_lines", schema = "orders")
internal class OrderLineEntity(
    @Id val id: Long?,
    val orderId: String,
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
)