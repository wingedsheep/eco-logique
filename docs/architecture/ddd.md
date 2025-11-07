# Domain-Driven Design for Economique

A practical guide to strategic and tactical DDD patterns applied to the Economique sustainable e-commerce platform.

## Introduction to Domain-Driven Design (DDD)

Domain-Driven Design (DDD) is a software development approach that emphasizes a deep understanding of the business domain. It aims to create a rich, expressive model of the domain that is closely aligned with the business's processes and rules. DDD is divided into two main parts: Strategic Design, which deals with the large-scale structure of the system, and Tactical Design, which focuses on the implementation details of individual components.

## Strategic Design: The Big Picture

Strategic Design is used to define the overall architecture of a system by breaking it down into manageable parts called Bounded Contexts and defining their relationships.

### Bounded Context

A Bounded Context is a clear boundary within which a specific domain model applies. Within this boundary, every term has a single, unambiguous meaning.

**Economique Examples:**

* **Products Context**: Models a `Product` with sustainability ratings, carbon footprint, pricing, and categories (clothing, household, electronics, food).
  ```kotlin
  data class Product(
      val id: ProductId,
      val name: String,
      val category: ProductCategory,
      val price: Money,
      val sustainabilityRating: SustainabilityRating,
      val carbonFootprint: CarbonFootprint
  )
  ```

* **Shipping Context**: Models a `Product` only by its weight and dimensions needed for shipping calculations, not its price or sustainability.
  ```kotlin
  // Shipping's view of Product - only cares about physical attributes
  data class ShippableItem(
      val productId: ProductId,
      val weight: Weight,
      val dimensions: Dimensions
  )
  ```

* **Inventory Context**: Models a `Product` as stock levels per warehouse location.
  ```kotlin
  data class InventoryItem(
      val productId: ProductId,
      val warehouseId: WarehouseId,
      val quantity: Quantity,
      val reorderPoint: Int
  )
  ```

**Key Insight**: The same real-world concept (`Product`) has different representations in different contexts because each context cares about different aspects.

### Ubiquitous Language

This is a shared, common language developed by developers and domain experts for a specific Bounded Context. It is used in all project communications and code to eliminate ambiguity.

**Economique Examples:**

* **Products Context**:
    - `SustainabilityRating` (A+, A, B, C, D) - not "eco-score" or "green-rating"
    - `CarbonFootprint` measured in kg CO₂ - consistently used in code and business discussions
    - `ProductCategory` (CLOTHING, HOUSEHOLD, ELECTRONICS, FOOD)
  ```kotlin
  enum class SustainabilityRating { A_PLUS, A, B, C, D }
  data class CarbonFootprint(val kgCo2: BigDecimal, val unit: CarbonUnit)
  ```

* **Payment Context**:
    - `PaymentStatus` (PENDING, COMPLETED, FAILED) - never "processing" or "done"
    - `Settlement` - the term used for PSP (Payment Service Provider) reconciliation
    - `Transaction` - a single payment attempt
  ```kotlin
  enum class PaymentStatus { PENDING, COMPLETED, FAILED }
  data class Settlement(val amount: Money, val settledAt: Instant)
  ```

* **Shipping Context**:
    - `Carrier` - shipping company (DHL, PostNL, etc.)
    - `TrackingNumber` - unique shipment identifier
    - `ShipmentStatus` (PENDING, IN_TRANSIT, DELIVERED)
  ```kotlin
  enum class ShipmentStatus { PENDING, IN_TRANSIT, DELIVERED }
  data class TrackingNumber(val value: String)
  ```

**Key Insight**: Team members say "The product has an A+ sustainability rating" both in meetings and in code. No translation needed.

### Context Map

This is a diagram that shows the relationships and points of integration between different Bounded Contexts. It helps clarify how different parts of the system interact.

**Economique Context Relationships:**

#### Partnership
Two contexts are interdependent and must collaborate closely.

**Example**: `Products` ⟷ `Inventory`
- Products context defines what can be sold
- Inventory context tracks what's in stock
- They must collaborate: can't sell products without inventory, can't stock items that aren't products
- Both contexts need to be updated when new products are introduced

#### Shared Kernel
Two or more contexts share a common part of the model.

**Example**: All contexts share common value objects:
```kotlin
// common-money module
data class Money(val amount: BigDecimal, val currency: Currency)

// common-country module  
enum class Country(val iso2: String) {
    NETHERLANDS("NL"),
    GERMANY("DE"),
    FRANCE("FR")
}

// common-time module
data class DayNL(val date: LocalDate)  // Business day in Netherlands timezone
```

