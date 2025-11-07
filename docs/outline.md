# Building a Modular Monolith with DDD and Clean Architecture

## Overview: Core Principles

### 1. Dependency Rule
**All dependencies point inward and downward in the hierarchy.**

```
Infrastructure → Application → Domain
   (impl)           (api)
```

- Domain has no dependencies on other layers
- Application defines interfaces, infrastructure implements them
- Modules can only depend on other modules' API, never their implementation
- Dependencies flow in one direction only (no circular references)

### 2. Bounded Context Boundaries
**Each module represents a distinct bounded context with its own ubiquitous language.**

- Payment speaks payment language (transactions, settlements)
- Products speaks product language (SKUs, variants, categories)
- Shipping speaks logistics language (carriers, tracking, routes)
- Clear boundaries prevent model contamination

### 3. Separation by Business Capability
**Organize by domain, not by technical layer.**

```
✓ payment/api, payment/impl
✗ api/payment, impl/payment
```

### 4. Data Isolation
**Each module owns its data exclusively.**

- No shared tables between modules
- Use separate schemas or table prefixes (e.g., `payment_*`, `products_*`)
- Cross-module queries go through public APIs only

### 5. Communication Patterns
**Choose the right pattern for each interaction.**

- **Synchronous**: Direct API calls for immediate consistency (Shipping → Products to verify product exists)
- **Asynchronous**: Events for eventual consistency (Payment → Event → Shipping starts fulfillment)
- **Query**: Read-only views for reporting across modules

### 6. Worldview for Domain Knowledge
**Each module contains realistic domain data as code.**

- Enables realistic testing
- Serves as living documentation
- Provides shared vocabulary for team

---

## Project Structure

```
economique/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── deploy.yml
├── docs/
│   ├── architecture/
│   │   ├── decisions/          # ADRs
│   │   └── diagrams/
│   └── modules/
│       ├── payment.md
│       ├── products.md
│       └── ...
├── build-logic/
│   ├── settings.gradle.kts
│   └── src/main/kotlin/
│       ├── economique.kotlin-common.gradle.kts
│       ├── economique.spring-boot.gradle.kts
│       └── economique.testing.gradle.kts
├── docker/
│   ├── docker-compose.yml      # postgres, rabbitmq, etc.
│   └── init/
│       └── init-databases.sql  # Create schemas
├── common/
│   ├── common-time/
│   ├── common-country/
│   └── common-money/
└── deployables/
    └── economique/
        ├── application/
        ├── domain/
        │   ├── payment/
        │   │   ├── payment-api/
        │   │   ├── payment-impl/
        │   │   └── payment-worldview/
        │   ├── products/
        │   │   ├── products-api/
        │   │   ├── products-impl/
        │   │   └── products-worldview/
        │   ├── shipping/
        │   │   ├── shipping-api/
        │   │   ├── shipping-impl/
        │   │   └── shipping-worldview/
        │   ├── inventory/
        │   │   ├── inventory-api/
        │   │   ├── inventory-impl/
        │   │   └── inventory-worldview/
        │   └── users/
        │       ├── users-api/
        │       ├── users-impl/
        │       └── users-worldview/
        └── test/
            ├── src/test/kotlin/
            │   └── e2e/
            └── src/test/resources/
                └── features/
                    ├── checkout.feature
                    └── fulfillment.feature
```

---

## Module Structure Guidelines

### API Module (`domain-api`)

**Purpose**: Public contract exposed to other modules. Defines what, not how.

**Contents**:
```
products-api/
├── service/
│   └── ProductService.kt           # Interface only
├── model/
│   ├── Product.kt                  # Domain entity
│   ├── ProductCategory.kt          # Value object
│   └── ProductId.kt                # Strongly typed ID
├── event/
│   ├── ProductCreated.kt           # Domain event
│   └── ProductPriceChanged.kt
└── exception/
    └── ProductNotFoundException.kt
```

**Dependencies**: Only common modules (money, country, time)

