# AGENTS.md

## Project Overview

**Eco-nomique** is a modular monolith e-commerce application for eco-friendly products, built with Kotlin, Spring Boot, and Domain-Driven Design principles.

---

## Quick Reference

### Architecture
- **Style**: Modular Monolith with DDD
- **Modules**: Payment, Products, Shipping, Inventory, Users
- **Structure**: Single module per domain (no api/impl split)
- **Communication**: Synchronous (direct calls) + Asynchronous (events)
- **Database**: PostgreSQL with separate schemas per module

### Key Documentation
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
│   ├── payment/                 # Payment domain module
│   ├── products/                # Products domain module
│   ├── shipping/                # Shipping domain module
│   ├── inventory/               # Inventory domain module
│   ├── users/                   # Users domain module
│   ├── worldview-loader/        # Loads worldview data on startup
│   └── test/                    # Cucumber E2E tests
├── docker/                      # Local dev environment
└── docs/                        # All documentation
```
