# ADR-008: Error Handling

**Status**: Accepted

**Date**: 2024-11-04

**Updated**: 2026-01-09

---

## Decision

We implement structured error handling using:
1. **Sealed classes** for domain errors returned as `Result<T, E>`
2. **Controller-level translation** to HTTP via `ResponseStatusException`
3. **RFC 7807 Problem Details** via Spring Boot 3 automatic conversion
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

// products-api/ProductService.kt
interface ProductService {
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
) : ProductService {

    override fun getProduct(id: String): Result<ProductDto, ProductError> {
        val product = repository.findById(ProductId(id))
            ?: return Err(ProductError.NotFound(id))
        return Ok(product.toDto())
    }
}
```

---

## Translate at the Boundary

The domain layer doesn't know about HTTP. It returns `ProductError.NotFound`. Somewhere, that needs to become a 404 response.

That translation happens at the **controller**—the boundary between your domain and the web:

```kotlin
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<ProductDto> {
        return productService.getProduct(id).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toResponseStatusException() }
        )
    }

    @PostMapping
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ProductDto> {
        return productService.createProduct(request).fold(
            onSuccess = { ResponseEntity.status(HttpStatus.CREATED).body(it) },
            onFailure = { throw it.toResponseStatusException() }
        )
    }
}
```

**Key principles:**
- Controllers return the **actual type** (`ResponseEntity<ProductDto>`), not `ResponseEntity<Any>`
- On success, return the typed response
- On failure, **throw** `ResponseStatusException` - Spring handles the rest

---

## Error Mappers

Define a single extension function that converts domain errors to HTTP:

```kotlin
fun ProductError.toResponseStatusException(): ResponseStatusException = when (this) {
    is ProductError.NotFound -> ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Product not found: $id"
    )
    is ProductError.ValidationFailed -> ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        reason
    )
    is ProductError.DuplicateName -> ResponseStatusException(
        HttpStatus.CONFLICT,
        "Product name already exists: $name"
    )
}
```

Spring Boot 3 converts `ResponseStatusException` to RFC 7807 Problem Details automatically—clients get consistent, parseable error responses.

When you add a new error type, the `when` expression forces you to decide what HTTP status it maps to.

---

## Benefits of This Approach

1. **Type safety**: Controllers return actual types, not `Any`
2. **Clean domain**: Domain layer has no HTTP knowledge
3. **Explicit translation**: Error-to-HTTP mapping is in one place
4. **Compiler help**: Adding a new error case forces handling
5. **Automatic RFC 7807**: Spring Boot handles Problem Details

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
- Type-safe controller return types improve API documentation
- Domain layer stays pure (no HTTP concerns)
- Consistent RFC 7807 responses from Spring Boot
- No exception overhead in normal error paths
- Explicit control flow with `fold`

### Negative
- Error hierarchy maintenance
- Mapper boilerplate per error type
