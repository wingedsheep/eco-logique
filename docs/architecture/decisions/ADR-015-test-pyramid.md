# ADR-015: Test Pyramid Strategy

**Status**: Accepted

**Date**: 2024-12-13

---

## Decision

We adopt a four-layer test pyramid prioritizing fast feedback at lower layers.

---

## The Layers

### 1. Unit Tests (Base, ~70%)

- **Focus**: Domain logic, value objects, utilities
- **Scope**: Single class with mocked dependencies
- **Speed**: Milliseconds
- **Use for**: Complex calculations, edge cases, state machines

```kotlin
class MoneyTest {
    @Test
    fun `cannot create negative amount`() {
        assertThatThrownBy { Money(BigDecimal("-10"), EUR) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
```

### 2. Module Tests (Foundation, ~20%)

- **Focus**: Module fulfills its contract
- **Scope**: One module fully wired, others mocked
- **Speed**: Seconds
- **Use for**: All requirements, happy paths, error cases

```gherkin
Feature: Shipment Creation

  Scenario: Create shipment for valid product
    Given a product exists with id "prod-123" and weight 500 grams
    When I create a shipment for product "prod-123"
    Then the shipment should be created with weight 500 grams
```

### 3. Integration Tests (~7%)

- **Focus**: Infrastructure adapters
- **Scope**: Repository + real DB, Controller + real Spring
- **Speed**: Seconds
- **Use for**: Complex queries, custom serialization

### 4. Application Tests (Top, ~3%)

- **Focus**: Critical user journeys
- **Scope**: Full application, all modules wired
- **Speed**: Minutes
- **Use for**: Integration verification only

---

## Testing Matrix

| Layer       | Subject                 | Mocks         | Context     |
|-------------|-------------------------|---------------|-------------|
| Unit        | Entities, Value Objects | Yes           | None        |
| Module      | Requirements            | Other modules | Full module |
| Integration | Adapters                | No            | Sliced      |
| Application | User journeys           | None          | Full app    |

---

## Guidelines

### "Shift Left"

Push assertions as low as possible. If testable in a unit test, don't test in E2E.

### Module Isolation

Module tests must mock other modules. This proves the module works assuming contracts are respected.

### Where to Focus

- **Unit tests**: Technical edge cases, complex logic
- **Module tests**: Every requirement
- **Integration tests**: Where other tests don't cover
- **Application tests**: Critical paths only

---

## Consequences

### Positive

- Fast feedback (most tests are unit/module)
- Clear failure isolation
- Architecture enforcement through mocking

### Negative

- Mock drift risk (mitigated by application tests)
- Setup complexity for multiple test types
