# Module Dependency Validation

The `buildlogic.module-validation` plugin enforces module boundaries in a modular monolith.

## Usage
```bash
./gradlew validateModuleDependencies
```

Also runs as part of `./gradlew check`.

## Core Principle

**Depend on abstractions, not implementations.**

- ✓ Depend on `api` modules
- ✓ Depend on library modules (no suffix, e.g. `:common:common-money`)
- ✗ Depend on `impl` or `worldview` modules across boundaries

## Rules

### Production Code (main)
```
impl  ───►  api  ◄───  api
             ▲
             │
worldview ───┘
```

Cross-module dependencies must go through `api` modules and be whitelisted in `allowedDependencies`.

The only exception: `worldview` modules may depend on other `worldview` modules for test data composition.

### Test Code

Tests and test fixtures follow the same principle with one addition: they may depend on `testFixtures` of allowed modules.
```
a:impl:test ────────►  a:impl:testFixtures
                       a:api:testFixtures
                       b:api:testFixtures   (if b is allowed)
```

This enables sharing test builders across modules without exposing implementation details.

### Composition Roots

Modules in `compositionRoots` (e.g., `:application`) are leaf nodes—no module may depend on them.

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