These are shared because they represent fundamental business concepts that must be consistent across all contexts.

#### Customer-Supplier
One context (the supplier) provides data or services to another (the customer).

**Example 1**: `Payment` (supplier) → `Shipping` (customer)
- Shipping context consumes `PaymentCompleted` events
- Shipping only starts fulfillment after payment is confirmed
- Shipping depends on Payment, but Payment doesn't know about Shipping

**Example 2**: `Products` (supplier) → `Shipping` (customer)
```kotlin
// Shipping context depends on Products for weight information
class ShipmentServiceImpl(
    private val productService: ProductService  // From products-api
) {
    fun calculateShippingCost(productId: ProductId): Money {
        val product = productService.getProduct(productId).getOrThrow()
        return calculateCostByWeight(product.weight)
    }
}
```

#### Anti-Corruption Layer (ACL)
A translation layer prevents a downstream context's model from being polluted by an upstream one.

**Example**: Integrating with external PSP (Payment Service Provider)

```kotlin
// payment-impl/service/PspAdapter.kt
@Service
internal class PspAdapter(
    private val pspClient: PspClient  // External PSP library
) {
    fun processPayment(payment: Payment): Result<Payment> {
        // Translate our domain model to PSP's format
        val pspRequest = PspPaymentRequest(
            externalId = payment.id.value,
            amountInCents = (payment.amount.amount * BigDecimal(100)).toInt(),
            currencyCode = payment.amount.currency.name
        )
        
        // Call external system
        val pspResponse = pspClient.createPayment(pspRequest)
        
        // Translate PSP response back to our domain model
        return Result.success(
            payment.copy(
                status = when (pspResponse.status) {
                    "SUCCESS" -> PaymentStatus.COMPLETED
                    "FAILURE" -> PaymentStatus.FAILED
                    "PENDING" -> PaymentStatus.PENDING
                    else -> throw IllegalStateException("Unknown PSP status")
                }
            )
        )
    }
}
```

**Key Insight**: The ACL shields our Payment domain from the PSP's terminology and data structures.

### Economique Context Map Overview

```
┌─────────────┐
│   Products  │◄────────┐
│             │         │
└──────┬──────┘         │
       │                │
       │ supplies       │ supplies
       │                │
       ▼                │
┌─────────────┐    ┌────┴──────┐
│   Shipping  │    │ Inventory │
│             │    │           │
└──────▲──────┘    └───────────┘
       │
       │ listens to
       │
┌──────┴──────┐
│   Payment   │◄──── ACL ◄──── External PSP
│             │
└─────────────┘

Shared Kernel (all contexts):
├── Money
├── Country  
└── DayNL
```

## Tactical Design: The Building Blocks

Tactical Design provides a set of patterns for building a rich and expressive domain model within a single Bounded Context.

### Entity

An object defined by its unique identity, which remains constant throughout its lifecycle, rather than by its attributes.

**Economique Examples:**

```kotlin
// Product is an entity - same product even if name or price changes
data class Product(
    val id: ProductId,  // Identity - this makes it an entity
    val name: String,
    val price: Money,
    val sustainabilityRating: SustainabilityRating
) {
    // Business logic
    fun withUpdatedPrice(newPrice: Money): Product {
        require(newPrice.amount > BigDecimal.ZERO) { "Price must be positive" }
        return copy(price = newPrice)
    }
}

// Customer is an entity
data class User(
    val id: UserId,  // Identity
    val email: String,
    val name: String,
    val address: Address
)

// Shipment is an entity
data class Shipment(
    val id: ShipmentId,  // Identity
    val productId: ProductId,
    val status: ShipmentStatus,
    val trackingNumber: TrackingNumber?
) {
    fun markAsInTransit(trackingNumber: TrackingNumber): Shipment {
        require(status == ShipmentStatus.PENDING) {
            "Can only mark pending shipments as in transit"
        }
        return copy(
            status = ShipmentStatus.IN_TRANSIT,
            trackingNumber = trackingNumber
        )
    }
}
```

**Key Characteristics**:
- Has a unique identifier (ProductId, UserId, ShipmentId)
- Identity remains constant even when attributes change
- Tracked throughout its lifecycle

### Value Object

An immutable object defined by its attributes rather than a unique identity. To change a value object, you create a new one with the desired attributes.

**Economique Examples:**

