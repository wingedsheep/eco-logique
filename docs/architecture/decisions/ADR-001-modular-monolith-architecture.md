# ADR-001: Use Modular Monolith Architecture

**Status**: Accepted

**Date**: 2024-11-02

**Updated**: 2025-01-15

---

## Decision

We implement a **modular monolith** with strict Gradle module boundaries for physical separation. Each module maintains clean internal structure with dependencies pointing inward. We utilize the **Gradle Test Fixtures plugin** to share test helpers without creating separate physical modules.

---

## Module Structure

Each domain is a Gradle module with two submodules:

```
domain-name/
├── domain-name-api/        # Public Contract: Interfaces, DTOs, Events + Test Fixtures
└── domain-name-impl/       # Implementation: Domain Logic, Services, Infrastructure
```

---

## Dependency Rules

1. **`-impl` depends on `-api`**: Implementation fulfills the contract.
2. **Modules depend only on other modules' `-api`**: Never on `-impl`.
3. **Test scopes depend on other modules' `testFixtures`**: To generate valid data for integration tests.
4. **No circular dependencies**: Enforced by Gradle build.

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
```

---

## Data Isolation

1. **Separate PostgreSQL schemas** per module (or table prefixes for simpler setups).
2. **No shared tables** between modules.
3. **No foreign keys** across schemas.
4. **Cross-module data access** only through service APIs.

---

## Communication

### Synchronous (Direct Calls)

Use for immediate consistency. The implementation invokes the **Interface** defined in the target's `-api` module.

```kotlin
// In shipping-impl
@Service
class ShippingServiceImpl(
    private val productService: ProductServiceApi
) : ShippingServiceApi {
    override fun createShipment(request: CreateShipmentRequest): Result<ShipmentDto, ShipmentError> {
        val product = productService.getProduct(request.productId).getOrElse {
            return Err(ShipmentError.ProductNotFound(request.productId))
        }
        // Logic using product data
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

---

## Implementation Patterns

### API Module (`-api`)

**The Public Contract.**

* **Interfaces**: Service definitions (e.g., `ProductServiceApi`).
* **DTOs**: Data carriers. Request DTOs may include validation; response DTOs are plain data.
* **Events**: Integration events.
* **Errors**: Sealed class hierarchies for domain errors.
* **Test Fixtures**: Source located in `src/testFixtures/kotlin`.

### Implementation Module (`-impl`)

**The Implementation.**

* **Domain**: Pure business logic, entities, value objects. *Internal only.*
* **Services/Handlers**: Implements `-api` interfaces, orchestrates use cases.
* **Infrastructure**: Controllers, repository implementations, external clients.

Internal structure is flexible—flat for simple modules, vertical slices for complex ones.

---

## Deployment

Single Spring Boot application:

```kotlin
// application/build.gradle.kts
dependencies {
    implementation(project(":payment:payment-impl"))
    implementation(project(":products:products-impl"))
    implementation(project(":shipping:shipping-impl"))
    implementation(project(":inventory:inventory-impl"))
    implementation(project(":users:users-impl"))
}
```

* One deployable artifact.
* Single database with multiple schemas.
* All modules scale together.

---

## Consequences

### Positive

* **Strict Encapsulation**: Domain entities never leak outside the module.
* **Refactoring Safety**: Internal implementation can change without breaking consumers.
* **Efficient Builds**: Test fixtures compiled once and reused.
* **Simple Operations**: Monolithic deployment eliminates distributed system complexity.
* **Split-Ready**: Modules can be extracted to services if needed.

### Negative

* **Boilerplate**: Requires mapping between internal domain and public DTOs.
* **Coupling**: All modules share the same runtime and database instance.
* **Discipline**: Requires vigilance to ensure `-impl` details don't leak into `-api`.
