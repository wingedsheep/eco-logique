package com.wingedsheep.ecologique.products.impl.infrastructure.web.v1

import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.products.api.dto.ProductCreateRequest
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import com.wingedsheep.ecologique.products.api.dto.ProductUpdatePriceRequest
import com.wingedsheep.ecologique.products.api.error.ProductError
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/products")
class ProductControllerV1(
    private val productService: ProductService
) {

    @PostMapping
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<Any> {
        return productService.createProduct(request).fold(
            onSuccess = { product ->
                ResponseEntity.status(HttpStatus.CREATED).body(product)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<Any> {
        return productService.getProduct(id).fold(
            onSuccess = { product ->
                ResponseEntity.ok(product)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @GetMapping
    fun getAllProducts(@RequestParam(required = false) category: String?): ResponseEntity<Any> {
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
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @PutMapping("/{id}/price")
    fun updateProductPrice(
        @PathVariable id: String,
        @RequestBody request: ProductUpdatePriceRequest
    ): ResponseEntity<Any> {
        return productService.updateProductPrice(id, request).fold(
            onSuccess = { product ->
                ResponseEntity.ok(product)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: String): ResponseEntity<Any> {
        return productService.deleteProduct(id).fold(
            onSuccess = {
                ResponseEntity.noContent().build()
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    private fun ProductError.toHttpStatus(): HttpStatus = when (this) {
        is ProductError.NotFound -> HttpStatus.NOT_FOUND
        is ProductError.ValidationFailed -> HttpStatus.BAD_REQUEST
        is ProductError.DuplicateName -> HttpStatus.CONFLICT
        is ProductError.InvalidCategory -> HttpStatus.BAD_REQUEST
        is ProductError.Unexpected -> HttpStatus.INTERNAL_SERVER_ERROR
    }

    private fun ProductError.toProblemDetail(): ProblemDetail = when (this) {
        is ProductError.NotFound -> ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Product not found with id: $id"
        ).apply {
            type = URI.create("urn:problem:product:not-found")
            title = "Product Not Found"
        }
        is ProductError.ValidationFailed -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            reason
        ).apply {
            type = URI.create("urn:problem:product:validation-failed")
            title = "Validation Failed"
        }
        is ProductError.DuplicateName -> ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "Product with name '$name' already exists"
        ).apply {
            type = URI.create("urn:problem:product:duplicate-name")
            title = "Duplicate Product Name"
        }
        is ProductError.InvalidCategory -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Invalid category: $category. Valid categories are: CLOTHING, HOUSEHOLD, ELECTRONICS, FOOD, PERSONAL_CARE"
        ).apply {
            type = URI.create("urn:problem:product:invalid-category")
            title = "Invalid Category"
        }
        is ProductError.Unexpected -> ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            message
        ).apply {
            type = URI.create("urn:problem:product:unexpected")
            title = "Unexpected Error"
        }
    }
}
