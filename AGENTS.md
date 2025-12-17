# AGENTS.md

## Project Overview

**Eco-logique** is a modular monolith e-commerce application for eco-friendly products, built with Kotlin, Spring Boot,
and Domain-Driven Design (DDD).

The architecture is defined through ADRs in `docs/architecture/decisions/`. If in doubt: follow the ADRs.

---

## Quick Reference

### Architecture

- **Style**: Modular Monolith + DDD
- **Modules / Bounded Contexts**: Payment, Products, Shipping, Inventory, Users
- **Internal structure**: Flexible (flat for simple modules, vertical slices for complex ones)
- **Communication**: Synchronous (direct API calls) + Asynchronous (domain events)
- **Database**: PostgreSQL with **separate schema per module** (or table prefixes for simpler setups)
- **Persistence**: **Spring Data JDBC** (not JPA/Hibernate)

### Architecture Decisions

All ADRs live in `docs/architecture/decisions/`:

- ADR-001: Modular Monolith Architecture
- ADR-002: Domain-Driven Design
- ADR-003: Separate Database Schemas per Bounded Context
- ADR-004: Mappers in Outer Layers
- ADR-005: Event-Driven Communication Between Modules
- ADR-006: Worldview Pattern for Domain Knowledge
- ADR-007: Feature File Tests Strategy
- ADR-008: Error Handling in REST Endpoints (RFC 7807 / `ProblemDetail`)
- ADR-009: Module Internal Architecture
- ADR-010: Gradle Build Configuration & Versioning Strategy
- ADR-011: Spring Data JDBC over JPA
- ADR-012: Validation Strategy
- ADR-014: Gradle Test Fixtures
- ADR-015: Test Pyramid Strategy
- ADR-016: Module Dependency Direction Validation

---

## Module Structure

Each domain is represented as a Gradle module group. The standard artifacts are:

```text
domain-name/
├── domain-name-api/         # Public Contract: interfaces, DTOs, events, errors (+ testFixtures)
├── domain-name-impl/        # Implementation: domain + services/handlers + infrastructure
└── domain-name-worldview/   # (ADR-006) Realistic domain data for runtime seeding (optional)
```

### Dependency Rules (Hard Rules)

1. **`-impl` depends on its own `-api`**
2. Modules may depend **only** on other modules' **`-api`** (never on `-impl`)
3. Cross-module test helpers:

    * Prefer `testFixtures(project(":other:other-api"))` (ADR-014)
    * Use `-worldview` for **runtime seeding only** (local dev, demos, app-level tests) (ADR-006)
4. **No circular dependencies** (enforced by Gradle)

---

## Data Isolation

* One PostgreSQL **schema per module** (`payment`, `products`, `shipping`, `inventory`, `users`)
* Alternative: table prefixes for simpler setups (`products_product`, `shipping_shipment`)
* No shared tables between modules
* **No foreign keys across schemas**
* Cross-module data access happens through **service APIs**, not database joins

Migrations are organized per module (Flyway locations per schema).

---

## Communication Between Modules

### Synchronous (Direct Calls)

Use when **immediate consistency** is required.

* Call interfaces defined in the target's `-api`
* Exchange **DTOs**, never domain entities/value objects

### Asynchronous (Events)

Use when **eventual consistency** and loose coupling is acceptable.

* Events are defined in the publisher's `-api`
* Multiple modules can listen without introducing compile-time coupling

---

## Module Internal Structure (Per `-impl`)

Internal structure is **flexible** based on module complexity. The key principle: **dependencies point inward**.

### Simple Modules: Flat Structure

```text
notifications-impl/
└── src/main/kotlin/com/example/notifications/
    ├── Notification.kt
    ├── NotificationServiceImpl.kt
    ├── NotificationRepository.kt
    └── persistence/
        ├── NotificationEntity.kt
        └── NotificationRepositoryImpl.kt
```

### Complex Modules: Vertical Slices

```text
products-impl/
└── src/main/kotlin/com/example/products/
    ├── CreateProductHandler.kt
    ├── GetProductHandler.kt
    ├── UpdatePriceHandler.kt
    ├── bulkimport/
    │   ├── BulkImportHandler.kt
    │   └── ImportParser.kt
    ├── shared/
    │   ├── Product.kt
    │   ├── ProductRepository.kt
    │   └── ProductMappers.kt
    └── persistence/
        ├── ProductEntity.kt
        └── ProductRepositoryImpl.kt
```

### Layer Responsibilities

**Domain** (in `shared/` or root)
* Pure Kotlin business logic, no Spring dependencies
* Entities/value objects validate in `init` blocks
* Repository interfaces defined here

