# ADR-006: Worldview Pattern for Domain Knowledge

**Status**: Accepted

**Date**: 2024-11-02

**Updated**: 2024-11-05

**Related ADRs**: [ADR-001: Modular Monolith](ADR-001-modular-monolith-architecture.md), [ADR-002: Domain-Driven Design](ADR-002-domain-driven-design.md)

---

## Decision

Each module contains **realistic domain data as code** in its `-impl` test fixtures. This serves as test data, living documentation, and shared domain vocabulary.

---

## Structure

Worldview objects and builders live in `-impl/src/testFixtures`:

```
products-impl/
├── src/
│   ├── main/kotlin/
│   │   └── domain/model/
│   │       ├── Product.kt           # internal
│   │       ├── ProductId.kt         # internal
│   │       └── Money.kt             # internal
│   └── testFixtures/kotlin/
│       └── com/example/products/
│           ├── WorldviewProduct.kt  # Realistic product instances
│           └── ProductBuilder.kt    # Test data builders
```

**Dependencies**: Test fixtures can access internal types from the same module.

```kotlin
// products-impl/build.gradle.kts
plugins {
    `java-test-fixtures`
}

dependencies {
    // Test fixtures can use internal domain types from main
    testFixturesImplementation(project(":domain:products:products-impl"))
}
```

Other modules consume test fixtures in test scope:

```kotlin
// shipping-impl/build.gradle.kts
dependencies {
    testImplementation(testFixtures(project(":domain:products:products-impl")))
    testImplementation(testFixtures(project(":domain:inventory:inventory-impl")))
}
```

---

## Worldview Objects

Define realistic, named instances of domain concepts:

```kotlin
// products-impl/src/testFixtures/kotlin/.../WorldviewProduct.kt
object WorldviewProduct {
    val organicCottonTShirt = Product(
        id = ProductId("PROD-001"),
        name = "Organic Cotton T-Shirt",
        category = ProductCategory.CLOTHING,
        price = Money(BigDecimal("29.99"), EUR),
        weight = Weight(150, GRAMS),
        sustainabilityRating = SustainabilityRating.A_PLUS,
        carbonFootprint = CarbonFootprint(BigDecimal("2.1"), KG_CO2)
    )

    val bambooToothbrush = Product(
        id = ProductId("PROD-002"),
        name = "Bamboo Toothbrush Set (4 pack)",
        category = ProductCategory.HOUSEHOLD,
        price = Money(BigDecimal("12.50"), EUR),
        weight = Weight(80, GRAMS),
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprint = CarbonFootprint(BigDecimal("0.4"), KG_CO2)
    )

    val solarPoweredCharger = Product(
        id = ProductId("PROD-003"),
        name = "Solar Powered Phone Charger",
        category = ProductCategory.ELECTRONICS,
        price = Money(BigDecimal("45.00"), EUR),
        weight = Weight(300, GRAMS),
        sustainabilityRating = SustainabilityRating.A_PLUS,
        carbonFootprint = CarbonFootprint(BigDecimal("3.2"), KG_CO2)
    )

    val reusableWaterBottle = Product(
        id = ProductId("PROD-004"),
        name = "Stainless Steel Water Bottle 750ml",
        category = ProductCategory.HOUSEHOLD,
        price = Money(BigDecimal("18.99"), EUR),
        weight = Weight(250, GRAMS),
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprint = CarbonFootprint(BigDecimal("1.8"), KG_CO2)
    )
}
```

```kotlin
// users-impl/src/testFixtures/kotlin/.../WorldviewUser.kt
object WorldviewUser {
    val johnDoe = User(
        id = UserId("USER-001"),
        email = "john.doe@example.com",
        name = "John Doe",
        address = Address(
            street = "Prinsengracht 263",
            city = "Amsterdam",
            postalCode = "1016HV",
            country = Country.NETHERLANDS
        )
    )

    val hansMuller = User(
        id = UserId("USER-002"),
        email = "hans.mueller@example.de",
        name = "Hans Müller",
        address = Address(
            street = "Hauptstraße 42",
            city = "Berlin",
            postalCode = "10115",
            country = Country.GERMANY
        )
    )
}
```

---

## Builder Functions

Provide builders with sensible defaults:

