# ADR-015: Kotlin Coding Guidelines

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We establish Kotlin coding guidelines covering style, idioms, and antipatterns to ensure consistent, readable, and
maintainable code.

---

## Naming Conventions

### General Rules

| Element               | Convention                | Example                              |
|-----------------------|---------------------------|--------------------------------------|
| Classes, Objects      | PascalCase                | `ProductService`, `OrderStatus`      |
| Functions, Properties | camelCase                 | `createProduct`, `userName`          |
| Constants             | SCREAMING_SNAKE_CASE      | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT` |
| Packages              | lowercase, no underscores | `com.example.products`               |
| Type Parameters       | Single uppercase letter   | `T`, `R`, `E`                        |

### Naming Priority

**1. Ubiquitous Language First**

Use the terminology that domain experts use. If the business says "Smart Meter Allocation", name it
`SmartMeterAllocation`:

```kotlin
// ✓ Good - Matches domain language
class SmartMeterAllocation
class InvoiceLineItem
class ShippingLabel
class BalanceResponsibleParty

// ✗ Bad - Forced taxonomic structure that doesn't match domain
class AllocationSmartMeter
class ItemLineInvoice
class LabelShipping
```

**2. Taxonomic Naming for Technical Concerns**

For infrastructure and technical components without strong domain terminology, taxonomic naming (generic to specific)
helps with discoverability and sorting:

```kotlin
// ✓ Good - Technical components benefit from grouping
ProductRepository
ProductRepositoryImpl
ProductRepositoryJdbc
ProductController
ProductControllerV1

// Exception handlers, configurations, etc.
ExceptionHandlerGlobal
ConfigurationSecurity
```

**3. When in Doubt**

Ask: "How would a domain expert refer to this?" If there's a natural term, use it. If it's purely technical
infrastructure, taxonomic naming can help organize code.

### Method Naming

```kotlin
// find* returns nullable
fun findById(id: ProductId): Product?

// get* returns non-nullable (throws if not found)
fun getById(id: ProductId): Product

// findAll* returns collection (possibly empty)
fun findAllByCategory(category: Category): List<Product>
```

---

## Nullability

### Prefer Non-Nullable Types

Design APIs to minimize nullability:

```kotlin
// ✓ Good - Non-nullable with default
data class Config(
    val timeout: Duration = Duration.ofSeconds(30),
    val retries: Int = 3
)

// ✗ Bad - Unnecessary nullability
data class Config(
    val timeout: Duration?,
    val retries: Int?
)
```

### Never Use `!!`

The not-null assertion operator bypasses null safety:

```kotlin
// ✗ Bad - Crashes at runtime if null
val name = user!!.name

// ✓ Good - Handle null explicitly
val name = user?.name ?: "Unknown"

// ✓ Good - Early return
val user = findUser(id) ?: return Result.err(UserNotFound(id))
```

### Use `?.let` for Null-Safe Operations

```kotlin
// ✓ Good - Execute only if non-null
user?.let { sendEmail(it) }

// ✓ Good - Transform nullable value
val upperName = name?.let { it.uppercase() }
```

### Treat Java Returns as Nullable

```kotlin
// ✓ Good - Assume Java returns are nullable
val value: String? = javaObject.getValue()

// ✗ Bad - Trust platform types
val value: String = javaObject.getValue() // May crash
```

---

## Immutability

### Prefer `val` Over `var`

```kotlin
// ✓ Good - Immutable
val products = listOf(product1, product2)
val user = User(name = "John", age = 30)

// ✗ Bad - Mutable without reason
var products = listOf(product1, product2)
```

### Use Immutable Collections

```kotlin
// ✓ Good - Immutable by default
val items: List<Item> = listOf(item1, item2)

// Only use mutable when necessary
val mutableItems: MutableList<Item> = mutableListOf()
```

### Use `copy()` for Data Class Updates

```kotlin
// ✓ Good - Create new instance
val updatedUser = user.copy(name = "Jane")

// ✗ Bad - Mutable property
user.name = "Jane"
```

---

## Scope Functions

### Quick Reference

| Function | Context | Returns       | Use Case                     |
|----------|---------|---------------|------------------------------|
| `let`    | `it`    | Lambda result | Null checks, transformations |
| `also`   | `it`    | Object        | Side effects (logging)       |
| `apply`  | `this`  | Object        | Object configuration         |
| `run`    | `this`  | Lambda result | Compute with object          |
| `with`   | `this`  | Lambda result | Group operations             |

### When to Use Each

**`let`** - Null-safe operations and transformations:

```kotlin
// ✓ Good - Null check
user?.let { sendNotification(it) }

// ✓ Good - Transform
val length = name?.let { it.trim().length } ?: 0
```

**`apply`** - Object initialization:

```kotlin
// ✓ Good - Configure object
val request = HttpRequest().apply {
    url = "https://api.example.com"
    timeout = Duration.ofSeconds(30)
    addHeader("Authorization", token)
}
```

**`also`** - Side effects without changing the object:

```kotlin
// ✓ Good - Logging
return product.also {
    logger.info("Created product: ${it.id}")
}
```

**`run`** - Compute result from object:

```kotlin
// ✓ Good - Compute value
val fullAddress = address.run {
    "$street, $city, $country"
}
```

### Antipatterns

```kotlin
// ✗ Bad - Nested scope functions (hard to read)
user?.let { u ->
    u.address?.let { a ->
        a.city?.let { c ->
            processCity(c)
        }
    }
}

// ✓ Good - Flatten with safe calls
user?.address?.city?.let { processCity(it) }

// ✗ Bad - Long blocks in scope functions
user.apply {
    // 50 lines of code...
}

