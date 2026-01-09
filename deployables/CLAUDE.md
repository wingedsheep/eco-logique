# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

All commands run from `backend/` directory:

```bash
./gradlew build                    # Build all modules and run tests
./gradlew test                     # Run all tests
./gradlew :products:products-impl:test   # Run tests for a single module
./gradlew check                    # Build, test, and validate module dependencies
./gradlew validateModuleDependencies     # Validate module dependency rules only
./gradlew jacocoAggregatedReport  # Generate aggregated test coverage report
./gradlew bootRun                  # Run the application (from :application module)
```

Run a single test class:

```bash
./gradlew :products:products-impl:test --tests "com.wingedsheep.ecologique.products.impl.domain.ProductTest"
```

## Architecture

This is a Kotlin/Spring Boot modular monolith following Domain-Driven Design principles with a strict module dependency
structure.

### Module Structure

Each domain (products, users, orders, cart) is split into three submodules:

- **`-api`**: Public contract. Contains:
    - Service interfaces
    - DTOs (request/response)
    - Error sealed classes
    - Domain events
    - **Typed IDs** (e.g., `ProductId`)
    - **Domain enums** (e.g., `ProductCategory`)
    - Request validation via `init` blocks

- **`-impl`**: Hidden implementation (all `internal`). Contains:
    - Domain model with behavior
    - Service implementations
    - Repository interfaces and implementations
    - Database entities
    - Controllers

- **`-worldview`**: Predefined domain objects for local dev seeding and application-level tests.

The **`application`** module is a composition root that wires all impl and worldview modules into a deployable Spring
Boot app. No module may depend on it.

### API Module Contents

API modules define the **public contract**. They may contain:

| Allowed                      | Example                |
|------------------------------|------------------------|
| Service interfaces           | `ProductService`       |
| Request DTOs with validation | `ProductCreateRequest` |
| Response DTOs                | `ProductDto`           |
| Error sealed classes         | `ProductError`         |
| Domain events                | `ProductCreated`       |
| Typed IDs                    | `ProductId`            |
| Domain enums                 | `ProductCategory`      |

**Forbidden in API modules:**

- Implementation classes (`@Service`, `@Repository`)
- Database entities
- Business logic

### Module Dependency Rules

Enforced by `validateModuleDependencies` task. Key rules:

- **impl → api only**: impl modules depend on api modules, never other impl
- **worldview → api + worldview**: worldview can depend on api and other worldview modules
- **api → api only**: api modules cannot depend on impl or worldview
- **testFixtures in api**: Shared test builders live in `-api/src/testFixtures/kotlin`

Cross-domain dependencies must be whitelisted in root `build.gradle.kts`:

```kotlin
allowedDependencies.set(
    mapOf(
        "products" to emptySet(),
        "users" to emptySet(),
        "orders" to setOf("products", "users"),
        "cart" to setOf("products", "users"),
    )
)
```

See [ADR-016](../docs/architecture/decisions/ADR-016-module-dependency-direction-validation.md) for the complete
dependency matrix.

### Common Modules

Located at `../../common/`:

- `common-result`: Typed Result<T, E> monad for error handling
- `common-money`: Money and Currency value objects
- `common-country`: Country codes
- `common-time`: Time utilities

### Convention Plugins

Located at `../../build-logic/`:

- `kotlin-conventions`: Standard Kotlin/Spring configuration (Java 21, JaCoCo)
- `common-library`: For library modules without Spring (API modules use this)
- `buildlogic.module-validation`: Enforces module dependency rules

## Code Patterns

### Controller Pattern

Controllers translate between HTTP and domain. Return proper types, throw on error:

```kotlin
@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val productService: ProductService) {

    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<ProductDto> {
        return productService.getProduct(id).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toResponseStatusException() }
        )
    }
}

private fun ProductError.toResponseStatusException() = when (this) {
    is ProductError.NotFound -> ResponseStatusException(NOT_FOUND, "Product not found: $id")
    is ProductError.ValidationFailed -> ResponseStatusException(BAD_REQUEST, reason)
    is ProductError.DuplicateName -> ResponseStatusException(CONFLICT, "Name exists: $name")
}
```

Key principles:

- Return actual types (`ResponseEntity<ProductDto>`), not `Any`
- Throw `ResponseStatusException` on error - Spring converts to RFC 7807
- Error mapping is explicit with exhaustive `when`

### Service Layer

Services in `-impl` implement interfaces from `-api`:

```kotlin
// products-api: ProductService interface with Result<ProductDto, ProductError> return types
// products-impl: ProductServiceImpl annotated with @Service, internal visibility
```

Domain entities are marked `internal` to prevent leakage. Use DTOs from API modules for cross-module communication.

### Error Handling

Use `Result<T, E>` from common-result instead of exceptions:

```kotlin
fun getProduct(id: String): Result<ProductDto, ProductError>
```

### Typed IDs

IDs use prefixed strings externally, native UUID in database:

```kotlin
// In API module
@JvmInline
value class ProductId private constructor(val value: String) {
    val uuid: UUID get() = UUID.fromString(value.removePrefix("PROD-"))

    companion object {
        fun generate(): ProductId = ProductId("PROD-${UUID.randomUUID()}")
        fun fromUuid(uuid: UUID): ProductId = ProductId("PROD-$uuid")
    }
}

// In database entity
@Table("products")
class ProductEntity(@Id val id: UUID, ...)  // Native UUID storage
```

### Request Validation

Request DTOs validate format/range constraints:

```kotlin
data class ProductCreateRequest(
    val name: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

### Database Migrations

Each module owns its own Flyway migrations and PostgreSQL schema:

- Migrations in `src/main/resources/db/migration/{module}/V{n}__{description}.sql`
- FlywayConfig auto-discovers and runs migrations per schema (products, users, orders, cart)

### Testing

- **Unit tests**: Domain logic, service mocks
- **Integration tests**: Repository tests with Testcontainers PostgreSQL
- **Cucumber tests**:
    - Module-level: `{module}-impl/src/test/resources/features/{module}_module.feature`
    - Application-level: `application/src/test/resources/features/*.feature`

Worldview modules provide canonical domain examples:

```kotlin
WorldviewProduct.organicCottonTShirt  // Pre-built ProductDto for seeding/tests
```
