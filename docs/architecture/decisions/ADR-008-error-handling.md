# ADR-008: Error Handling

**Status**: Accepted

**Date**: 2024-11-04

**Updated**: 2025-01-09

---

## Decision

We implement structured error handling using:
1. **Sealed classes** for domain errors returned as `Result<T, E>`
2. **Controller-level mapping** to HTTP responses via `fold`
3. **RFC 7807 Problem Details** with URN type identifiers
4. **Global exception handler** for unexpected errors

---

## Domain Errors as Data

Service methods return `Result<T, E>` where `E` is a sealed class:

```kotlin
// products-api/error/ProductError.kt
sealed class ProductError {
    data class NotFound(val id: String) : ProductError()
    data class ValidationFailed(val reason: String) : ProductError()
    data class DuplicateName(val name: String) : ProductError()
}

// products-api/ProductServiceApi.kt
interface ProductServiceApi {
    fun getProduct(id: String): Result<ProductDto, ProductError>
    fun createProduct(request: CreateProductRequest): Result<ProductDto, ProductError>
}
```

---

## Service Implementation

```kotlin
@Service
internal class ProductServiceImpl(
    private val repository: ProductRepository,
) : ProductServiceApi {

    override fun getProduct(id: String): Result<ProductDto, ProductError> {
        val product = repository.findById(ProductId(id))
            ?: return Err(ProductError.NotFound(id))
        return Ok(product.toDto())
    }
}
```

---

## Controller Mapping

Controllers use `fold` to map results to `ResponseEntity` with `ProblemDetail`:

```kotlin
@RestController
class ProductController(
    private val productService: ProductServiceApi,
) {
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<Any> {
        return productService.getProduct(id).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { ResponseEntity.status(it.toHttpStatus()).body(it.toProblemDetail()) }
        )
    }
}
```

### Error Mappers

Define mappers in the REST layer:

```kotlin
private fun ProductError.toHttpStatus(): HttpStatus = when (this) {
    is ProductError.NotFound -> HttpStatus.NOT_FOUND
    is ProductError.ValidationFailed -> HttpStatus.BAD_REQUEST
    is ProductError.DuplicateName -> HttpStatus.CONFLICT
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
}
```

### URN Type Convention

Use URN format for problem types: `urn:problem:<domain>:<error-type>`

Examples:
- `urn:problem:product:not-found`
- `urn:problem:order:access-denied`
- `urn:problem:user:validation-failed`

---

## Global Exception Handler

Catches validation failures and unexpected errors:

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleValidation(ex: IllegalArgumentException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Invalid request"
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ProblemDetail {
        logger.error("Unexpected error", ex)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        )
    }
}
```

---

## Consequences

### Positive
- Compile-time safety: all error cases must be handled
- Domain layer stays pure (no HTTP concerns)
- Consistent RFC 7807 responses with typed URNs
- No exception overhead in normal error paths
- Explicit control flow with `fold`

### Negative
- Error hierarchy maintenance
- Mapper boilerplate per error type
