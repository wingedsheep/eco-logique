# ADR-004: Mappers in Persistence Layer

**Status**: Accepted

**Date**: 2024-11-02

---

## Decision

Database entities and their mappers stay in the persistence package marked `internal`. Entities **never leak** outside this layer.

---

## Rationale

Database entities are implementation details. Domain models are the contract. Keeping entities internal allows:
- Changing persistence technology without affecting domain
- Keeping domain models clean of persistence annotations
- Enforcing separation between domain and infrastructure

---

## Implementation

### Entity Definition

```kotlin
// persistence/ProductEntity.kt
@Table("products", schema = "products")
internal data class ProductEntity(
    @Id val id: String,
    val name: String,
    val priceAmount: BigDecimal,
    val priceCurrency: String
)
```

### Entity Mappers

```kotlin
// persistence/ProductEntityMappers.kt
internal fun ProductEntity.toProduct(): Product = Product(
    id = ProductId(this.id),
    name = this.name,
    price = Money(this.priceAmount, Currency.valueOf(this.priceCurrency))
)

internal fun Product.toEntity(): ProductEntity = ProductEntity(
    id = this.id.value,
    name = this.name,
    priceAmount = this.price.amount,
    priceCurrency = this.price.currency.name
)
```

### Repository Interface (Domain)

```kotlin
// ProductRepository.kt
internal interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: ProductId): Product?
    fun findAll(): List<Product>
}
```

### Repository Implementation (Persistence)

```kotlin
// persistence/ProductRepositoryImpl.kt
@Component
internal class ProductRepositoryImpl(
    private val jdbc: ProductRepositoryJdbc
) : ProductRepository {

    override fun save(product: Product): Product {
        return jdbc.save(product.toEntity()).toProduct()
    }

    override fun findById(id: ProductId): Product? {
        return jdbc.findById(id.value)
            .map { it.toProduct() }
            .orElse(null)
    }
}

@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String>
```

---

## Naming Convention

- Entity mappers: `<Type>EntityMappers.kt`
- Functions: `ProductEntity.toProduct()`, `Product.toEntity()`

---

## Consequences

### Positive
- Domain models independent of persistence technology
- Easy to swap persistence implementations
- Entities cannot leak through APIs
- Clear separation of concerns

### Negative
- Additional mapping layer
- More code to maintain