**Services/Handlers**
* Implement `-api` interfaces or handle specific use cases
* Orchestrate: load → domain logic → save → publish event
* Return `Result<T, E>` with sealed error hierarchies from `-api`

**Infrastructure** (in `persistence/`, `rest/`, `messaging/`)
* Controllers, repository implementations, external clients
* Mappers live in their respective outer layers (ADR-004)
* Infrastructure types stay internal, never leak inward

---

## Mappers (ADR-004)

Mappers live in the **outer layer** that needs them:

| Layer | File | Functions |
|-------|------|-----------|
| Persistence | `<Type>EntityMappers.kt` | `Entity.toDomain()`, `Domain.toEntity()` |
| REST | `<Type>Mappers.kt` | `Request.toDomain()`, `Domain.toDto()` |
| Messaging | `<Type>MessageMappers.kt` | `Message.toDomain()`, `Domain.toMessage()` |

The domain layer contains no mappers—it doesn't know about external representations.

---

## Persistence Rules (Spring Data JDBC)

* Prefer `CrudRepository` + explicit queries when needed
* No ORM magic, no lazy loading expectations
* Aggregates are loaded fully or not at all
* Keep entities simple and **internal**
* Entity mappers live in persistence layer (ADR-004)

---

## Error Handling (RFC 7807 / Spring Boot 3)

* Domain/business failures are **data**, not exceptions:

    * Service methods return `Result<T, DomainError>`
    * Domain errors are sealed classes in `-api`
* Controllers map domain errors to HTTP responses using Spring Boot 3 `ProblemDetail`
* Global exception handling catches validation failures and unexpected errors (ADR-008)

---

## Validation (ADR-012)

Two-level validation strategy:

### Request DTOs (in `-api`)
Validate input at the boundary—fail fast on bad requests:

```kotlin
data class CreateProductRequest(
    val name: String,
    val priceAmount: BigDecimal,
) {
    init {
        require(name.isNotBlank()) { "Name is required" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

### Domain Objects (in `-impl`)
Enforce business invariants:

```kotlin
internal data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
    }
}
```

Response DTOs are plain data carriers—no validation needed.

---

## Build & Versioning (Gradle)

* Dependency versions via **Version Catalog** (`gradle/libs.versions.toml`)
* Shared build configuration via **build-logic convention plugins**
* Application version is centralized in `gradle.properties` (ADR-010)

---

## Testing Strategy

### Test Pyramid (ADR-015)

1. **Unit Tests** (~70%): domain logic, edge cases, no Spring, no DB
2. **Module Tests** (~20%): single module with real DB; other modules' APIs mocked
3. **Integration Tests** (~7%): adapters (repositories/controllers) where not covered by module tests
4. **Application / E2E Tests** (~3%): critical user journeys across modules

### Feature Files (ADR-007)

* **Module feature tests**: isolated module, mocks for other modules' APIs
* **Application feature tests**: full wiring, real module interaction

### Shared Test Data

**Builders** (in `testFixtures` of `-api` modules):
* Use for module tests
* Feature files should be self-documenting with explicit values

```kotlin
fun buildProductDto(
    id: String = "prod-${UUID.randomUUID()}",
    name: String = "Test Product",
    priceAmount: BigDecimal = BigDecimal("29.99"),
) = ProductDto(id = id, name = name, ...)
```

**Worldview** (in `-worldview` modules):
* Use for **runtime seeding only**: local dev, demos, staging
* Use for **application-level tests** (not module tests)
* Module tests should NOT reference worldview data

---

## Coding Standards (Project Rules)

* Prefer `val` over `var`
* Avoid `!!`
* Prefer early returns over deep nesting
* Keep modules clean: domain types never leak through APIs
* No cross-schema database joins in application code
* Keep code self-documenting (avoid explanatory inline comments where naming/structure can do the job)

---

## Working on the Project

### Adding a Module (Checklist)

Each module should provide:

1. `-api`: interfaces, DTOs (request DTOs with validation), events, sealed errors (+ testFixtures with builders)
2. `-impl`: domain + handlers/services + infrastructure (web + persistence)
3. `-worldview`: realistic named data + loaders for runtime seeding (optional but encouraged)
4. Flyway migrations under the module's schema path
5. Tests across the pyramid (unit + module; integration where needed; E2E only for critical flows)

### When to Use Which Internal Structure

**Flat structure** when:
- Few operations (< 5 service methods)
- Simple CRUD operations
- Single developer on module

**Vertical slices** when:
- Many distinct features
- Features have supporting classes
- Multiple developers on module
- Service class is getting large

---

## When You Need Help

Start with the relevant ADR in `docs/architecture/decisions/`.

If you're making a change that alters boundaries, contracts, persistence strategy, or testing approach:

* Add or update an ADR first (or in the same PR).
