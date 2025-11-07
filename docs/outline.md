# Building a Modular Monolith with DDD

## Overview: Core Principles

### 1. Module Boundaries
**Each module is a bounded context with clear responsibilities.**

```
payment/          # Payment processing
products/         # Product catalog
shipping/         # Logistics and fulfillment
inventory/        # Stock management
users/            # User accounts
```

### 2. Dependency Management
**Modules depend on each other through public API packages.**

- Public API (interfaces, domain models, events) in `api/` package
- Implementation marked `internal` - not accessible to other modules
- Services injected via constructors
- No circular dependencies (enforced by Gradle)

### 3. Data Isolation
**Each module owns its data exclusively.**

- Separate PostgreSQL schemas per module
- No shared tables or foreign keys across schemas
- Cross-module data access only through service APIs

### 4. Communication Patterns
**Choose the right pattern for each interaction.**

- **Synchronous**: Direct service calls for immediate consistency
- **Asynchronous**: Domain events for eventual consistency

---

## Project Structure

```
economique/
├── common/
│   ├── common-money/
│   ├── common-country/
│   └── common-time/
├── deployables/economique/
│   ├── application/           # Spring Boot app
│   ├── payment/               # Payment module
│   ├── products/              # Products module
│   ├── shipping/              # Shipping module
│   ├── inventory/             # Inventory module
│   ├── users/                 # Users module
│   ├── worldview-loader/      # Worldview data
│   └── test/                  # E2E Cucumber tests
└── docker/
    └── docker-compose.yml
```

---

## Module Structure

Each module separates public API from internal implementation:

```
products/
├── api/                               # PUBLIC - exposed to other modules
│   ├── ProductService.kt              # Service interface
│   ├── Product.kt                     # Domain model
│   ├── ProductId.kt                   # Value object
│   ├── ProductCategory.kt             # Enum
│   └── ProductCreated.kt              # Domain event
├── service/
│   └── ProductServiceImpl.kt          # INTERNAL - implementation
├── rest/
│   └── v1/
│       ├── ProductControllerV1.kt     # INTERNAL
│       ├── ProductCreateRequest.kt
│       ├── ProductResponseV1.kt
│       └── ProductRequestMappers.kt
└── persistence/
    ├── ProductRepository.kt           # INTERNAL - interface
    ├── ProductRepositoryImpl.kt       # INTERNAL
    ├── ProductRepositoryJdbc.kt       # INTERNAL - Spring Data
    ├── ProductEntity.kt               # INTERNAL - JPA entity
    └── ProductEntityMappers.kt        # INTERNAL - mappers
```

**Key principle:** Everything outside `api/` package is marked `internal`. Other modules can only access what's in the `api/` package.

---

## Domain Modeling

### Public API (in api/ package)

**Service Interface:**
```kotlin
// products/api/ProductService.kt
interface ProductService {
    fun createProduct(name: String, category: ProductCategory, price: Money): Result<Product>
    fun findProduct(id: ProductId): Product?
    fun getAllProducts(): List<Product>
}
```

**Domain Model:**
```kotlin
// products/api/Product.kt
data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
    val weight: Weight
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(price.amount > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

**Value Objects:**
```kotlin
// products/api/ProductId.kt
@JvmInline
value class ProductId(val value: String) {
    companion object {
        fun generate(): ProductId = ProductId("PROD-${UUID.randomUUID()}")
    }
}
```

**Domain Events:**
```kotlin
// products/api/ProductCreated.kt
data class ProductCreated(
    val productId: ProductId,
    val timestamp: Instant
)
```

### Internal Implementation

**Service Implementation:**
```kotlin
// products/service/ProductServiceImpl.kt
@Service
internal class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher
) : ProductService {
    override fun createProduct(name: String, category: ProductCategory, price: Money): Result<Product> {
        // Implementation details
    }
}
```

**Repositories (internal):**
```kotlin
// products/persistence/ProductRepository.kt
internal interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
}

// products/persistence/ProductRepositoryImpl.kt
@Component
internal class ProductRepositoryImpl(
    private val jdbc: ProductRepositoryJdbc
) : ProductRepository {
    // Implementation
}
```

---

## Communication Patterns

### Synchronous (Direct Calls)
For immediate consistency - other modules depend on the public API:

```kotlin
// shipping/service/ShipmentServiceImpl.kt
@Service
internal class ShipmentService(
    private val productService: ProductService  // From products/api/
) {
    fun createShipment(productId: ProductId): Result<Shipment> {
        // Can only access public ProductService interface
        val product = productService.findProduct(productId)
            ?: return Result.failure(ProductNotFoundException(productId))
        
        return Result.success(Shipment(productId, product.weight))
    }
}
```

**Gradle dependency:**
```kotlin
// shipping/build.gradle.kts
dependencies {
    implementation(project(":deployables:economique:products"))
    // Can only access products/api/ package due to internal modifier
}
```

### Asynchronous (Events)
For loose coupling - events are in the api/ package:

```kotlin
// payment/api/PaymentCompleted.kt
data class PaymentCompleted(
    val paymentId: PaymentId,
    val orderId: OrderId,
    val timestamp: Instant
)