```kotlin
// products-impl/src/testFixtures/kotlin/.../ProductBuilder.kt
fun buildProduct(
    id: ProductId = ProductId("PROD-TEST-${UUID.randomUUID()}"),
    name: String = "Test Product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    price: Money = Money(BigDecimal("19.99"), EUR),
    weight: Weight = Weight(100, GRAMS),
    sustainabilityRating: SustainabilityRating = SustainabilityRating.B,
    carbonFootprint: CarbonFootprint = CarbonFootprint(BigDecimal("1.5"), KG_CO2)
): Product = Product(id, name, category, price, weight, sustainabilityRating, carbonFootprint)
```

```kotlin
// inventory-impl/src/testFixtures/kotlin/.../InventoryItemBuilder.kt
fun buildInventoryItem(
    productId: ProductId = WorldviewProduct.organicCottonTShirt.id,
    warehouseId: WarehouseId = WorldviewWarehouse.amsterdam.id,
    quantity: Quantity = Quantity(100)
): InventoryItem = InventoryItem(productId, warehouseId, quantity)
```

---

## Worldview Data Loader

For local development and integration tests, the `application` module loads worldview data on startup. Since domain types are internal, insertion goes through repositories (which the application module wires together):

```kotlin
// application/src/main/kotlin/worldview/WorldviewDataLoader.kt
@Component
@Profile("!prod")
class WorldviewDataLoader(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val inventoryRepository: InventoryRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun loadWorldviewData() {
        logger.info("Loading worldview data...")
        loadProducts()
        loadUsers()
        loadInventory()
        logger.info("Worldview data loaded successfully")
    }

    private fun loadProducts() {
        listOf(
            WorldviewProduct.organicCottonTShirt,
            WorldviewProduct.bambooToothbrush,
            WorldviewProduct.solarPoweredCharger,
            WorldviewProduct.reusableWaterBottle
        ).forEach { product ->
            productRepository.save(product)
        }
    }

    private fun loadUsers() {
        listOf(
            WorldviewUser.johnDoe,
            WorldviewUser.hansMuller
        ).forEach { user ->
            userRepository.save(user)
        }
    }

    private fun loadInventory() {
        listOf(
            buildInventoryItem(
                productId = WorldviewProduct.organicCottonTShirt.id,
                quantity = Quantity(50)
            ),
            buildInventoryItem(
                productId = WorldviewProduct.bambooToothbrush.id,
                quantity = Quantity(100)
            )
        ).forEach { item ->
            inventoryRepository.save(item)
        }
    }
}
```

The application module includes test fixtures as a runtime dependency for local development:

```kotlin
// application/build.gradle.kts
dependencies {
    // Production dependencies
    implementation(project(":domain:products:products-impl"))
    implementation(project(":domain:users:users-impl"))
    implementation(project(":domain:inventory:inventory-impl"))
    
    // Worldview for local development (non-prod profiles)
    runtimeOnly(testFixtures(project(":domain:products:products-impl")))
    runtimeOnly(testFixtures(project(":domain:users:users-impl")))
    runtimeOnly(testFixtures(project(":domain:inventory:inventory-impl")))
}
```

---

## Usage in Tests

### Unit Tests

Reference worldview data for realistic assertions:

```kotlin
@Test
fun `calculateShippingCost should apply correct rate for heavy products`() {
    // Given
    val product = WorldviewProduct.solarPoweredCharger  // 300g

    // When
    val cost = shippingService.calculateCost(product.weight)

    // Then
    assertThat(cost.amount).isGreaterThan(BigDecimal("5.00"))
}
```

### Integration Tests

Use builders based on worldview:

```kotlin
@SpringBootTest
class ProductRepositoryIntegrationTest {

    @Test
    fun `save should persist product`() {
        // Given
        val product = buildProduct(
            name = "Test Eco Bag",
            category = ProductCategory.HOUSEHOLD
        )

        // When
        val saved = productRepository.save(product)

        // Then
        assertThat(saved.id).isNotNull()
    }
}
```

### Cucumber Tests

Reference worldview by name:

```gherkin
# checkout.feature
Feature: Checkout Flow

  Scenario: Customer orders eco-product
    Given the product "Organic Cotton T-Shirt" exists
    And the customer "John Doe" is logged in
    When the customer places an order for 2 "Organic Cotton T-Shirt"
    And the payment completes successfully
    Then a shipment should be created
    And inventory should be reduced by 2
```

