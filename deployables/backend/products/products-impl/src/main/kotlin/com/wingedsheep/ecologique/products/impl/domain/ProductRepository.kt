package com.wingedsheep.ecologique.products.impl.domain

import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId

internal interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findByName(name: String): Product?
    fun findByCategory(category: ProductCategory): List<Product>
    fun findAll(): List<Product>
    fun deleteById(id: ProductId): Boolean
    fun existsById(id: ProductId): Boolean
}