**Rules**:
- No Spring annotations
- No implementation details
- Interfaces only, no concrete classes except domain models
- Domain models must be technology-agnostic

### Implementation Module (`domain-impl`)

**Purpose**: Implements the domain logic, API endpoints, and persistence.

**Contents**:
```
products-impl/
├── service/
│   └── ProductServiceImpl.kt       # Implements ProductService
├── rest/
│   ├── v1/
│   │   ├── ProductControllerV1.kt
│   │   ├── ProductCreateRequest.kt
│   │   ├── ProductResponseV1.kt
│   │   └── ProductRequestMappers.kt    # Request → Domain
│   └── internal/
│       └── ProductAdminController.kt
└── persistence/
    ├── ProductRepository.kt            # Returns domain types
    ├── ProductRepositoryJdbc.kt        # Spring Data JDBC (internal)
    ├── ProductEntity.kt                # JPA/JDBC entity (internal)
    └── ProductEntityMappers.kt         # Entity ↔ Domain
```

**Dependencies**:
- Own `-api` module
- Other modules' `-api` (only what's needed)
- Common modules
- Spring Boot, persistence frameworks

**Package structure**:
```kotlin
com.economique.products
├── service        // Business logic implementation
├── rest.v1        // Customer-facing REST APIs
├── rest.internal  // Operator-facing REST APIs
└── persistence    // Data access + entity mappers
```

**Key principle**: Database entities never leave the persistence package. Mappers in the persistence layer convert between entities and domain models.

### Worldview Module (`domain-worldview`)

**Purpose**: Realistic domain data for testing and onboarding.

**Contents**:
```
products-worldview/
├── WorldviewProduct.kt
├── WorldviewCategory.kt
├── ProductBuilder.kt
└── WorldviewProductData.kt
```

**Example**:
```kotlin
object WorldviewProduct {
    val organicCottonTShirt = Product(
        id = ProductId("PROD-001"),
        name = "Organic Cotton T-Shirt",
        category = ProductCategory.CLOTHING,
        price = Money(29.99, EUR),
        weight = Weight(150, GRAMS),
        sustainabilityRating = SustainabilityRating.A_PLUS,
        carbonFootprint = CarbonFootprint(2.1, KG_CO2)
    )
    
    val bambooToothbrush = Product(
        id = ProductId("PROD-002"),
        name = "Bamboo Toothbrush Set (4 pack)",
        category = ProductCategory.HOUSEHOLD,
        price = Money(12.50, EUR),
        weight = Weight(80, GRAMS),
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprint = CarbonFootprint(0.4, KG_CO2)
    )
}

fun buildProduct(
    id: ProductId = ProductId("PROD-TEST-${UUID.randomUUID()}"),
    name: String = "Test Product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    price: Money = Money(19.99, EUR),
    weight: Weight = Weight(100, GRAMS)
): Product = Product(id, name, category, price, weight)
```

**Dependencies**: Only `-api` module

---

## Module Dependency Rules

### Dependency Matrix

```
                 Payment  Products  Shipping  Inventory  Users
Payment            -        ✗         ✗         ✗         ✗
Products           ✗        -         ✗         ✗         ✗
Shipping           ✗        ✓         -         ✓         ✗
Inventory          ✗        ✓         ✗         -         ✗
Users              ✗        ✗         ✗         ✗         -
```

✓ = Allowed to depend on
✗ = Not allowed to depend on

### Rationale

**Payment** is completely isolated:
- PSP integration doesn't need business domain knowledge
- Receives payment instructions via events
- Publishes payment status events

**Products** is foundational:
- Core domain entity
- No dependencies on other domains
- Other domains reference products by ProductId only

**Shipping** depends on Products and Inventory:
- Needs product weight, dimensions for shipping calculation
- Checks inventory location to determine shipping origin

**Inventory** depends on Products:
- Tracks quantity per product
- References product by ProductId

**Users** is isolated:
- User identity management is separate concern
- Other modules reference users by UserId only

