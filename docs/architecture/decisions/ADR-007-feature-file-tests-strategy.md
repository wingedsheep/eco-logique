# ADR-007: Feature File Tests Strategy

**Status**: Accepted

**Date**: 2025-11-28

---

## Decision

We implement feature file tests on two distinct levels: **Module** and **Application**.

---

## Module Tests

### Scope and Boundaries

- **Scope**: Single module under test
- **Dependencies**: Other modules are **mocked**
- **Data**: Real database (module's own schema)

### Scenarios

- Happy paths
- Error conditions and edge cases
- Extensive coverage of module's capabilities

### Implementation

```kotlin
class ShipmentSteps(
    private val shippingService: ShippingServiceApi,
    private val productService: ProductServiceApi,  // Mocked
) {
    @Given("a product exists with id {string} and weight {int} grams")
    fun mockProduct(productId: String, weight: Int) {
        every { productService.getProduct(productId) } returns Ok(
            buildProductDto(id = productId, weightGrams = weight)
        )
    }
}
```

---

## Application Tests

### Scope and Boundaries

- **Scope**: Entire application
- **Entry Points**: REST endpoints, public API
- **Dependencies**: All modules wired, no mocking

### Scenarios

- Critical user journeys only
- Happy flows and integration paths
- Significantly fewer tests than module tests

---

## Comparison

| Aspect       | Module Tests      | Application Tests |
|--------------|-------------------|-------------------|
| Scope        | Single module     | Full application  |
| Dependencies | Mocked            | Real              |
| Entry Point  | Service interface | REST/API          |
| Focus        | Logic, edge cases | Integration, E2E  |
| Quantity     | Many              | Few               |

---

## Step Definition Boundaries

**Module test fixtures own only their module's steps.** They mock external dependencies rather than calling them.

**Steps that cross module boundaries belong to application-level tests.** These live in the application module with full
visibility.

This keeps test dependencies flat and failures local.

---

## Consequences

### Positive

- Fast feedback from isolated module tests
- Better edge case coverage with mocks
- Clear failure isolation

### Negative

- Mock maintenance when APIs change
- Risk of integration bugs if mocks diverge (mitigated by application tests)
