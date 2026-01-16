package com.wingedsheep.ecologique.shipping.impl.infrastructure

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import com.wingedsheep.ecologique.shipping.api.dto.ShipmentDto
import com.wingedsheep.ecologique.shipping.api.dto.ShippingAddressDto
import com.wingedsheep.ecologique.shipping.impl.domain.Shipment
import com.wingedsheep.ecologique.shipping.impl.domain.ShippingAddress

internal fun Shipment.toEntity(): ShipmentEntity = ShipmentEntity(
    id = id.value,
    orderId = orderId.value,
    trackingNumber = trackingNumber,
    status = status.name,
    recipientName = shippingAddress.recipientName,
    street = shippingAddress.street,
    houseNumber = shippingAddress.houseNumber,
    postalCode = shippingAddress.postalCode,
    city = shippingAddress.city,
    countryCode = shippingAddress.countryCode,
    warehouseId = warehouseId.value,
    weightKg = weightKg,
    createdAt = createdAt,
    shippedAt = shippedAt,
    deliveredAt = deliveredAt
)

internal fun ShipmentEntity.toDomain(): Shipment = Shipment(
    id = ShipmentId(id),
    orderId = OrderId(orderId),
    trackingNumber = trackingNumber,
    status = ShipmentStatus.valueOf(status),
    shippingAddress = ShippingAddress(
        recipientName = recipientName,
        street = street,
        houseNumber = houseNumber,
        postalCode = postalCode,
        city = city,
        countryCode = countryCode
    ),
    warehouseId = WarehouseId(warehouseId),
    weightKg = weightKg,
    createdAt = createdAt,
    shippedAt = shippedAt,
    deliveredAt = deliveredAt
)

internal fun Shipment.toDto(): ShipmentDto = ShipmentDto(
    id = id,
    orderId = orderId,
    trackingNumber = trackingNumber,
    status = status,
    shippingAddress = ShippingAddressDto(
        recipientName = shippingAddress.recipientName,
        street = shippingAddress.street,
        houseNumber = shippingAddress.houseNumber,
        postalCode = shippingAddress.postalCode,
        city = shippingAddress.city,
        countryCode = shippingAddress.countryCode
    ),
    warehouseId = warehouseId,
    weightKg = weightKg,
    createdAt = createdAt,
    shippedAt = shippedAt,
    deliveredAt = deliveredAt
)

internal fun ShippingAddressDto.toDomain(): ShippingAddress = ShippingAddress(
    recipientName = recipientName,
    street = street,
    houseNumber = houseNumber,
    postalCode = postalCode,
    city = city,
    countryCode = countryCode
)
