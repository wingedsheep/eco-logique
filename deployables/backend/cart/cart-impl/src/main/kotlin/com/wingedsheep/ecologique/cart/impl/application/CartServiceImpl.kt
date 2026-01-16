package com.wingedsheep.ecologique.cart.impl.application

import com.wingedsheep.ecologique.cart.api.CartService
import com.wingedsheep.ecologique.cart.api.dto.AddCartItemRequest
import com.wingedsheep.ecologique.cart.api.dto.CartDto
import com.wingedsheep.ecologique.cart.api.dto.UpdateCartItemRequest
import com.wingedsheep.ecologique.cart.api.error.CartError
import com.wingedsheep.ecologique.cart.impl.domain.Cart
import com.wingedsheep.ecologique.cart.impl.domain.CartItem
import com.wingedsheep.ecologique.cart.impl.domain.CartRepository
import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.users.api.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
internal class CartServiceImpl(
    private val cartRepository: CartRepository,
    private val productService: ProductService
) : CartService {

    @Transactional(readOnly = true)
    override fun getCart(userId: UserId): Result<CartDto, CartError> {
        val cart = cartRepository.findByUserId(userId) ?: Cart.empty(userId)
        return Result.ok(cart.toDto())
    }

    @Transactional
    override fun addItem(userId: UserId, request: AddCartItemRequest): Result<CartDto, CartError> {
        val productId = request.productId

        val productResult = productService.getProduct(productId)
        if (productResult.isErr) {
            return Result.err(CartError.ProductNotFound(productId))
        }

        val product = productResult.getOrNull()
            ?: return Result.err(CartError.ProductNotFound(productId))
        val cart = cartRepository.findByUserId(userId) ?: Cart.empty(userId)

        val cartItem = try {
            CartItem.create(
                productId = product.id,
                productName = product.name,
                unitPrice = product.priceAmount,
                quantity = request.quantity
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(CartError.ValidationFailed(e.message ?: "Invalid cart item"))
        }

        val updatedCart = cart.addItem(cartItem)
        val savedCart = cartRepository.save(updatedCart)
        return Result.ok(savedCart.toDto())
    }

    @Transactional
    override fun updateItem(
        userId: UserId,
        productId: ProductId,
        request: UpdateCartItemRequest
    ): Result<CartDto, CartError> {
        val cart = cartRepository.findByUserId(userId) ?: Cart.empty(userId)

        if (!cart.containsProduct(productId)) {
            return Result.err(CartError.ItemNotFound(productId))
        }

        val updatedCart = try {
            cart.updateItemQuantity(productId, request.quantity)
        } catch (e: IllegalArgumentException) {
            return Result.err(CartError.InvalidQuantity(request.quantity))
        }

        val savedCart = cartRepository.save(updatedCart)
        return Result.ok(savedCart.toDto())
    }

    @Transactional
    override fun removeItem(userId: UserId, productId: ProductId): Result<CartDto, CartError> {
        val cart = cartRepository.findByUserId(userId) ?: Cart.empty(userId)

        if (!cart.containsProduct(productId)) {
            return Result.err(CartError.ItemNotFound(productId))
        }

        val updatedCart = cart.removeItem(productId)
        val savedCart = cartRepository.save(updatedCart)
        return Result.ok(savedCart.toDto())
    }

    @Transactional
    override fun clearCart(userId: UserId): Result<Unit, CartError> {
        cartRepository.deleteByUserId(userId)
        return Result.ok(Unit)
    }
}
