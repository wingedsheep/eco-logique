# Eco-nomique Backlog

## Progress Overview

- **Phase 1 (Foundation)**: 4/6 complete
- **Phase 2 (Products)**: 0/1 complete
- **Phase 3 (Users)**: 0/1 complete
- **Phase 4 (Payment)**: 0/1 complete
- **Phase 5 (Inventory)**: 0/1 complete
- **Phase 6 (Shipping)**: 0/1 complete
- **Phase 7 (Integration)**: 0/3 complete
- **Phase 8 (E2E Scenarios)**: 0/3 complete
- **Phase 9 (Production)**: 0/3 complete

**Total Progress**: 4/20 items complete

---

## Phase 1: Foundation

### - [x] 1. Project Structure Setup
**Description**: Create the complete Gradle project structure with build-logic, common modules, and domain module placeholders.

**Acceptance Criteria**:
- [x] Create gradle project in `deployables/ecologique`
- [x] Root `settings.gradle.kts` includes all modules
- [x] `build-logic/` contains reusable Gradle plugins
- [x] Empty module folders exist for all domains
- [x] `./gradlew build` succeeds on empty project

---

### - [x] 2. CI Pipeline
**Description**: Set up automated build and test pipeline.

**Acceptance Criteria**:
- [x] GitHub Actions workflow runs on PR and main branch
- [x] Workflow builds all modules
- [x] Workflow runs all tests (unit, integration, cucumber)
- [x] Workflow fails on test failure or compilation error
- [x] Build status badge in README

---

### - [x] 3. Docker Development Environment
**Description**: Set up local development environment with PostgreSQL and RabbitMQ.

**Acceptance Criteria**:
- [x] `docker-compose.yml` starts PostgreSQL with separate schemas
- [x] `docker-compose.yml` starts RabbitMQ
- [x] Init script creates schemas: payment, products, shipping, inventory, users
- [x] Can connect to databases from host machine
- [x] README documents how to start environment

---

### - [ ] 4. Common Modules
**Description**: Implement shared domain primitives used across all modules.

**Acceptance Criteria**:
- [x] `common-money`: Money value object with currency
- [x] `common-country`: Country enum with ISO codes
- [ ] `common-time`: DayNL with timezone handling
- [ ] Unit tests for all common types
- [ ] All common modules compile independently

---

### - [x] 5. Spring Boot Application Module
**Description**: Create the main Spring Boot application that wires all modules together.

**Acceptance Criteria**:
- [x] Spring Boot application starts successfully
- [x] Health check endpoint responds
- [x] Application connects to PostgreSQL
- [x] Flyway migrations run on startup
- [x] Create a just file for common commands
- [x] Cucumber test infrastructure configured with Testcontainers

---

### - [x] 6. Architecture Decision Records
**Description**: Document all architectural decisions made for the project foundation.

**Acceptance Criteria**:
- [x] ADR template created in `docs/architecture/decisions/`
- [x] **ADR-001**: Use Modular Monolith Architecture
    - [x] Context: Need to balance simplicity with modularity
    - [x] Decision: Build modular monolith, not microservices
    - [x] Consequences documented
- [x] **ADR-002**: Apply Domain-Driven Design
    - [x] Context: Complex business domain with multiple bounded contexts
    - [x] Decision: Use DDD with bounded contexts per module
    - [x] Consequences documented
- [x] **ADR-003**: Separate Database Schemas per Bounded Context
    - [x] Context: Need data isolation between modules
    - [x] Decision: Use separate PostgreSQL schemas
    - [x] Consequences documented
- [x] **ADR-004**: Mappers in Persistence Layer
    - [x] Context: Database entities should not leak outside persistence
    - [x] Decision: Entity mappers stay in persistence package
    - [x] Consequences documented
- [x] **ADR-005**: Event-Driven Communication Between Modules
    - [x] Context: Need loose coupling for async operations
    - [x] Decision: Use domain events with Spring ApplicationEventPublisher
    - [x] Consequences documented
- [x] **ADR-006**: Worldview Pattern for Domain Knowledge
    - [x] Context: Need realistic test data and domain documentation
    - [x] Decision: Implement worldview modules with realistic data
    - [x] Consequences documented
- [x] All ADRs follow consistent format
- [x] ADRs are referenced in module documentation

---

## Phase 2: Products Module

### - [ ] 7. Products Module with Tests and API Documentation
**Description**: Complete Products module: domain model, implementation, REST API, worldview, feature tests, and Swagger documentation.

**Acceptance Criteria**:
- [ ] **Domain (products-api)**:
    - [ ] `Product` entity with ProductId, name, category, price, weight, sustainability rating
    - [ ] `ProductCategory` enum
    - [ ] `ProductService` interface with CRUD operations
    - [ ] Value objects: `Weight`, `CarbonFootprint`, `SustainabilityRating`
    - [ ] `ProductCreated` domain event
    - [ ] All domain models have validation in init blocks

