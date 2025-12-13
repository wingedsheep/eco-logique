# ADR-016: Automated Module Dependency Validation

**Status**: Accepted  
**Date**: 2024-12-13  
**Related ADRs**: [ADR-001: Modular Monolith](ADR-001-modular-monolith-architecture.md), [ADR-010: Gradle Build Configuration](ADR-010-gradle-build-configuration-and-versioning-strategy.md)

---

## Decision

We enforce architectural boundaries automatically using a custom Gradle plugin (`buildlogic.module-validation`). This plugin runs during the build process to verify that module dependencies adhere to our Modular Monolith rules.

---

## Context

In a Modular Monolith, maintaining loose coupling is critical. Without automated enforcement, two common forms of architectural erosion occur:

1. **Leaking Implementation Details**: Module A depends directly on Module B's implementation (`-impl`) instead of its contract (`-api`).
2. **Spaghetti Dependencies**: Modules begin depending on each other arbitrarily, violating the intended domain flow.

While Gradle prevents circular dependencies, it does not prevent a module from accessing another module's internal classes if the dependency is declared.

---

## Validation Rules

The plugin enforces the following rules on every `check` or `build` run:

### 1. The "Contract" Rule (Api → Impl)

**Rule**: An `-api` module must **never** depend on an `-impl` module.

- **Reason**: The API defines the contract. Depending on implementation details creates a circular conceptual dependency and leaks internals.

### 2. The "Encapsulation" Rule (Impl → Impl)

**Rule**: An `-impl` module must **never** depend on another `-impl` module.

- **Reason**: Cross-module interaction must happen exclusively through public APIs (interfaces and DTOs).
- **Correction**: Change the dependency to point to the target's `-api` module.

### 3. The "Whitelist" Rule (Domain Flow)

**Rule**: If a whitelist is configured, a module can only depend on modules explicitly listed in `allowedDependencies`.

- **Reason**: This prevents arbitrary coupling between unrelated domains (e.g., `Inventory` should not know about `Payment` details unless explicitly allowed).

### Exception: Test Fixtures

Tests within an `-impl` module are allowed to depend on the `testFixtures` of another `-impl` module. This is permitted to allow integration tests to set up complex infrastructure states (like database seeds) that are not part of the public API.

---

## Implementation

The logic is encapsulated in `build-logic/src/main/kotlin/buildlogic.module-validation.gradle.kts`.

### Usage

The plugin is applied in the root `build.gradle.kts` and configured via the `moduleDependencyValidation` extension:

```kotlin
moduleDependencyValidation {
    failOnViolation.set(true)

    // Optional: Whitelist specific domain flows
    allowedDependencies.set(
        mapOf(
            "shipping" to setOf("products", "orders"),
            "orders" to setOf("products", "inventory")
        )
    )
}
````

### Reporting

The task `./gradlew validateModuleDependencies` produces a Markdown report at:

* `build/reports/module-validation/report.md`

This report is integrated into CI pipelines (GitHub Actions) to provide immediate feedback on architectural violations.

---

## Consequences

### Positive

* **Architectural Fitness Function**: The architecture is verified by code, not just documentation.
* **Immediate Feedback**: Developers are notified instantly if they import an internal class from another module.
* **Clean Classpaths**: Ensures that `-impl` classes are physically inaccessible to other modules at compilation time.

### Negative

* **Configuration Overhead**: Adding legitimate new dependencies requires updating the whitelist (if strictly configured).
* **Build Logic Complexity**: Maintenance of the custom Gradle plugin code.