---

## Mapper Guidelines

### REST Layer Mappers

**Location**: `rest.v1/` package alongside DTOs

**Purpose**: Convert between REST DTOs and domain models

**Example**:
```kotlin
// products-impl/src/main/kotlin/com/economique/products/rest/v1/ProductRequestMappers.kt

fun ProductCreateRequest.toProduct(): Product {
    return Product(
        id = ProductId.generate(),
        name = this.name,
        category = this.category,
        price = Money(this.priceAmount, this.priceCurrency),
        weight = Weight(this.weightGrams, GRAMS)
    )
}

fun Product.toProductResponseV1(): ProductResponseV1 {
    return ProductResponseV1(
        id = this.id.value,
        name = this.name,
        category = this.category,
        price = this.price.amount,
        currency = this.price.currency
    )
}
```

### Persistence Layer Mappers

**Location**: `persistence/` package alongside entities

**Purpose**: Convert between database entities and domain models

**Example**:
```kotlin
// products-impl/src/main/kotlin/com/economique/products/persistence/ProductEntityMappers.kt

internal fun ProductEntity.toProduct(): Product {
    return Product(
        id = ProductId(this.id),
        name = this.name,
        category = ProductCategory.valueOf(this.categoryCode),
        price = Money(this.priceAmount, Currency.valueOf(this.priceCurrency)),
        weight = Weight(this.weightGrams, GRAMS),
        sustainabilityRating = SustainabilityRating.valueOf(this.sustainabilityRating),
        carbonFootprint = CarbonFootprint(this.carbonFootprintKg, KG_CO2)
    )
}

internal fun Product.toProductEntity(): ProductEntity {
    return ProductEntity(
        id = this.id.value,
        name = this.name,
        categoryCode = this.category.name,
        priceAmount = this.price.amount,
        priceCurrency = this.price.currency.name,
        weightGrams = this.weight.grams,
        sustainabilityRating = this.sustainabilityRating.name,
        carbonFootprintKg = this.carbonFootprint.kgCo2
    )
}
```

**Key principles**:
- Mappers are `internal` to prevent leaking outside the module
- Entities never leave the persistence package
- Repository interface returns domain types, not entities

### Repository Implementation Example

```kotlin
// products-impl/src/main/kotlin/com/economique/products/persistence/ProductRepository.kt
interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findAll(): List<Product>
    fun findByCategory(category: ProductCategory): List<Product>
}

// products-impl/src/main/kotlin/com/economique/products/persistence/ProductRepositoryJdbc.kt
@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String> {
    fun findByCategoryCode(categoryCode: String): List<ProductEntity>
}

// products-impl/src/main/kotlin/com/economique/products/persistence/ProductRepositoryImpl.kt
@Component
internal class ProductRepositoryImpl(
    private val jdbc: ProductRepositoryJdbc
) : ProductRepository {
    
    override fun save(product: Product): Product {
        val entity = product.toProductEntity()
        return jdbc.save(entity).toProduct()
    }
    
    override fun findById(id: ProductId): Product? {
        return jdbc.findById(id.value)
            .map { it.toProduct() }
            .orElse(null)
    }
    
    override fun findAll(): List<Product> {
        return jdbc.findAll().map { it.toProduct() }
    }
    
    override fun findByCategory(category: ProductCategory): List<Product> {
        return jdbc.findByCategoryCode(category.name)
            .map { it.toProduct() }
    }
}
```

---

## Communication Patterns

### Synchronous Communication (Direct API Calls)

**When to use**: Immediate consistency required, operation fails if dependency unavailable.

**Example**: Shipping validates product exists before creating shipment
```kotlin
// shipping-impl
class ShipmentService(
    private val productService: ProductService  // From products-api
) {
    fun createShipment(productId: ProductId): Result<Shipment> {
        val product = productService.findProduct(productId)
            ?: return Result.failure(ProductNotFoundException(productId))
        
        return Result.success(
            Shipment(
                productId = productId,
                weight = product.weight,
                dimensions = product.dimensions
            )
        )
    }
}
```

