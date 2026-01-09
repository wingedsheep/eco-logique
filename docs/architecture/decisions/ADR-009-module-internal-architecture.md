# ADR-009: Module Internal Architecture

**Status**: Accepted

**Date**: 2024-05-23

**Updated**: 2026-01-09

---

## Decision

Each domain is split into three modules with clear responsibilities:

1. **`-api`**: The public contract - interfaces, DTOs, errors, events, typed IDs, enums
2. **`-impl`**: Hidden implementation - domain model, services, infrastructure
3. **`-worldview`**: Predefined examples for testing and seeding

Internal module structure is **flexible** based on complexity. The key principle is **dependencies point inward**: infrastructure depends on domain, never the reverse.

---

## API Module: The Public Contract

The API module defines **what** a domain offers. Other modules depend only on API modules.

**What belongs in API modules:**

| Element | Example | Why |
|---------|---------|-----|
| Service interfaces | `ProductService` | Contract for cross-module calls |
| Request DTOs | `ProductCreateRequest` | Input validation at boundary |
| Response DTOs | `ProductDto` | Stable output format |
| Error sealed classes | `ProductError` | Typed error handling |
| Domain events | `ProductCreated` | Event-driven integration |
| Typed IDs | `ProductId` | Type-safe identifiers |
| Domain enums | `ProductCategory` | Shared vocabulary |

**What is forbidden in API modules:**
- Implementation classes (`@Service`, `@Repository`)
- Database entities
- Framework-specific code (Spring annotations on implementations)
- Business logic

```kotlin
// products-api - ALLOWED
interface ProductService {
    fun getProduct(id: ProductId): Result<ProductDto, ProductError>
}

@JvmInline
value class ProductId(val value: String) {
    init {
        require(value.startsWith("PROD-")) { "ProductId must start with PROD-" }
    }
}

enum class ProductCategory {
    CLOTHING, HOUSEHOLD, ELECTRONICS, FOOD, PERSONAL_CARE;

    companion object {
        fun fromString(value: String): ProductCategory? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}

data class ProductCreateRequest(
    val name: String,
    val category: ProductCategory,  // Use enum, not String
    val priceAmount: BigDecimal,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= 255) { "Name cannot exceed 255 characters" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
    }
}

sealed class ProductError {
    data class NotFound(val id: ProductId) : ProductError()
    data class ValidationFailed(val reason: String) : ProductError()
}
```

---

## Request DTO Validation

Request DTOs can have `init` block validation for:
- Format constraints (not blank, max length, regex patterns)
- Range validation (positive numbers, min/max values)
- Required fields

This provides **early feedback** to API callers about what they can enter.

```kotlin
data class ProductCreateRequest(
    val name: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= 255) { "Name cannot exceed 255 characters" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

**Note**: Business rule validation (duplicate checks, cross-entity rules) still happens in the service layer.

---

## Impl Module: Hidden Implementation

The impl module contains all implementation details. Everything here can change without affecting other modules.

**Structure options:**

### Simple Modules: Flat Structure

```
notifications-impl/
└── src/main/kotlin/com/example/notifications/
    ├── Notification.kt
    ├── NotificationServiceImpl.kt
    ├── NotificationRepository.kt
    └── NotificationEntity.kt
```

### Complex Modules: Layered or Per-Feature

```
products-impl/
└── src/main/kotlin/com/example/products/
    ├── domain/
    │   ├── Product.kt
    │   └── ProductRepository.kt
    ├── application/
    │   ├── ProductServiceImpl.kt
    │   └── ProductMappers.kt
    └── infrastructure/
        ├── persistence/
        │   ├── ProductEntity.kt
        │   └── ProductRepositoryJdbc.kt
        └── web/
            └── ProductController.kt
```

**Key principle**: Domain has no framework dependencies. Infrastructure depends on domain.

---

## Domain Models Stay Internal

Domain models in impl are `internal` and have behavior:

```kotlin
// products-impl - internal
internal data class Product(
    val id: ProductId,  // Uses typed ID from API
    val name: String,
    val price: Money,
    val category: ProductCategory,  // Uses enum from API
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
    }

    fun updatePrice(newPrice: Money): Product {
        require(newPrice.currency == price.currency) { "Currency cannot change" }
        return copy(price = newPrice)
    }
}
```

The impl module uses types from the API module (typed IDs, enums) but adds its own behavior.

---

## Worldview Module: Examples for Testing

Provides canonical domain examples:

```kotlin
object WorldviewProduct {
    val organicCottonTShirt = ProductDto(
        id = ProductId("PROD-organic-cotton"),
        name = "Organic Cotton T-Shirt",
        category = ProductCategory.CLOTHING,
        priceAmount = BigDecimal("29.99"),
    )
}
```

---

## Module Dependencies

```
┌─────────────────────────────────────────────┐
│  Other Domains                              │
│  (depend only on -api modules)              │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│  products-api                               │
│  ├── ProductService (interface)             │
│  ├── ProductDto, ProductCreateRequest       │
│  ├── ProductError (sealed class)            │
│  ├── ProductId (typed ID)                   │
│  └── ProductCategory (enum)                 │
└─────────────────────────────────────────────┘
                     ▲
                     │ implements
                     │
┌─────────────────────────────────────────────┐
│  products-impl (internal)                   │
│  ├── ProductServiceImpl                     │
│  ├── Product (domain model)                 │
│  ├── ProductRepository                      │
│  ├── ProductEntity                          │
│  └── ProductController                      │
└─────────────────────────────────────────────┘
```

---

## Consequences

### Positive
- Clear contract: API modules show exactly what a domain offers
- Type safety: Typed IDs and enums in API prevent stringly-typed code
- Early validation: Request DTOs validate at the boundary
- Hidden implementation: Impl internals can change freely
- Compiler help: Enum exhaustiveness checks, type mismatches caught early

### Negative
- More upfront design for API modules
- Changes to API module affect consumers