// payment/service/PaymentServiceImpl.kt
@Service
internal class PaymentServiceImpl(
    private val eventPublisher: ApplicationEventPublisher
) : PaymentService {
    fun completePayment(paymentId: PaymentId): Result<Payment> {
        // ... payment logic ...
        eventPublisher.publishEvent(PaymentCompleted(paymentId, orderId, Instant.now()))
        return Result.success(payment)
    }
}

// shipping/event/PaymentCompletedListener.kt
@Component
internal class PaymentCompletedListener(
    private val shipmentService: ShipmentService
) {
    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {  // From payment/api/
        shipmentService.startFulfillment(event.orderId)
    }
}
```

---

## Mappers

### Entity Mappers (Persistence Layer)
Marked `internal`, stay in persistence package:

```kotlin
// ProductEntityMappers.kt
internal fun ProductEntity.toProduct(): Product = Product(
    id = ProductId(this.id),
    name = this.name,
    price = Money(this.priceAmount, Currency.valueOf(this.priceCurrency))
)

internal fun Product.toProductEntity(): ProductEntity = ProductEntity(
    id = this.id.value,
    name = this.name,
    priceAmount = this.price.amount,
    priceCurrency = this.price.currency.name
)
```

### Request/Response Mappers (REST Layer)
In rest package alongside DTOs:

```kotlin
// ProductRequestMappers.kt
fun ProductCreateRequest.toProduct(): Product = Product(
    id = ProductId.generate(),
    name = this.name,
    price = Money(this.priceAmount, this.priceCurrency)
)

fun Product.toProductResponseV1(): ProductResponseV1 = ProductResponseV1(
    id = this.id.value,
    name = this.name,
    price = this.price.amount,
    currency = this.price.currency
)
```

---

## Database Schema Separation

Each module has its own schema:

```sql
-- docker/init/init-databases.sql
CREATE SCHEMA payment;
CREATE SCHEMA products;
CREATE SCHEMA shipping;
CREATE SCHEMA inventory;
CREATE SCHEMA users;
```

Flyway migrations per module:

```
application/src/main/resources/db/migration/
├── payment/
│   └── V1__create_payment_tables.sql
├── products/
│   └── V1__create_products_table.sql
└── shipping/
    └── V1__create_shipment_tables.sql
```

---

## Worldview Pattern

Realistic domain data in `worldview-loader` module:

```kotlin
object WorldviewProduct {
    val organicCottonTShirt = Product(
        id = ProductId("PROD-001"),
        name = "Organic Cotton T-Shirt",
        price = Money(BigDecimal("29.99"), EUR),
        weight = Weight(150, GRAMS)
    )
}

object WorldviewUser {
    val johnDoe = User(
        id = UserId("USER-001"),
        email = "john.doe@example.com",
        name = "John Doe"
    )
}
```

Used in Cucumber tests:

```gherkin
Given the product "Organic Cotton T-Shirt" exists
And the customer "John Doe" is logged in
When the customer places an order
```

---

## Testing Strategy

### Unit Tests
Service logic with mocked dependencies:

```kotlin
@Test
fun `createProduct should save and publish event`() {
    // Given
    val product = buildProduct()
    whenever(productRepository.save(any())).thenReturn(product)
    
    // When
    val result = productService.createProduct(...)
    
    // Then
    assertThat(result.isSuccess).isTrue()
    verify(productRepository).save(any())
    verify(eventPublisher).publishEvent(any<ProductCreated>())
}
```

### Integration Tests
Repository with real database:

```kotlin
@SpringBootTest
@Testcontainers
class ProductRepositoryIntegrationTest {
    @Test
    fun `should persist and retrieve product`() {
        val product = buildProduct()
        val saved = productRepository.save(product)
        val retrieved = productRepository.findById(saved.id)
        
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo(product.name)
    }
}
```

### Cucumber Tests
E2E workflows with worldview data:

```gherkin
Feature: Checkout Flow
  Scenario: Customer orders product
    Given the product "Organic Cotton T-Shirt" exists
    When the customer places an order
    And payment completes successfully
    Then a shipment should be created
```

---

## Application Module

Wires everything together:

```kotlin
// EconomiqueApplication.kt
@SpringBootApplication(scanBasePackages = ["com.economique"])
class EconomiqueApplication

fun main(args: Array<String>) {
    runApplication<EconomiqueApplication>(*args)
}
```

Dependencies:

```kotlin
// application/build.gradle.kts
dependencies {
    implementation(project(":deployables:economique:payment"))
    implementation(project(":deployables:economique:products"))
    implementation(project(":deployables:economique:shipping"))
    implementation(project(":deployables:economique:inventory"))
    implementation(project(":deployables:economique:users"))
    implementation(project(":deployables:economique:worldview-loader"))
}
```

---

## Key Principles Summary

1. **Modules are bounded contexts** - clear boundaries, own their data
2. **Public API in api/ package** - interfaces, domain models, and events
3. **Implementation is internal** - everything outside api/ marked `internal`
4. **Dependencies via DI** - services injected through constructors
5. **Events for loose coupling** - use when eventual consistency acceptable
6. **Entities stay internal** - persistence details never leak
7. **Worldview as shared vocabulary** - realistic data everyone understands
8. **Keep it simple** - don't do more than required
