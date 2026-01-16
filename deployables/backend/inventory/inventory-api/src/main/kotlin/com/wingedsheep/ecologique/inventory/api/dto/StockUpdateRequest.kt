package com.wingedsheep.ecologique.inventory.api.dto

import com.wingedsheep.ecologique.products.api.ProductId

data class StockUpdateRequest(
    val productId: ProductId,
    val quantityOnHand: Int
) {
    init {
        require(quantityOnHand >= 0) { "Quantity on hand cannot be negative" }
    }
}
