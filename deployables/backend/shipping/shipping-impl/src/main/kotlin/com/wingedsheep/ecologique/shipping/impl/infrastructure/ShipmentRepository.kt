package com.wingedsheep.ecologique.shipping.impl.infrastructure

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface ShipmentRepository : CrudRepository<ShipmentEntity, UUID> {

    @Query("SELECT * FROM shipping.shipments WHERE tracking_number = :trackingNumber")
    fun findByTrackingNumber(trackingNumber: String): ShipmentEntity?

    @Query("SELECT * FROM shipping.shipments WHERE order_id = :orderId")
    fun findByOrderId(orderId: UUID): ShipmentEntity?

    @Query("SELECT COUNT(*) > 0 FROM shipping.shipments WHERE order_id = :orderId")
    fun existsByOrderId(orderId: UUID): Boolean
}
