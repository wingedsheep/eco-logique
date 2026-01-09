package com.wingedsheep.ecologique.cart.api

import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import com.wingedsheep.ecologique.common.result.Result

interface CartService {
    fun getCart(userId: String): Result<CartDto, CartError>
    fun addItem(userId: String, request: AddCartItemRequest): Result<CartDto, CartError>
    fun updateItem(userId: String, productId: String, request: UpdateCartItemRequest): Result<CartDto, CartError>
    fun removeItem(userId: String, productId: String): Result<CartDto, CartError>
    fun clearCart(userId: String): Result<Unit, CartError>
}
