package com.wingedsheep.ecologique.products.impl.infrastructure.web.v1

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
    @Operation(summary = "Create a new product", description = "Creates a new eco-friendly product with sustainability rating")
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ProductDto> {
        return productService.createProduct(request).fold(
            onSuccess = { product ->
                ResponseEntity.status(HttpStatus.CREATED).body(product)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its unique identifier")
    fun getProduct(@PathVariable id: UUID): ResponseEntity<ProductDto> {
        return productService.getProduct(id).fold(
            onSuccess = { product ->
                ResponseEntity.ok(product)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @GetMapping
    @Operation(summary = "List products", description = "Lists all products, optionally filtered by category")
    fun getAllProducts(@RequestParam(required = false) category: String?): ResponseEntity<List<ProductDto>> {
        val result = if (category != null) {
            productService.findProductsByCategory(category)
        } else {
            productService.findAllProducts()
        }

        return result.fold(
            onSuccess = { products ->
                ResponseEntity.ok(products)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @PutMapping("/{id}/price")
    @Operation(summary = "Update product price", description = "Updates the price of an existing product")
    fun updateProductPrice(
        @PathVariable id: UUID,
        @RequestBody request: ProductUpdatePriceRequest
    ): ResponseEntity<ProductDto> {
        return productService.updateProductPrice(id, request).fold(
            onSuccess = { product ->
                ResponseEntity.ok(product)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Removes a product from the catalog")
    fun deleteProduct(@PathVariable id: UUID): ResponseEntity<Unit> {
        return productService.deleteProduct(id).fold(
            onSuccess = {
                ResponseEntity.noContent().build()
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }
}

private fun ProductError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is ProductError.NotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Product Not Found",
            "Product not found: $id"
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
        is ProductError.InvalidCategory -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Category",
            "Invalid category: $category"
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
