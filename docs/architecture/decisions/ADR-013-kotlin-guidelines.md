# ADR-013: Kotlin Coding Guidelines

**Status**: Accepted

**Date**: 2024-12-13

---

## Naming Conventions

| Element               | Convention           | Example                |
|-----------------------|----------------------|------------------------|
| Classes, Objects      | PascalCase           | `ProductService`       |
| Functions, Properties | camelCase            | `createProduct`        |
| Constants             | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT`      |
| Packages              | lowercase            | `com.example.products` |

### Method Naming

```kotlin
fun findById(id: ProductId): Product?      // Returns nullable
fun getById(id: ProductId): Product        // Returns non-nullable, throws if not found
fun findAllByCategory(category: Category): List<Product>  // Returns collection
```

---

## Nullability

### Prefer Non-Nullable Types

```kotlin
// Good - Non-nullable with default
data class Config(
    val timeout: Duration = Duration.ofSeconds(30),
)

// Bad - Unnecessary nullability
data class Config(
    val timeout: Duration?,
)
```

### Never Use `!!`

```kotlin
// Bad
val name = user!!.name

// Good
val name = user?.name ?: "Unknown"

// Good - Early return
val user = findUser(id) ?: return Err(UserNotFound(id))
```

---

## Immutability

### Prefer `val` Over `var`

```kotlin
val products = listOf(product1, product2)  // Good
var products = listOf(product1, product2)  // Bad without reason
```

### Use `copy()` for Updates

```kotlin
val updatedUser = user.copy(name = "Jane")  // Good
user.name = "Jane"  // Bad - mutable property
```

---

## Data Classes

### Use for DTOs and Value Objects

```kotlin
data class ProductDto(
    val id: String,
    val name: String,
    val price: BigDecimal,
)
```

### Add Validation in `init` Block

```kotlin
data class Email(val value: String) {
    init {
        require(value.contains("@")) { "Invalid email format" }
    }
}
```

---

## Extension Functions

### Use for Mappers

```kotlin
internal fun Product.toDto(): ProductDto = ProductDto(
    id = id.value,
    name = name,
    price = price.amount,
)
```

### Limit Visibility

```kotlin
internal fun ProductEntity.toProduct(): Product  // Good - scoped
fun Any.doSomething()  // Bad - pollutes API
```

---

## Test Method Naming

```kotlin
@Test
fun `createProduct should return error when name is blank`()

@Test
fun `createProduct should generate unique id`()  // Context omitted when obvious
```

---

## Consequences

### Positive

- Consistent, readable codebase
- Fewer runtime errors through null safety
- Self-documenting code

### Negative

- Learning curve for Java developers
- Discipline required