### Asynchronous Communication (Events)

**When to use**: Eventual consistency acceptable, loose coupling desired.

**Example**: Order placed → Payment processed → Fulfillment started

**Event Definition** (in publishing module's `-api`):
```kotlin
// payment-api
data class PaymentCompleted(
    val paymentId: PaymentId,
    val orderId: OrderId,
    val amount: Money,
    val timestamp: Instant
) : DomainEvent
```

**Publishing** (in `-impl`):
```kotlin
// payment-impl
class PaymentService(
    private val eventPublisher: DomainEventPublisher
) {
    fun completePayment(paymentId: PaymentId): Result<Payment> {
        // ... payment logic ...
        
        eventPublisher.publish(
            PaymentCompleted(
                paymentId = paymentId,
                orderId = payment.orderId,
                amount = payment.amount,
                timestamp = Instant.now()
            )
        )
        
        return Result.success(payment)
    }
}
```

**Subscribing** (in consuming module's `-impl`):
```kotlin
// shipping-impl
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentService
) {
    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        shipmentService.startFulfillment(event.orderId)
    }
}
```

---

## Application Module Structure

The `application` module wires everything together.

```
application/
├── src/main/kotlin/
│   └── com/economique/
│       ├── EconomiqueApplication.kt     # Main Spring Boot app
│       ├── config/
│       │   ├── DataSourceConfig.kt      # Separate schemas per module
│       │   ├── EventBusConfig.kt        # RabbitMQ/internal events
│       │   └── SecurityConfig.kt
│       └── worldview/
│           └── WorldviewDataLoader.kt   # Loads all worldview data
└── src/main/resources/
    ├── application.yml
    └── db/migration/
        ├── payment/
        │   └── V1__create_payment_schema.sql
        ├── products/
        │   └── V1__create_products_schema.sql
        └── ...
```

**Dependencies** (build.gradle.kts):
```kotlin
dependencies {
    // All domain implementations
    implementation(project(":domain:payment:payment-impl"))
    implementation(project(":domain:products:products-impl"))
    implementation(project(":domain:shipping:shipping-impl"))
    implementation(project(":domain:inventory:inventory-impl"))
    implementation(project(":domain:users:users-impl"))
    
    // All worldviews for dev/test data
    implementation(project(":domain:payment:payment-worldview"))
    implementation(project(":domain:products:products-worldview"))
    // ...
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

**WorldviewDataLoader** (for local dev):
```kotlin
@Component
class WorldviewDataLoader(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
    // ... other repositories
) {
    @PostConstruct
    fun loadWorldviewData() {
        if (!isProductionEnvironment()) {
            loadProducts()
            loadUsers()
            // ...
        }
    }
    
    private fun loadProducts() {
        productRepository.saveAll(
            listOf(
                WorldviewProduct.organicCottonTShirt,
                WorldviewProduct.bambooToothbrush,
                WorldviewProduct.solarPoweredCharger
            )
        )
    }
}
```

---

## Testing Strategy

### Unit Tests (Per Module)

**Location**: `domain-impl/src/test/`

**Focus**: Business logic in isolation

```kotlin
class ProductServiceImplTest {
    private val productRepository = mock<ProductRepository>()
    private val eventPublisher = mock<DomainEventPublisher>()
    private val productService = ProductServiceImpl(productRepository, eventPublisher)
    
    @Test
    fun `createProduct should create product and publish event when valid`() {
        // Given
        val productRequest = buildProductCreateRequest()
        val product = buildProduct()
        whenever(productRepository.save(any())).thenReturn(product)
        
        // When
        val result = productService.createProduct(productRequest)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        verify(productRepository).save(any())
        verify(eventPublisher).publish(any<ProductCreated>())
    }
}
```

### Integration Tests (Per Module)

**Location**: `domain-impl/src/test/`

**Focus**: Module internals working together with real database

```kotlin
@SpringBootTest
@Transactional
class ProductRepositoryIntegrationTest {
    @Autowired
    private lateinit var productRepository: ProductRepository
    
    @Test
    fun `findByCategory should return products in category when products exist`() {
        // Given
        val product = buildProduct(category = ProductCategory.CLOTHING)
        productRepository.save(product)
        
        // When
        val products = productRepository.findByCategory(ProductCategory.CLOTHING)
        
        // Then
        assertThat(products).hasSize(1)
        assertThat(products.first().category).isEqualTo(ProductCategory.CLOTHING)
    }
}
```

### E2E Tests (Application Level)

**Location**: `test/src/test/`

**Focus**: Complete workflows across modules

```kotlin
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class CheckoutE2ETest {
    @Container
    private val postgres = PostgreSQLContainer<Nothing>("postgres:15")
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @Test
    fun `complete checkout workflow from cart to shipment`() {
        // Given
        val product = WorldviewProduct.organicCottonTShirt
        
        // When
        val orderResponse = restTemplate.postForEntity(
            "/api/v1/orders",
            OrderCreateRequest(
                items = listOf(OrderItem(product.id, quantity = 2)),
                userId = WorldviewUser.johnDoe.id
            ),
            OrderResponse::class.java
        )
        
        // And
        val paymentResponse = restTemplate.postForEntity(
            "/api/v1/payments",
            PaymentRequest(orderId = orderResponse.body.id),
            PaymentResponse::class.java
        )
        
        // Then
        await().atMost(Duration.ofSeconds(5)).untilAsserted {
            val shipment = restTemplate.getForEntity(
                "/api/v1/shipments?orderId=${orderResponse.body.id}",
                ShipmentResponse::class.java
            )
            assertThat(shipment.statusCode).isEqualTo(HttpStatus.OK)
        }
    }
}
```

### Cucumber Tests

**Location**: `test/src/test/resources/features/`

```gherkin
Feature: Checkout Flow
  
  Scenario: Customer places order for eco products
    Given the product "Organic Cotton T-Shirt" exists
    And the customer "John Doe" is logged in
    When the customer adds 2 "Organic Cotton T-Shirt" to cart
    And the customer proceeds to checkout
    And the payment is completed successfully
    Then a shipment should be created
    And the inventory should be reduced by 2 for "Organic Cotton T-Shirt"
```

---

## Database Schema Separation

### Separate Schemas

```sql
-- docker/init/init-databases.sql
CREATE SCHEMA payment;
CREATE SCHEMA products;
CREATE SCHEMA shipping;
CREATE SCHEMA inventory;
CREATE SCHEMA users;

GRANT ALL ON SCHEMA payment TO economique_app;
GRANT ALL ON SCHEMA products TO economique_app;
-- ...
```

### Flyway Configuration

```yaml
# application.yml
spring:
  flyway:
    locations:
      - classpath:db/migration/payment
      - classpath:db/migration/products
      - classpath:db/migration/shipping
      - classpath:db/migration/inventory
      - classpath:db/migration/users
```

---

## Build Configuration

### Root `settings.gradle.kts`

```kotlin
rootProject.name = "economique"

include(
    ":common:common-time",
    ":common:common-country",
    ":common:common-money",
    
    ":deployables:economique:application",
    
    ":deployables:economique:domain:payment:payment-api",
    ":deployables:economique:domain:payment:payment-impl",
    ":deployables:economique:domain:payment:payment-worldview",
    
    ":deployables:economique:domain:products:products-api",
    ":deployables:economique:domain:products:products-impl",
    ":deployables:economique:domain:products:products-worldview",
    
    ":deployables:economique:domain:" +
            "hipping:shipping-api",
    ":deployables:economique:domain:shipping:shipping-impl",
    ":deployables:economique:domain:shipping:shipping-worldview",
    
    ":deployables:economique:domain:inventory:inventory-api",
    ":deployables:economique:domain:inventory:inventory-impl",
    ":deployables:economique:domain:inventory:inventory-worldview",
    
    ":deployables:economique:domain:users:users-api",
    ":deployables:economique:domain:users:users-impl",
    ":deployables:economique:domain:users:users-worldview",
    
    ":deployables:economique:test"
)
```

### Module `build.gradle.kts` Examples

**API Module**:
```kotlin
// products-api/build.gradle.kts
plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":common:common-money"))
    api(project(":common:common-country"))
}
```

**Implementation Module**:
```kotlin
// products-impl/build.gradle.kts
plugins {
    id("economique.kotlin-common")
    id("economique.spring-boot")
}

