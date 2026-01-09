# ADR-012: Validation Strategy

**Status**: Accepted

**Date**: 2024-12-13

**Updated**: 2026-01-09

---

## Decision

We validate using Kotlin `require`/`check` instead of JSR-303 annotations. Validation happens at multiple levels:

1. **Request DTOs** (in `-api`): Validate format and range constraints
2. **Domain objects** (in `-impl`): Enforce business invariants
3. **Service layer** (in `-impl`): Validate business rules (duplicates, cross-entity)

---

## Request DTO Validation

Request DTOs in API modules validate **what callers can enter**:

```kotlin
// products-api/dto/ProductCreateRequest.kt
data class ProductCreateRequest(
    val name: String,
    val description: String,
    val category: ProductCategory,  // Use enum, not String
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val weightGrams: Int,
    val carbonFootprintKg: BigDecimal,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= 255) { "Name cannot exceed 255 characters" }
        require(description.length <= 2000) { "Description cannot exceed 2000 characters" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
        require(weightGrams > 0) { "Weight must be positive" }
        require(carbonFootprintKg >= BigDecimal.ZERO) { "Carbon footprint cannot be negative" }
    }
}
```

**What to validate in request DTOs:**
- Not blank / not empty
- String length limits
- Numeric ranges (positive, min/max)
- Format patterns (email, phone)

**What NOT to validate in request DTOs:**
- Business rules (duplicates, permissions)
- Cross-field validation requiring domain knowledge
- Database lookups

---

## Using Typed Values in Requests

Prefer enums and typed IDs over plain strings:

```kotlin
// Instead of this:
data class OrderCreateRequest(
    val productId: String,        // Easy to pass wrong ID type
    val category: String,         // What values are valid?
)

// Do this:
data class OrderCreateRequest(
    val productId: ProductId,     // Type-safe, self-validating
    val category: ProductCategory, // Compiler-checked values
)
```

Benefits:
- Compile-time type checking
- Self-documenting API
- Validation happens automatically on deserialization

---

## Domain Object Validation

Domain objects in impl enforce **business invariants**:

```kotlin
// products-impl/domain/Product.kt
internal data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(name.length <= 255) { "Product name cannot exceed 255 characters" }
    }
}

// common-money/Money.kt
data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    init {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
    }
}
```

Domain validation may duplicate some request validation—this is intentional. Domain objects must be valid regardless of how they're created.

---

## Service Layer Validation

Services validate **business rules** that require lookups or cross-entity checks:

```kotlin
@Service
internal class ProductServiceImpl(
    private val repository: ProductRepository,
) : ProductService {

    override fun createProduct(request: ProductCreateRequest): Result<ProductDto, ProductError> {
        // Business rule: no duplicate names
        repository.findByName(request.name)?.let {
            return Result.err(ProductError.DuplicateName(request.name))
        }

        // Domain object creation validates invariants
        val product = try {
            Product.create(
                name = request.name,
                priceAmount = request.priceAmount,
                priceCurrency = request.priceCurrency,
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed(e.message ?: "Validation failed"))
        }

        return Result.ok(repository.save(product).toDto())
    }
}
```

---

## Response DTOs: No Validation

Response DTOs are plain data carriers constructed from validated domain objects:

```kotlin
data class ProductDto(
    val id: ProductId,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
)
```

No `init` block needed—the domain object was already valid.

---

## Why Not JSR-303?

JSR-303 requires `@Valid` annotations at call sites—easy to forget:

```kotlin
fun createProduct(@Valid request: ProductRequest)  // Easy to forget @Valid
```

Kotlin `init` blocks are **always enforced**:
```kotlin
val request = ProductCreateRequest(name = "", ...)  // Throws immediately
```

---

## Validation Summary

| Location | What to Validate | Mechanism |
|----------|------------------|-----------|
| Request DTOs | Format, range, required | `init` with `require` |
| Domain objects | Business invariants | `init` with `require` |
| Service layer | Business rules, lookups | Explicit checks → `Result.err` |
| Response DTOs | Nothing | Constructed from valid domain |

---

## Consequences

### Positive
- Early feedback: Request validation catches issues before service layer
- Type safety: Enums and typed IDs in requests prevent string mistakes
- Domain invariants cannot be bypassed
- Typed errors via `Result` instead of exceptions
- No annotation processing required

### Negative
- Some validation duplicated between request and domain
- Must catch domain exceptions in service layer
