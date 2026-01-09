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

This is a Kotlin/Spring Boot modular monolith following Domain-Driven Design principles with a strict module dependency structure.

### Module Structure

Each domain (products, users, orders) is split into three submodules:
- **`-api`**: Public interfaces, DTOs, error types, and domain events. Other modules depend only on API modules.
- **`-impl`**: Implementation with domain model (internal), application services, infrastructure (persistence, web controllers).
- **`-worldview`**: Predefined domain objects for local dev seeding and application-level tests.

The **`application`** module is a composition root that wires all impl and worldview modules into a deployable Spring Boot app. It contains configuration (Flyway, Security, OpenAPI) and application-level tests, but no domain logic. No module may depend on it.

### Module Dependency Rules

Enforced by `validateModuleDependencies` task. Key rules:
- **impl → api only**: impl modules depend on api modules, never other impl
- **worldview → api + worldview**: worldview can depend on api and other worldview modules
- **api → api only**: api modules cannot depend on impl or worldview
- **testFixtures in api**: Shared test builders live in `-api/src/testFixtures/kotlin`

Cross-domain dependencies must be whitelisted in root `build.gradle.kts`:
```kotlin
allowedDependencies.set(mapOf(
    "products" to emptySet(),
    "users" to emptySet(),
    "orders" to setOf("products", "users"),
))
```

See [ADR-016](../docs/architecture/decisions/ADR-016-module-dependency-direction-validation.md) for the complete dependency matrix.

### Common Modules

Located at `../../common/`:
- `common-result`: Typed Result<T, E> monad for error handling (use instead of exceptions for domain errors)
- `common-money`: Money and Currency value objects
- `common-country`: Country codes
- `common-time`: Time utilities

### Convention Plugins

Located at `../../build-logic/`:
- `kotlin-conventions`: Standard Kotlin/Spring configuration (Java 21, JaCoCo)
- `common-library`: For library modules without Spring (API modules use this)
- `buildlogic.module-validation`: Enforces module dependency rules

## Code Patterns

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

### Database Migrations

Each module owns its own Flyway migrations and PostgreSQL schema:
- Migrations in `src/main/resources/db/migration/{module}/V{n}__{description}.sql`
- FlywayConfig auto-discovers and runs migrations per schema (products, users, orders)

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