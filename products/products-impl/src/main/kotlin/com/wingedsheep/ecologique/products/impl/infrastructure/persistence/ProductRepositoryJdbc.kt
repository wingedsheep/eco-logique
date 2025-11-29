package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String> {
    fun findByName(name: String): ProductEntity?
    fun findByCategoryCode(categoryCode: String): List<ProductEntity>
}
