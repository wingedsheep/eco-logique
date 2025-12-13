# ADR-006: Worldview Pattern for Domain Knowledge

**Status**: Accepted

**Date**: 2024-11-02

**Updated**: 2025-11-30

**Related ADRs**: [ADR-001: Modular Monolith](ADR-001-modular-monolith-architecture.md), [ADR-002: Domain-Driven Design](ADR-002-domain-driven-design.md)

---

## Decision

Each module contains **realistic domain data as code** in test fixtures. This serves as test data, living documentation, and shared domain vocabulary.

---

## Structure

Worldview lives in `-impl/src/testFixtures` alongside builders:

```
products/
├── products-api/
│   └── src/testFixtures/          # Builders for public DTOs
└── products-impl/
    ├── src/main/kotlin/           # Implementation
    └── src/testFixtures/kotlin/   # Worldview + domain builders
        └── com/example/products/
            ├── WorldviewProduct.kt    # Realistic product instances
            └── ProductBuilder.kt      # Test data builders
```

**Dependencies**:

```kotlin
// products-impl/build.gradle.kts
dependencies {
    testFixturesApi(project(":products:products-api"))
    testFixturesApi(testFixtures(project(":products:products-api")))
}

// Other modules can use worldview data in their tests
// orders-impl/build.gradle.kts
dependencies {
    testImplementation(testFixtures(project(":products:products-impl")))
}
```

---

## Design Trade-offs

### Why Test Fixtures (Not a Separate Module)

| Aspect | Test Fixtures in `-impl` | Separate `-worldview` Module |
|--------|--------------------------|------------------------------|
| Access to internal types | ✓ Yes (can use domain entities) | ✗ No (uses public DTOs only) |
| Data loading | Direct repository access | Through public API |
| Simplicity | ✓ No extra module | ✗ Additional module to maintain |
| Local development | Requires loader configuration | Easy to include/exclude |
| Test data colocation | ✓ Builders and worldview together | Spread across modules |

**Rationale**: Keeping worldview in test fixtures allows direct use of domain types, keeps builders and worldview data together, and avoids module proliferation.

---

## Worldview Objects

Define realistic, named instances using domain types:

```kotlin
// products-impl/src/testFixtures/kotlin/.../WorldviewProduct.kt
object WorldviewProduct {

    val organicCottonTShirt = buildProduct(
        name = ProductName("Organic Cotton T-Shirt"),
        description = "Soft, breathable t-shirt made from 100% organic cotton. GOTS certified.",
        category = ProductCategory.CLOTHING,
        price = Money(BigDecimal("29.99"), Currency.EUR),
        weight = Weight.grams(150),
        sustainabilityRating = SustainabilityRating.A_PLUS,
        carbonFootprint = CarbonFootprint.kg(BigDecimal("2.1"))
    )

    val bambooToothbrushSet = buildProduct(
        name = ProductName("Bamboo Toothbrush Set (4 pack)"),
        description = "Eco-friendly bamboo toothbrushes with soft BPA-free bristles.",
        category = ProductCategory.PERSONAL_CARE,
        price = Money(BigDecimal("12.50"), Currency.EUR),
        weight = Weight.grams(80),
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprint = CarbonFootprint.kg(BigDecimal("0.4"))
    )

    val solarPoweredCharger = buildProduct(
        name = ProductName("Solar Powered Phone Charger"),
        description = "Portable solar charger with 10000mAh battery. Waterproof and durable.",
        category = ProductCategory.ELECTRONICS,
        price = Money(BigDecimal("45.00"), Currency.EUR),
        weight = Weight.grams(300),
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprint = CarbonFootprint.kg(BigDecimal("3.2"))
    )

    val allProducts = listOf(
        organicCottonTShirt,
        bambooToothbrushSet,
        solarPoweredCharger
    )

    fun findByName(name: String): Product? =
        allProducts.find { it.name.value == name }
}
```

---

## Builder Functions

Builders live alongside worldview in test fixtures:

```kotlin
// products-impl/src/testFixtures/kotlin/.../ProductBuilder.kt
fun buildProduct(
    id: ProductId = ProductId.generate(),
    name: ProductName = ProductName("Test Eco Product"),
    description: String = "A sustainable test product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    price: Money = Money(BigDecimal("19.99"), Currency.EUR),
    weight: Weight = Weight.grams(100),
    sustainabilityRating: SustainabilityRating = SustainabilityRating.B,
    carbonFootprint: CarbonFootprint = CarbonFootprint.kg(BigDecimal("1.5"))
): Product = Product(
    id = id,
    name = name,
    description = description,
    category = category,
    price = price,
    weight = weight,
    sustainabilityRating = sustainabilityRating,
    carbonFootprint = carbonFootprint
)
```

