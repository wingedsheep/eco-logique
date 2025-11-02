# Kotlin Coding Guidelines

## Table of Contents
1. [General Principles](#general-principles)
2. [Naming Conventions](#naming-conventions)
3. [Data Classes and Value Objects](#data-classes-and-value-objects)
4. [Nullability](#nullability)
5. [Functions and Methods](#functions-and-methods)
6. [Extension Functions](#extension-functions)
7. [Error Handling](#error-handling)
8. [Testing](#testing)
9. [Package Structure](#package-structure)

---

## General Principles

### Clean Code
- Write self-documenting code with clear variable and method names
- No inline comments - if code needs a comment, refactor it to be clearer
- Apply SOLID principles
- Keep things simple - don't do more than required

### SOLID Principles
- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Open for extension, closed for modification
- **Liskov Substitution**: Subtypes must be substitutable for their base types
- **Interface Segregation**: Many specific interfaces over one general interface
- **Dependency Inversion**: Depend on abstractions, not concretions

---

## Naming Conventions

### Taxonomic Naming
Use generic-to-specific ordering, not specific-to-generic.

```kotlin
// ✓ Good - Generic to specific
class ProductInventoryRepository
class ShipmentTrackingService
data class PaymentTransactionEntity

// ✗ Bad - Specific to generic
class InventoryProductRepository
class TrackingShipmentService
data class TransactionPaymentEntity
```

### Repository Methods

**`find<X>` returns nullable types:**
```kotlin
interface ProductRepository {
    fun findById(id: ProductId): Product?
    fun findByCategory(category: ProductCategory): List<Product>?
}
```

**`get<X>` returns non-nullable types:**
```kotlin
interface ProductService {
    fun getProduct(id: ProductId): Result<Product>
    fun getAllProducts(): List<Product>
}
```

**`findAll<X>` returns potentially empty collections:**
```kotlin
interface InventoryRepository {
    fun findAllByWarehouse(warehouseId: WarehouseId): List<InventoryItem>
}
```

### Boolean Properties
Use `is`, `has`, or `can` prefixes for clarity.

```kotlin
data class Product(
    val id: ProductId,
    val name: String,
    val isAvailable: Boolean,
    val hasStock: Boolean
)

data class Shipment(
    val id: ShipmentId,
    val canBeTracked: Boolean,
    val isInTransit: Boolean
)
```

---

## Data Classes and Value Objects

### Data Classes for Domain Models
Use `data class` for all domain entities and value objects.

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
```

### Value Objects with Inline Value Classes
Use `@JvmInline value class` for strongly typed IDs and single-property wrappers.

```kotlin
@JvmInline
value class ProductId(val value: String) {
    init {
        require(value.isNotBlank()) { "ProductId cannot be blank" }
    }
    
    companion object {
        fun generate(): ProductId = ProductId("PROD-${UUID.randomUUID()}")
    }
}

@JvmInline
value class WarehouseId(val value: String)

@JvmInline
value class ShipmentId(val value: String)
```

### Validation in Init Blocks
Always validate domain invariants in `init` blocks, never using JSR-303 annotations.

```kotlin
data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Money amount cannot be negative" }
    }
    
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return Money(amount + other.amount, currency)
    }
}

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
```

### Domain Types Over Primitives
Use domain-specific types instead of primitives.

```kotlin
// ✓ Good - Domain types
data class InventoryItem(
    val productId: ProductId,
    val warehouseId: WarehouseId,
    val quantity: Quantity
)

@JvmInline
value class Quantity(val value: Int) {
    init {
        require(value >= 0) { "Quantity cannot be negative" }
    }
}

// ✗ Bad - Primitive obsession
data class InventoryItem(
    val productId: String,
    val warehouseId: String,
    val quantity: Int
)
```

---

## Nullability

### Use Nullable Types Instead of Optionals
Kotlin's nullable types are more idiomatic than `Optional`.

```kotlin
// ✓ Good
interface ProductRepository {
    fun findById(id: ProductId): Product?
}

fun processProduct(productId: ProductId) {
    val product = productRepository.findById(productId)
    if (product != null) {
        shipProduct(product)
    }
}

// ✗ Bad
interface ProductRepository {
    fun findById(id: ProductId): Optional<Product>
}
```

### Never Use the `!!` Operator
The not-null assertion operator `!!` is a code smell. Use safe calls or proper null handling.

```kotlin
// ✓ Good
fun createShipment(productId: ProductId): Result<Shipment> {
    val product = productService.findProduct(productId)
        ?: return Result.failure(ProductNotFoundException(productId))
    
    return Result.success(
        Shipment(
            id = ShipmentId.generate(),
            productId = productId,
            weight = product.weight
        )
    )
}

// ✗ Bad
fun createShipment(productId: ProductId): Shipment {
    val product = productService.findProduct(productId)!!
    return Shipment(
        id = ShipmentId.generate(),
        productId = productId,
        weight = product.weight
    )
}
```

### Elvis Operator for Defaults
Use the Elvis operator `?:` for providing default values.

```kotlin
fun calculateShippingCost(shipment: Shipment): Money {
    val product = productRepository.findById(shipment.productId)
        ?: return Money(BigDecimal.ZERO, EUR)
    
    val baseRate = shippingRates[product.weight.unit] ?: Money(BigDecimal("5.00"), EUR)
    return baseRate * product.weight.grams
}
```

### Safe Calls and Let
Chain safe calls and use `let` for null-safe operations.

```kotlin
fun getShipmentTrackingInfo(shipmentId: ShipmentId): String? {
    return shipmentRepository
        .findById(shipmentId)
        ?.trackingNumber
        ?.let { "Track your shipment: $it" }
}
```

---

## Functions and Methods

### Single Responsibility
Each function should do one thing well.

```kotlin
// ✓ Good - Single responsibility
fun reserveInventory(productId: ProductId, quantity: Quantity): Result<Unit> {
    return inventoryService.reserve(productId, quantity)
}

fun validateStockAvailability(productId: ProductId, quantity: Quantity): Boolean {
    val stock = inventoryRepository.findByProductId(productId) ?: return false
    return stock.quantity >= quantity.value
}

// ✗ Bad - Multiple responsibilities
fun reserveInventoryAndValidateAndNotify(productId: ProductId, quantity: Quantity): Result<Unit> {
    val stock = inventoryRepository.findByProductId(productId) ?: return Result.failure(...)
    if (stock.quantity < quantity.value) return Result.failure(...)
    inventoryService.reserve(productId, quantity)
    emailService.notifyStockReserved(productId)
    return Result.success(Unit)
}
```

### Expression Bodies for Simple Functions
Use expression bodies when the function body is a single expression.

```kotlin
fun calculateTotalPrice(items: List<OrderItem>): Money =
    items.fold(Money(BigDecimal.ZERO, EUR)) { acc, item -> acc + item.totalPrice }

fun isInStock(productId: ProductId): Boolean =
    inventoryRepository.findByProductId(productId)?.quantity?.value ?: 0 > 0

fun ProductId.toTrackingReference(): String =
    "REF-${this.value}"
```

### Named Parameters for Clarity
Use named parameters when calling functions with multiple parameters, especially for booleans.

```kotlin
// ✓ Good
val product = buildProduct(
    name = "Organic Cotton T-Shirt",
    category = ProductCategory.CLOTHING,
    price = Money(BigDecimal("29.99"), EUR),
    weight = Weight(150, GRAMS)
)

shipmentService.createShipment(
    productId = product.id,
    warehouseId = warehouse.id,
    expedited = true
)

// ✗ Bad
val product = buildProduct(
    "Organic Cotton T-Shirt",
    ProductCategory.CLOTHING,
    Money(BigDecimal("29.99"), EUR),
    Weight(150, GRAMS)
)

shipmentService.createShipment(product.id, warehouse.id, true)
```

### Default Parameters
Use default parameters instead of overloading methods.

```kotlin
// ✓ Good
fun buildProduct(
    id: ProductId = ProductId.generate(),
    name: String = "Test Product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    price: Money = Money(BigDecimal("19.99"), EUR),
    weight: Weight = Weight(100, GRAMS),
    sustainabilityRating: SustainabilityRating = SustainabilityRating.B
): Product = Product(id, name, category, price, weight, sustainabilityRating)

// ✗ Bad - Method overloading
fun buildProduct(): Product
fun buildProduct(name: String): Product
fun buildProduct(name: String, category: ProductCategory): Product
```

---

## Extension Functions

### Mapper Extension Functions
Mappers should be top-level extension functions, not class methods.

```kotlin
// ✓ Good - Extension functions
// ProductRequestMappers.kt
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
        priceAmount = this.price.amount,
        priceCurrency = this.price.currency
    )
}

// ✗ Bad - Static methods or mapper classes
class ProductMapper {
    companion object {
        fun toProduct(request: ProductCreateRequest): Product { ... }
        fun toResponse(product: Product): ProductResponseV1 { ... }
    }
}
```

### Entity Mappers in Persistence Layer
Entity mappers must be in the persistence package and marked `internal`.

```kotlin
// ProductEntityMappers.kt (in persistence package)
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

### Domain Extension Functions
Create domain-specific extensions for common operations.

```kotlin
fun Money.format(): String =
    "${this.amount.setScale(2, RoundingMode.HALF_UP)} ${this.currency}"

fun Weight.toKilograms(): BigDecimal =
    BigDecimal(this.grams).divide(BigDecimal(1000), 3, RoundingMode.HALF_UP)

fun List<Product>.totalValue(): Money =
    this.fold(Money(BigDecimal.ZERO, EUR)) { acc, product -> acc + product.price }

fun Shipment.isDelivered(): Boolean =
    this.status == ShipmentStatus.DELIVERED
```

---

## Error Handling

### Use Kotlin Result for Recoverable Errors
Use `Result<T>` for operations that can fail in expected ways.

```kotlin
// ✓ Good - Result for recoverable errors
fun createPayment(orderId: OrderId, amount: Money): Result<Payment> = runCatching {
    require(amount.amount > BigDecimal.ZERO) { "Payment amount must be positive" }
    
    val payment = Payment(
        id = PaymentId.generate(),
        orderId = orderId,
        amount = amount,
        status = PaymentStatus.PENDING
    )
    
    paymentRepository.save(payment)
}

fun processPayment(paymentId: PaymentId): Result<Payment> {
    val payment = paymentRepository.findById(paymentId)
        ?: return Result.failure(PaymentNotFoundException(paymentId))
    
    return pspService.process(payment)
        .map { processedPayment ->
            eventPublisher.publish(PaymentCompleted(processedPayment.id, Instant.now()))
            processedPayment
        }
}
```

### Use require/check for Unrecoverable Errors
Use `require` for preconditions and `check` for invariants.

```kotlin
fun reserveStock(productId: ProductId, quantity: Quantity): Result<Unit> = runCatching {
    require(quantity.value > 0) { "Quantity must be positive" }
    
    val inventoryItem = inventoryRepository.findByProductId(productId)
        ?: throw IllegalStateException("Inventory item not found for product: ${productId.value}")
    
    check(inventoryItem.quantity.value >= quantity.value) {
        "Insufficient stock: available=${inventoryItem.quantity.value}, requested=${quantity.value}"
    }
    
    val updatedItem = inventoryItem.copy(
        quantity = Quantity(inventoryItem.quantity.value - quantity.value)
    )
    
    inventoryRepository.save(updatedItem)
}
```

### Avoid Custom Exceptions
Prefer `Result<T>` over creating custom exception types.

```kotlin
// ✓ Good - Result with standard exceptions
fun getProduct(id: ProductId): Result<Product> {
    val product = productRepository.findById(id)
        ?: return Result.failure(NoSuchElementException("Product not found: ${id.value}"))
    
    return Result.success(product)
}

// ✗ Bad - Custom exceptions
class ProductNotFoundException(id: ProductId) : Exception("Product not found: ${id.value}")

fun getProduct(id: ProductId): Product {
    return productRepository.findById(id)
        ?: throw ProductNotFoundException(id)
}
```

### Fold for Result Handling
Use `fold` to handle both success and failure cases elegantly.

```kotlin
fun handlePaymentResult(paymentId: PaymentId): ResponseEntity<PaymentResponseV1> {
    return paymentService.processPayment(paymentId).fold(
        onSuccess = { payment -> 
            ResponseEntity.ok(payment.toPaymentResponseV1()) 
        },
        onFailure = { error -> 
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build() 
        }
    )
}
```

---

## Testing

### Test Structure with Given/When/Then
Clearly separate test phases with comments.

```kotlin
@Test
fun `createProduct should create product and publish event when valid`() {
    // Given
    val request = buildProductCreateRequest(
        name = "Bamboo Toothbrush",
        category = ProductCategory.HOUSEHOLD
    )
    val product = buildProduct()
    whenever(productRepository.save(any())).thenReturn(product)
    
    // When
    val result = productService.createProduct(request)
    
    // Then
    assertThat(result.isSuccess).isTrue()
    verify(productRepository).save(any())
    verify(eventPublisher).publish(any<ProductCreated>())
}
```

### Concise AssertJ Assertions
Use AssertJ for readable, fluent assertions.

```kotlin
@Test
fun `reserveStock should reduce quantity when sufficient stock available`() {
    // Given
    val productId = ProductId("PROD-001")
    val inventoryItem = buildInventoryItem(
        productId = productId,
        quantity = Quantity(10)
    )
    whenever(inventoryRepository.findByProductId(productId)).thenReturn(inventoryItem)
    
    // When
    val result = inventoryService.reserveStock(productId, Quantity(3))
    
    // Then
    assertThat(result.isSuccess).isTrue()
    verify(inventoryRepository).save(
        argThat { it.quantity.value == 7 }
    )
}

@Test
fun `calculateTotalPrice should sum all item prices`() {
    // Given
    val items = listOf(
        buildOrderItem(price = Money(BigDecimal("10.00"), EUR)),
        buildOrderItem(price = Money(BigDecimal("15.00"), EUR)),
        buildOrderItem(price = Money(BigDecimal("5.00"), EUR))
    )
    
    // When
    val total = orderService.calculateTotalPrice(items)
    
    // Then
    assertThat(total.amount).isEqualByComparingTo(BigDecimal("30.00"))
    assertThat(total.currency).isEqualTo(EUR)
}
```

### Concrete Arguments in Stubbing
Always pass concrete arguments to stubbing calls, never use `any()` for stub setup.

```kotlin
// ✓ Good - Concrete arguments
@Test
fun `getProduct should return product when exists`() {
    // Given
    val productId = ProductId("PROD-001")
    val product = buildProduct(id = productId)
    whenever(productRepository.findById(productId)).thenReturn(product)
    
    // When
    val result = productService.getProduct(productId)
    
    // Then
    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrNull()).isEqualTo(product)
}

// ✗ Bad - any() in stubbing
@Test
fun `getProduct should return product when exists`() {
    // Given
    val product = buildProduct()
    whenever(productRepository.findById(any())).thenReturn(product)  // Too lenient
    
    // When
    val result = productService.getProduct(ProductId("PROD-001"))
    
    // Then
    assertThat(result.isSuccess).isTrue()
}
```

### Builder Functions with Defaults
Use builder functions with default parameters for test data.

```kotlin
fun buildProduct(
    id: ProductId = ProductId.generate(),
    name: String = "Test Product",
    category: ProductCategory = ProductCategory.HOUSEHOLD,
    price: Money = Money(BigDecimal("19.99"), EUR),
    weight: Weight = Weight(100, GRAMS),
    sustainabilityRating: SustainabilityRating = SustainabilityRating.B,
    carbonFootprint: CarbonFootprint = CarbonFootprint(BigDecimal("1.5"), KG_CO2)
): Product = Product(id, name, category, price, weight, sustainabilityRating, carbonFootprint)

fun buildInventoryItem(
    productId: ProductId = ProductId.generate(),
    warehouseId: WarehouseId = WarehouseId("WH-001"),
    quantity: Quantity = Quantity(10)
): InventoryItem = InventoryItem(productId, warehouseId, quantity)

fun buildShipment(
    id: ShipmentId = ShipmentId.generate(),
    productId: ProductId = ProductId.generate(),
    status: ShipmentStatus = ShipmentStatus.PENDING,
    trackingNumber: String? = null
): Shipment = Shipment(id, productId, status, trackingNumber)
```

### Test Naming Convention
Use backticks for descriptive test names in Kotlin.

```kotlin
// ✓ Good - Readable test names
class ProductServiceImplTest {
    @Test
    fun `createProduct should create product and publish event when valid`() { }
    
    @Test
    fun `createProduct should fail when name is blank`() { }
    
    @Test
    fun `createProduct should fail when price is negative`() { }
    
    @Test
    fun `updatePrice should update product price when product exists`() { }
    
    @Test
    fun `updatePrice should fail when product not found`() { }
}

// Pattern: `method should expected result when context`
```

---

## Package Structure

### Organize by Feature, Not Layer
Within a module implementation, organize by feature/subdomain when possible.

```kotlin
// ✓ Good - Feature-based
products-impl/
├── service/
│   └── ProductServiceImpl.kt
├── rest/
│   └── v1/
│       ├── ProductControllerV1.kt
│       ├── ProductCreateRequest.kt
│       ├── ProductResponseV1.kt
│       └── ProductRequestMappers.kt
└── persistence/
    ├── ProductRepository.kt
    ├── ProductRepositoryImpl.kt
    ├── ProductRepositoryJdbc.kt
    ├── ProductEntity.kt
    └── ProductEntityMappers.kt

// ✗ Bad - Layer-based (avoid this within modules)
products-impl/
├── controllers/
│   └── ProductController.kt
├── services/
│   └── ProductService.kt
├── repositories/
│   └── ProductRepository.kt
└── entities/
    └── ProductEntity.kt
```

### Package Visibility
Use `internal` modifier to restrict visibility within a module.

```kotlin
// Entities are internal to persistence package
@Table("products")
internal data class ProductEntity(
    @Id val id: String,
    val name: String,
    val categoryCode: String
)

// JDBC repository is internal
@Repository
internal interface ProductRepositoryJdbc : CrudRepository<ProductEntity, String>

// Repository implementation is internal
@Component
internal class ProductRepositoryImpl(
    private val jdbc: ProductRepositoryJdbc
) : ProductRepository {
    override fun save(product: Product): Product {
        return jdbc.save(product.toProductEntity()).toProduct()
    }
}
```

---

## Code Examples Summary

### Complete Domain Model Example
```kotlin
// Product.kt (in products-api)
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
    
    fun withUpdatedPrice(newPrice: Money): Product {
        require(newPrice.amount > BigDecimal.ZERO) { "New price must be positive" }
        return copy(price = newPrice)
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

data class Weight(val grams: Int, val unit: WeightUnit) {
    init {
        require(grams > 0) { "Weight must be positive" }
    }
    
    fun toKilograms(): BigDecimal =
        BigDecimal(grams).divide(BigDecimal(1000), 3, RoundingMode.HALF_UP)
}

enum class WeightUnit { GRAMS, KILOGRAMS }

data class CarbonFootprint(val kgCo2: BigDecimal, val unit: CarbonUnit) {
    init {
        require(kgCo2 >= BigDecimal.ZERO) { "Carbon footprint cannot be negative" }
    }
}

enum class CarbonUnit { KG_CO2 }

enum class SustainabilityRating { A_PLUS, A, B, C, D }
```

### Complete Service Implementation Example
```kotlin
// ProductServiceImpl.kt (in products-impl)
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
            sustainabilityRating = calculateSustainabilityRating(carbonFootprint),
            carbonFootprint = estimateCarbonFootprint(weight, category)
        )
        
        val savedProduct = productRepository.save(product)
        eventPublisher.publish(ProductCreated(savedProduct.id, Instant.now()))
        savedProduct
    }
    
    override fun getProduct(id: ProductId): Result<Product> {
        val product = productRepository.findById(id)
            ?: return Result.failure(NoSuchElementException("Product not found: ${id.value}"))
        
        return Result.success(product)
    }
    
    override fun updatePrice(id: ProductId, newPrice: Money): Result<Product> = runCatching {
        val product = productRepository.findById(id)
            ?: throw NoSuchElementException("Product not found: ${id.value}")
        
        val updatedProduct = product.withUpdatedPrice(newPrice)
        productRepository.save(updatedProduct)
    }
    
    private fun estimateCarbonFootprint(weight: Weight, category: ProductCategory): CarbonFootprint {
        val baseFootprint = when (category) {
            ProductCategory.CLOTHING -> BigDecimal("2.1")
            ProductCategory.HOUSEHOLD -> BigDecimal("1.5")
            ProductCategory.ELECTRONICS -> BigDecimal("5.0")
            ProductCategory.FOOD -> BigDecimal("0.8")
        }
        return CarbonFootprint(baseFootprint, KG_CO2)
    }
    
    private fun calculateSustainabilityRating(footprint: CarbonFootprint): SustainabilityRating =
        when {
            footprint.kgCo2 < BigDecimal("1.0") -> SustainabilityRating.A_PLUS
            footprint.kgCo2 < BigDecimal("2.0") -> SustainabilityRating.A
            footprint.kgCo2 < BigDecimal("4.0") -> SustainabilityRating.B
            footprint.kgCo2 < BigDecimal("6.0") -> SustainabilityRating.C
            else -> SustainabilityRating.D
        }
}
```

---

## Key Takeaways

1. **Use taxonomic naming** (generic to specific)
2. **No `!!` operator** - handle nullability properly
3. **`find` returns nullable, `get` returns non-nullable**
4. **Validate in `init` blocks**, not with annotations
5. **Use `data class` for domain models**
6. **Use `@JvmInline value class` for IDs and wrappers**
7. **Extension functions for mappers**
8. **`Result<T>` for recoverable errors**
9. **`require`/`check` for unrecoverable errors**
10. **Clear test structure with Given/When/Then**
11. **Concrete arguments in stubbing**
12. **Self-documenting code over comments**