```kotlin
// Money - value object (no identity)
data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
    }
    
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return Money(amount + other.amount, currency)
    }
}

// Address - value object
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: Country
) {
    init {
        require(street.isNotBlank()) { "Street cannot be blank" }
        require(city.isNotBlank()) { "City cannot be blank" }
        require(postalCode.matches(Regex("^[0-9]{4}[A-Z]{2}$"))) {
            "Postal code must be in format 1234AB"
        }
    }
}

// Weight - value object
data class Weight(
    val grams: Int,
    val unit: WeightUnit
) {
    init {
        require(grams > 0) { "Weight must be positive" }
    }
    
    fun toKilograms(): BigDecimal =
        BigDecimal(grams).divide(BigDecimal(1000), 3, RoundingMode.HALF_UP)
}

// CarbonFootprint - value object
data class CarbonFootprint(
    val kgCo2: BigDecimal,
    val unit: CarbonUnit
) {
    init {
        require(kgCo2 >= BigDecimal.ZERO) { "Carbon footprint cannot be negative" }
    }
}

// Strongly-typed IDs as value objects
@JvmInline
value class ProductId(val value: String) {
    companion object {
        fun generate(): ProductId = ProductId("PROD-${UUID.randomUUID()}")
    }
}
```

**Key Characteristics**:
- No unique identifier
- Immutable
- Defined by its attributes
- Interchangeable (two Money objects with same amount and currency are equivalent)
- Contains validation logic

### Aggregate

A cluster of related entities and value objects that are treated as a single unit for data changes. It has a single entry point, the Aggregate Root, which enforces the consistency of the objects within the aggregate.

**Economique Examples:**

#### Example 1: Order Aggregate

```kotlin
// Order is the Aggregate Root
data class Order(
    val id: OrderId,                    // Aggregate root identity
    val userId: UserId,
    val items: List<OrderItem>,         // Part of aggregate
    val shippingAddress: Address,       // Value object in aggregate
    val totalPrice: Money,
    val status: OrderStatus,
    val createdAt: Instant
) {
    // All changes go through the aggregate root
    fun addItem(productId: ProductId, quantity: Int, price: Money): Order {
        require(status == OrderStatus.DRAFT) { "Cannot modify confirmed order" }
        require(quantity > 0) { "Quantity must be positive" }
        
        val newItem = OrderItem(productId, quantity, price)
        return copy(
            items = items + newItem,
            totalPrice = calculateTotal(items + newItem)
        )
    }
    
    fun confirm(): Order {
        require(status == OrderStatus.DRAFT) { "Order already confirmed" }
        require(items.isNotEmpty()) { "Cannot confirm empty order" }
        return copy(status = OrderStatus.CONFIRMED)
    }
    
    private fun calculateTotal(items: List<OrderItem>): Money =
        items.fold(Money(BigDecimal.ZERO, EUR)) { acc, item -> 
            acc + (item.unitPrice * item.quantity) 
        }
}

// OrderItem is NOT a separate entity - always accessed through Order
data class OrderItem(
    val productId: ProductId,
    val quantity: Int,
    val unitPrice: Money
)
```

**Consistency Rules Enforced**:
- Can't add items to confirmed orders
- Can't confirm empty orders
- Total price always matches sum of items
- External code can't modify OrderItems directly

#### Example 2: Product Aggregate (Simple)

```kotlin
// Product is its own aggregate root (no child entities)
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
        require(price.amount > BigDecimal.ZERO) { "Price must be positive" }
    }
    
    fun updateSustainabilityData(
        rating: SustainabilityRating,
        footprint: CarbonFootprint
    ): Product {
        return copy(
            sustainabilityRating = rating,
            carbonFootprint = footprint
        )
    }
}
```

