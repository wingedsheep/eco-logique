# ADR-006: Worldview Pattern for Domain Knowledge

**Status**: Accepted

**Date**: 2024-12-13

**Updated**: 2025-01-15

---

## Decision

We implement **Worldview Modules** (`-worldview`) alongside API and Implementation modules. These modules contain realistic domain data for runtime seeding—local development, demos, staging environments.

**Important**: Module tests should NOT use worldview data. Feature files should be self-documenting with explicit values.

---

## Structure

```
products/
├── products-api/
├── products-impl/
└── products-worldview/
    └── src/main/kotlin/
        └── com/example/products/worldview/
            ├── WorldviewProducts.kt        # Named, realistic instances
            └── WorldviewProductLoader.kt   # Spring component to seed DB
```

### Dependencies

```kotlin
// products-worldview/build.gradle.kts
dependencies {
    api(project(":products:products-api"))
}

// application/build.gradle.kts (for local dev seeding)
dependencies {
    implementation(project(":products:products-worldview"))
}
```

---

## Worldview Objects

```kotlin
// products-worldview/WorldviewProducts.kt
object WorldviewProducts {
    val organicCottonTShirt = ProductDto(
        id = "PROD-001",
        name = "Organic Cotton T-Shirt",
        priceAmount = BigDecimal("29.99"),
        priceCurrency = "EUR",
        weightGrams = 150,
    )

    val allProducts = listOf(organicCottonTShirt, /* ... */)
}
```

---

## Data Loaders

```kotlin
@Component
@Profile("local", "demo")
class WorldviewProductLoader(
    private val productService: ProductServiceApi,
) {
    @PostConstruct
    fun load() {
        WorldviewProducts.allProducts.forEach { dto ->
            productService.createProduct(dto.toCreateRequest())
        }
    }
}
```

---

## Usage Guidelines

### Module Tests: DO NOT Use Worldview

Feature files should be self-contained:

```gherkin
# Bad - reader must look up the price
Given the worldview product "Organic Cotton T-Shirt" exists
When a 20% discount is applied
Then the price should be €23.99

# Good - scenario is self-documenting
Given a product "Organic Cotton T-Shirt" with price €29.99
When a 20% discount is applied
Then the price should be €23.99
```

Module tests mock external dependencies, so worldview data isn't even in the database. Use builders with explicit values instead.

### Application-Level Tests: MAY Use Worldview

These tests run against the real system with seeded data:

```gherkin
# Application-level test - worldview data is seeded
Given I am logged in as a user
When I add "Organic Cotton T-Shirt" to my cart
And I complete checkout
Then an order should be created
```

---

## Consequences

### Positive
- Runtime availability for local dev and demos
- Living documentation of canonical business examples
- API validation (worldview only sees `-api`)

### Negative
- Extra module per domain
- Must update when API DTOs change
