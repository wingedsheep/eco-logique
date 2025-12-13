# ADR-015: Test Pyramid Strategy

**Status:** Accepted  
**Date:** 2024-12-13  
**Related ADRs:** ADR-007: Feature File Tests Strategy

## Decision

We adopt a four-layer test pyramid tailored for a Modular Monolith. We prioritize fast feedback at the bottom layers
while ensuring architectural integrity at the higher layers.

## The Pyramid Layers

### 1. Unit Tests (The Base)

- **Focus:** Pure Domain Logic, Domain Entities, Value Objects, Utility Functions.
- **Scope:** Single class or small cluster of related classes.
- **Dependencies:** Mocked or Stubbed. No Spring Context. No Database.
- **Volume:** High (~70%).
- **Execution Time:** Milliseconds.
- **Example:** `ProductTest.kt` ensuring a product cannot have a negative price.

### 2. Integration Tests (The Glue)

- **Focus:** Infrastructure Adapters (Repositories, Web Controllers, External Clients).
- **Scope:** Interaction between application code and infrastructure components.
- **Dependencies:** Real Database (Testcontainers), WireMock for external APIs. Slice-based Spring Context (e.g.,
  `@DataJdbcTest`, `@WebMvcTest`).
- **Volume:** Medium (~20%).
- **Execution Time:** Seconds.
- **Example:** `ProductRepositoryImplIntegrationTest.kt` verifying SQL mappings.

### 3. Module/Component Tests (The Boundary)

- **Focus:** Acceptance criteria for a specific module (Bounded Context).
- **Scope:** One specific `-impl` module (e.g., `products-impl`).
- **Dependencies:**
    - **Internal:** Real wiring of the module (Service + Repository + DB).
    - **External Modules:** **MOCKED** `-api` interfaces of other modules.
- **Format:** Gherkin Feature files (Cucumber) or `@SpringBootTest`.
- **Goal:** Verify the module works in isolation and fulfills its contract.
- **Volume:** Low-Medium (~7%).
- **Example:** `products_module.feature` testing "Create Product" flow without involving Shipping or Inventory.

### 4. E2E / Application Tests (The Top)

- **Focus:** Critical User Journeys across the entire system.
- **Scope:** The full application.
- **Dependencies:** Everything real. All modules wired together. Real Database.
- **Format:** Gherkin Feature files (Cucumber).
- **Goal:** Verify that modules interact correctly (e.g., Events flowing from Payment -> Inventory -> Shipping).
- **Volume:** Low (~3%).
- **Execution Time:** Minutes.
- **Example:** `products.feature` (Application level) or Checkout E2E flow.

## Testing Matrix

| Layer       | Subject                           | Mocks                     | Context                         | Location               |
|-------------|-----------------------------------|---------------------------|---------------------------------|------------------------|
| Unit        | Entities, Value Objects, Services | Yes (Mockito)             | None                            | `*-impl/src/test`      |
| Integration | Repositories, Controllers         | No (Docker)               | Sliced (`@DataJdbcTest`)        | `*-impl/src/test`      |
| Module      | Functional Requirements           | Other Modules             | Full Module (`@SpringBootTest`) | `*-impl/src/test`      |
| E2E         | User Journeys                     | None / External 3rd Party | Full App (`@SpringBootTest`)    | `application/src/test` |

## Guidelines

### "Shift Left"

Push assertions as low as possible. If a rule can be tested in a Unit Test (e.g., "Price format"), do not test it in
E2E.

### Module Isolation Rule

Module Tests must **NOT** depend on the implementation of other modules.

If Shipping calls `ProductService`, the Module Test for Shipping must mock `ProductService`.

This proves that Shipping works assuming the contract (`-api`) is respected.

### Integration Verification

E2E Tests are the only place where real module-to-module communication is verified.

Use these sparingly to verify wiring and critical paths.

Rely on Worldview data to set up complex scenarios easily.

### Database Usage

- **Unit:** No DB.
- **Integration:** Testcontainers (clean DB per test class or method).
- **Module/E2E:** Testcontainers (schema initialized via Flyway).

## Consequences

### Positive

- **Speed:** Most tests run instantly (Unit).
- **Stability:** Flaky E2E tests are minimized.
- **Architecture enforcement:** Module tests force strict boundary checks by mocking external module APIs.
- **Debuggability:** Failures in lower layers point exactly to the broken line of code.

### Negative

- **Mock Drift:** Module tests might pass with mocks even if the real dependency has changed behavior (mitigated by E2E
  tests).
- **Setup Complexity:** Requires maintaining configurations for Slice tests, Module tests, and App tests.
