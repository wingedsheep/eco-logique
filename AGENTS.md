# AGENTS.md

## Project Overview

**Eco-nomique** is a modular monolith e-commerce application for eco-friendly products, built with Kotlin, Spring Boot, and Domain-Driven Design principles.

---

## Quick Reference

### Architecture
- **Style**: Modular Monolith with DDD
- **Modules**: Payment, Products, Shipping, Inventory, Users
- **Structure**: Each module has `-api`, `-impl`, `-worldview` submodules
- **Communication**: Synchronous (direct calls) + Asynchronous (events)
- **Database**: PostgreSQL with separate schemas per module

### Key Documentation
- [Building a Modular Monolith](docs/principles.md) - Complete architecture guide
- [Kotlin Coding Guidelines](docs/coding-guidelines.md) - Code standards and examples
- [Backlog](backlog.md) - Development roadmap with acceptance criteria

### Architecture Decisions
All ADRs in `docs/architecture/decisions/`:
- [ADR-001: Modular Monolith](docs/architecture/decisions/ADR-001.md)
- [ADR-002: Domain-Driven Design](docs/architecture/decisions/ADR-002.md)
- [ADR-003: Separate Database Schemas](docs/architecture/decisions/ADR-003.md)
- [ADR-004: Mappers in Persistence Layer](docs/architecture/decisions/ADR-004.md)
- [ADR-005: Event-Driven Communication](docs/architecture/decisions/ADR-005.md)
- [ADR-006: Worldview Pattern](docs/architecture/decisions/ADR-006.md)
- [ADR-007: Feature File Tests Strategy](docs/architecture/decisions/ADR-007.md)
- [ADR-009: Hexagonal Module Architecture](docs/architecture/decisions/ADR-009.md)

---

## Project Structure

```
economique/
├── common/                      # Shared domain primitives
│   ├── common-money/
│   ├── common-country/
│   └── common-time/
├── deployables/economique/
│   ├── application/             # Spring Boot app (wires everything)
│   ├── domain/
│   │   ├── payment/
│   │   │   ├── payment-api/
│   │   │   ├── payment-impl/
│   │   │   └── payment-worldview/
│   │   ├── products/
│   │   ├── shipping/
│   │   ├── inventory/
│   │   └── users/
│   └── test/                    # Cucumber E2E tests
├── docker/                      # Local dev environment
└── docs/                        # All documentation
```

---

## Critical Rules

### Module Dependencies
- Modules depend **only on other modules' `-api`**, never `-impl`
- No circular dependencies (enforced by Gradle)
- Dependency matrix documented in ADR-001

### Data Isolation
- Each module owns its PostgreSQL schema exclusively
- No foreign keys across schemas
- Cross-module data access **only through service APIs**

### Mappers
- **Entity mappers**: In `persistence/` package, marked `internal`
- **Request/Response mappers**: In `rest/` package
- Entities **never leak** outside persistence layer

### Error Handling
- Use Kotlin's `Result<T>` for recoverable errors
- Use `require`/`check` for preconditions/invariants
- Avoid custom exception types

### Naming
- **Taxonomic**: Generic to specific (`ProductInventoryRepository`, not `InventoryProductRepository`)
- **Repository methods**: `find<X>` returns nullable, `get<X>` returns non-nullable
- **Test names**: `` `method should expected result when context` ``

### Code Quality
- No inline comments (self-documenting code)
- No `!!` operator (handle nullability properly)
- Validation in `init` blocks, not annotations
- SOLID principles

---

## Working on the Project

### Adding a Module
See backlog item template - each module needs:
1. Domain models in `-api` (no Spring annotations)
2. Service interface in `-api`
3. Service implementation in `-impl`
4. REST controllers in `-impl/rest/v1/`
5. Repository in `-impl/persistence/` (returns domain types)
6. Entities in `-impl/persistence/` (marked `internal`)
7. Entity mappers in `-impl/persistence/` (marked `internal`)
8. Flyway migration in `application/src/main/resources/db/migration/{module}/`
9. Worldview data in `-worldview`
10. Tests (unit, integration, Cucumber)

### Testing Strategy
- **Unit tests**: Service logic with mocked dependencies
- **Integration tests**: Repository with real database (Testcontainers)
- **Cucumber tests**: E2E workflows using worldview data by name

### Worldview Pattern
- Realistic domain data as code (e.g., `WorldviewProduct.organicCottonTShirt`)
- Used for testing, documentation, and local dev
- Reference by name in Cucumber: `Given the product "Organic Cotton T-Shirt" exists`

---

## Development Workflow

1. **Start environment**: `docker-compose up -d`
2. **Run application**: `./gradlew bootRun`
3. **Run tests**: `./gradlew test`
4. **Swagger UI**: `http://localhost:8080/swagger-ui.html`
5. **View backlog**: Check `backlog.md` for current sprint items

---

## When You Need Help

- **Architecture questions**: Read `docs/principles.md` and relevant ADRs
- **Coding standards**: Check `docs/coding-guidelines.md` with domain examples
- **Module structure**: Look at existing Products module as reference
- **Testing patterns**: See examples in `coding-guidelines.md` testing section
- **Backlog items**: Each has detailed acceptance criteria

---

## Key Principles

1. **Modules are bounded contexts** - clear boundaries, own their data
2. **Dependencies point inward** - infrastructure → application → domain
3. **Events for loose coupling** - use when eventual consistency acceptable
4. **Worldview is shared vocabulary** - realistic data everyone understands
5. **Keep it simple** - don't do more than required

For detailed explanations, examples, and rationale, refer to the documentation in `docs/`.
