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
- **Internal structure**: Hexagonal ("Hex Light") inside each module
- **Communication**: Synchronous (direct API calls) + Asynchronous (domain events)
- **Database**: PostgreSQL with **separate schema per module**
- **Persistence**: **Spring Data JDBC** (not JPA/Hibernate)

### Architecture Decisions

All ADRs live in `docs/architecture/decisions/`:

- ADR-001: Modular Monolith Architecture
- ADR-002: Domain-Driven Design
- ADR-003: Separate Database Schemas per Bounded Context
- ADR-004: Mappers in Persistence Layer
- ADR-005: Event-Driven Communication Between Modules
- ADR-006: Worldview Pattern for Domain Knowledge
- ADR-007: Feature File Tests Strategy
- ADR-008: Error Handling in REST Endpoints (RFC 7807 / `ProblemDetail`)
- ADR-009: Hexagonal Module Architecture (Light)
- ADR-010: Gradle Build Configuration & Versioning Strategy
- ADR-011: Spring Data JDBC over JPA
- ADR-012: Validation in `init` blocks
- ADR-014: Gradle Test Fixtures
- ADR-015: Test Pyramid Strategy

---

## Module Structure

Each domain is represented as a Gradle module group. The standard artifacts are:

```text
domain-name/
├── domain-name-api/         # Public Contract: interfaces, DTOs, events, errors (+ testFixtures)
├── domain-name-impl/        # The Hexagon: domain + application + infrastructure
└── domain-name-worldview/   # (ADR-006) Realistic domain data/builders usable at runtime (optional but encouraged)
````

### Dependency Rules (Hard Rules)

1. **`-impl` depends on its own `-api`**
2. Modules may depend **only** on other modules’ **`-api`** (never on `-impl`)
3. Cross-module test helpers:

    * Prefer `testFixtures(project(":other:other-api"))` (ADR-014)
    * Use `-worldview` when the data must be usable at **runtime** (ADR-006)
4. **No circular dependencies** (enforced by Gradle)

---

## Data Isolation

* One PostgreSQL **schema per module** (`payment`, `products`, `shipping`, `inventory`, `users`)
* No shared tables between modules
* **No foreign keys across schemas**
* Cross-module data access happens through **service APIs**, not database joins

Migrations are organized per module (Flyway locations per schema).

---

## Communication Between Modules

### Synchronous (Direct Calls)

Use when **immediate consistency** is required.

* Call interfaces defined in the target’s `-api`
* Exchange **DTOs**, never domain entities/value objects

### Asynchronous (Events)

Use when **eventual consistency** and loose coupling is acceptable.

* Events are defined in the publisher’s `-api`
* Multiple modules can listen without introducing compile-time coupling

---

## Hexagonal Layering (Per `-impl`)

**Domain (`-impl/domain`)**

* Pure Kotlin business logic
* No Spring dependencies
* Entities/value objects validate in `init` blocks (ADR-012)
* Repositories are **interfaces** defined in domain

**Application (`-impl/application`)**

* Implements `-api` services
* Orchestrates use cases: load → domain logic → save → publish event
* Maps **Domain ↔ DTOs**
* Returns `Result<T, E>` with sealed error hierarchies from `-api`

**Infrastructure (`-impl/infrastructure`)**

* Web controllers, persistence adapters, external clients
* Persistence uses Spring Data JDBC (ADR-011)
* Persistence entities + mappers stay internal (ADR-004)

---

## Persistence Rules (Spring Data JDBC)

* Prefer `CrudRepository` + explicit queries when needed
* No ORM magic, no lazy loading expectations
* Aggregates are loaded fully or not at all
* Keep entities simple and **internal**
* Mappers live next to entities in `infrastructure/persistence` (ADR-004)

---

## Error Handling (RFC 7807 / Spring Boot 3)

* Domain/business failures are **data**, not exceptions:

    * Service methods return `Result<T, DomainError>`
    * Domain errors are sealed classes in `-api`
* Controllers map domain errors to HTTP responses using Spring Boot 3 `ProblemDetail`
* Global exception handling is reserved for unexpected/framework errors (ADR-008)

---

## Validation

* Validate domain objects in Kotlin `init` blocks (ADR-012)
* Use `require` for preconditions, `check` for invariants
* Avoid JSR-303 annotations as the primary validation mechanism

---

## Build & Versioning (Gradle)

* Dependency versions via **Version Catalog** (`gradle/libs.versions.toml`)
* Shared build configuration via **build-logic convention plugins**
* Application version is centralized in `gradle.properties` (ADR-010)

---

## Testing Strategy

### Test Pyramid (ADR-015)

1. **Unit Tests**: domain logic, no Spring, no DB (most tests)
2. **Integration Tests**: adapters (repositories/controllers) with real DB/Testcontainers
3. **Module Tests**: single module behavior; other modules’ APIs mocked
4. **Application / E2E Tests**: critical user journeys across modules (fewest tests)

### Feature Files (ADR-007)

* Two levels of feature tests:

    * **Module feature tests**: isolated module, mocks for other modules’ APIs
    * **Application feature tests**: full wiring, real module interaction

### Shared Test Data

* Use **`testFixtures`** from `-api` modules for cross-module test builders (ADR-014)
* Use **`-worldview`** for realistic named examples (usable in tests and runtime seeding) (ADR-006)

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

1. `-api`: interfaces, DTOs, events, sealed errors (+ testFixtures)
2. `-impl`: domain + application + infrastructure (web + persistence)
3. `-worldview`: realistic named data/builders + optional loaders (where useful)
4. Flyway migrations under the module’s schema path
5. Tests across the pyramid (unit + integration + module; E2E only for critical flows)

---

## When You Need Help

Start with the relevant ADR in `docs/architecture/decisions/`.

If you’re making a change that alters boundaries, contracts, persistence strategy, or testing approach:

* Add or update an ADR first (or in the same PR).
