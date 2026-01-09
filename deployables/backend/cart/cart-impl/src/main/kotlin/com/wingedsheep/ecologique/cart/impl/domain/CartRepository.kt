package com.wingedsheep.ecologique.cart.impl.domain

internal interface CartRepository {
    fun findByUserId(userId: String): Cart?
    fun save(cart: Cart): Cart
    fun deleteByUserId(userId: String)
}
