package com.wingedsheep.ecologique.products.impl.domain.repository

import com.wingedsheep.ecologique.products.impl.domain.model.Product
import com.wingedsheep.ecologique.products.impl.domain.model.ProductCategory
import com.wingedsheep.ecologique.products.impl.domain.model.ProductId

internal interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findByName(name: String): Product?
    fun findByCategory(category: ProductCategory): List<Product>
    fun findAll(): List<Product>
    fun deleteById(id: ProductId): Boolean
    fun existsById(id: ProductId): Boolean
}