```kotlin
// CheckoutSteps.kt
@Given("the product {string} exists")
fun productExists(productName: String) {
    val product = when (productName) {
        "Organic Cotton T-Shirt" -> WorldviewProduct.organicCottonTShirt
        "Bamboo Toothbrush Set (4 pack)" -> WorldviewProduct.bambooToothbrush
        else -> throw IllegalArgumentException("Unknown product: $productName")
    }
    productRepository.save(product)
}

@Given("the customer {string} is logged in")
fun customerLoggedIn(customerName: String) {
    val user = when (customerName) {
        "John Doe" -> WorldviewUser.johnDoe
        "Hans Müller" -> WorldviewUser.hansMuller
        else -> throw IllegalArgumentException("Unknown user: $customerName")
    }
    userRepository.save(user)
}
```

---

## Principles

### Be Realistic

Use actual product names, weights, prices:

```kotlin
// ✓ Good - Realistic
val organicCottonTShirt = Product(
    name = "Organic Cotton T-Shirt",
    weight = Weight(150, GRAMS),
    price = Money(BigDecimal("29.99"), EUR)
)

// ✗ Bad - Generic
val productA = Product(
    name = "Product A",
    weight = Weight(100, GRAMS),
    price = Money(BigDecimal("10.00"), EUR)
)
```

### Cover Edge Cases

Include boundary conditions:

```kotlin
object WorldviewProduct {
    // Very light product
    val paperStraw = Product(
        weight = Weight(5, GRAMS),  // Minimum weight
        ...
    )

    // Very heavy product
    val solarPanel = Product(
        weight = Weight(22500, GRAMS),  // 22.5kg
        ...
    )

    // High value triggers special handling
    val premiumSolarKit = Product(
        price = Money(BigDecimal("999.99"), EUR),
        ...
    )
}
```

### Keep IDs Stable

Worldview IDs should never change:

```kotlin
// ✓ Good - Stable, predictable IDs
val organicCottonTShirt = Product(
    id = ProductId("PROD-001"),  // Always PROD-001
    ...
)

// ✗ Bad - Random IDs
val organicCottonTShirt = Product(
    id = ProductId.generate(),  // Different every time
    ...
)
```

---

## Worldview as Documentation

New team members can browse worldview to understand the domain:

```kotlin
// products-impl testFixtures show what products look like
WorldviewProduct.organicCottonTShirt  // Real name, weight, price

// inventory-impl testFixtures show warehouse structure
object WorldviewWarehouse {
    val amsterdam = Warehouse(
        id = WarehouseId("WH-NL-001"),
        name = "Amsterdam Fulfillment Center",
        country = Country.NETHERLANDS
    )

    val berlin = Warehouse(
        id = WarehouseId("WH-DE-001"),
        name = "Berlin Fulfillment Center",
        country = Country.GERMANY
    )
}

// Scenarios show realistic workflows
object WorldviewScenario {
    val localDeliveryNL = Scenario(
        customer = WorldviewUser.johnDoe,  // Lives in NL
        product = WorldviewProduct.organicCottonTShirt,
        warehouse = WorldviewWarehouse.amsterdam,  // Ships from NL
        expectedShippingCost = Money(BigDecimal("3.50"), EUR)
    )

    val crossBorderDelivery = Scenario(
        customer = WorldviewUser.hansMuller,  // Lives in DE
        product = WorldviewProduct.bambooToothbrush,
        warehouse = WorldviewWarehouse.amsterdam,  // Ships from NL
        expectedShippingCost = Money(BigDecimal("5.99"), EUR)
    )
}
```

---

## Consequences

### Positive

- Shared vocabulary across team
- Realistic test data catches bugs
- Living documentation (can't drift from code)
- New developers learn domain quickly
- Consistent test data across all tests
- Easy manual testing via loaded data
- Uses standard Gradle test fixtures plugin
- Domain types stay internal to `-impl`
- No additional submodules to maintain

### Negative

- Test fixtures depend on `-impl` (acceptable for test scope)
- Need to keep worldview data updated
- Can grow large over time
- Temptation to use in production (prevented by `@Profile("!prod")`)
- Application module needs `runtimeOnly` dependency on test fixtures for local dev

---

## Best Practices

1. **Name worldview objects clearly**: `organicCottonTShirt`, not `product1`
2. **Use realistic values**: actual weights, prices, names
3. **Keep IDs stable**: tests depend on them
4. **Cover edge cases**: heavy/light, expensive/cheap
5. **Document scenarios**: explain why each worldview object exists
6. **Load only in dev/test**: use `@Profile("!prod")`
7. **Reference by name in Cucumber**: "Organic Cotton T-Shirt", not IDs
