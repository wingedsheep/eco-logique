# Module Dependency Validation

The `buildlogic.module-validation` plugin enforces module boundaries in a modular monolith by ensuring modules only depend on each other through their public APIs.

## Usage
```bash
./gradlew validateModuleDependencies
```

Also runs as part of `./gradlew check`.

## Core Principle

**Depend on abstractions, not implementations.**

| Allowed | Not Allowed |
|---------|-------------|
| `api` modules (public contracts) | `impl` modules from other domains |
| Library modules (e.g., `:common:common-money`) | `worldview` modules from other domains |

## Module Types

| Type | Purpose | Example |
|------|---------|---------|
| `api` | Public contracts (interfaces, DTOs) | `:orders:orders-api` |
| `impl` | Private implementation details | `:orders:orders-impl` |
| `worldview` | Test data factories and fixtures | `:orders:orders-worldview` |

## Rules

### Production Code

Dependencies flow **toward** API modules, never toward implementations:

```
┌─────────────────────────────────────────────────────────┐
│                      orders domain                      │
│                                                         │
│   orders-impl ──────► orders-api ◄────── orders-worldview
│                           │                             │
└───────────────────────────┼─────────────────────────────┘
                            │
              ┌─────────────┼─────────────┐
              │             ▼             │
              │        users-api          │
              │      (allowed dep)        │
              └───────────────────────────┘
```

- An `impl` module may only depend on its own `api` and on `api` modules from allowed domains
- Cross-domain dependencies must be declared in `allowedDependencies`
- Exception: `worldview` modules may depend on other `worldview` modules for composing test data

### Test Code

Tests and `testFixtures` follow the same rules, plus they may depend on `testFixtures` of allowed modules:

```
orders-impl/test can depend on:
  ├── orders-impl/testFixtures     (own module)
  ├── orders-api/testFixtures      (own domain)
  └── users-api/testFixtures       (allowed domain)
```

This enables sharing test builders across modules without exposing implementation details.

### Composition Roots

Modules listed in `compositionRoots` (e.g., `:application`) are leaf nodes that wire everything together. **No module may depend on a composition root.**

## Configuration
```kotlin
plugins {
    id("buildlogic.module-validation")
}

moduleDependencyValidation {
    failOnViolation.set(true)
    excludedModules.set(setOf(":application"))
    compositionRoots.set(setOf(":application"))
    reportFile.set(layout.buildDirectory.file("reports/module-validation/report.md"))
    allowedDependencies.set(
        mapOf(
            "products" to emptySet(),
            "users" to emptySet(),
            "orders" to setOf("products", "users"),
            "cart" to setOf("products", "users"),
        )
    )
}
```

| Option | Description |
|--------|-------------|
| `failOnViolation` | Fail build on violations (default: `true`) |
| `excludedModules` | Project path prefixes to skip validation |
| `compositionRoots` | Modules that nothing may depend on |
| `reportFile` | Path for markdown report |
| `allowedDependencies` | Cross-module dependency whitelist |

Module names are extracted from paths: `:orders:orders-impl` → `orders`

## CI Integration
```yaml
- name: Module Validation Report
  if: always()
  run: |
    REPORT_FILE="build/reports/module-validation/report.md"
    if [ -f "$REPORT_FILE" ]; then
      cat "$REPORT_FILE" >> "$GITHUB_STEP_SUMMARY"
    fi
```

## See Also

- [ADR-016: Module Dependency Strategy](../../architecture/decisions/ADR-016-module-dependency-direction-validation.md)
