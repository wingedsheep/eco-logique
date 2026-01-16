package com.wingedsheep.ecologique.orders.impl.domain

import com.wingedsheep.ecologique.products.api.ProductId
import java.math.BigDecimal

internal data class OrderLine(
    val productId: ProductId,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal
) {
    init {
        require(productName.isNotBlank()) { "Product name cannot be blank" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(quantity > 0) { "Quantity must be positive" }
        require(lineTotal >= BigDecimal.ZERO) { "Line total must be non-negative" }
    }

    companion object {
        fun create(
            productId: ProductId,
            productName: String,
            unitPrice: BigDecimal,
            quantity: Int
        ): OrderLine = OrderLine(
            productId = productId,
            productName = productName,
            unitPrice = unitPrice,
            quantity = quantity,
            lineTotal = unitPrice.multiply(BigDecimal(quantity))
        )
    }
}