dependencies {
    api(project(":deployables:economique:domain:products:products-api"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

**Worldview Module**:
```kotlin
// products-worldview/build.gradle.kts
plugins {
    id("economique.kotlin-common")
}

dependencies {
    api(project(":deployables:economique:domain:products:products-api"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
}
```

---

## Architecture Decision Records

**Location**: `docs/architecture/decisions/`

Key ADRs to create:

1. **ADR-001: Use Modular Monolith Architecture**
2. **ADR-002: Apply Domain-Driven Design**
3. **ADR-003: Separate Schemas per Bounded Context**
4. **ADR-004: Event-Driven Communication Between Modules**
5. **ADR-005: Worldview Pattern for Domain Knowledge**
6. **ADR-006: Mappers in Persistence Layer**

**Template**:
```markdown
# ADR-003: Separate Schemas per Bounded Context

## Status
Accepted

## Context
Each module needs data isolation to maintain bounded context boundaries.

## Decision
Use separate PostgreSQL schemas (payment, products, shipping, inventory, users).

## Consequences
- **Positive**: Strong data isolation, easier to extract to microservices later
- **Positive**: Clear ownership boundaries
- **Negative**: Cannot use foreign keys across modules
- **Negative**: Requires managing multiple migration paths
```

**Example: ADR-006**:
```markdown
# ADR-006: Mappers in Persistence Layer

## Status
Accepted

## Context
We need to convert between database entities and domain models. Database entities should not leak outside the persistence layer.

## Decision
All entity mappers are defined in the persistence package alongside entities. Entities are marked as internal. Repository interfaces return domain types only.

## Consequences
- **Positive**: Strong encapsulation of persistence concerns
- **Positive**: Domain models remain clean of persistence annotations
- **Positive**: Easy to swap persistence technology
- **Negative**: Additional mapping layer adds slight complexity
```

---

## Documentation Structure

```
docs/
├── architecture/
│   ├── decisions/           # ADRs
│   ├── diagrams/
│   │   ├── context.puml    # C4 context diagram
│   │   ├── containers.puml # C4 container diagram
│   │   └── components/
│   │       ├── payment.puml
│   │       └── products.puml
│   └── principles.md       # This document
├── modules/
│   ├── payment.md          # Payment module overview
│   ├── products.md
│   ├── shipping.md
│   ├── inventory.md
│   └── users.md
├── guides/
│   ├── getting-started.md
│   ├── adding-new-module.md
│   └── testing-strategy.md
└── api/
    └── openapi.yml         # Aggregate all REST APIs
```

---

## Common Modules

### common-money

```kotlin
data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return Money(amount + other.amount, currency)
    }
    
    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract different currencies" }
        return Money(amount - other.amount, currency)
    }
    
    operator fun times(multiplier: Int): Money {
        return Money(amount * multiplier.toBigDecimal(), currency)
    }
}

enum class Currency {
    EUR, USD, GBP
}
```

### common-country

```kotlin
enum class Country(val iso2: String, val iso3: String) {
    NETHERLANDS("NL", "NLD"),
    GERMANY("DE", "DEU"),
    FRANCE("FR", "FRA"),
    BELGIUM("BE", "BEL")
}
```

### common-time

```kotlin
data class DayNL(val date: LocalDate) {
    companion object {
        fun today(): DayNL = DayNL(LocalDate.now(ZoneId.of("Europe/Amsterdam")))
    }
}
```

---

## Complete Example: Products Module

### products-api

**Product.kt**:
```kotlin
data class Product(
    val id: ProductId,
    val name: String,
    val category: ProductCategory,
    val price: Money,
    val weight: Weight,
    val sustainabilityRating: SustainabilityRating,
    val carbonFootprint: CarbonFootprint
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(price.amount > BigDecimal.ZERO) { "Product price must be positive" }
        require(weight.grams > 0) { "Product weight must be positive" }
    }
}

@JvmInline
value class ProductId(val value: String) {
    companion object {
        fun generate(): ProductId = ProductId("PROD-${UUID.randomUUID()}")
    }
}

enum class ProductCategory {
    CLOTHING,
    HOUSEHOLD,
    ELECTRONICS,
    FOOD
}

data class Weight(val grams: Int, val unit: WeightUnit)

enum class WeightUnit { GRAMS, KILOGRAMS }

data class CarbonFootprint(val kgCo2: BigDecimal, val unit: CarbonUnit)

enum class CarbonUnit { KG_CO2 }

enum class SustainabilityRating { A_PLUS, A, B, C, D }
```

**ProductService.kt**:
```kotlin
interface ProductService {
    fun createProduct(name: String, category: ProductCategory, price: Money, weight: Weight): Result<Product>
    fun getProduct(id: ProductId): Result<Product>
    fun getAllProducts(): List<Product>
    fun updatePrice(id: ProductId, newPrice: Money): Result<Product>
}
```

**ProductCreated.kt**:
```kotlin
data class ProductCreated(
    val productId: ProductId,
    val timestamp: Instant
) : DomainEvent
```

### products-impl

**ProductServiceImpl.kt**:
```kotlin
@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val eventPublisher: DomainEventPublisher
) : ProductService {
    
    override fun createProduct(
        name: String,
        category: ProductCategory,
        price: Money,
        weight: Weight
    ): Result<Product> = runCatching {
        val product = Product(
            id = ProductId.generate(),
            name = name,
            category = category,
            price = price,
            weight = weight,
            sustainabilityRating = SustainabilityRating.B,
            carbonFootprint = CarbonFootprint(BigDecimal("1.5"), KG_CO2)
        )
        
        val savedProduct = productRepository.save(product)
        eventPublisher.publish(ProductCreated(savedProduct.id, Instant.now()))
        savedProduct
    }
    
    override fun getProduct(id: ProductId): Result<Product> = runCatching {
        productRepository.findById(id)
            ?: throw ProductNotFoundException(id)
    }
    
    override fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }
    
    override fun updatePrice(id: ProductId, newPrice: Money): Result<Product> = runCatching {
        val product = productRepository.findById(id)
            ?: throw ProductNotFoundException(id)
        
        val updatedProduct = product.copy(price = newPrice)
        productRepository.save(updatedProduct)
    }
}
```

**ProductControllerV1.kt**:
```kotlin
@RestController
@RequestMapping("/api/v1/products")
class ProductControllerV1(
    private val productService: ProductService
) {
    @PostMapping
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ProductResponseV1> {
        return productService.createProduct(
            name = request.name,
            category = request.category,
            price = Money(request.priceAmount, request.priceCurrency),
            weight = Weight(request.weightGrams, GRAMS)
        ).fold(
            onSuccess = { product -> ResponseEntity.ok(product.toProductResponseV1()) },
            onFailure = { error -> ResponseEntity.badRequest().build() }
        )
    }
    
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): ResponseEntity<ProductResponseV1> {
        return productService.getProduct(ProductId(id)).fold(
            onSuccess = { product -> ResponseEntity.ok(product.toProductResponseV1()) },
            onFailure = { error -> ResponseEntity.notFound().build() }
        )
    }
}
```

**ProductCreateRequest.kt**:
```kotlin
data class ProductCreateRequest(
    val name: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val weightGrams: Int
)
```

**ProductResponseV1.kt**:
```kotlin
data class ProductResponseV1(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val priceAmount: BigDecimal,
    val priceCurrency: Currency,
    val weightGrams: Int,
    val sustainabilityRating: SustainabilityRating
)
```

**ProductRequestMappers.kt** (in rest.v1 package):
```kotlin
fun Product.toProductResponseV1(): ProductResponseV1 {
    return ProductResponseV1(
        id = this.id.value,
        name = this.name,
        category = this.category,
        priceAmount = this.price.amount,
        priceCurrency = this.price.currency,
        weightGrams = this.weight.grams,
        sustainabilityRating = this.sustainabilityRating
    )
}
```

**ProductEntity.kt** (in persistence package):
```kotlin
@Table("products")
internal data class ProductEntity(
    @Id
    val id: String,
    val name: String,
    val categoryCode: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String,
    val carbonFootprintKg: BigDecimal
)
```

**ProductEntityMappers.kt** (in persistence package):
```kotlin
internal fun ProductEntity.toProduct(): Product {
    return Product(
        id = ProductId(this.id),
        name = this.name,
        category = ProductCategory.valueOf(this.categoryCode),
        price = Money(this.priceAmount, Currency.valueOf(this.priceCurrency)),
        weight = Weight(this.weightGrams, GRAMS),
        sustainabilityRating = SustainabilityRating.valueOf(this.sustainabilityRating),
        carbonFootprint = CarbonFootprint(this.carbonFootprintKg, KG_CO2)
    )
}

internal fun Product.toProductEntity(): ProductEntity {
    return ProductEntity(
        id = this.id.value,
        name = this.name,
        categoryCode = this.category.name,
        priceAmount = this.price.amount,
        priceCurrency = this.price.currency.name,
        weightGrams = this.weight.grams,
        sustainabilityRating = this.sustainabilityRating.name,
        carbonFootprintKg = this.carbonFootprint.kgCo2
    )
}
```

**ProductRepository.kt** (in persistence package):
```kotlin
interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findAll(): List<Product>
    fun findByCategory(category: ProductCategory): List<Product>
}
```

**ProductRepositoryJdbc.kt** (in persistence package):
```kotlin
@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String> {
    fun findByCategoryCode(categoryCode: String): List<ProductEntity>
}
```

**ProductRepositoryImpl.kt** (in persistence package):
```kotlin
@Component
internal class ProductRepositoryImpl(
    private val jdbc: ProductRepositoryJdbc
) : ProductRepository {
    
    override fun save(product: Product): Product {
        val entity = product.toProductEntity()
        return jdbc.save(entity).toProduct()
    }
    
    override fun findById(id: ProductId): Product? {
        return jdbc.findById(id.value)
            .map { it.toProduct() }
            .orElse(null)
    }
    
    override fun findAll(): List<Product> {
        return jdbc.findAll().map { it.toProduct() }
    }
    
    override fun findByCategory(category: ProductCategory): List<Product> {
        return jdbc.findByCategoryCode(category.name)
            .map { it.toProduct() }
    }
}
```

### products-worldview

**WorldviewProduct.kt**:
```kotlin
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
}
```

**ProductBuilder.kt**:
```kotlin
fun buildProduct(
    id: ProductId = ProductId("PROD-TEST-${UUID.randomUUID()}"),
    name: String = "Test Product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    price: Money = Money(BigDecimal("19.99"), EUR),
    weight: Weight = Weight(100, GRAMS),
    sustainabilityRating: SustainabilityRating = SustainabilityRating.B,
    carbonFootprint: CarbonFootprint = CarbonFootprint(BigDecimal("1.5"), KG_CO2)
): Product = Product(
    id = id,
    name = name,
    category = category,
    price = price,
    weight = weight,
    sustainabilityRating = sustainabilityRating,
    carbonFootprint = carbonFootprint
)
```
