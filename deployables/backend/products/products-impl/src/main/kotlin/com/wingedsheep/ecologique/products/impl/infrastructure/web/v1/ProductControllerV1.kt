package com.wingedsheep.ecologique.products.impl.infrastructure.web.v1

import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import com.wingedsheep.ecologique.products.api.error.ProductError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Product catalog management")
class ProductControllerV1(
    private val productService: ProductService
) {

    @PostMapping
    @Operation(summary = "Create a new product")
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ProductDto> {
        return productService.createProduct(request).fold(
            onSuccess = { ResponseEntity.status(HttpStatus.CREATED).body(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    fun getProduct(@PathVariable id: UUID): ResponseEntity<ProductDto> {
        return productService.getProduct(ProductId(id)).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @GetMapping
    @Operation(summary = "List products")
    fun getAllProducts(@RequestParam(required = false) category: String?): ResponseEntity<List<ProductDto>> {
        val result = if (category != null) {
            val productCategory = com.wingedsheep.ecologique.products.api.ProductCategory
                .entries.find { it.name.equals(category, ignoreCase = true) }
                ?: throw ErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid category: $category").apply {
                        title = "Invalid Category"
                    },
                    null
                )
            productService.findProductsByCategory(productCategory)
        } else {
            productService.findAllProducts()
        }

        return result.fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @PutMapping("/{id}/price")
    @Operation(summary = "Update product price")
    fun updateProductPrice(
        @PathVariable id: UUID,
        @RequestBody request: ProductUpdatePriceRequest
    ): ResponseEntity<ProductDto> {
        return productService.updateProductPrice(ProductId(id), request).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    fun deleteProduct(@PathVariable id: UUID): ResponseEntity<Unit> {
        return productService.deleteProduct(ProductId(id)).fold(
            onSuccess = { ResponseEntity.noContent().build() },
            onFailure = { throw it.toErrorResponseException() }
        )
    }
}

private fun ProductError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is ProductError.NotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Product Not Found",
            "Product not found: ${id.value}"
        )
        is ProductError.ValidationFailed -> Triple(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            reason
        )
        is ProductError.DuplicateName -> Triple(
            HttpStatus.CONFLICT,
            "Duplicate Product Name",
            "Product name already exists: $name"
        )
        is ProductError.Unexpected -> Triple(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            message
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}
