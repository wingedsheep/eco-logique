package com.wingedsheep.ecologique.inventory.impl.infrastructure.persistence

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.impl.domain.Warehouse
import com.wingedsheep.ecologique.inventory.impl.domain.WarehouseRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Component
internal class WarehouseRepositoryImpl(
    private val jdbc: WarehouseRepositoryJdbc
) : WarehouseRepository {

    override fun save(warehouse: Warehouse): Warehouse {
        val entity = warehouse.toEntity()
        if (jdbc.existsById(warehouse.id.value)) {
            entity.markAsExisting()
        }
        return jdbc.save(entity).toDomain()
    }

    override fun findById(id: WarehouseId): Warehouse? {
        return jdbc.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findAll(): List<Warehouse> {
        return jdbc.findAll().map { it.toDomain() }
    }
}

@Repository
internal interface WarehouseRepositoryJdbc : CrudRepository<WarehouseEntity, UUID>