- [ ] **Implementation (products-impl)**:
    - [ ] `ProductServiceImpl` implements all operations
    - [ ] `ProductRepository` with Spring Data JDBC
    - [ ] `ProductEntity` and entity mappers in persistence package
    - [ ] REST endpoints: POST, GET, PUT, DELETE products
    - [ ] Request/Response DTOs with mappers
    - [ ] Flyway migration creates products table

- [ ] **API Documentation**:
    - [ ] SpringDoc OpenAPI dependency added
    - [ ] Swagger UI accessible at `/swagger-ui.html`
    - [ ] All Products endpoints documented with descriptions
    - [ ] Request/response examples using worldview data
    - [ ] API versioning clearly shown

- [ ] **Worldview (products-worldview)**:
    - [ ] At least 5 realistic eco-products defined
    - [ ] Product builder with sensible defaults
    - [ ] WorldviewDataLoader inserts products on startup (non-prod only)
    - [ ] Products cover all categories
    - [ ] Products include edge cases (very light, very heavy, high-value)

- [ ] **Tests**:
    - [ ] Unit tests for domain model validation
    - [ ] Unit tests for service
    - [ ] Integration tests for repository
    - [ ] Cucumber feature: "Manage Products"
        - [ ] Scenario: Create new eco-product
        - [ ] Scenario: Retrieve product by ID
        - [ ] Scenario: Update product price
        - [ ] Scenario: List products by category
    - [ ] Manual testing possible via Swagger UI
    - [ ] All tests pass in CI

---

## Phase 3: Users Module

### - [ ] 8. Users Module with Tests
**Description**: Complete Users module: domain model, implementation, REST API, worldview, and feature tests.

**Acceptance Criteria**:
- [ ] **Domain (users-api)**:
    - [ ] `User` entity with UserId, email, name, address
    - [ ] `UserService` interface
    - [ ] `Address` value object
    - [ ] `UserRegistered` domain event
    - [ ] Email validation in User entity

- [ ] **Implementation (users-impl)**:
    - [ ] `UserServiceImpl` with registration and update
    - [ ] `UserRepository` with Spring Data JDBC
    - [ ] `UserEntity` and mappers in persistence package
    - [ ] REST endpoints: POST, GET, PUT users
    - [ ] Flyway migration creates users table

- [ ] **API Documentation**:
    - [ ] All Users endpoints documented in Swagger UI
    - [ ] Request/response examples provided

- [ ] **Worldview (users-worldview)**:
    - [ ] At least 3 realistic users (different countries)
    - [ ] User builder with sensible defaults
    - [ ] WorldviewDataLoader inserts users on startup
    - [ ] Users include various scenarios (different countries, addresses)

- [ ] **Tests**:
    - [ ] Unit tests for domain model
    - [ ] Unit tests for service
    - [ ] Integration tests for repository
    - [ ] Cucumber feature: "User Management"
        - [ ] Scenario: Register new user
        - [ ] Scenario: Retrieve user profile
        - [ ] Scenario: Update user address
    - [ ] All tests pass in CI

---

## Phase 4: Payment Module

### - [ ] 9. Payment Module with Tests
**Description**: Complete Payment module: domain model, implementation, REST API, worldview, and feature tests.

**Acceptance Criteria**:
- [ ] **Domain (payment-api)**:
    - [ ] `Payment` entity with PaymentId, orderId, amount, status
    - [ ] `PaymentService` interface
    - [ ] `PaymentStatus` enum (pending, completed, failed)
    - [ ] `PaymentCompleted` and `PaymentFailed` domain events

- [ ] **Implementation (payment-impl)**:
    - [ ] `PaymentServiceImpl` with create and complete operations
    - [ ] `PaymentRepository` with Spring Data JDBC
    - [ ] `PaymentEntity` and mappers in persistence package
    - [ ] REST endpoints: POST, GET payments
    - [ ] Mock PSP service
    - [ ] Publishes PaymentCompleted event
    - [ ] Flyway migration creates payment tables

- [ ] **API Documentation**:
    - [ ] All Payment endpoints documented in Swagger UI
    - [ ] Request/response examples provided

- [ ] **Worldview (payment-worldview)**:
    - [ ] Payment builder with defaults
    - [ ] Scenarios: successful payment, failed payment, pending payment
    - [ ] Various payment amounts and currencies

- [ ] **Tests**:
    - [ ] Unit tests for domain model
    - [ ] Unit tests for service
    - [ ] Integration tests for repository
    - [ ] Cucumber feature: "Payment Processing"
        - [ ] Scenario: Process successful payment
        - [ ] Scenario: Handle failed payment
        - [ ] Scenario: Retrieve payment status
    - [ ] All tests pass in CI

