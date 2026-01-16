package com.wingedsheep.ecologique.shipping.api.dto

import com.wingedsheep.ecologique.orders.api.OrderId
import java.math.BigDecimal

/**
 * Request to create a new shipment.
 */
data class CreateShipmentRequest(
    val orderId: OrderId,
    val shippingAddress: ShippingAddressDto,
    val weightKg: BigDecimal? = null
) {
    init {
        weightKg?.let {
            require(it > BigDecimal.ZERO) { "Weight must be positive" }
        }
    }
}
