package com.wingedsheep.ecologique.inventory.api.event

import com.wingedsheep.ecologique.products.api.ProductId
import java.time.Instant

/**
 * Domain event published when inventory has been successfully reserved.
 *
 * Consumers of this event can use it to trigger downstream actions such as:
 * - Updating order status to RESERVED
 * - Notifying warehouse systems
 */
data class InventoryReserved(
    val reservationId: String,
    val productId: ProductId,
    val quantity: Int,
    val correlationId: String,
    val timestamp: Instant
)
