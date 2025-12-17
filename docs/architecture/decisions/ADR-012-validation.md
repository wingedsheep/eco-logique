# ADR-012: Validation Strategy

**Status**: Accepted

**Date**: 2024-12-13

**Updated**: 2025-01-15

---

## Decision

We validate using Kotlin `init` blocks instead of JSR-303 annotations. Validation happens at two levels:

1. **Request DTOs** (in `-api`): Validate input at the boundary
2. **Domain objects** (in `-impl`): Enforce business invariants

---

## Request DTO Validation

Request DTOs validate input immediately when deserialized:

```kotlin
// products-api/dto/CreateProductRequest.kt
data class CreateProductRequest(
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
) {
    init {
        require(name.isNotBlank()) { "Name is required" }
        require(name.length <= 200) { "Name too long" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

Invalid requests fail with `IllegalArgumentException`, caught by the global exception handler and returned as 400 Bad Request.

---

## Domain Object Validation

Domain objects enforce business invariants:

```kotlin
// products-impl/Product.kt
internal data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
    }
}

// products-impl/Money.kt
internal data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }
}
```

---

## Why Not JSR-303?

JSR-303 requires `@Valid` annotations at call sites—easy to forget:

```kotlin
// Validation only runs if caller remembers @Valid
fun createProduct(@Valid request: ProductRequest)  // Easy to forget
```

Init block validation is **always enforced**:
```kotlin
// Impossible to create invalid Product
val product = Product(name = "", price = money)  // Throws immediately
```

---

## Response DTOs: No Validation

Response DTOs are plain data carriers. They're constructed from validated domain objects, so no additional validation needed:

```kotlin
data class ProductDto(
    val id: String,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
)
```

---

## Guidelines

- Use `require` for preconditions (argument validation)
- Use `check` for state invariants
- Request DTOs: validate format, presence, basic constraints
- Domain objects: validate business rules, invariants

---

## Consequences

### Positive
- Validation cannot be bypassed
- No annotation processing required
- Clear, readable validation logic
- Works everywhere (not just Spring contexts)
- Fail-fast at boundaries

### Negative
- More verbose than annotations
- Validation logic in multiple places (but appropriate places)
