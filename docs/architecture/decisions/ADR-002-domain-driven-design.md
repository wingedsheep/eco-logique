# ADR-002: Apply Domain-Driven Design

**Status**: Accepted

**Date**: 2024-11-02

**Updated**: 2025-01-15

---

## Decision

We apply **Domain-Driven Design** principles to organize code around business domains and maintain a shared understanding between developers and domain experts.

---

## Bounded Contexts

Each module represents one bounded context with its own domain model and language:

- **Payment**: Transactions, settlements, PSP integration
- **Products**: SKUs, categories, pricing, sustainability
- **Shipping**: Carriers, tracking, routes, fulfillment
- **Inventory**: Stock levels, warehouses, reservations
- **Users**: Identity, profiles, addresses

**Context boundaries = module boundaries**. The Product in the Products context is different from how Shipping sees a product.

---

## Domain Model Location

Domain models live in the **`-impl/domain`** package and are **internal** to the module. The `-api` module contains only interfaces, DTOs, events, and error hierarchies.

```
products-api/
├── ProductService.kt              # Public interface
├── dto/
│   ├── ProductDto.kt              # Data carrier, no behavior
│   └── CreateProductRequest.kt
├── error/
│   └── ProductError.kt            # Sealed error hierarchy
└── event/
    └── ProductCreated.kt

products-impl/
├── domain/
│   ├── model/
│   │   ├── Product.kt             # Rich domain entity (internal)
│   │   ├── ProductId.kt           # Value object (internal)
│   │   └── Money.kt               # Value object with behavior (internal)
│   └── repository/
│       └── ProductRepository.kt   # Repository interface (internal)
├── application/
│   └── service/
│       └── ProductServiceImpl.kt  # Implements API, maps DTO ↔ Domain
└── infrastructure/
    ├── persistence/
    └── web/
```

**Rules**:
- Domain models are `internal` to `-impl`
- DTOs in `-api` are dumb data carriers
- Application layer handles mapping between DTOs and domain models
- Domain models contain validation and business logic

---

## Tactical Design Patterns

### Entities (in `-impl/domain`)

Objects with identity that persist over time. Internal to the module.

```kotlin
// products-impl/domain/model/Product.kt
internal data class Product(
    val id: ProductId,
    val name: String,
    val price: Money
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(price.amount > BigDecimal.ZERO) { "Price must be positive" }
    }

    fun withUpdatedPrice(newPrice: Money): Product {
        require(newPrice.amount > BigDecimal.ZERO)
        return copy(price = newPrice)
    }
}
```

### Value Objects (in `-impl/domain`)

Objects defined by their attributes, not identity. Immutable, with behavior.

```kotlin
// products-impl/domain/model/Money.kt
internal data class Money(
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

// products-impl/domain/model/ProductId.kt
@JvmInline
internal value class ProductId(val value: String) {
    init {
        require(value.isNotBlank()) { "ProductId cannot be blank" }
    }

    companion object {
        fun generate(): ProductId = ProductId(UUID.randomUUID().toString())
    }
}
```

### DTOs (in `-api`)

Dumb data carriers. No validation, no business logic.

```kotlin
// products-api/dto/ProductDto.kt
data class ProductDto(
    val id: String,
    val name: String,
    val category: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
    val sustainabilityRating: String
)

// products-api/dto/CreateProductRequest.kt
data class CreateProductRequest(
    val name: String,
    val category: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int
)
```

### Aggregates (in `-impl/domain`)

Cluster of entities and value objects with one root entity.

```kotlin
// shipping-impl/domain/model/Order.kt
internal data class Order(
    val id: OrderId,
    val items: List<OrderItem>,
    val totalPrice: Money,
    val shippingAddress: Address
) {
    fun addItem(item: OrderItem): Order {
        return copy(
            items = items + item,
            totalPrice = totalPrice + item.price
        )
    }
}
```

---

## Ubiquitous Language

Use domain terms consistently in code, conversations, and documentation.

### In Domain Layer (internal)
```kotlin
// products-impl/domain/model/SustainabilityRating.kt
internal enum class SustainabilityRating { A_PLUS, A, B, C, D }

// shipping-impl/domain/model/ShipmentStatus.kt
internal enum class ShipmentStatus { PENDING, IN_TRANSIT, DELIVERED }
```

### In API Layer (public DTOs)
```kotlin
// products-api/dto/ProductDto.kt
data class ProductDto(
    val sustainabilityRating: String  // String representation
)

// shipping-api/dto/ShipmentDto.kt
data class ShipmentDto(
    val status: String  // String representation
)
```

The application layer translates between domain enums and string representations.

---

## Services

### API Interface (in `-api`)

Defines the public contract using DTOs.

```kotlin
// products-api/ProductService.kt
interface ProductService {
    fun createProduct(request: CreateProductRequest): Result<ProductDto, ProductError>
    fun getProduct(id: String): Result<ProductDto, ProductError>
    fun updatePrice(id: String, newPrice: BigDecimal, currency: String): Result<ProductDto, ProductError>
}
```

### Domain Service (in `-impl/application`)

Implements the API, orchestrates domain logic, handles mapping.

