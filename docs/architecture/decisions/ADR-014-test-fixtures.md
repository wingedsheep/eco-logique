# ADR-014: Use Gradle Test Fixtures for Shared Test Code

**Status:** Accepted  
**Date:** 2024-12-13

## Decision

We use the Gradle `java-test-fixtures` plugin to share test helpers, data builders (Object Mothers), and assertions
across module boundaries without polluting the production classpath or creating circular dependencies.

## Context

In a modular architecture, downstream modules often need to create valid instances of upstream objects for testing. For
example, the Shipping module needs to create valid `ProductDto` objects to test its integration with the Products
module.

### Alternatives considered

- **Duplication:** Copy-pasting builders into every module. *(High maintenance, data inconsistency risk).*
- **Shared common-test module:** A global module containing all test helpers. *(Leads to a "God object" dependency graph
  and tight coupling).*
- **Production Helpers:** putting builders in `src/main`. *(Pollutes production code with test concerns).*

## Implementation

### 1. Enabling the Plugin

The plugin is applied in our build-logic conventions (specifically `common-library.gradle.kts` and
`kotlin-conventions.gradle.kts`) so it is available by default.

```kotlin
plugins {
    `java-test-fixtures`
}
````

### 2. Defining Fixtures

Test fixtures live in `src/testFixtures/kotlin`. They can depend on the module's main source set.

**Example Location:**
`products-api/src/testFixtures/kotlin/com/wingedsheep/ecologique/products/api/ProductDtoBuilders.kt`

```kotlin
package com.wingedsheep.ecologique.products.api

import java.math.BigDecimal

// Shared Builder / Object Mother
fun buildProductDto(
    id: String = "PROD-123",
    name: String = "Default Product",
    priceAmount: BigDecimal = BigDecimal("10.00")
): ProductDto = ProductDto(
    id = id,
    name = name,
    priceAmount = priceAmount,
    // ...
)
```

### 3. Consuming Fixtures

Consumers declare a dependency on the test fixtures capability of the target project.

**Example:** `shipping-impl` testing against `products-api` data structures.

```kotlin
// shipping-impl/build.gradle.kts
dependencies {
    // Access Product DTOs
    implementation(project(":products:products-api"))

    // Access Product Builders for tests
    testImplementation(testFixtures(project(":products:products-api")))
}
```

## Guidelines

* **API Modules:** heavily utilize test fixtures to expose builders for their DTOs. This allows consumers to easily
  create valid request/response objects for mocking.
* **Impl Modules:** utilize test fixtures for internal integration test helpers (e.g., specific database setup helpers),
  though these are rarely shared with other modules.
* **No Test Logic:** Fixtures should contain data setup and helper code, not actual test cases (no `@Test` methods).
* **Dependencies:** Test fixtures can have their own dependencies, defined in `testFixturesImplementation`.

## Consequences

### Positive

* **Encapsulation:** Test code stays with the module that owns the domain/API.
* **Consistency:** A single source of truth for "how to create a valid X for testing."
* **Classpath Hygiene:** Test fixtures are not included in the production artifact (JAR/WAR).
* **Build Efficiency:** Gradle handles incremental compilation for fixtures separately from main and test sources.

### Negative

* **Complexity:** Adds another source set (`testFixtures`) to the project structure which may confuse developers
  unfamiliar with the plugin.
* **IDEs:** Requires modern IDE support (IntelliJ IDEA handles this natively).

```
```

````md
# ADR-014: Use Gradle Test Fixtures for Shared Test Code

**Status:** Accepted  
**Date:** 2024-12-13

## Decision

We use the Gradle `java-test-fixtures` plugin to share test helpers, data builders (Object Mothers), and assertions
across module boundaries without polluting the production classpath or creating circular dependencies.

## Context

In a modular architecture, downstream modules often need to create valid instances of upstream objects for testing. For
example, the Shipping module needs to create valid `ProductDto` objects to test its integration with the Products
module.

### Alternatives considered

- **Duplication:** Copy-pasting builders into every module. *(High maintenance, data inconsistency risk).*
- **Shared common-test module:** A global module containing all test helpers. *(Leads to a "God object" dependency graph
  and tight coupling).*
- **Production Helpers:** putting builders in `src/main`. *(Pollutes production code with test concerns).*

## Implementation

### 1. Enabling the Plugin

The plugin is applied in our build-logic conventions (specifically `common-library.gradle.kts` and
`kotlin-conventions.gradle.kts`) so it is available by default.

```kotlin
plugins {
    `java-test-fixtures`
}
````

### 2. Defining Fixtures

Test fixtures live in `src/testFixtures/kotlin`. They can depend on the module's main source set.

**Example Location:**
`products-api/src/testFixtures/kotlin/com/wingedsheep/ecologique/products/api/ProductDtoBuilders.kt`

```kotlin
package com.wingedsheep.ecologique.products.api

import java.math.BigDecimal

// Shared Builder / Object Mother
fun buildProductDto(
    id: String = "PROD-123",
    name: String = "Default Product",
    priceAmount: BigDecimal = BigDecimal("10.00")
): ProductDto = ProductDto(
    id = id,
    name = name,
    priceAmount = priceAmount,
    // ...
)
```

### 3. Consuming Fixtures

Consumers declare a dependency on the test fixtures capability of the target project.

**Example:** `shipping-impl` testing against `products-api` data structures.

```kotlin
// shipping-impl/build.gradle.kts
dependencies {
    // Access Product DTOs
    implementation(project(":products:products-api"))

    // Access Product Builders for tests
    testImplementation(testFixtures(project(":products:products-api")))
}
```

## Guidelines

* **API Modules:** heavily utilize test fixtures to expose builders for their DTOs. This allows consumers to easily
  create valid request/response objects for mocking.
* **Impl Modules:** utilize test fixtures for internal integration test helpers (e.g., specific database setup helpers),
  though these are rarely shared with other modules.
* **No Test Logic:** Fixtures should contain data setup and helper code, not actual test cases (no `@Test` methods).
* **Dependencies:** Test fixtures can have their own dependencies, defined in `testFixturesImplementation`.

## Consequences

### Positive

* **Encapsulation:** Test code stays with the module that owns the domain/API.
* **Consistency:** A single source of truth for "how to create a valid X for testing."
* **Classpath Hygiene:** Test fixtures are not included in the production artifact (JAR/WAR).
* **Build Efficiency:** Gradle handles incremental compilation for fixtures separately from main and test sources.

### Negative

* **Complexity:** Adds another source set (`testFixtures`) to the project structure which may confuse developers
  unfamiliar with the plugin.
* **IDEs:** Requires modern IDE support (IntelliJ IDEA handles this natively).
