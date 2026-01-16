package com.wingedsheep.ecologique.shipping.api.dto

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import java.math.BigDecimal
import java.time.Instant

/**
 * Data transfer object for shipment information.
 */
data class ShipmentDto(
    val id: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: String,
    val status: ShipmentStatus,
    val shippingAddress: ShippingAddressDto,
    val warehouseId: WarehouseId,
    val weightKg: BigDecimal?,
    val createdAt: Instant,
    val shippedAt: Instant?,
    val deliveredAt: Instant?
)
