# ADR-001: Use Modular Monolith Architecture

**Status**: Accepted

**Date**: 2024-11-02

-----

## Decision

We implement a **modular monolith** combining **Hexagonal Architecture** for logical separation within modules and strict Gradle module boundaries for physical separation. We utilize the **Gradle Test Fixtures plugin** to share test helpers without creating separate physical modules.

-----

## Module Structure

Each domain is a Gradle module with two submodules:

```

domain-name/
├── domain-name-api/        \# Public Contract: Interfaces, DTOs, Events + Test Fixtures
└── domain-name-impl/       \# The Hexagon: Domain Logic, App Layer, Infrastructure

````

-----

## Dependency Rules

1.  **`-impl` depends on `-api`**: Implementation fulfills the contract.
2.  **Modules depend only on other modules' `-api`**: Never on `-impl`.
3.  **Test scopes depend on other modules' `testFixtures`**: To generate valid data for integration tests.
4.  **No circular dependencies**: Enforced by Gradle build.

```kotlin
// shipping-impl/build.gradle.kts
dependencies {
    // Internal Contract
    implementation(project(":domain:shipping:shipping-api"))

    // External Dependencies (API only)
    implementation(project(":domain:products:products-api"))
    implementation(project(":domain:inventory:inventory-api"))

    // Test Dependencies (using Gradle Test Fixtures)
    testImplementation(testFixtures(project(":domain:products:products-api")))
}
````

**Module dependency matrix**:

```
                 Payment  Products  Shipping  Inventory  Users
Payment             -        ✗         ✗         ✗         ✗
Products            ✗        -         ✗         ✗         ✗
Shipping            ✗        ✓         -         ✓         ✗
Inventory           ✗        ✓         ✗         -         ✗
Users               ✗        ✗         ✗         ✗         -
```

-----

## Data Isolation

1.  **Separate PostgreSQL schemas** per module: `payment`, `products`, `shipping`, `inventory`, `users`.
2.  **No shared tables** between modules.
3.  **No foreign keys** across schemas.
4.  **Cross-module data access** only through service APIs.

-----

## Communication

### Synchronous (Direct Calls)

Use for immediate consistency. The implementation invokes the **Interface** defined in the target's `-api` module.

```kotlin
// In shipping-impl
@Service
class ShippingServiceImpl(
    // Injected from products-api interface
    private val productService: ProductServiceApi
) : ShippingService {
    override fun createShipment(productId: String): Result<Shipment> {
        // Returns a DTO, not a Domain Entity
        val productDto = productService.getProduct(productId).getOrElse {
            return Result.failure(it)
        }
        // Logic using productDto
    }
}
```

### Asynchronous (Events)

Use for eventual consistency and loose coupling. Events are defined in the `-api` module.

```kotlin
// Publish (payment-impl via payment-api event definition)
eventPublisher.publishEvent(PaymentCompletedEvent(paymentId, orderId))

// Listen (shipping-impl)
@EventListener
fun onPaymentCompleted(event: PaymentCompletedEvent) {
    shipmentService.startFulfillment(event.orderId)
}
```

-----

## Implementation Patterns

### API Module (`-api`)

**The Public Contract.**

* **Interfaces**: Service definitions (e.g., `InventoryApi`).
* **DTOs**: Dumb data carriers. **No Domain Entities allowed.**
* **Events**: Integration events.
* **Test Fixtures**: Source located in `src/testFixtures/kotlin`.
* *Dependencies*: Minimal/None + `java-test-fixtures` plugin.

### Implementation Module (`-impl`)

**The Hexagon.**

* **Domain Layer**: Pure logic, entities (e.g., `InventoryItem`), business rules. *Internal only.*
* **Application Layer**: Implements `-api` interfaces, orchestrates use cases, maps Entities ↔ DTOs.
* **Infrastructure Layer**: Web controllers, database repositories.
* **Repositories**: Interfaces defined in Domain, implemented in Infrastructure.

### Test Fixtures (Gradle Plugin)

**Shared Test Helpers.**
Enabled via `java-test-fixtures` plugin in the `-api` module. Provides factories to create valid DTOs defined in `-api` for consumers to use in their tests.

```kotlin
// products-api/src/testFixtures/kotlin/.../ProductFixtures.kt
object ProductFixtures {
    fun standardProductDto(
        id: String = "prod-123",
        name: String = "Eco Toothbrush"
    ) = ProductDto(id, name)
}
```

-----

## Deployment

Single Spring Boot application:

```kotlin
// application/build.gradle.kts
dependencies {
    implementation(project(":domain:payment:payment-impl"))
    implementation(project(":domain:products:products-impl"))
    implementation(project(":domain:shipping:shipping-impl"))
    implementation(project(":domain:inventory:inventory-impl"))
    implementation(project(":domain:users:users-impl"))
}
```

* One deployable artifact.
* Single database with multiple schemas.
* All modules scale together.

-----

## Consequences

### Positive

* **Strict Encapsulation**: Domain entities never leak outside the module.
* **Refactoring Safety**: Internal implementation (DB, logic) can change without breaking consumers, provided API DTOs remain stable.
* **Efficient Builds**: Test fixtures are compiled only once and reused, without the overhead of maintaining a separate project structure.
* **Simple Operations**: Monolithic deployment eliminates distributed system complexity (network latency, partial failures).

### Negative

* **Boilerplate**: Requires mapping between internal Domain Entities and public API DTOs.
* **Coupling**: All modules share the same runtime and database instance.
* **Discipline**: Requires code review vigilance to ensure `-impl` details don't leak into `-api`.
