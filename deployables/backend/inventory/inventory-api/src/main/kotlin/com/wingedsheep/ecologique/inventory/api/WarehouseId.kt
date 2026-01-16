package com.wingedsheep.ecologique.inventory.api

import java.util.UUID

@JvmInline
value class WarehouseId(val value: UUID) {
    companion object {
        fun generate(): WarehouseId = WarehouseId(UUID.randomUUID())
    }
}
