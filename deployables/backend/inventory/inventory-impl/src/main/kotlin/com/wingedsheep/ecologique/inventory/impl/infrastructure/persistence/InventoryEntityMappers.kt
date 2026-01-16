package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.impl.domain.Address
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItem
import com.wingedsheep.ecologique.inventory.impl.domain.ReservationStatus
import com.wingedsheep.ecologique.inventory.impl.domain.StockReservation
import com.wingedsheep.ecologique.inventory.impl.domain.Warehouse
import com.wingedsheep.ecologique.products.api.ProductId

internal fun Warehouse.toEntity() = WarehouseEntity(
    id = id.value,
    name = name,
    countryCode = countryCode,
    street = address?.street,
    houseNumber = address?.houseNumber,
    postalCode = address?.postalCode,
    city = address?.city,
    countryCodeAddress = address?.country?.name
)

internal fun WarehouseEntity.toDomain() = Warehouse(
    id = WarehouseId(id),
    name = name,
    countryCode = countryCode,
    address = toAddress()
)

private fun WarehouseEntity.toAddress(): Address? {
    val countryCode = countryCodeAddress ?: return null
    val country = Country.fromCode(countryCode) ?: return null
    val street = street ?: return null
    val houseNumber = houseNumber ?: return null
    val postalCode = postalCode ?: return null
    val city = city ?: return null
    return Address(
        street = street,
        houseNumber = houseNumber,
        postalCode = postalCode,
        city = city,
        country = country
    )
}

internal fun InventoryItem.toEntity(existingId: Long? = null) = InventoryItemEntity(
    id = existingId,
    productId = productId.value,
    warehouseId = warehouseId.value,
    quantityOnHand = quantityOnHand,
    quantityReserved = quantityReserved
)

internal fun InventoryItemEntity.toDomain() = InventoryItem(
    productId = ProductId(productId),
    warehouseId = WarehouseId(warehouseId),
    quantityOnHand = quantityOnHand,
    quantityReserved = quantityReserved
)

internal fun StockReservation.toEntity() = StockReservationEntity(
    id = id.value,
    productId = productId.value,
    warehouseId = warehouseId.value,
    quantity = quantity,
    correlationId = correlationId,
    status = status.name,
    createdAt = createdAt
)

internal fun StockReservationEntity.toDomain() = StockReservation(
    id = ReservationId(id),
    productId = ProductId(productId),
    warehouseId = WarehouseId(warehouseId),
    quantity = quantity,
    correlationId = correlationId,
    status = ReservationStatus.valueOf(status),
    createdAt = createdAt
)
