package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.products.api.ProductId

/**
 * Represents inventory stock for a product in a warehouse.
 */
internal data class InventoryItem(
    val productId: ProductId,
    val warehouseId: WarehouseId,
    val quantityOnHand: Int,
    val quantityReserved: Int
) {
    val quantityAvailable: Int get() = quantityOnHand - quantityReserved

    fun reserve(quantity: Int): InventoryItem {
        require(quantity <= quantityAvailable) {
            "Cannot reserve $quantity, only $quantityAvailable available"
        }
        return copy(quantityReserved = quantityReserved + quantity)
    }

    fun releaseReservation(quantity: Int): InventoryItem {
        require(quantity <= quantityReserved) {
            "Cannot release $quantity, only $quantityReserved reserved"
        }
        return copy(quantityReserved = quantityReserved - quantity)
    }
}
