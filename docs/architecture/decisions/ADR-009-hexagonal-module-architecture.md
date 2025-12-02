# ADR-009: Hexagonal Module Architecture (Light)

**Status**: Accepted
**Date**: 2024-05-23
**Related ADRs**: [ADR-008: Error Handling in REST Endpoints](ADR-008-error-handling-in-rest-endpoints.md)

-----

## Decision

To balance maintainability, modularity, and simplicity, we adopt a **"Hex Light"** architecture nested within a **Modular Monolith** physical structure.

Each high-level Domain (e.g., Inventory, Shipping) will be split into two physical Gradle modules:

1.  **`-api` Module**: The Public Contract and Public Errors.
2.  **`-impl` Module**: The Hexagonal Implementation.

We strictly follow **ADR-008** for error handling, utilizing Kotlin `Result` types and Sealed Classes for domain errors, and mapping them to **Zalando Problem** responses at the controller level.

-----

## Module Organization & Package Structure

### 1\. The API Module (`-api`)

**Role**: Defines **what** the module can do. Acts as the contract for other domains.
**Dependencies**: None (or Minimal Shared Kernel).

```text
com.wingedsheep.ecologique.inventory.api
├── InventoryApi.kt         # Public Interface (Service Contract)
├── model                   # Public DTOs (Data Transfer Objects)
├── error                   # Public Sealed Class Error Hierarchies
└── event                   # Integration Events
```

### 2\. The Implementation Module (`-impl`)

**Role**: Owns the "Hexagon." Implements the contract and maps errors to HTTP responses.
**Dependencies**: `inventory-api`, `problem-spring-web`.

```text
com.wingedsheep.ecologique.inventory.impl
├── domain                  # CORE: Pure business logic
│   ├── model               # Entities, Value Objects (INTERNAL ONLY)
│   └── repository          # Repository Interfaces
├── application             # ORCHESTRATION: Use Cases
│   └── service             # Implements API Interfaces
└── infrastructure          # FRAMEWORK & IO
    ├── web                 # REST Controllers (Maps Domain Errors to Problem JSON)
    └── persistence         # Spring Data JDBC Repositories
```

-----

## Layer Responsibilities & Rules

### The Golden Rules

1.  **Domain Isolation**: The **Domain Model** (Entities) stays strictly inside the `-impl` module.
2.  **Error as Data**: Methods return `Result<T, DomainError>`. We do not throw exceptions for business rules (per ADR-008).
3.  **Controller Responsibility**: The Controller handles the mapping of `DomainError` -\> `Zalando Problem`. The Domain layer knows nothing about HTTP.

### 1\. Domain Layer (`impl/domain`)

* **Responsibility**: Encapsulate enterprise business rules.
* **Rules**:
    * Pure Kotlin. NO Spring dependencies.
    * Returns internal `Result` types or reuses API Error types if aligned.

### 2\. Application Layer (`impl/application`)

* **Responsibility**: Implementation of the `-api` interface.
* **Rules**:
    * Orchestrates the flow: Load → Logic → Save → Publish Event.
    * Returns `Result<T, PublicError>` defined in the `-api` module.

### 3\. Infrastructure Layer (`impl/infrastructure`)

* **Responsibility**: Adapters.
* **Rules**:
    * **Web**: Explicitly matches on the `Result` failure case to build `Problem` responses.

-----

## Complete Example: Inventory Module

### A. The Public Contract (`inventory-api`)

**`api/error/InventoryError.kt` (The Public Error Hierarchy)**

```kotlin
sealed class InventoryError {
    data object InsufficientStock : InventoryError()
    data class NotFound(val sku: String) : InventoryError()
    data class InvalidQuantity(val reason: String) : InventoryError()
}
```

**`api/InventoryApi.kt`**

```kotlin
interface InventoryApi {
    // Returns Result using the Sealed Class Error
    fun reduceStock(sku: String, amount: Int): Result<Unit, InventoryError>
}
```

-----

### B. The Internal Hexagon (`inventory-impl`)

**`impl/domain/model/InventoryItem.kt` (Internal Entity)**

```kotlin
data class InventoryItem(
    val sku: Sku,
    val quantity: Quantity
) {
    // Internal logic returns Result
    fun reduceStock(amount: Quantity): Result<InventoryItem, InventoryError> {
        if (quantity < amount) return Err(InventoryError.InsufficientStock)
        return Ok(copy(quantity = quantity - amount))
    }
}
```

**`impl/application/service/InventoryService.kt`**

```kotlin
@Service
class InventoryService(
    private val repository: InventoryRepository,
    private val eventPublisher: ApplicationEventPublisher
) : InventoryApi {

    @Transactional
    override fun reduceStock(sku: String, amount: Int): Result<Unit, InventoryError> {
        // 1. Load
        val item = repository.findBySku(Sku(sku))
            ?: return Err(InventoryError.NotFound(sku))

        // 2. Domain Logic
        return item.reduceStock(Quantity(amount)).andThen { savedItem ->
            // 3. Persist
            repository.save(savedItem)
            // 4. Publish Event
            eventPublisher.publishEvent(StockReducedEvent(sku, amount))
            Ok(Unit)
        }
    }
}
```

**`impl/infrastructure/web/InventoryController.kt`**

```kotlin
@RestController
class InventoryController(
    private val service: InventoryService
) {
    @PostMapping("/inventory/{sku}/reduce")
    fun reduce(@PathVariable sku: String, @RequestBody request: ReduceStockRequest): ResponseEntity<Any> {
        return service.reduceStock(sku, request.amount)
            .fold(
                // SUCCESS: Return DTO (or Empty OK)
                ifOk = { ResponseEntity.ok().build() },

                // FAILURE: Explicit Mapping to Zalando Problem (ADR-008)
                ifErr = { error ->
                    when (error) {
                        is InventoryError.InsufficientStock -> Problem.builder()
                            .withType(URI.create("urn:problem:inventory:insufficient-stock"))
                            .withTitle("Insufficient Stock")
                            .withStatus(Status.CONFLICT)
                            .withDetail("The requested quantity exceeds available inventory.")
                            .build()

                        is InventoryError.NotFound -> Problem.builder()
                            .withType(URI.create("urn:problem:inventory:not-found"))
                            .withTitle("Inventory Not Found")
                            .withStatus(Status.NOT_FOUND)
                            .withDetail("No inventory found for SKU: ${error.sku}")
                            .build()

                        is InventoryError.InvalidQuantity -> Problem.builder()
                            .withType(URI.create("urn:problem:inventory:invalid-quantity"))
                            .withTitle("Invalid Quantity")
                            .withStatus(Status.BAD_REQUEST)
                            .withDetail(error.reason)
                            .build()
                    }.toResponseEntity()
                }
            )
    }
}
```
