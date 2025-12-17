# ADR-008: Error Handling

**Status**: Accepted

**Date**: 2024-11-04

---

## Decision

We implement structured error handling using:
1. **Sealed classes** for domain errors returned as `Result<T, E>`
2. **Controller-level mapping** to HTTP responses
3. **Global exception handler** for unexpected errors
4. **RFC 7807 Problem Details** via Spring Boot 3's native support

---

## Domain Errors as Data

Service methods return `Result<T, E>` where `E` is a sealed class:

```kotlin
// products-api/error/ProductError.kt
sealed class ProductError {
    data class NotFound(val id: String) : ProductError()
    data class InvalidData(val reason: String) : ProductError()
    data object DuplicateName : ProductError()
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

Controllers translate domain errors to HTTP using `ResponseStatusException`:

```kotlin
@RestController
internal class ProductController(
    private val productService: ProductServiceApi,
) {
    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<ProductDto> {
        return productService.getProduct(id).fold(
            ifOk = { ResponseEntity.ok(it) },
            ifErr = { throw it.toResponseStatusException() }
        )
    }
}

// Error mapper (in REST layer)
fun ProductError.toResponseStatusException() = when (this) {
    is ProductError.NotFound -> ResponseStatusException(NOT_FOUND, "Product not found: $id")
    is ProductError.InvalidData -> ResponseStatusException(BAD_REQUEST, reason)
    is ProductError.DuplicateName -> ResponseStatusException(CONFLICT, "Product name already exists")
}
```

Spring Boot 3 automatically converts `ResponseStatusException` to RFC 7807 Problem Details.

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
- Consistent RFC 7807 responses
- No hidden exceptions

### Negative
- Error hierarchy maintenance
- Mapper boilerplate per error type
