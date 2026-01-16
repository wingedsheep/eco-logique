package com.wingedsheep.ecologique.cart.impl.domain

import com.wingedsheep.ecologique.users.api.UserId

internal interface CartRepository {
    fun findByUserId(userId: UserId): Cart?
    fun save(cart: Cart): Cart
    fun deleteByUserId(userId: UserId)
}
