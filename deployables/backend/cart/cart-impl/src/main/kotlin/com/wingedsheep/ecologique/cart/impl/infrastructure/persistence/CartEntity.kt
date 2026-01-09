package com.wingedsheep.ecologique.cart.impl.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("carts", schema = "cart")
internal class CartEntity(
    @Id private val userId: String
) : Persistable<String> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): String = userId

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): CartEntity {
        isNewEntity = false
        return this
    }
}

@Table("cart_items", schema = "cart")
internal class CartItemEntity(
    @Id val id: Long?,
    val userId: String,
    val productId: String,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int
)