**Key Rules**:
- External references point to aggregate root only (Order, not OrderItem)
- Consistency boundaries match transaction boundaries
- One aggregate per transaction (don't modify Order and Product in same transaction)

### Repository

Provides an interface for storing and retrieving aggregate roots, abstracting the details of data persistence. It gives the illusion of an in-memory collection of domain objects.

**Economique Examples:**

```kotlin
// ProductRepository - works with Product aggregate
interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findByCategory(category: ProductCategory): List<Product>
    fun findAll(): List<Product>
}

// Implementation hides persistence details
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
    
    override fun findByCategory(category: ProductCategory): List<Product> {
        return jdbc.findByCategoryCode(category.name)
            .map { it.toProduct() }
    }
}

// OrderRepository
interface OrderRepository {
    fun save(order: Order): Order
    fun findById(id: OrderId): Order?
    fun findByUserId(userId: UserId): List<Order>
}

// InventoryRepository
interface InventoryRepository {
    fun save(item: InventoryItem): InventoryItem
    fun findByProductId(productId: ProductId): InventoryItem?
    fun findByWarehouse(warehouseId: WarehouseId): List<InventoryItem>
}
```

**Key Points**:
- Repository interface returns domain objects, not database entities
- Named after aggregates (ProductRepository, not ProductEntityRepository)
- Abstracts persistence technology (could be SQL, NoSQL, in-memory)
- Methods use domain language (findByCategory, not getByCategoryCode)

### Domain Service

Encapsulates domain logic that doesn't naturally fit within an entity or value object. These are typically stateless operations.

**Economique Examples:**

#### Example 1: Shipping Cost Calculation

```kotlin
@Service
class ShippingCostCalculator(
    private val productService: ProductService
) {
    fun calculateShippingCost(
        productId: ProductId,
        destination: Country
    ): Result<Money> = runCatching {
        val product = productService.getProduct(productId).getOrThrow()
        
        val baseRate = when (product.weight.toKilograms()) {
            in BigDecimal.ZERO..BigDecimal.ONE -> Money(BigDecimal("3.50"), EUR)
            in BigDecimal.ONE..BigDecimal("5.0") -> Money(BigDecimal("6.50"), EUR)
            else -> Money(BigDecimal("12.00"), EUR)
        }
        
        val destinationMultiplier = if (destination == Country.NETHERLANDS) {
            BigDecimal.ONE
        } else {
            BigDecimal("1.5")
        }
        
        Money(baseRate.amount * destinationMultiplier, EUR)
    }
}
```

**Why Domain Service**: Shipping cost depends on both Product (weight) and Country (destination). It doesn't belong in Product (Product shouldn't know about shipping) or Country (Country is just an enum).

#### Example 2: Sustainability Rating Calculation

```kotlin
@Service
class SustainabilityRatingService {
    fun calculateRating(
        carbonFootprint: CarbonFootprint,
        category: ProductCategory
    ): SustainabilityRating {
        val threshold = when (category) {
            ProductCategory.CLOTHING -> BigDecimal("2.5")
            ProductCategory.HOUSEHOLD -> BigDecimal("1.5")
            ProductCategory.ELECTRONICS -> BigDecimal("5.0")
            ProductCategory.FOOD -> BigDecimal("0.5")
        }
        
        return when {
            carbonFootprint.kgCo2 <= threshold * BigDecimal("0.4") -> SustainabilityRating.A_PLUS
            carbonFootprint.kgCo2 <= threshold * BigDecimal("0.7") -> SustainabilityRating.A
            carbonFootprint.kgCo2 <= threshold -> SustainabilityRating.B
            carbonFootprint.kgCo2 <= threshold * BigDecimal("1.5") -> SustainabilityRating.C
            else -> SustainabilityRating.D
        }
    }
}
```

#### Example 3: Payment Processing

```kotlin
@Service
class PaymentProcessingService(
    private val paymentRepository: PaymentRepository,
    private val pspAdapter: PspAdapter,
    private val eventPublisher: DomainEventPublisher
) {
    fun processPayment(paymentId: PaymentId): Result<Payment> = runCatching {
        val payment = paymentRepository.findById(paymentId)
            ?: throw IllegalArgumentException("Payment not found")
        
        check(payment.status == PaymentStatus.PENDING) {
            "Payment already processed"
        }
        
        // Interact with external PSP
        val processedPayment = pspAdapter.processPayment(payment).getOrThrow()
        
        // Save updated payment
        val savedPayment = paymentRepository.save(processedPayment)
        
        // Publish domain event if successful
        if (savedPayment.status == PaymentStatus.COMPLETED) {
            eventPublisher.publish(
                PaymentCompleted(
                    paymentId = savedPayment.id,
                    orderId = savedPayment.orderId,
                    amount = savedPayment.amount,
                    timestamp = Instant.now()
                )
            )
        }
        
        savedPayment
    }
}
```

**Why Domain Service**: Payment processing orchestrates multiple operations (validation, external call, persistence, event publishing) and coordinates between payment and PSP domains.

### Domain Event

Represents something significant that has happened in the domain. Aggregates can publish events when their state changes, allowing other parts of the system to react in a loosely coupled way.

**Economique Examples:**

#### Event Definitions

