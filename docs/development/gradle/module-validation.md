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

The plugin enforces two levels of validation:

### Default Rules (always enforced)

1. **API modules cannot depend on impl modules** - Modules ending in `-api` or `:api` cannot depend on modules ending in
   `-impl` or `:impl`
2. **Impl modules cannot depend on other impl modules** - Use the corresponding `-api` module instead

### Module Whitelist (when configured)

When `allowedDependencies` is configured, modules can only depend on explicitly listed modules.

### Excluded Modules

Modules matching `excludedModules` prefixes (e.g., `:application`, `:test`) are completely excluded from all validation
rules. This is typically used for composition roots that need to wire everything together.

### Validation Matrix

The following table shows which dependencies are allowed for each configuration type:

| Configuration                | Target                            | Impl→Impl Rule | Whitelist Rule    |
|------------------------------|-----------------------------------|----------------|-------------------|
| `api`                        | `:other:other-api`                | ✅ Allowed      | ✅ If in whitelist |
| `api`                        | `:other:other-impl`               | ❌ Blocked      | ❌ Blocked         |
| `implementation`             | `:other:other-api`                | ✅ Allowed      | ✅ If in whitelist |
| `implementation`             | `:other:other-impl`               | ❌ Blocked      | ❌ Blocked         |
| `testImplementation`         | `:other:other-api`                | ✅ Allowed      | ✅ If in whitelist |
| `testImplementation`         | `:other:other-impl`               | ❌ Blocked      | ❌ Blocked         |
| `testImplementation`         | `testFixtures(:other:other-impl)` | ✅ Allowed      | ✅ If in whitelist |
| `testFixturesImplementation` | `:other:other-api`                | ✅ Allowed      | ✅ If in whitelist |
| `testFixturesImplementation` | `:other:other-impl`               | ❌ Blocked      | ❌ Blocked         |
| `testFixturesImplementation` | `testFixtures(:other:other-impl)` | ❌ Blocked      | ❌ Blocked         |

**Key points:**

- Only `testImplementation` can depend on `testFixtures` of other impl modules
- `testFixturesImplementation` must follow the same rules as production code
- Whitelist rules apply to all configurations including test configurations
- If whitelist is not configured, only the impl→impl rule is enforced

### Test Exception

Test code (`testImplementation`) may depend on `testFixtures` of impl modules without violating the impl→impl rule.
However, the module must still be in the `allowedDependencies` whitelist if configured.

## Configuration

```kotlin
moduleDependencyValidation {
    failOnViolation.set(true)
    excludedModules.set(setOf(":application", ":test"))
    allowedDependencies.set(
        mapOf(
            "module-a" to setOf("module-b", "module-c"),
            "module-b" to setOf("module-c"),
            "module-c" to emptySet(),
        )
    )
}
```

### Options

| Option                | Type                       | Default      | Description                                                                                   |
|-----------------------|----------------------------|--------------|-----------------------------------------------------------------------------------------------|
| `failOnViolation`     | `Boolean`                  | `true`       | Fail build on violations. Set to `false` to log warnings only.                                |
| `excludedModules`     | `Set<String>`              | `emptySet()` | Project paths to exclude (e.g., composition roots like `:application`). Uses prefix matching. |
| `reportFile`          | `RegularFile`              | not set      | Path to write a Markdown report file. Useful for CI integration.                              |
| `allowedDependencies` | `Map<String, Set<String>>` | `emptyMap()` | Defines which modules a module may depend on. Dependencies are always on the api module only. |

### Module Names

Module names are extracted from project paths by removing the `-api` or `-impl` suffix:

- `:orders:orders-impl` → `orders`
- `:products:impl` → `products`

### Unconfigured Modules

Modules not present in `allowedDependencies` are not validated for dependency direction. The default api/impl rules
still apply.

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