// ✓ Good - Keep scope functions short
user.apply {
    name = "John"
    age = 30
}
```

---

## Data Classes

### Use for DTOs and Value Objects

```kotlin
// ✓ Good - Simple data carrier
data class ProductDto(
    val id: String,
    val name: String,
    val price: BigDecimal
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

### Prefer Data Classes Over Pair/Triple

```kotlin
// ✗ Bad - No semantic meaning
fun getNameAndAge(): Pair<String, Int>

// ✓ Good - Self-documenting
data class PersonInfo(val name: String, val age: Int)

fun getPersonInfo(): PersonInfo
```

---

## Extension Functions

### Use for Type-Specific Operations

```kotlin
// ✓ Good - Extends String functionality
fun String.toSlug(): String =
    this.lowercase().replace(" ", "-")

// ✓ Good - Mapper extensions
fun Product.toDto(): ProductDto = ProductDto(
    id = id.value,
    name = name,
    price = price.amount
)
```

### Limit Visibility

```kotlin
// ✓ Good - Internal to module
internal fun ProductEntity.toProduct(): Product

// ✗ Bad - Public extension pollutes API
fun Any.doSomething()
```

---

## Control Flow

### Use `when` as Expression

```kotlin
// ✓ Good - Exhaustive, compiler-checked
val status = when (result) {
    is Success -> "OK"
    is Error -> "Failed"
}

// ✓ Good - Sealed class exhaustiveness
fun handle(error: ProductError): HttpStatus = when (error) {
    is ProductError.NotFound -> HttpStatus.NOT_FOUND
    is ProductError.ValidationFailed -> HttpStatus.BAD_REQUEST
    is ProductError.DuplicateName -> HttpStatus.CONFLICT
}
```

### Prefer Early Returns

```kotlin
// ✓ Good - Early return reduces nesting
fun processOrder(order: Order?): Result<Unit> {
    order ?: return Result.err(OrderNotFound)
    if (!order.isValid()) return Result.err(InvalidOrder)

    // Main logic here
    return Result.ok(Unit)
}

// ✗ Bad - Deep nesting
fun processOrder(order: Order?): Result<Unit> {
    if (order != null) {
        if (order.isValid()) {
            // Main logic here
        }
    }
}
```

---

## Collections

### Use Sequences for Large Collections

```kotlin
// ✓ Good - Lazy evaluation for large data
products.asSequence()
    .filter { it.isActive }
    .map { it.toDto() }
    .take(10)
    .toList()

// For small collections, regular operations are fine
smallList.filter { it.isActive }.map { it.name }
```

### Prefer Specific Collection Functions

```kotlin
// ✓ Good - Specific function
val activeProducts = products.filter { it.isActive }
val names = products.map { it.name }
val product = products.firstOrNull { it.id == id }

// ✗ Bad - Manual iteration
val activeProducts = mutableListOf<Product>()
for (p in products) {
    if (p.isActive) activeProducts.add(p)
}
```

---

## Common Antipatterns

### Avoid Reflection

```kotlin
// ✗ Bad - Slow, bypasses type safety
val properties = MyClass::class.memberProperties

// ✓ Good - Use language features
data class MyClass(val a: String, val b: Int)

val map = mapOf("a" to myClass.a, "b" to myClass.b)
```

### Avoid Platform Types in Public APIs

```kotlin
// ✗ Bad - Exposes platform type
fun getName(): String = javaObject.getName()

// ✓ Good - Explicit nullability
fun getName(): String? = javaObject.getName()
```

### Avoid `lateinit` When Possible

```kotlin
// ✗ Bad - Runtime risk
class Service {
    lateinit var repository: Repository
}

// ✓ Good - Constructor injection
class Service(
    private val repository: Repository
)
```

### Avoid Overusing DSLs

```kotlin
// ✗ Bad - Unnecessarily complex
config {
    database {
        url = "..."
        username = "..."
    }
}

// ✓ Good - Simple constructor
val config = DatabaseConfig(
    url = "...",
    username = "..."
)
```

---

## Formatting

### Line Length

Limit lines to 120 characters. Break at higher syntactic levels:

```kotlin
// Break after operators
val result = veryLongVariableName +
        anotherLongVariableName

// Break after commas in parameters
fun createProduct(
    name: String,
    description: String,
    category: ProductCategory,
    price: Money
): Product
```

### Trailing Commas

Use trailing commas in multi-line declarations:

```kotlin
data class Product(
    val id: ProductId,
    val name: String,
    val price: Money,
)
```

### Braces

Always use braces for control structures:

```kotlin
// ✓ Good
if (condition) {
    doSomething()
}

// ✗ Bad
if (condition) doSomething()
```

---

## Project-Specific Conventions

### Test Method Naming

```kotlin
// Kotlin style
@Test
fun `createProduct should return error when name is blank`()

// Context can be omitted if obvious
@Test
fun `createProduct should generate unique id`()
```

### Mapper Naming

```kotlin
// File: ProductEntityMappers.kt
internal fun ProductEntity.toProduct(): Product
internal fun Product.toEntity(): ProductEntity

// File: ProductDtoMappers.kt  
fun Product.toDto(): ProductDto
fun ProductCreateRequest.toDomain(): Product
```

---

## Consequences

### Positive

- Consistent, readable codebase
- Fewer runtime errors through null safety
- Self-documenting code
- Easier onboarding for new developers

### Negative

- Learning curve for Java developers
- Some verbosity in null handling
- Discipline required to avoid antipatterns

---

## References

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Kotlin Scope Functions](https://kotlinlang.org/docs/scope-functions.html)