---

## Usage in Tests

### Unit Tests

Use builders directly:

```kotlin
@Test
fun `calculateShippingCost should apply correct rate`() {
    val product = buildProduct(
        weight = Weight.grams(300),
        category = ProductCategory.ELECTRONICS
    )
    // ...
}
```

### Integration Tests

Use worldview for realistic data:

```kotlin
@SpringBootTest
class ProductServiceIntegrationTest {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    fun `should calculate correct sustainability rating for electronics`() {
        // Given
        productRepository.save(WorldviewProduct.solarPoweredCharger)

        // When
        val result = productService.findByCategory(ProductCategory.ELECTRONICS)

        // Then
        assertThat(result).containsExactly(WorldviewProduct.solarPoweredCharger)
    }
}
```

### Cucumber Tests

Reference worldview by name:

```gherkin
Feature: Checkout Flow

  Scenario: Customer orders eco-product
    Given the product "Organic Cotton T-Shirt" exists
    When the customer places an order
    Then the order should contain "Organic Cotton T-Shirt"
```

```kotlin
// ProductSteps.kt
@Given("the product {string} exists")
fun productExists(productName: String) {
    val product = WorldviewProduct.findByName(productName)
        ?: throw IllegalArgumentException("Unknown worldview product: $productName")

    productRepository.save(product)
}
```

---

## Principles

### Be Realistic

Use actual product names, weights, prices:

```kotlin
// ✓ Good - Realistic
val organicCottonTShirt = buildProduct(
    name = ProductName("Organic Cotton T-Shirt"),
    weight = Weight.grams(150),
    price = Money(BigDecimal("29.99"), Currency.EUR)
)

// ✗ Bad - Generic
val productA = buildProduct(
    name = ProductName("Product A"),
    weight = Weight.grams(100),
    price = Money(BigDecimal("10.00"), Currency.EUR)
)
```

### Cover Edge Cases

Include boundary conditions:

```kotlin
object WorldviewProduct {
    // Very light product (minimum shipping weight considerations)
    val paperStraw = buildProduct(weight = Weight.grams(5))

    // Very heavy product (special handling required)
    val solarPanel = buildProduct(weight = Weight.grams(22500))

    // High value (triggers insurance requirements)
    val premiumSolarKit = buildProduct(price = Money(BigDecimal("999.99"), Currency.EUR))
}
```

### Document with Names

Worldview object names should be self-explanatory:

```kotlin
// ✓ Good - Descriptive names
WorldviewProduct.organicCottonTShirt
WorldviewUser.johnDoeNetherlands
WorldviewWarehouse.amsterdamFulfillmentCenter

// ✗ Bad - Generic names
WorldviewProduct.product1
WorldviewUser.testUser
```

---

## Worldview as Documentation

New team members can browse worldview to understand the domain:

```kotlin
// Products show what eco-products look like
WorldviewProduct.organicCottonTShirt

// Users show customer personas
object WorldviewUser {
    val johnDoeNetherlands = buildUser(
        name = UserName("John Doe"),
        email = Email("john.doe@example.com"),
        country = Country.NL
    )

    val hansMullerGermany = buildUser(
        name = UserName("Hans Müller"),
        email = Email("hans.mueller@example.de"),
        country = Country.DE
    )
}

// Warehouses show fulfillment structure
object WorldviewWarehouse {
    val amsterdam = buildWarehouse(
        name = WarehouseName("Amsterdam Fulfillment Center"),
        country = Country.NL
    )
}
```

---

## Consequences

### Positive

- Shared vocabulary across team
- Realistic data catches integration issues
- Living documentation (can't drift from code)
- New developers learn domain quickly
- Direct access to domain types (more expressive)
- Builders and worldview colocated
- No extra modules to maintain

### Negative

- Need to keep worldview data updated
- Duplicate worldview objects if same data needed in multiple modules
- Test fixtures dependency chain can grow

---

## Best Practices

1. **Use realistic values**: actual weights, prices, names from the domain
2. **Cover edge cases**: heavy/light, expensive/cheap, cross-border scenarios
3. **Document with names**: `organicCottonTShirt`, not `product1`
4. **Reference by name in Cucumber**: "Organic Cotton T-Shirt", not by ID
5. **Keep builders and worldview together**: both in `-impl/src/testFixtures`
6. **Use domain types**: leverage the expressiveness of internal types
