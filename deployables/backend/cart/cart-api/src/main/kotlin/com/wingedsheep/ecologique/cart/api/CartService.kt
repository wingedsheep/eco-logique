package com.wingedsheep.ecologique.cart.api

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.users.api.UserId

interface CartService {
    fun getCart(userId: UserId): Result<CartDto, CartError>
    fun addItem(userId: UserId, request: AddCartItemRequest): Result<CartDto, CartError>
    fun updateItem(userId: UserId, productId: ProductId, request: UpdateCartItemRequest): Result<CartDto, CartError>
    fun removeItem(userId: UserId, productId: ProductId): Result<CartDto, CartError>
    fun clearCart(userId: UserId): Result<Unit, CartError>
}
