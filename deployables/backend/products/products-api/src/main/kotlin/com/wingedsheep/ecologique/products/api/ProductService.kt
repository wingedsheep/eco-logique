package com.wingedsheep.ecologique.products.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import com.wingedsheep.ecologique.products.api.error.ProductError

interface ProductService {
    fun createProduct(request: ProductCreateRequest): Result<ProductDto, ProductError>
    fun getProduct(id: ProductId): Result<ProductDto, ProductError>
    fun findAllProducts(): Result<List<ProductDto>, ProductError>
    fun findProductsByCategory(category: ProductCategory): Result<List<ProductDto>, ProductError>
    fun updateProductPrice(id: ProductId, request: ProductUpdatePriceRequest): Result<ProductDto, ProductError>
    fun deleteProduct(id: ProductId): Result<Unit, ProductError>
}
