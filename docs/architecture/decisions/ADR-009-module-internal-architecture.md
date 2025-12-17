# ADR-009: Module Internal Architecture

**Status**: Accepted

**Date**: 2024-05-23

**Updated**: 2025-01-15

---

## Decision

Internal module structure is **flexible** based on complexity. The key principle is **dependencies point inward**: infrastructure depends on domain, never the reverse.

We support two patterns:
1. **Flat structure** for simple modules
2. **Vertical slices** for complex modules with multiple features

---

## Core Principle: Dependencies Point Inward

Regardless of structure:
- **Domain** (entities, value objects, repository interfaces) has no framework dependencies
- **Services/Handlers** orchestrate domain operations
- **Infrastructure** (controllers, repository implementations) depends on domain, never the reverse

---

## Simple Modules: Flat Structure

For modules with straightforward operations:

```
notifications-impl/
└── src/main/kotlin/com/example/notifications/
    ├── Notification.kt
    ├── NotificationServiceImpl.kt
    ├── NotificationRepository.kt
    ├── NotificationRepositoryJdbc.kt
    ├── NotificationEntity.kt
    └── EmailClient.kt
```

Everything in one package. Easy to navigate. When it grows, reorganize.

---

## Complex Modules: Vertical Slices

For modules with multiple distinct features, organize by use case:

```
products-impl/
└── src/main/kotlin/com/example/products/
    ├── CreateProductHandler.kt
    ├── GetProductHandler.kt
    ├── UpdatePriceHandler.kt
    ├── bulkimport/
    │   ├── BulkImportHandler.kt
    │   └── ImportParser.kt
    ├── shared/
    │   ├── Product.kt
    │   ├── ProductId.kt
    │   ├── Money.kt
    │   ├── ProductMappers.kt
    │   └── ProductRepository.kt
    └── persistence/
        ├── ProductEntity.kt
        ├── ProductEntityMappers.kt
        └── ProductRepositoryImpl.kt
```

**Guidelines**:
- Simple handlers are just files
- Folders only when a feature has multiple classes
- Shared domain code in `shared/`
- Infrastructure adapters in `persistence/`, `web/`, etc.

---

## Handler Pattern

Each use case gets its own handler:

```kotlin
@Service
internal class CreateProductHandler(
    private val repository: ProductRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun handle(request: CreateProductRequest): ProductDto {
        val product = Product(
            id = ProductId.generate(),
            name = request.name,
            price = Money(request.priceAmount, Currency.valueOf(request.priceCurrency)),
        )
        
        val saved = repository.save(product)
        eventPublisher.publishEvent(ProductCreatedEvent(saved.id.value))
        
        return saved.toDto()
    }
}
```

---

## Connecting to API Contract

A facade delegates to handlers:

```kotlin
@Service
internal class ProductServiceFacade(
    private val createHandler: CreateProductHandler,
    private val getHandler: GetProductHandler,
    private val updatePriceHandler: UpdatePriceHandler,
) : ProductServiceApi {

    override fun createProduct(request: CreateProductRequest) = 
        createHandler.handle(request)
    
    override fun getProduct(id: String) = 
        getHandler.handle(id)
}
```

Controllers can inject handlers directly since they're in the same module.

---

## Domain Models Stay Internal

```kotlin
// Internal - has behavior, validation
internal data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
) {
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
    }
}

// Public - just data
data class ProductDto(
    val id: String,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
)
```

---

## When to Use Which

**Flat structure when:**
- Few operations (< 5 service methods)
- Operations are simple CRUD
- Single developer working on module

**Vertical slices when:**
- Many distinct features
- Features have supporting classes
- Multiple developers on the module
- You're thinking "this service class is getting big"

---

## Consequences

### Positive
- Flexibility to match module complexity
- Related code stays together in slices
- Easy to navigate and understand
- Can refactor structure without affecting other modules

### Negative
- Less consistency across modules
- Team needs to agree on when to restructure
