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

Domain models live in the **`-impl`** module and are **internal**. The `-api` module contains interfaces, DTOs, events, and error hierarchies.

```
products-api/
├── ProductServiceApi.kt           # Public interface
├── dto/
│   ├── ProductDto.kt              # Response DTO (plain data)
│   └── CreateProductRequest.kt    # Request DTO (may validate)
├── error/
│   └── ProductError.kt            # Sealed error hierarchy
└── event/
    └── ProductCreatedEvent.kt

products-impl/
├── Product.kt                     # Domain entity (internal)
├── ProductId.kt                   # Value object (internal)
├── Money.kt                       # Value object (internal)
├── ProductRepository.kt           # Repository interface (internal)
├── ProductServiceImpl.kt          # Implements API
├── ProductMappers.kt              # DTO ↔ Domain mapping
└── persistence/
    ├── ProductEntity.kt
    └── ProductRepositoryJdbc.kt
```

**Rules**:
- Domain models are `internal` to `-impl`
- Response DTOs in `-api` are plain data carriers
- Request DTOs in `-api` may include validation (fail-fast at boundary)
- Domain models contain business rules and invariants

---

## Tactical Design Patterns

### Entities (in `-impl`)

Objects with identity that persist over time. Internal to the module.

```kotlin
// products-impl/Product.kt
internal data class Product(
    val id: ProductId,
    val name: String,
    val price: Money
) {
    init {
        require(name.isNotBlank()) { "Product name cannot be blank" }
    }

    fun withUpdatedPrice(newPrice: Money): Product {
        return copy(price = newPrice)
    }
}
```

### Value Objects (in `-impl`)

Objects defined by their attributes, not identity. Immutable, with behavior.

```kotlin
// products-impl/Money.kt
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
```

### Request DTOs (in `-api`)

Request DTOs validate input at the boundary. Invalid requests fail immediately.

```kotlin
// products-api/dto/CreateProductRequest.kt
data class CreateProductRequest(
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
) {
    init {
        require(name.isNotBlank()) { "Name is required" }
        require(priceAmount > BigDecimal.ZERO) { "Price must be positive" }
    }
}
```

### Response DTOs (in `-api`)

Response DTOs are plain data carriers. No validation, no business logic.

```kotlin
// products-api/dto/ProductDto.kt
data class ProductDto(
    val id: String,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val weightGrams: Int,
)
```

---

## Services

### API Interface (in `-api`)

Defines the public contract using DTOs and sealed error types.

```kotlin
// products-api/ProductServiceApi.kt
interface ProductServiceApi {
    fun createProduct(request: CreateProductRequest): Result<ProductDto, ProductError>
    fun getProduct(id: String): Result<ProductDto, ProductError>
}
```

### Service Implementation (in `-impl`)

Implements the API, orchestrates domain logic, handles mapping.

```kotlin
// products-impl/ProductServiceImpl.kt
@Service
internal class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : ProductServiceApi {

    override fun createProduct(request: CreateProductRequest): Result<ProductDto, ProductError> {
        val product = Product(
            id = ProductId.generate(),
            name = request.name,
            price = Money(request.priceAmount, Currency.valueOf(request.priceCurrency)),
        )
        
        val saved = productRepository.save(product)
        eventPublisher.publishEvent(ProductCreatedEvent(saved.id.value))
        
        return Ok(saved.toDto())
    }

    override fun getProduct(id: String): Result<ProductDto, ProductError> {
        val product = productRepository.findById(ProductId(id))
            ?: return Err(ProductError.NotFound(id))
        return Ok(product.toDto())
    }
}
```

---

## Domain Events

Events capture important business occurrences. Defined in `-api` using primitive types.

```kotlin
// payment-api/event/PaymentCompletedEvent.kt
data class PaymentCompletedEvent(
    val paymentId: String,
    val orderId: String,
    val amount: BigDecimal,
    val currency: String,
    val timestamp: Instant,
)
```

---

## Error Hierarchies (in `-api`)

Sealed classes for exhaustive error handling.

```kotlin
// products-api/error/ProductError.kt
sealed class ProductError {
    data class NotFound(val id: String) : ProductError()
    data class InvalidData(val reason: String) : ProductError()
    data object DuplicateName : ProductError()
}
```

---

## Consequences

### Positive
- Code reflects business language
- Clear boundaries between contexts
- Business logic isolated from infrastructure
- API contracts are stable
- Modules are truly decoupled

### Negative
- More upfront modeling effort
- Mapping between DTOs and domain models
- Requires collaboration with domain experts
