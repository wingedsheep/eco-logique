package com.wingedsheep.ecologique.products.impl.infrastructure.persistence

import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.impl.domain.Product
import com.wingedsheep.ecologique.products.impl.domain.ProductRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Component
internal class ProductRepositoryImpl(
    private val jdbc: ProductRepositoryJdbc
) : ProductRepository {

    override fun save(product: Product): Product {
        val entity = product.toEntity()
        if (jdbc.existsById(product.id.value)) {
            entity.markAsExisting()
        }
        return jdbc.save(entity).toProduct()
    }

    override fun findById(id: ProductId): Product? {
        return jdbc.findById(id.value)
            .map { it.toProduct() }
            .orElse(null)
    }

    override fun findByName(name: String): Product? {
        return jdbc.findByName(name)?.toProduct()
    }

    override fun findByCategory(category: ProductCategory): List<Product> {
        return jdbc.findByCategoryCode(category.name).map { it.toProduct() }
    }

    override fun findAll(): List<Product> {
        return jdbc.findAll().map { it.toProduct() }
    }

    override fun deleteById(id: ProductId): Boolean {
        return if (jdbc.existsById(id.value)) {
            jdbc.deleteById(id.value)
            true
        } else {
            false
        }
    }

    override fun existsById(id: ProductId): Boolean {
        return jdbc.existsById(id.value)
    }
}

@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, UUID> {
    fun findByName(name: String): ProductEntity?
    fun findByCategoryCode(categoryCode: String): List<ProductEntity>
}
