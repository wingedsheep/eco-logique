package com.wingedsheep.ecologique.inventory.api.dto

import com.wingedsheep.ecologique.products.api.ProductId

/**
 * Represents the current stock level for a product.
 */
data class StockLevel(
    val productId: ProductId,
    val available: Int,
    val reserved: Int
) {
    val total: Int get() = available + reserved
}
