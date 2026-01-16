package com.wingedsheep.ecologique.inventory.api

import java.util.UUID

@JvmInline
value class ReservationId(val value: UUID) {
    companion object {
        fun generate(): ReservationId = ReservationId(UUID.randomUUID())
    }
}
