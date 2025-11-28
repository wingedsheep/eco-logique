# ADR-007: Feature File Tests Strategy

**Status**: Accepted

**Date**: 2025-11-28

---

## Decision

We implement feature file tests on two distinct levels: **Module** and **Application**. This separation ensures fast
feedback, thorough coverage of domain logic, and verification of the integrated system.

---

## Module Tests

Module tests focus on the behavior of a single module in isolation.

### Scope and Boundaries

- **Scope**: Limited to the module under test.
- **Boundaries**: The tests end at the module boundaries.
- **Dependencies**: Interfaces from other modules (e.g., other `-api` modules) are **mocked**.
- **Data**: Uses the module's own database schema.

### Scenarios

- **Primary Goal**: Verification of business logic and domain rules within the module.
- **Coverage**:
    - Happy paths.
    - **Alternative scenarios**: Error conditions, edge cases, business rule violations.
    - Extensive coverage of the module's capabilities.

### Implementation

- Tests interact with the module's public API.
- External dependencies are replaced with test doubles (mocks/stubs) to simulate various responses and failures from
  other parts of the system.

```kotlin
// Example: Testing Shipping Module
// ProductService is mocked to return specific product details
val productService = mock<ProductService>()
whenever(productService.getProduct(any())).thenReturn(product)

// Test runs against ShippingService
shippingService.createShipment(...)
```

---

## Application Tests

Application tests verify that the modules work together correctly as a cohesive system.

### Scope and Boundaries

- **Scope**: The entire application.
- **Entry Points**: Tests use the **actual entry points** of the application (e.g., REST endpoints, public API surface).
- **Dependencies**: Real wiring of all modules. No mocking of internal module communication.

### Scenarios

- **Primary Goal**: Verification of critical user journeys and integration between modules.
- **Coverage**:
    - **Happy flows**: End-to-end success scenarios.
    - Critical integration paths.
    - Significantly **fewer** tests than module tests.

### Implementation

- Tests run against the fully assembled application (e.g., Spring Boot context with all modules loaded).
- Verifies that real modules interact correctly.

---

## Comparison

| Feature          | Module Tests                             | Application Tests                   |
|:-----------------|:-----------------------------------------|:------------------------------------|
| **Scope**        | Single Module                            | Full Application                    |
| **Dependencies** | Mocked (other modules)                   | Real (all modules wired)            |
| **Entry Point**  | Module Interface/Service                 | Application Entry Points (REST/API) |
| **Focus**        | Logic, Alternative Scenarios, Edge Cases | Happy Flows, Integration, E2E       |
| **Quantity**     | Many                                     | Few                                 |

---

## Consequences

### Positive

- **Faster Feedback**: Module tests run faster as they are isolated.
- **Better Coverage**: Easier to test edge cases in isolation using mocks.
- **Clearer Failures**: Module test failures clearly indicate the source of the problem.
- **Critical Paths Secured**: Application tests ensure the system holds together for main use cases.

### Negative

- **Mock Maintenance**: Mocks in module tests need to be updated when external APIs change.
- **Gap Risk**: Risk of integration bugs if mocks don't behave exactly like the real implementations (mitigated by
  Application tests).