```kotlin
// products-impl/application/service/ProductServiceImpl.kt
@Service
internal class ProductServiceImpl(
    private val productRepository: ProductRepository
) : ProductService {

    override fun createProduct(request: CreateProductRequest): Result<ProductDto, ProductError> {
        return runCatching {
            val product = Product(
                id = ProductId.generate(),
                name = request.name,
                category = ProductCategory.valueOf(request.category),
                price = Money(request.priceAmount, Currency.valueOf(request.priceCurrency)),
                weight = Weight(request.weightGrams, GRAMS),
                sustainabilityRating = calculateRating(request.category)
            )
            productRepository.save(product)
            product.toDto()
        }.mapError { e ->
            when (e) {
                is IllegalArgumentException -> ProductError.ValidationFailed(e.message ?: "Validation failed")
                else -> ProductError.Unexpected(e.message ?: "Unexpected error")
            }
        }
    }

    override fun getProduct(id: String): Result<ProductDto, ProductError> {
        val product = productRepository.findById(ProductId(id))
            ?: return Err(ProductError.NotFound(id))
        return Ok(product.toDto())
    }
}
```

### Mapping Extension Functions (in `-impl`)

```kotlin
// products-impl/application/ProductMappers.kt
internal fun Product.toDto(): ProductDto = ProductDto(
    id = id.value,
    name = name,
    category = category.name,
    priceAmount = price.amount,
    priceCurrency = price.currency.name,
    weightGrams = weight.grams,
    sustainabilityRating = sustainabilityRating.name
)
```

---

## Domain Events

Events capture important business occurrences. Defined in `-api` as simple data classes.

```kotlin
// payment-api/event/PaymentCompleted.kt
data class PaymentCompleted(
    val paymentId: String,
    val orderId: String,
    val amount: BigDecimal,
    val currency: String,
    val timestamp: Instant
)

// products-api/event/ProductCreated.kt
data class ProductCreated(
    val productId: String,
    val name: String,
    val category: String,
    val timestamp: Instant
)
```

Events use primitive types and strings, not domain value objects (which are internal to `-impl`).

---

## Anti-Corruption Layer

When integrating with external systems, use adapters to translate to our domain model.

```kotlin
// payment-impl/infrastructure/psp/PspAdapter.kt
@Component
internal class PspAdapter(
    private val pspClient: PspClient
) {
    fun processPayment(payment: Payment): Result<Payment, PaymentError> {
        val pspRequest = PspPaymentRequest(
            externalId = payment.id.value,
            amount = payment.amount.amount.toDouble(),
            currency = payment.amount.currency.name
        )

        return runCatching {
            val pspResponse = pspClient.createPayment(pspRequest)
            payment.copy(
                status = mapPspStatus(pspResponse.status),
                externalReference = pspResponse.transactionId
            )
        }.mapError { PaymentError.PspFailure(it.message ?: "PSP call failed") }
    }

    private fun mapPspStatus(pspStatus: String): PaymentStatus = when (pspStatus) {
        "SUCCESS" -> PaymentStatus.COMPLETED
        "FAILED" -> PaymentStatus.FAILED
        else -> PaymentStatus.PENDING
    }
}
```

---

## Repository Interface (in `-impl/domain`)

Repositories use domain types and are internal to the module.

```kotlin
// products-impl/domain/repository/ProductRepository.kt
internal interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findByCategory(category: ProductCategory): List<Product>
    fun findAll(): List<Product>
}
```

---

## Error Hierarchies (in `-api`)

Sealed classes for exhaustive error handling.

```kotlin
// products-api/error/ProductError.kt
sealed class ProductError {
    data class NotFound(val id: String) : ProductError()
    data class ValidationFailed(val reason: String) : ProductError()
    data class DuplicateName(val name: String) : ProductError()
    data class Unexpected(val message: String) : ProductError()
}
```

---

## Consequences

### Positive
- Code reflects business language
- Domain experts can discuss ubiquitous language
- Clear boundaries between contexts
- Business logic isolated from technical concerns (internal to `-impl`)
- API contracts are stable (DTOs don't change when domain evolves)
- Modules are truly decoupled (no shared domain types)

### Negative
- More upfront modeling effort
- Mapping between DTOs and domain models adds boilerplate
- Requires continuous collaboration with domain experts
- Can be overkill for simple CRUD operations

---

## Examples

### Rich Domain Model (internal)
```kotlin
// shipping-impl/domain/model/Shipment.kt
internal data class Shipment(
    val id: ShipmentId,
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

    fun isDelivered(): Boolean = status == ShipmentStatus.DELIVERED
}
```

### Corresponding DTO (public)
```kotlin
// shipping-api/dto/ShipmentDto.kt
data class ShipmentDto(
    val id: String,
    val productId: String,
    val status: String,
    val trackingNumber: String?
)
```

### Anemic Model (avoid)
```kotlin
// ✗ Bad - No validation, mutable, business logic elsewhere
internal data class Shipment(
    val id: ShipmentId,
    var status: ShipmentStatus,
    var trackingNumber: String?
)

internal class ShipmentDomainService {
    fun markAsInTransit(shipment: Shipment, tracking: String) {
        if (shipment.status == ShipmentStatus.PENDING) {
            shipment.status = ShipmentStatus.IN_TRANSIT
            shipment.trackingNumber = tracking
        }
    }
}
```