```kotlin
// payment-api/event/PaymentCompleted.kt
data class PaymentCompleted(
    val paymentId: PaymentId,
    val orderId: OrderId,
    val amount: Money,
    val timestamp: Instant
)

// products-api/event/ProductCreated.kt
data class ProductCreated(
    val productId: ProductId,
    val name: String,
    val category: ProductCategory,
    val timestamp: Instant
)

// products-api/event/ProductPriceChanged.kt
data class ProductPriceChanged(
    val productId: ProductId,
    val oldPrice: Money,
    val newPrice: Money,
    val timestamp: Instant
)

// shipping-api/event/ShipmentDispatched.kt
data class ShipmentDispatched(
    val shipmentId: ShipmentId,
    val orderId: OrderId,
    val trackingNumber: TrackingNumber,
    val carrier: Carrier,
    val timestamp: Instant
)

// inventory-api/event/StockReserved.kt
data class StockReserved(
    val productId: ProductId,
    val quantity: Quantity,
    val orderId: OrderId,
    val timestamp: Instant
)
```

#### Publishing Events

```kotlin
@Service
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val eventPublisher: DomainEventPublisher
) : ProductService {
    override fun createProduct(
        name: String,
        category: ProductCategory,
        price: Money
    ): Result<Product> = runCatching {
        val product = Product(
            id = ProductId.generate(),
            name = name,
            category = category,
            price = price,
            // ... other fields
        )
        
        val savedProduct = productRepository.save(product)
        
        // Publish event
        eventPublisher.publish(
            ProductCreated(
                productId = savedProduct.id,
                name = savedProduct.name,
                category = savedProduct.category,
                timestamp = Instant.now()
            )
        )
        
        savedProduct
    }
}
```

#### Consuming Events

```kotlin
// shipping-impl/event/PaymentCompletedListener.kt
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        logger.info(
            "Payment completed for order ${event.orderId.value}, " +
            "starting fulfillment"
        )
        
        shipmentService.startFulfillment(event.orderId)
            .onFailure { error ->
                logger.error(
                    "Failed to start fulfillment for order ${event.orderId.value}",
                    error
                )
            }
    }
}

// inventory-impl/event/ProductCreatedListener.kt
@Component
class ProductCreatedListener(
    private val inventoryService: InventoryService
) {
    @EventListener
    fun onProductCreated(event: ProductCreated) {
        // Initialize inventory for new product
        inventoryService.createInventoryItem(
            productId = event.productId,
            initialQuantity = Quantity(0)
        )
    }
}
```

#### Event Flow Example

**Scenario**: Customer completes order payment

```
1. PaymentService.completePayment()
   └─> Publishes: PaymentCompleted
   
2. InventoryService listens to PaymentCompleted
   └─> Reserves stock
   └─> Publishes: StockReserved
   
3. ShipmentService listens to StockReserved  
   └─> Creates shipment
   └─> Publishes: ShipmentCreated
   
4. NotificationService listens to ShipmentCreated
   └─> Sends shipping confirmation email
```

**Benefits**:
- **Decoupling**: Payment doesn't know about Shipping or Inventory
- **Scalability**: Can move event processing to queues (RabbitMQ, Kafka)
- **Auditability**: Events provide history of what happened
- **Flexibility**: Easy to add new event listeners without changing publishers

## DDD in Economique Architecture

### Module Structure Aligned with DDD

```
payment/                          ← Bounded Context
├── payment-api/                  ← Domain Model (entities, value objects, events)
├── payment-impl/                 ← Domain Services, Repositories, Infrastructure
└── payment-worldview/            ← Domain Knowledge as realistic test data

products/                         ← Bounded Context
├── products-api/                 ← Domain Model
├── products-impl/                ← Implementation
└── products-worldview/           ← Domain Knowledge

common-money/                     ← Shared Kernel
common-country/                   ← Shared Kernel
common-time/                      ← Shared Kernel
```

### Layering Within Implementation Modules

```
products-impl/
├── service/                      ← Domain Services
│   └── ProductServiceImpl.kt
├── rest/                         ← Application Layer (REST adapters)
│   └── v1/
│       ├── ProductControllerV1.kt
│       └── ProductRequestMappers.kt
└── persistence/                  ← Infrastructure Layer
    ├── ProductRepository.kt      ← Domain Repository Interface
    ├── ProductRepositoryImpl.kt  ← Repository Implementation
    ├── ProductEntity.kt          ← Database Entity (internal)
    └── ProductEntityMappers.kt   ← Entity Mappers (internal)
```

**Key Principles**:
- Domain models (`products-api`) have no infrastructure dependencies
- Mappers prevent infrastructure concerns from leaking into domain
- Entities (database) ≠ Entities (domain)
- Repository interface is domain, implementation is infrastructure
