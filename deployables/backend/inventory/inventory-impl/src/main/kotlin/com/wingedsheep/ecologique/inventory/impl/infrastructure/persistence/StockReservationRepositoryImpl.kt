package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.inventory.impl.domain.ReservationStatus
import com.wingedsheep.ecologique.inventory.impl.domain.StockReservation
import com.wingedsheep.ecologique.inventory.impl.domain.StockReservationRepository
import com.wingedsheep.ecologique.products.api.ProductId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Component
internal class StockReservationRepositoryImpl(
    private val jdbc: StockReservationRepositoryJdbc
) : StockReservationRepository {

    override fun save(reservation: StockReservation): StockReservation {
        val entity = reservation.toEntity()
        if (jdbc.existsById(reservation.id.value)) {
            entity.markAsExisting()
        }
        return jdbc.save(entity).toDomain()
    }

    override fun findById(id: ReservationId): StockReservation? {
        return jdbc.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByProductIdAndStatus(productId: ProductId, status: ReservationStatus): List<StockReservation> {
        return jdbc.findByProductIdAndStatus(productId.value, status.name).map { it.toDomain() }
    }

    override fun findActiveByProductId(productId: ProductId): List<StockReservation> {
        return findByProductIdAndStatus(productId, ReservationStatus.ACTIVE)
    }
}

@Repository
internal interface StockReservationRepositoryJdbc : CrudRepository<StockReservationEntity, UUID> {
    fun findByProductIdAndStatus(productId: UUID, status: String): List<StockReservationEntity>
}
