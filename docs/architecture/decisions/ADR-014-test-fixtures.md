# ADR-014: Use Gradle Test Fixtures for Shared Test Code

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We use the Gradle `java-test-fixtures` plugin to share test helpers and builders across module boundaries.

---

## Implementation

### Enabling the Plugin

Applied in convention plugins:

```kotlin
plugins {
    `java-test-fixtures`
}
```

### Defining Fixtures

Test fixtures live in `src/testFixtures/kotlin`:

```kotlin
// products-api/src/testFixtures/kotlin/.../ProductBuilders.kt
fun buildProductDto(
    id: String = "prod-${UUID.randomUUID()}",
    name: String = "Test Product",
    priceAmount: BigDecimal = BigDecimal("29.99"),
    weightGrams: Int = 500,
) = ProductDto(
    id = id,
    name = name,
    priceAmount = priceAmount,
    priceCurrency = "EUR",
    weightGrams = weightGrams,
)
```

### Consuming Fixtures

```kotlin
// shipping-impl/build.gradle.kts
dependencies {
    implementation(project(":products:products-api"))
    testImplementation(testFixtures(project(":products:products-api")))
}
```

---

## Guidelines

- **API modules**: Expose builders for DTOs
- **Impl modules**: Internal test helpers (rarely shared)
- **No test logic**: Data setup only, no `@Test` methods

---

## Consequences

### Positive
- Encapsulation: test code stays with owning module
- Consistency: single source of truth for test data
- Classpath hygiene: not in production artifact

### Negative
- Adds `testFixtures` source set
- Requires IDE support (IntelliJ handles natively)
