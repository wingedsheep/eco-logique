package com.wingedsheep.ecologique.shipping.api

import java.util.UUID

/**
 * Unique identifier for a shipment.
 */
@JvmInline
value class ShipmentId(val value: UUID) {
    companion object {
        fun generate(): ShipmentId = ShipmentId(UUID.randomUUID())
    }
}
