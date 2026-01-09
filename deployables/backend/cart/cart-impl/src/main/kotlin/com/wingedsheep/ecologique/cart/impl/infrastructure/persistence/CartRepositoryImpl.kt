package com.wingedsheep.ecologique.cart.impl.infrastructure.persistence

import com.wingedsheep.ecologique.cart.impl.domain.Cart
import com.wingedsheep.ecologique.cart.impl.domain.CartRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
internal class CartRepositoryImpl(
    private val cartJdbc: CartRepositoryJdbc,
    private val cartItemJdbc: CartItemRepositoryJdbc,
    private val jdbcTemplate: JdbcTemplate
) : CartRepository {

    override fun findByUserId(userId: String): Cart? {
        val items = cartItemJdbc.findByUserId(userId)
        if (items.isEmpty()) {
            return null
        }
        return Cart(
            userId = userId,
            items = items.map { it.toCartItem() }
        )
    }

    override fun save(cart: Cart): Cart {
        if (!cartJdbc.existsById(cart.userId)) {
            cartJdbc.save(CartEntity(cart.userId))
        } else {
            cartJdbc.save(CartEntity(cart.userId).markAsExisting())
        }

        // Use native SQL to ensure delete executes immediately before inserts
        jdbcTemplate.update("DELETE FROM cart.cart_items WHERE user_id = ?", cart.userId)

        cart.items.forEach { item ->
            cartItemJdbc.save(item.toEntity(cart.userId))
        }

        return cart
    }

    override fun deleteByUserId(userId: String) {
        jdbcTemplate.update("DELETE FROM cart.cart_items WHERE user_id = ?", userId)
        cartJdbc.deleteById(userId)
    }
}

@Repository
internal interface CartRepositoryJdbc : CrudRepository<CartEntity, String>

@Repository
internal interface CartItemRepositoryJdbc : CrudRepository<CartItemEntity, Long> {
    fun findByUserId(userId: String): List<CartItemEntity>
}
