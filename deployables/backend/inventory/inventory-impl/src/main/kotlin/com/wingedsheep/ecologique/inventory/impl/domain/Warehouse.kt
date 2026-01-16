package com.wingedsheep.ecologique.inventory.impl.domain

import com.wingedsheep.ecologique.inventory.api.WarehouseId

/**
 * Represents a warehouse that holds inventory.
 */
internal data class Warehouse(
    val id: WarehouseId,
    val name: String,
    val countryCode: String
)