---

## Phase 5: Inventory Module

### - [ ] 10. Inventory Module with Tests
**Description**: Complete Inventory module: domain model, implementation, REST API, worldview, and feature tests.

**Acceptance Criteria**:
- [ ] **Domain (inventory-api)**:
    - [ ] `InventoryItem` entity with productId, warehouseId, quantity
    - [ ] `Warehouse` entity with location (Country)
    - [ ] `InventoryService` interface
    - [ ] `StockReserved` and `StockReleased` domain events
    - [ ] Depends on Products API for ProductId

- [ ] **Implementation (inventory-impl)**:
    - [ ] `InventoryServiceImpl` with stock operations
    - [ ] `InventoryRepository` with Spring Data JDBC
    - [ ] `InventoryEntity` and mappers in persistence package
    - [ ] REST endpoints: GET stock, POST reserve, POST release
    - [ ] Flyway migration creates inventory tables

- [ ] **API Documentation**:
    - [ ] All Inventory endpoints documented in Swagger UI
    - [ ] Request/response examples provided

- [ ] **Worldview (inventory-worldview)**:
    - [ ] At least 2 warehouses (Netherlands, Germany)
    - [ ] Inventory for all worldview products
    - [ ] Various stock levels (in-stock, low-stock, out-of-stock)
    - [ ] WorldviewDataLoader inserts inventory on startup

- [ ] **Tests**:
    - [ ] Unit tests for domain model
    - [ ] Unit tests for service
    - [ ] Integration tests for repository
    - [ ] Cucumber feature: "Inventory Management"
        - [ ] Scenario: Check product stock availability
        - [ ] Scenario: Reserve stock for order
        - [ ] Scenario: Release reserved stock
        - [ ] Scenario: Handle insufficient stock
    - [ ] All tests pass in CI

---

## Phase 6: Shipping Module

### - [ ] 11. Shipping Module with Tests
**Description**: Complete Shipping module: domain model, implementation, REST API, worldview, and feature tests.

**Acceptance Criteria**:
- [ ] **Domain (shipping-api)**:
    - [ ] `Shipment` entity with orderId, productId, status, trackingNumber
    - [ ] `ShipmentService` interface
    - [ ] `ShipmentStatus` enum (pending, in-transit, delivered)
    - [ ] `ShipmentCreated` domain event
    - [ ] Depends on Products API and Inventory API

- [ ] **Implementation (shipping-impl)**:
    - [ ] `ShippingServiceImpl` creates shipments based on product weight
    - [ ] Calls ProductService to get product details
    - [ ] Calls InventoryService to determine warehouse location
    - [ ] `ShipmentRepository` with Spring Data JDBC
    - [ ] `ShipmentEntity` and mappers in persistence package
    - [ ] REST endpoints: POST, GET shipments
    - [ ] Flyway migration creates shipment tables

- [ ] **API Documentation**:
    - [ ] All Shipping endpoints documented in Swagger UI
    - [ ] Request/response examples provided

- [ ] **Worldview (shipping-worldview)**:
    - [ ] Shipment builder with defaults
    - [ ] Scenarios for different statuses
    - [ ] Shipments from different warehouses

- [ ] **Tests**:
    - [ ] Unit tests for domain model
    - [ ] Unit tests for service
    - [ ] Integration tests for repository
    - [ ] Cucumber feature: "Shipment Management"
        - [ ] Scenario: Create shipment for product
        - [ ] Scenario: Calculate shipping from correct warehouse
        - [ ] Scenario: Handle shipment for out-of-stock product
    - [ ] All tests pass in CI

---

## Phase 7: Integration & Events

### - [ ] 12. Domain Event Infrastructure
**Description**: Implement event publishing and subscription mechanism.

**Acceptance Criteria**:
- [ ] `DomainEventPublisher` interface and implementation
- [ ] Spring ApplicationEventPublisher integration
- [ ] Events are published when domain actions occur
- [ ] Integration test verifying event flow
- [ ] Documentation on event patterns

---

### - [ ] 13. Payment to Shipping Integration with Tests
**Description**: When payment completes, automatically create shipment via events.

**Acceptance Criteria**:
- [ ] `PaymentCompletedListener` in shipping-impl
- [ ] Listens to PaymentCompleted event
- [ ] Calls ShippingService to create shipment
- [ ] Integration test: payment complete → shipment created
- [ ] Cucumber feature: "Order Fulfillment"
    - [ ] Scenario: Payment completion triggers shipment
    - [ ] Scenario: Failed payment does not create shipment
- [ ] All tests pass in CI

---

### - [ ] 14. Shipping Inventory Integration with Tests
**Description**: When shipment is created, reserve inventory synchronously.

