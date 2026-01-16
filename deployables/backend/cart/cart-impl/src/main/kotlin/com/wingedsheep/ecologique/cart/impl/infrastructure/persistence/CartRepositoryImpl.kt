package com.wingedsheep.ecologique.cart.impl.infrastructure.persistence

import com.wingedsheep.ecologique.cart.impl.domain.Cart
import com.wingedsheep.ecologique.cart.impl.domain.CartRepository
import com.wingedsheep.ecologique.users.api.UserId
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

    override fun findByUserId(userId: UserId): Cart? {
        val userIdString = userId.value.toString()
        val items = cartItemJdbc.findByUserId(userIdString)
        if (items.isEmpty()) {
            return null
        }
        return Cart(
            userId = userId,
            items = items.map { it.toCartItem() }
        )
    }

    override fun save(cart: Cart): Cart {
        val userIdString = cart.userId.value.toString()
        if (!cartJdbc.existsById(userIdString)) {
            cartJdbc.save(CartEntity(userIdString))
        } else {
            cartJdbc.save(CartEntity(userIdString).markAsExisting())
        }

        // Use native SQL to ensure delete executes immediately before inserts
        jdbcTemplate.update("DELETE FROM cart.cart_items WHERE user_id = ?", userIdString)

        cart.items.forEach { item ->
            cartItemJdbc.save(item.toEntity(userIdString))
        }

        return cart
    }

    override fun deleteByUserId(userId: UserId) {
        val userIdString = userId.value.toString()
        jdbcTemplate.update("DELETE FROM cart.cart_items WHERE user_id = ?", userIdString)
        cartJdbc.deleteById(userIdString)
    }
}

@Repository
internal interface CartRepositoryJdbc : CrudRepository<CartEntity, String>

@Repository
internal interface CartItemRepositoryJdbc : CrudRepository<CartItemEntity, Long> {
    fun findByUserId(userId: String): List<CartItemEntity>
}
