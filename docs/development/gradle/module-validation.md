# Module Dependency Validation

The `buildlogic.module-validation` plugin validates module dependencies in a modular monolith architecture.

## Usage

Apply the plugin in your root `build.gradle.kts`:

```kotlin
plugins {
    id("buildlogic.module-validation")
}
```

Run validation with:

```bash
./gradlew validateModuleDependencies
```

The task also runs as part of `./gradlew check`.

## Rules

The plugin enforces the following rules:

### Module Type Rules

| From | To | Allowed? |
|------|-----|:--------:|
| api | api | ✓ |
| api | impl | ✗ |
| api | worldview | ✗ |
| impl | api | ✓ |
| impl | impl (cross-domain) | ✗ |
| impl | worldview | ✗ |
| worldview | api | ✓ |
| worldview | impl | ✗ |
| worldview | worldview | ✓ |

### Composition Roots

Modules in `compositionRoots` (e.g., `:application`) cannot be depended upon by any other module. They are leaf nodes in the dependency graph that wire everything together.

### Module Whitelist

When `allowedDependencies` is configured, modules can only depend on explicitly listed domains.

### Excluded Modules

Modules matching `excludedModules` prefixes are completely excluded from all validation rules. Use this for composition roots that need to depend on impl modules.

### Test Exception

Test code (`testImplementation`) may depend on `testFixtures` of api modules without violating cross-domain rules. The module must still be in the `allowedDependencies` whitelist if configured.

## Configuration

```kotlin
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
        )
    )
}
```

### Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `failOnViolation` | `Boolean` | `true` | Fail build on violations. Set to `false` to log warnings only. |
| `excludedModules` | `Set<String>` | `emptySet()` | Project paths to exclude from validation. Uses prefix matching. |
| `compositionRoots` | `Set<String>` | `emptySet()` | Modules that no other module may depend on (e.g., `:application`). |
| `reportFile` | `RegularFile` | not set | Path to write a Markdown report file. |
| `allowedDependencies` | `Map<String, Set<String>>` | `emptyMap()` | Cross-domain dependency whitelist. |

### Module Names

Module names are extracted from project paths by removing the suffix:

- `:orders:orders-impl` → `orders`
- `:products:products-api` → `products`
- `:users:users-worldview` → `users`

## CI Integration

Configure `reportFile` to generate a Markdown report for GitHub Actions:

```kotlin
moduleDependencyValidation {
    reportFile.set(layout.buildDirectory.file("reports/module-validation/report.md"))
}
```

Add this step to your workflow to display the report in the GitHub Actions summary:

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