**Acceptance Criteria**:
- [ ] ShippingService calls InventoryService synchronously
- [ ] Reserves stock when shipment created
- [ ] Returns error if insufficient stock
- [ ] Integration test: shipment creation fails when out of stock
- [ ] Integration test: successful shipment reserves inventory
- [ ] Cucumber feature: "Stock Reservation"
    - [ ] Scenario: Shipment reserves available stock
    - [ ] Scenario: Shipment fails when stock insufficient
- [ ] All tests pass in CI

---

## Phase 8: End-to-End Scenarios

### - [ ] 15. Complete Checkout Flow
**Description**: Full user journey from product selection through payment to shipment.

**Acceptance Criteria**:
- [ ] Cucumber feature: "Complete Checkout Flow"
    - [ ] Scenario: Customer orders eco-product successfully
        - [ ] Given customer "John Doe" is registered
        - [ ] And product "Organic Cotton T-Shirt" is in stock
        - [ ] When customer creates payment for product
        - [ ] And payment completes successfully
        - [ ] Then a shipment should be created
        - [ ] And inventory should be reduced
    - [ ] Scenario: Customer cannot order out-of-stock product
        - [ ] Given product is out of stock
        - [ ] When customer attempts to create shipment
        - [ ] Then shipment creation fails
        - [ ] And appropriate error message is returned
- [ ] All steps implemented with worldview data
- [ ] All tests pass in CI

---

### - [ ] 16. Cross-Border Shipping Scenario
**Description**: Complex scenario with cross-border logistics.

**Acceptance Criteria**:
- [ ] Cucumber feature: "Cross-Border Shipping"
    - [ ] Scenario: German customer orders from Netherlands warehouse
        - [ ] Given customer "Hans Müller" lives in Germany
        - [ ] And product is stocked in Netherlands warehouse
        - [ ] When customer completes order
        - [ ] Then shipment originates from Netherlands warehouse
        - [ ] And shipping cost reflects cross-border delivery
    - [ ] Scenario: Local customer orders from local warehouse
        - [ ] Given customer lives in Netherlands
        - [ ] And product is stocked in Netherlands warehouse
        - [ ] Then shipment uses local delivery
        - [ ] And shipping cost is domestic rate
- [ ] All tests pass in CI

---

### - [ ] 17. Multi-Product Order Scenario
**Description**: Handle orders with multiple products from different warehouses.

**Acceptance Criteria**:
- [ ] Cucumber feature: "Multi-Product Orders"
    - [ ] Scenario: Order with products from different categories
        - [ ] Given customer orders 2 clothing items and 1 household item
        - [ ] When payment completes
        - [ ] Then inventory is reduced for all items
        - [ ] And shipments are created appropriately
    - [ ] Scenario: Partial stock availability
        - [ ] Given customer orders 3 items
        - [ ] And only 2 items are in stock
        - [ ] Then order creation fails
        - [ ] And no inventory is reserved
- [ ] All tests pass in CI

---

## Phase 9: Production Readiness

### - [ ] 18. Observability
**Description**: Add logging, metrics, and health checks.

**Acceptance Criteria**:
- [ ] Structured logging with correlation IDs
- [ ] Actuator endpoints enabled
- [ ] Custom health indicators per module
- [ ] Metrics exported for Prometheus
- [ ] Log aggregation works in Docker
- [ ] Integration test verifies health checks

---

### - [ ] 19. CD Pipeline
**Description**: Automated deployment pipeline.

**Acceptance Criteria**:
- [ ] GitHub Actions workflow deploys on main branch
- [ ] Publishes Docker image to registry
- [ ] Deployment to staging environment
- [ ] Smoke tests run after deployment
- [ ] Rollback mechanism documented

---

### - [ ] 20. Module Documentation
**Description**: Document each module's responsibilities and APIs.

**Acceptance Criteria**:
- [ ] `docs/modules/` contains one markdown file per module
- [ ] Each doc describes: purpose, dependencies, key entities, API endpoints
- [ ] Getting started guide for new developers
- [ ] Diagrams showing module relationships
- [ ] Worldview documentation explains domain vocabulary
- [ ] Links to relevant ADRs from module docs

---

## Total: 20 Items
- **Phase 1 (Foundation)**: 6 items
- **Phase 2 (Products)**: 1 item
- **Phase 3 (Users)**: 1 item
- **Phase 4 (Payment)**: 1 item
- **Phase 5 (Inventory)**: 1 item
- **Phase 6 (Shipping)**: 1 item
- **Phase 7 (Integration)**: 3 items
- **Phase 8 (E2E Scenarios)**: 3 items
- **Phase 9 (Production)**: 3 items

Each item is independently testable and delivers complete, working functionality including Cucumber feature tests where applicable.
