# ADR-016: Automated Module Dependency Validation

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We enforce architectural boundaries using a custom Gradle plugin that verifies module dependencies during build.

---

## Validation Rules

### 1. The "Contract" Rule

**An `-api` module must never depend on an `-impl` module.**

The API defines the contract. Depending on implementation creates circular conceptual dependency.

### 2. The "Encapsulation" Rule

**An `-impl` module must never depend on another `-impl` module.**

Cross-module interaction happens through APIs only.

### 3. The "Whitelist" Rule (Optional)

If configured, a module can only depend on explicitly allowed modules.

### Exception: Test Fixtures

Tests in `-impl` may depend on `testFixtures` of another `-impl` for integration test setup.

---

## Configuration

```kotlin
// root build.gradle.kts
moduleDependencyValidation {
    failOnViolation.set(true)

    allowedDependencies.set(mapOf(
        "shipping" to setOf("products", "orders"),
        "orders" to setOf("products", "inventory"),
    ))
}
```

---

## Reporting

```bash
./gradlew validateModuleDependencies
```

Produces report at `build/reports/module-validation/report.md`.

---

## Consequences

### Positive
- Architecture verified by code, not documentation
- Immediate feedback on violations
- Clean classpaths enforced at compile time

### Negative
- Configuration overhead for new dependencies
- Build logic maintenance
