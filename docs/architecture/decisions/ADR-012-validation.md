# ADR-012: Validation Strategy

**Status**: Accepted

**Date**: 2024-12-13

**Updated**: 2025-01-09

---

## Decision

We validate using Kotlin `require`/`check` instead of JSR-303 annotations. Validation happens at two levels:

1. **Domain objects** (in `-impl`): Enforce business invariants via `init` blocks
2. **Service layer** (in `-impl`): Validate inputs and convert exceptions to `Result` errors

Request DTOs remain plain data classes for JSON serialization compatibility.

---

## Domain Object Validation

Domain objects enforce business invariants in `init` blocks:

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

// products-impl/domain/Money.kt
internal data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    init {
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
    }
}
```

---

## Service Layer Validation

Services validate inputs and catch domain exceptions, converting them to typed errors:

```kotlin
@Service
internal class ProductServiceImpl(
    private val repository: ProductRepository,
) : ProductServiceApi {

    override fun createProduct(request: CreateProductRequest): Result<ProductDto, ProductError> {
        // Validate enum/lookup values
        val category = ProductCategory.fromString(request.category)
            ?: return Result.err(ProductError.InvalidCategory(request.category))

        // Check business rules
        repository.findByName(request.name)?.let {
            return Result.err(ProductError.DuplicateName(request.name))
        }

        // Domain object creation may throw - catch and convert
        val product = try {
            Product.create(
                name = request.name,
                priceAmount = request.priceAmount,
                // ...
            )
        } catch (e: IllegalArgumentException) {
            return Result.err(ProductError.ValidationFailed(e.message ?: "Validation failed"))
        }

        return Result.ok(repository.save(product).toDto())
    }
}
```

---

## Request DTOs: Plain Data

Request DTOs are plain data classes without validation. This keeps them serialization-friendly:

```kotlin
// products-api/dto/ProductCreateRequest.kt
data class ProductCreateRequest(
    val name: String,
    val description: String,
    val category: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val carbonFootprintKg: BigDecimal,
)
```

**Why no init blocks in DTOs?**
- JSON deserialization creates objects directly—init block failures produce poor error messages
- Validation often requires domain knowledge (valid categories, duplicate checks)
- Service layer can return typed `Result` errors instead of throwing exceptions

---

## Response DTOs: No Validation

Response DTOs are plain data carriers constructed from validated domain objects:

```kotlin
data class ProductDto(
    val id: String,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
)
```

---

## Why Not JSR-303?

JSR-303 requires `@Valid` annotations at call sites—easy to forget:

```kotlin
fun createProduct(@Valid request: ProductRequest)  // Easy to forget
```

Domain `init` blocks are **always enforced**:
```kotlin
val product = Product(name = "", price = money)  // Throws immediately
```

---

## Guidelines

| Location | Mechanism | Purpose |
|----------|-----------|---------|
| Domain objects | `require` in `init` | Business invariants |
| Value objects | `require` in `init` | Type constraints |
| Service layer | Explicit checks → `Result.err` | Input validation, lookups |
| Request DTOs | None | Serialization-friendly |
| Response DTOs | None | Constructed from valid domain |

---

## Consequences

### Positive
- Domain invariants cannot be bypassed
- Typed errors via `Result` instead of exceptions
- Request DTOs work cleanly with JSON serialization
- Validation logic centralized in service layer
- No annotation processing required

### Negative
- Service layer has validation responsibility
- Must remember to catch domain exceptions
