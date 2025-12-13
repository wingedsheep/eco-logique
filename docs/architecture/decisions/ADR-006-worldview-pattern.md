# ADR-006: Worldview Pattern for Domain Knowledge

| Field            | Value                                                                                                                                   |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **Status**       | Accepted                                                                                                                                |
| **Date**         | 2024-12-13                                                                                                                              |
| **Related ADRs** | [ADR-001: Modular Monolith](ADR-001-modular-monolith-architecture.md), [ADR-002: Domain-Driven Design](ADR-002-domain-driven-design.md) |

---

## Decision

We implement **Worldview Modules** (`-worldview`) alongside API and Implementation modules. These modules contain
realistic domain data defined as code, builders, and data loaders.

Unlike Gradle `testFixtures`, which are limited to test scopes, a separate module allows this data to be used at *
*runtime** (e.g., for seeding development databases, demo modes, or documentation generation) as well as in tests.

---

## Structure

Each domain consists of three artifacts:

```text
products/
├── products-api/          # Public contract (Interfaces, DTOs)
├── products-impl/         # Business logic & Persistence
└── products-worldview/    # Shared Domain Knowledge
    ├── src/main/kotlin/
    │   └── com/example/products/worldview/
    │       ├── WorldviewProduct.kt         # Named, realistic instances
    │       ├── ProductBuilder.kt           # Builders for API DTOs
    │       └── WorldviewProductLoader.kt   # Spring component to seed DB
````

### Dependencies

1. **Worldview** depends on **API** (to construct DTOs).
2. **Impl** (test scope) depends on **Worldview**.
3. **Application** (runtime) depends on **Worldview** (optional, for dev profiles).

```kotlin
// products-worldview/build.gradle.kts
dependencies {
    api(project(":products:products-api")) // Constructs API objects
}

// products-impl/build.gradle.kts
dependencies {
    testImplementation(project(":products:products-worldview"))
}

// application/build.gradle.kts
dependencies {
    implementation(project(":products:products-worldview"))
}
```

---

## Rationale: Why not `testFixtures`?

While Gradle's `java-test-fixtures` plugin is excellent for sharing test code, it has significant limitations for our
use case:

1. **Runtime Unavailability**
   Test fixtures are not available on the main classpath. We cannot use them to seed a local development database or run
   a "Demo Mode" in production.

2. **Consumer Perspective**
   By forcing the Worldview module to depend **only** on the `-api` module (and not internals), we ensure our test data
   represents valid **consumer usage**. It forces us to "eat our own dog food" regarding the API design.

3. **Documentation**
   A separate module is more visible as "Living Documentation" to new developers than a hidden folder inside
   `src/testFixtures`.

---

## Worldview Objects

Define realistic, named instances using public API DTOs:

```kotlin
// products-worldview/.../WorldviewProduct.kt
object WorldviewProduct {

    val organicCottonTShirt = ProductDto(
        id = "PROD-001",
        name = "Organic Cotton T-Shirt",
        description = "Soft, breathable t-shirt made from 100% organic cotton. GOTS certified.",
        category = "CLOTHING",
        priceAmount = BigDecimal("29.99"),
        priceCurrency = "EUR",
        weightGrams = 150,
        sustainabilityRating = "A_PLUS",
        carbonFootprintKg = BigDecimal("2.1")
    )

    val allProducts = listOf(organicCottonTShirt /*, ... */)
}
```

---

## Data Loaders

Worldview modules can contain Spring components to seed data automatically when specific profiles are active.

```kotlin
// products-worldview/.../WorldviewProductDataLoader.kt
@Component
@Profile("local", "demo")
class WorldviewProductDataLoader(
    private val productService: ProductService
) {
    @PostConstruct
    fun load() {
        WorldviewProduct.allProducts.forEach { dto ->
            val request = dto.toCreateRequest()
            productService.createProduct(request)
        }
    }
}
```

---

## Usage in Tests

### Cucumber / Integration Tests

Reference entities by their Worldview name. This creates a ubiquitous language between the tests and the code.

```gherkin
Feature: Checkout Flow

  Scenario: Customer orders eco-product
    Given the product "Organic Cotton T-Shirt" exists
    When the customer places an order
    Then the order should be successful
```

```kotlin
// ProductSteps.kt
@Given("the product {string} exists")
fun productExists(productName: String) {
    val productDto = WorldviewProduct.findByName(productName)
        ?: throw IllegalArgumentException("Unknown worldview product: $productName")

    // Use the API to create the product (Black Box Testing)
    productService.createProduct(productDto.toRequest())
}
```

---

## Consequences

### Positive

* **Runtime Availability**: Data can be used for local development, demos, and seeding staging environments.
* **API Validation**: Since Worldview only sees the `-api` module, it validates that the API is sufficient to express
  complex domain scenarios.
* **Living Documentation**: Serves as a catalog of "canonical" business examples.
* **Reuse**: The same data is used for Unit Tests, Integration Tests, E2E Tests, and Local Development.

### Negative

* **Module Overhead**: Adds an extra Gradle module per domain.
* **DTO Maintenance**: If API DTOs change, Worldview data must be updated (though this ensures breaking changes are
  caught early).
