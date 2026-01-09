# ADR-017: UUID Identifiers

**Status**: Accepted

**Date**: 2026-01-09

---

## Context

We need a consistent strategy for entity identifiers across the application. Key considerations:
- PostgreSQL performance (native UUID type is efficient)
- API clarity
- Type safety across module boundaries

---

## Decision

Use raw **UUID** everywhere:
- **Database**: Native PostgreSQL `UUID` type
- **DTOs**: `java.util.UUID` type
- **Domain**: Typed value classes wrapping `UUID`
- **APIs**: Raw UUID format (`550e8400-e29b-41d4-a716-446655440000`)

No prefixes (no `PROD-`, `ORD-`, `USER-`).

---

## Typed ID Value Classes

Each domain has its own typed ID for compile-time type safety:

```kotlin
// products-impl/domain/ProductId.kt
@JvmInline
value class ProductId(val value: UUID) {
    companion object {
        fun generate(): ProductId = ProductId(UUID.randomUUID())
    }
}

// orders-impl/domain/OrderId.kt
@JvmInline
value class OrderId(val value: UUID) {
    companion object {
        fun generate(): OrderId = OrderId(UUID.randomUUID())
    }
}
```

Benefits:
- Cannot accidentally pass an `OrderId` where a `ProductId` is expected
- Zero-cost abstraction (`@JvmInline`)
- Simple conversion to/from `UUID`

---

## DTOs

DTOs use `java.util.UUID` directly:

```kotlin
data class ProductDto(
    val id: UUID,
    val name: String,
    val priceAmount: BigDecimal,
)

data class OrderDto(
    val id: UUID,
    val userId: String,  // External user ID from auth provider
    val status: String,
)
```

---

## Database Entities

Entities use native `UUID`:

```kotlin
@Table("products", schema = "products")
class ProductEntity(
    @Id val id: UUID,
    val name: String,
    // ...
)
```

---

## Database Schema

```sql
CREATE TABLE products.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    -- ...
);

CREATE TABLE orders.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,  -- External user ID (auth0|xxx)
    -- ...
);
```

---

## Service Layer Mapping

```kotlin
@Service
internal class ProductServiceImpl(...) : ProductService {

    override fun getProduct(id: UUID): Result<ProductDto, ProductError> {
        val product = productRepository.findById(ProductId(id))
            ?: return Result.err(ProductError.NotFound(id))
        return Result.ok(product.toDto())
    }
}

// Mapper
internal fun Product.toDto(): ProductDto = ProductDto(
    id = id.value,  // Unwrap typed ID to raw UUID
    name = name,
    priceAmount = price.amount,
)
```

---

## Consequences

### Positive
- **Storage efficiency**: 16 bytes per ID (native UUID)
- **Query performance**: PostgreSQL UUID operations are optimized
- **Simple API**: Clients send/receive standard UUIDs
- **Type safety**: Domain code uses typed IDs to prevent mix-ups
- **No parsing**: No prefix parsing/validation needed

### Negative
- Less human-readable than prefixed IDs (can't tell product from order at a glance)
- Must ensure correct typed ID usage in domain code
