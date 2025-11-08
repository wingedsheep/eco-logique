# Eco-nomique Backlog (Revised)

## Progress Overview

- **Phase 1 (Foundation)**: 6/8 complete
- **Phase 2 (Products Module)**: 0/5 complete
- **Phase 3 (Users Module)**: 0/3 complete
- **Phase 4 (Payment Module)**: 0/4 complete
- **Phase 5 (Inventory Module)**: 0/4 complete
- **Phase 6 (Shipping Module)**: 0/4 complete
- **Phase 7 (Integration)**: 0/5 complete
- **Phase 8 (Production Readiness)**: 0/3 complete

**Total Progress**: 6/36 items complete

---

## Testing Strategy

**Unit Tests**: Located in each module's `src/test/` directory. Test domain validation, service logic, repository operations, and controllers in isolation.

**Feature Tests**: Located in `:deployables:economique:test` module as Cucumber feature files. Always use worldview data. Step definitions live in each module's `testFixtures`.

**Integration Tests**: Test with real database using Testcontainers, verify cross-module interactions.

---

## Phase 1: Foundation (6/8 Complete)

### - [x] 1. Project Structure Setup
**Value**: Team can start developing modules with proper build setup and shared conventions.

**What This Delivers**: Complete Gradle multi-module project structure following modular monolith architecture.

**Acceptance Criteria**:
- [x] Gradle multi-module project with build-logic conventions
- [x] Root `settings.gradle.kts` includes all modules
- [x] `build-logic/` contains reusable plugins: kotlin-common, kotlin-library, spring-boot, test-fixtures
- [x] Module structure: common/, deployables/economique/{application,payment,products,shipping,inventory,users,worldview-loader,test}
- [x] `./gradlew build` succeeds on empty project

---

### - [x] 2. CI Pipeline
**Value**: Automated quality checks on every commit, preventing broken builds from merging.

**What This Delivers**: GitHub Actions workflow that builds and tests all modules automatically.

**Acceptance Criteria**:
- [x] GitHub Actions workflow runs on PR and main branch
- [x] Workflow builds all modules
- [x] Workflow runs all tests (unit, integration, cucumber)
- [x] Workflow fails on test failure or compilation error
- [x] Build status badge in README

---

### - [x] 3. Docker Development Environment
**Value**: Developers can run local database without installing PostgreSQL, with proper schema isolation per module.

**What This Delivers**: docker-compose setup matching production database structure.

**Acceptance Criteria**:
- [x] `docker-compose.yml` starts PostgreSQL with separate schemas
- [x] Init script creates schemas: payment, products, shipping, inventory, users
- [x] Database accessible from host machine at localhost:5432
- [x] README documents docker commands (up, down, logs)

---

### - [x] 4. Spring Boot Application Shell
**Value**: Application starts successfully, connects to database, and can accept HTTP requests.

**What This Delivers**: Runnable Spring Boot application that wires all modules together.

**Acceptance Criteria**:
- [x] Spring Boot application module in `:deployables:economique:application`
- [x] `EconomiqueApplication.kt` with component scanning for all modules
- [x] Health endpoint responds at `/actuator/health`
- [x] Application connects to PostgreSQL
- [x] Flyway configured for multiple schema locations
- [x] `application.yml` with datasource and flyway configuration
- [x] Application starts with `./gradlew :deployables:economique:application:bootRun`

---

### - [x] 5. Architecture Decision Records
**Value**: Team has documented, shared understanding of all major architectural decisions and patterns.

**What This Delivers**: Complete ADR documentation covering modular monolith principles, DDD, persistence, and communication patterns.

**Acceptance Criteria**:
- [x] ADR template created in `docs/architecture/decisions/`
- [x] **ADR-001**: Use Modular Monolith Architecture (module boundaries, public API pattern, data isolation)
- [x] **ADR-002**: Apply Domain-Driven Design (bounded contexts, ubiquitous language, tactical patterns)
- [x] **ADR-003**: Separate Database Schemas per Bounded Context (schema ownership, no cross-schema FKs)
- [x] **ADR-004**: Mappers in Persistence Layer (entity isolation, internal modifiers)
- [x] **ADR-005**: Event-Driven Communication Between Modules (synchronous vs asynchronous patterns)
- [x] **ADR-006**: Worldview Pattern for Domain Knowledge (realistic test data as code)
- [x] All ADRs follow consistent format with context, decision, consequences
- [x] ADRs referenced in module documentation

---

### - [x] 6. Development Tooling
**Value**: Common development tasks automated, reducing cognitive load and improving developer experience.

**What This Delivers**: justfile with standard commands for build, run, test, and docker operations.

**Acceptance Criteria**:
- [x] Justfile with commands: build, run, test, clean, docker-up, docker-down, docker-logs, rebuild, dev
- [x] README documents all just commands
- [x] `just dev` starts docker and runs application with one command
- [x] `just test` runs all tests

---

### - [ ] 7. Common Domain Types (Money & Country)
**Value**: Shared value objects prevent duplication and ensure consistent validation across all modules.

**What This Delivers**: Reusable Money and Country types with domain validation that all modules can depend on.

**Why This Matters**: Every module needs these types (prices, locations). Building them once ensures consistent behavior and validation rules.

**Acceptance Criteria**:

**common-money module**:
- [ ] `Money` data class with amount (BigDecimal) and currency (Currency enum)
- [ ] Validation: amount >= 0
- [ ] Arithmetic operators: plus, minus, times
- [ ] Unit tests for validation, arithmetic, edge cases
- [ ] TestFixtures: `buildMoney()` with defaults (amount=10.00, currency=EUR)

**common-country module**:
- [ ] `Country` enum with values: NETHERLANDS, GERMANY, BELGIUM, FRANCE
- [ ] Each country has iso2 property (NL, DE, BE, FR)
- [ ] Unit tests verify enum properties
- [ ] TestFixtures: reference to common countries

**Files to create**:
```
common/common-money/src/main/kotlin/com/economique/common/money/
  - Money.kt
common/common-money/src/test/kotlin/com/economique/common/money/
  - MoneyTest.kt
common/common-money/src/testFixtures/kotlin/com/economique/common/money/
  - MoneyBuilders.kt

common/common-country/src/main/kotlin/com/economique/common/country/
  - Country.kt
common/common-country/src/test/kotlin/com/economique/common/country/
  - CountryTest.kt
common/common-country/src/testFixtures/kotlin/com/economique/common/country/
  - CountryBuilders.kt
```

---

### - [ ] 8. Common Time Types (DayNL)
**Value**: Timezone-aware date handling eliminates bugs from incorrect date calculations in business logic.

**What This Delivers**: Type-safe date representation that always uses Netherlands timezone, preventing date boundary issues.

**Why This Matters**: Business dates (billing cycles, delivery dates) must use consistent timezone. DayNL encapsulates this complexity.

**Acceptance Criteria**:
- [ ] `DayNL` value class wrapping LocalDate
- [ ] Handles Netherlands timezone (Europe/Amsterdam) correctly
- [ ] Methods: today(), tomorrow(), yesterday(), plusDays(), minusDays()
- [ ] Unit tests for edge cases: DST transitions, year boundaries, leap years
- [ ] TestFixtures: `buildDayNL()` with default to LocalDate.now()

**Files to create**:
```
common/common-time/src/main/kotlin/com/economique/common/time/
  - DayNL.kt
common/common-time/src/test/kotlin/com/economique/common/time/
  - DayNLTest.kt
common/common-time/src/testFixtures/kotlin/com/economique/common/time/
  - DayNLBuilders.kt
```

---

## Phase 2: Products Module (0/5 Complete)

### - [ ] 9. Products Domain Model & Persistence
**Value**: Products can be stored and retrieved from database with proper domain validation.

**What This Delivers**: Complete persistence layer returning domain types, never exposing database entities.

**Why This Matters**: Foundation for Products module. Demonstrates proper entity/domain separation, repository pattern, and Flyway migrations.

**Acceptance Criteria**:

**Domain Model** (public, in model/ package):
- [ ] `Product` data class: ProductId, name, category, price (Money), weight
- [ ] `ProductId` value class with generate() method
- [ ] `ProductCategory` enum: CLOTHING, HOUSEHOLD, FOOD, BEAUTY
- [ ] `Weight` value class with grams, kilograms conversion
- [ ] `SustainabilityRating` enum: A_PLUS, A, B, C, D
- [ ] `CarbonFootprint` value class with kgCo2
- [ ] Validation in init blocks: name not blank, price > 0, weight > 0

**Persistence** (internal, in persistence/ package):
- [ ] `ProductRepository` interface: save(), findById(), findAll(), findByCategory()
- [ ] Returns domain types (Product, not ProductEntity)
- [ ] `ProductRepositoryImpl` uses Spring Data JDBC internally
- [ ] `ProductRepositoryJdbc` interface extends CrudRepository (marked internal)
- [ ] `ProductEntity` with @Table("products", schema = "products")
- [ ] Fields: id, name, category, priceAmount, priceCurrency, weightGrams
- [ ] `ProductEntityMappers.kt`: ProductEntity.toProduct(), Product.toProductEntity()
- [ ] All persistence classes marked `internal`

**Database**:
- [ ] Flyway migration `V1__create_products_table.sql` in `db/migration/products/`
- [ ] Creates products.products table with all fields, id as PK

**Testing**:
- [ ] Unit tests: domain validation (invalid name, negative price, zero weight)
- [ ] Repository integration tests with Testcontainers
- [ ] Integration tests: save product, find by id, find by category, returns null for missing id

**Files to create**:
```
deployables/economique/products/src/main/kotlin/com/economique/products/model/
  - Product.kt
  - ProductId.kt
  - ProductCategory.kt
  - Weight.kt
  - SustainabilityRating.kt
  - CarbonFootprint.kt
deployables/economique/products/src/main/kotlin/com/economique/products/persistence/
  - ProductRepository.kt
  - ProductRepositoryImpl.kt
  - ProductRepositoryJdbc.kt
  - ProductEntity.kt
  - ProductEntityMappers.kt
deployables/economique/products/src/test/kotlin/com/economique/products/
  - model/ProductTest.kt
  - persistence/ProductRepositoryIntegrationTest.kt
deployables/economique/application/src/main/resources/db/migration/products/
  - V1__create_products_table.sql
```

---

### - [ ] 10. Products Service Layer
**Value**: Business logic encapsulated in service layer, ready for REST API consumption.

**What This Delivers**: Service interface with domain validation, event publishing, and proper error handling.

**Why This Matters**: Demonstrates service pattern, Result<T> usage, domain events, and separation between service interface (public API) and implementation.

**Acceptance Criteria**:

**Public API** (api/ package):
- [ ] `ProductService` interface with methods:
    - [ ] createProduct(name, category, price, weight): Result<Product>
    - [ ] updateProduct(id, name?, price?, weight?): Result<Product>
    - [ ] findProduct(id): Product?
    - [ ] getAllProducts(): List<Product>
    - [ ] getProductsByCategory(category): List<Product>
- [ ] `ProductCreated` event: productId, timestamp

**Implementation** (service/ package, marked internal):
- [ ] `ProductServiceImpl` implements ProductService
- [ ] Uses ProductRepository for persistence
- [ ] Validates business rules (e.g., name uniqueness if required)
- [ ] Returns Result.failure() for validation errors
- [ ] Publishes ProductCreated event via ApplicationEventPublisher
- [ ] Uses `require()` for programmer errors (null checks)

**Testing**:
- [ ] Unit tests with mocked repository:
    - [ ] createProduct saves and publishes event
    - [ ] updateProduct updates fields correctly
    - [ ] updateProduct returns failure for non-existent id
    - [ ] getAllProducts returns repository data
    - [ ] getProductsByCategory filters correctly
- [ ] Integration tests with real database:
    - [ ] Full create-retrieve-update flow
    - [ ] Event publishing verified with @EventListener test component

**Files to create**:
```
deployables/economique/products/src/main/kotlin/com/economique/products/api/
  - ProductService.kt
  - ProductCreated.kt
deployables/economique/products/src/main/kotlin/com/economique/products/service/
  - ProductServiceImpl.kt
deployables/economique/products/src/test/kotlin/com/economique/products/service/
  - ProductServiceImplTest.kt
  - ProductServiceIntegrationTest.kt
```

---

### - [ ] 11. Products REST API
**Value**: Products can be created, retrieved, and updated via HTTP endpoints.

**What This Delivers**: RESTful API with proper request/response DTOs and version-specific controllers.

**Why This Matters**: Demonstrates REST layer pattern, DTO mapping, versioned APIs, and controller testing with MockMvc.

**Acceptance Criteria**:

**Controller** (rest/v1/ package):
- [ ] `ProductControllerV1` with @RequestMapping("/api/v1/products")
- [ ] POST /api/v1/products - create product
- [ ] GET /api/v1/products/{id} - get product by id
- [ ] GET /api/v1/products - list all products
- [ ] GET /api/v1/products?category={category} - filter by category
- [ ] PUT /api/v1/products/{id} - update product
- [ ] DELETE /api/v1/products/{id} - delete product
- [ ] Returns ResponseEntity with appropriate status codes (200, 201, 404, 400)

**DTOs**:
- [ ] `ProductCreateRequest`: name, category, priceAmount, priceCurrency, weightGrams
- [ ] `ProductUpdateRequest`: name?, priceAmount?, priceCurrency?, weightGrams?
- [ ] `ProductResponseV1`: id, name, category, priceAmount, priceCurrency, weightGrams, sustainabilityRating
- [ ] Bean validation annotations (@NotBlank, @Positive, etc.)

**Mappers** (extension functions in ProductRequestMappers.kt):
- [ ] ProductCreateRequest.toProduct()
- [ ] Product.toProductResponseV1()
- [ ] ProductUpdateRequest.applyTo(Product)

**Testing**:
- [ ] Unit tests with @WebMvcTest and mocked service:
    - [ ] POST returns 201 with created product
    - [ ] GET returns 200 with product
    - [ ] GET returns 404 for non-existent id
    - [ ] PUT returns 200 with updated product
    - [ ] DELETE returns 204
    - [ ] Validation errors return 400

**Files to create**:
```
deployables/economique/products/src/main/kotlin/com/economique/products/rest/v1/
  - ProductControllerV1.kt
  - ProductCreateRequest.kt
  - ProductUpdateRequest.kt
  - ProductResponseV1.kt
  - ProductRequestMappers.kt
deployables/economique/products/src/test/kotlin/com/economique/products/rest/v1/
  - ProductControllerV1Test.kt
```

---

### - [ ] 12. Products Worldview Data
**Value**: Realistic, named test data available project-wide for consistent testing and documentation.

**What This Delivers**: Named product instances representing real eco-friendly products, used across all test types.

**Why This Matters**: Demonstrates worldview pattern. Provides shared vocabulary between developers, testers, and domain experts. These products appear in Cucumber scenarios, API examples, and documentation.

**Acceptance Criteria**:

**Worldview Objects** (worldview-loader module):
- [ ] `WorldviewProduct` object with at least 5 realistic products:
    - [ ] organicCottonTShirt (CLOTHING, light, medium price)
    - [ ] bambooToothbrushSet (HOUSEHOLD, very light, cheap)
    - [ ] steelWaterBottle (HOUSEHOLD, medium weight, medium price)
    - [ ] organicCoffee (FOOD, heavy, expensive)
    - [ ] naturalShampooBar (BEAUTY, light, cheap)
- [ ] Cover all ProductCategory values
- [ ] Include edge cases: very light (<50g), heavy (>1kg), expensive (>€100)
- [ ] Stable IDs (PROD-001, PROD-002, etc.)
- [ ] Realistic names, weights, prices

**Builder** (testFixtures):
- [ ] `buildProduct()` function with sensible defaults
- [ ] Parameters: id, name, category, price, weight (all with defaults)
- [ ] Default values: id=generated, name="Test Product", category=HOUSEHOLD, price=€19.99, weight=100g

**Data Loader**:
- [ ] `WorldviewDataLoader` component
- [ ] @PostConstruct loads worldview products
- [ ] Only loads in non-prod profiles (checks spring.profiles.active)
- [ ] Uses ProductRepository to save

**Testing**:
- [ ] Integration test verifies worldview data loads on startup
- [ ] Integration test verifies all products retrievable by id

**Files to create**:
```
deployables/economique/worldview-loader/src/main/kotlin/com/economique/worldview/
  - WorldviewProduct.kt
  - WorldviewDataLoader.kt
deployables/economique/products/src/testFixtures/kotlin/com/economique/products/
  - ProductBuilders.kt
deployables/economique/worldview-loader/src/test/kotlin/com/economique/worldview/
  - WorldviewDataLoaderTest.kt
```

---

### - [ ] 13. Products API Documentation & Feature Tests
**Value**: API explorable via Swagger UI, complete end-to-end scenarios verified via Cucumber.

**What This Delivers**: Interactive API documentation and acceptance tests using Gherkin scenarios with worldview data.

**Why This Matters**: Swagger enables manual testing and external integration. Cucumber tests document business flows in plain language, verifying the complete stack works together.

**Acceptance Criteria**:

**API Documentation**:
- [ ] SpringDoc OpenAPI dependency in application module
- [ ] Swagger UI accessible at `http://localhost:8080/swagger-ui.html`
- [ ] All Products endpoints documented with @Operation annotations
- [ ] Request/response schemas include descriptions
- [ ] Examples use worldview data (organicCottonTShirt)
- [ ] API version (v1) clearly shown in paths
- [ ] README updated with Swagger UI link

**Feature Tests** (test module):
- [ ] Cucumber feature file: `products-management.feature`
- [ ] Scenario: Create new eco-product
    - [ ] When POST /api/v1/products with valid data
    - [ ] Then response is 201
    - [ ] And product can be retrieved by id
- [ ] Scenario: Retrieve product by ID
    - [ ] Given worldview product "Organic Cotton T-Shirt" exists
    - [ ] When GET /api/v1/products/{id}
    - [ ] Then response is 200 with correct product data
- [ ] Scenario: Update product price
    - [ ] Given product exists
    - [ ] When PUT /api/v1/products/{id}/price with new price
    - [ ] Then product price is updated
- [ ] Scenario: List products by category
    - [ ] Given multiple products exist in CLOTHING category
    - [ ] When GET /api/v1/products?category=CLOTHING
    - [ ] Then only CLOTHING products returned

**Step Definitions** (products testFixtures):
- [ ] Step definitions in ProductSteps.kt
- [ ] Steps reference worldview data by name
- [ ] Steps use TestRestTemplate or similar for HTTP calls

**Testing**:
- [ ] All Cucumber scenarios pass
- [ ] Scenarios use worldview data (not random data)
- [ ] @SpringBootTest with Testcontainers

**Files to create**:
```
deployables/economique/test/src/test/resources/features/
  - products-management.feature
deployables/economique/products/src/testFixtures/kotlin/com/economique/products/steps/
  - ProductSteps.kt
deployables/economique/application/build.gradle.kts (add springdoc dependency)
deployables/economique/products/src/main/kotlin/com/economique/products/rest/v1/
  - ProductControllerV1.kt (add @Operation annotations)
```

---

## Phase 3: Users Module (0/3 Complete)

### - [ ] 14. Users Domain Model, Persistence & Service
**Value**: User registration and management capability with proper address handling and email validation.

**What This Delivers**: Complete Users module from domain model through service layer, following same patterns as Products.

**Why This Matters**: Demonstrates value object composition (Address contains Country), email validation, and service layer with events.

**Acceptance Criteria**:

**Domain Model**:
- [ ] `User` data class: UserId, email, name, address
- [ ] `UserId` value class with generate() method
- [ ] `Address` value object: street, city, postalCode, country (Country from common-country)
- [ ] Email validation in User init block (basic format check)
- [ ] Address validation: street/city not blank

**Persistence**:
- [ ] `UserRepository` interface: save(), findById(), findByEmail(), findAll()
- [ ] `UserRepositoryImpl` with Spring Data JDBC
- [ ] `UserRepositoryJdbc` (internal)
- [ ] `UserEntity` with address flattened (street, city, postalCode, countryCode)
- [ ] `UserEntityMappers.kt`
- [ ] Flyway migration creates users.users table

**Service**:
- [ ] `UserService` interface: registerUser(), updateUser(), findUser(), getUserByEmail()
- [ ] `UserServiceImpl` (internal)
- [ ] Validation: email uniqueness (return failure if duplicate)
- [ ] `UserRegistered` event
- [ ] Publishes event on registration

**Testing**:
- [ ] Unit tests: domain validation (invalid email, blank name)
- [ ] Unit tests: service logic with mocked repository
- [ ] Integration tests: repository with Testcontainers
- [ ] Integration tests: service publishes event

**Files to create**:
```
deployables/economique/users/src/main/kotlin/com/economique/users/model/
  - User.kt, UserId.kt, Address.kt
deployables/economique/users/src/main/kotlin/com/economique/users/api/
  - UserService.kt, UserRegistered.kt
deployables/economique/users/src/main/kotlin/com/economique/users/service/
  - UserServiceImpl.kt
deployables/economique/users/src/main/kotlin/com/economique/users/persistence/
  - (repository files)
deployables/economique/application/src/main/resources/db/migration/users/
  - V1__create_users_table.sql
deployables/economique/users/src/test/kotlin/com/economique/users/
  - (unit and integration tests)
```

---

### - [ ] 15. Users REST API & Documentation
**Value**: Users can be registered and managed via HTTP API with Swagger documentation.

**What This Delivers**: RESTful endpoints for user management following established patterns.

**Acceptance Criteria**:

**Controller**:
- [ ] `UserControllerV1` at /api/v1/users
- [ ] POST /api/v1/users - register user (returns 201)
- [ ] GET /api/v1/users/{id} - get user (returns 200 or 404)
- [ ] GET /api/v1/users?email={email} - find by email
- [ ] PUT /api/v1/users/{id} - update user (address, name)

**DTOs**:
- [ ] `UserCreateRequest`: email, name, street, city, postalCode, country
- [ ] `UserUpdateRequest`: name?, street?, city?, postalCode?, country?
- [ ] `UserResponseV1`: id, email, name, address (nested object)
- [ ] Bean validation

**Mappers**:
- [ ] UserCreateRequest.toUser()
- [ ] User.toUserResponseV1()

**Documentation**:
- [ ] Swagger annotations on all endpoints
- [ ] Examples with worldview users

**Testing**:
- [ ] Controller unit tests with @WebMvcTest
- [ ] Tests for validation errors (invalid email format, missing required fields)

**Files to create**:
```
deployables/economique/users/src/main/kotlin/com/economique/users/rest/v1/
  - (controller, DTOs, mappers)
deployables/economique/users/src/test/kotlin/com/economique/users/rest/v1/
  - UserControllerV1Test.kt
```

---

### - [ ] 16. Users Worldview Data & Feature Tests
**Value**: Realistic user test data and end-to-end user management scenarios verified.

**What This Delivers**: Named users spanning multiple countries, used consistently across all tests.

**Acceptance Criteria**:

**Worldview Data**:
- [ ] `WorldviewUser` object with at least 3 users:
    - [ ] johnDoe (Netherlands, Amsterdam)
    - [ ] hansMuller (Germany, Berlin)
    - [ ] sophieDupont (France, Paris)
- [ ] Stable IDs (USER-001, USER-002, USER-003)
- [ ] Valid email addresses

**Builder**:
- [ ] `buildUser()` function in testFixtures
- [ ] Defaults: generated id, test email, Netherlands address

**Data Loader**:
- [ ] WorldviewDataLoader loads users (non-prod only)

**Feature Tests**:
- [ ] `user-management.feature`
- [ ] Scenario: Register new user
- [ ] Scenario: Retrieve user profile
- [ ] Scenario: Update user address
- [ ] Scenario: Prevent duplicate email registration

**Step Definitions**:
- [ ] UserSteps.kt in testFixtures

**Testing**:
- [ ] All Cucumber scenarios pass
- [ ] Integration test verifies worldview users loaded

**Files to create**:
```
deployables/economique/worldview-loader/src/main/kotlin/com/economique/worldview/
  - WorldviewUser.kt
deployables/economique/users/src/testFixtures/kotlin/com/economique/users/
  - UserBuilders.kt
  - steps/UserSteps.kt
deployables/economique/test/src/test/resources/features/
  - user-management.feature
```

---

## Phase 4: Payment Module (0/4 Complete)

### - [ ] 17. Payment Domain Model, Persistence & Service
**Value**: Payment tracking with status management and event-driven completion.

**What This Delivers**: Payment lifecycle management from creation through completion/failure.

**Why This Matters**: Demonstrates state management (status enum), external system integration preparation, and critical event publishing for downstream processes.

**Acceptance Criteria**:

**Domain Model**:
- [ ] `Payment` data class: PaymentId, orderId (string), amount (Money), status, createdAt
- [ ] `PaymentId` value class
- [ ] `PaymentStatus` enum: PENDING, COMPLETED, FAILED
- [ ] Validation: amount > 0

**Persistence**:
- [ ] `PaymentRepository`: save(), findById(), findByOrderId(), findAll()
- [ ] `PaymentRepositoryImpl` with Spring Data JDBC
- [ ] `PaymentEntity` with amount flattened
- [ ] Flyway migration creates payment.payments table

**Service**:
- [ ] `PaymentService` interface: createPayment(), completePayment(), failPayment(), findPayment()
- [ ] `PaymentServiceImpl`
- [ ] Status transition validation (can't complete already completed)
- [ ] Returns Result<Payment>

**Testing**:
- [ ] Unit tests: domain validation
- [ ] Unit tests: service validates status transitions
- [ ] Integration tests: repository operations
- [ ] Integration tests: service with real database

**Files to create**:
```
deployables/economique/payment/src/main/kotlin/com/economique/payment/model/
  - Payment.kt, PaymentId.kt, PaymentStatus.kt
deployables/economique/payment/src/main/kotlin/com/economique/payment/api/
  - PaymentService.kt
deployables/economique/payment/src/main/kotlin/com/economique/payment/service/
  - PaymentServiceImpl.kt
deployables/economique/payment/src/main/kotlin/com/economique/payment/persistence/
  - (repository files)
deployables/economique/application/src/main/resources/db/migration/payment/
  - V1__create_payment_tables.sql
deployables/economique/payment/src/test/kotlin/com/economique/payment/
  - (tests)
```

---

### - [ ] 18. Payment Service Provider Integration
**Value**: Payments can be processed through external payment gateway (mocked for now).

**What This Delivers**: Anti-corruption layer for PSP integration, demonstrating how to isolate external system complexity.

**Why This Matters**: Shows proper external system integration: adapter pattern, anti-corruption layer, mapping external models to domain. Mock PSP allows testing payment flows without real gateway.

**Acceptance Criteria**:

**PSP Integration**:
- [ ] `PspAdapter` in psp/ package
- [ ] Translates Payment domain model to PspPaymentRequest
- [ ] Translates PspPaymentResponse to Payment domain status
- [ ] `MockPspService` simulates payment gateway
- [ ] Mock returns success/failure based on amount (e.g., amounts ending in .99 fail)
- [ ] Handles timeout scenarios

**Domain Integration**:
- [ ] PaymentServiceImpl uses PspAdapter for actual processing
- [ ] completePayment() calls PSP and updates status
- [ ] Handles PSP failures gracefully (returns Result.failure)

**Testing**:
- [ ] Unit tests: PspAdapter translation logic
- [ ] Integration tests: PaymentService with MockPspService
- [ ] Tests for success, failure, and timeout scenarios

**Files to create**:
```
deployables/economique/payment/src/main/kotlin/com/economique/payment/psp/
  - PspAdapter.kt
  - MockPspService.kt
  - PspPaymentRequest.kt
  - PspPaymentResponse.kt
deployables/economique/payment/src/test/kotlin/com/economique/payment/psp/
  - PspAdapterTest.kt
deployables/economique/payment/src/test/kotlin/com/economique/payment/service/
  - PaymentServicePspIntegrationTest.kt
```

---

### - [ ] 19. Payment Events, REST API & Documentation
**Value**: Payment completion triggers downstream processes (shipment), API accessible via HTTP and Swagger.

**What This Delivers**: Domain events enabling asynchronous integration, RESTful payment API.

**Why This Matters**: PaymentCompleted event is critical trigger for fulfillment process. Demonstrates asynchronous module communication via events.

**Acceptance Criteria**:

**Events**:
- [ ] `PaymentCompleted` event: paymentId, orderId, amount, timestamp
- [ ] `PaymentFailed` event: paymentId, orderId, reason, timestamp
- [ ] Published by PaymentServiceImpl on status changes
- [ ] Events in api/ package (public)

**Controller**:
- [ ] `PaymentControllerV1` at /api/v1/payments
- [ ] POST /api/v1/payments - create payment
- [ ] POST /api/v1/payments/{id}/complete - trigger completion (calls PSP)
- [ ] GET /api/v1/payments/{id} - get payment status
- [ ] GET /api/v1/payments?orderId={orderId} - find by order

**DTOs**:
- [ ] `PaymentCreateRequest`: orderId, amount, currency
- [ ] `PaymentResponseV1`: id, orderId, status, amount, currency, createdAt

**Documentation**:
- [ ] Swagger annotations
- [ ] Examples with worldview data

**Testing**:
- [ ] Controller unit tests
- [ ] Integration test verifies events published
- [ ] Event listener test component captures events

**Files to create**:
```
deployables/economique/payment/src/main/kotlin/com/economique/payment/api/
  - PaymentCompleted.kt, PaymentFailed.kt
deployables/economique/payment/src/main/kotlin/com/economique/payment/rest/v1/
  - (controller, DTOs, mappers)
deployables/economique/payment/src/test/kotlin/com/economique/payment/
  - (tests)
```

---

### - [ ] 20. Payment Worldview Data & Feature Tests
**Value**: Realistic payment scenarios for testing complete payment flows.

**What This Delivers**: Named payments covering success, failure, and pending states.

**Acceptance Criteria**:

**Worldview Data**:
- [ ] `WorldviewPayment` object with scenarios:
    - [ ] successfulPayment (COMPLETED, €29.99)
    - [ ] failedPayment (FAILED, reason included)
    - [ ] pendingPayment (PENDING, €50.00)
- [ ] Payments linked to worldview users/products
- [ ] Stable IDs (PAY-001, PAY-002, PAY-003)

**Builder**:
- [ ] `buildPayment()` in testFixtures

**Feature Tests**:
- [ ] `payment-processing.feature`
- [ ] Scenario: Process successful payment
    - [ ] Given user and product exist
    - [ ] When create payment for order
    - [ ] And complete payment
    - [ ] Then payment status is COMPLETED
    - [ ] And PaymentCompleted event published
- [ ] Scenario: Handle failed payment
- [ ] Scenario: Retrieve payment status

**Step Definitions**:
- [ ] PaymentSteps.kt in testFixtures
- [ ] Steps verify event publishing

**Files to create**:
```
deployables/economique/worldview-loader/src/main/kotlin/com/economique/worldview/
  - WorldviewPayment.kt
deployables/economique/payment/src/testFixtures/kotlin/com/economique/payment/
  - PaymentBuilders.kt
  - steps/PaymentSteps.kt
deployables/economique/test/src/test/resources/features/
  - payment-processing.feature
```

---

## Phase 5: Inventory Module (0/4 Complete)

### - [ ] 21. Inventory Domain Model & Persistence
**Value**: Track stock levels across multiple warehouses with proper location management.

**What This Delivers**: Multi-warehouse inventory tracking with country-based warehouse locations.

**Why This Matters**: Demonstrates aggregate pattern (Warehouse + InventoryItems), cross-module references (productId), and multi-location inventory management.

**Acceptance Criteria**:

**Domain Model**:
- [ ] `Warehouse` entity: WarehouseId, name, location (Country)
- [ ] `InventoryItem` entity: InventoryItemId, productId (ProductId), warehouseId, quantity
- [ ] `WarehouseId`, `InventoryItemId` value classes
- [ ] Validation: quantity >= 0, productId not blank

**Persistence**:
- [ ] `WarehouseRepository`: save(), findById(), findAll(), findByLocation()
- [ ] `InventoryRepository`: save(), findById(), findByProductId(), findByWarehouse()
- [ ] Both repositories return domain types
- [ ] Entities (internal) with mappers
- [ ] Flyway migration creates inventory.warehouses and inventory.inventory_items tables

**Testing**:
- [ ] Unit tests: domain validation
- [ ] Integration tests: both repositories with Testcontainers
- [ ] Tests for multi-warehouse queries

**Files to create**:
```
deployables/economique/inventory/src/main/kotlin/com/economique/inventory/model/
  - Warehouse.kt, WarehouseId.kt
  - InventoryItem.kt, InventoryItemId.kt
deployables/economique/inventory/src/main/kotlin/com/economique/inventory/persistence/
  - WarehouseRepository.kt, WarehouseRepositoryImpl.kt, WarehouseRepositoryJdbc.kt
  - InventoryRepository.kt, InventoryRepositoryImpl.kt, InventoryRepositoryJdbc.kt
  - WarehouseEntity.kt, WarehouseEntityMappers.kt
  - InventoryItemEntity.kt, InventoryItemEntityMappers.kt
deployables/economique/application/src/main/resources/db/migration/inventory/
  - V1__create_inventory_tables.sql
deployables/economique/inventory/src/test/kotlin/com/economique/inventory/
  - (tests)
```

---

### - [ ] 22. Inventory Service with Product Integration
**Value**: Stock operations validate products exist, demonstrating synchronous cross-module communication.

**What This Delivers**: Inventory service that coordinates with Products module to ensure referential integrity.

**Why This Matters**: Shows synchronous cross-module dependency - Inventory depends on Products API to validate productId exists before allowing stock operations.

**Acceptance Criteria**:

**Service**:
- [ ] `InventoryService` interface: reserveStock(), releaseStock(), getStockLevel(), checkAvailability()
- [ ] `InventoryServiceImpl` (internal)
- [ ] Constructor-injected dependency on ProductService (from products module)
- [ ] reserveStock() validates product exists (calls productService.findProduct())
- [ ] Returns Result.failure if product doesn't exist
- [ ] Returns Result.failure if insufficient stock
- [ ] Updates quantity atomically

**Gradle Dependency**:
- [ ] inventory/build.gradle.kts depends on `:deployables:economique:products`

**Testing**:
- [ ] Unit tests with mocked ProductService:
    - [ ] Reserve succeeds when product exists and stock available
    - [ ] Reserve fails when product doesn't exist
    - [ ] Reserve fails when insufficient stock
    - [ ] Release increases stock
- [ ] Integration tests with both databases:
    - [ ] Tests use real ProductRepository and InventoryRepository
    - [ ] Verify cross-module calls work

**Files to create**:
```
deployables/economique/inventory/src/main/kotlin/com/economique/inventory/api/
  - InventoryService.kt
deployables/economique/inventory/src/main/kotlin/com/economique/inventory/service/
  - InventoryServiceImpl.kt
deployables/economique/inventory/build.gradle.kts (add products dependency)
deployables/economique/inventory/src/test/kotlin/com/economique/inventory/service/
  - InventoryServiceImplTest.kt (mocked)
  - InventoryServiceIntegrationTest.kt (real)
```

---

### - [ ] 23. Inventory Events, REST API & Documentation
**Value**: Stock changes notify interested parties, inventory accessible via HTTP API.

**What This Delivers**: Domain events for stock lifecycle, RESTful inventory API.

**Acceptance Criteria**:

**Events**:
- [ ] `StockReserved` event: inventoryItemId, productId, warehouseId, quantity, timestamp
- [ ] `StockReleased` event: inventoryItemId, productId, warehouseId, quantity, timestamp
- [ ] Published by InventoryServiceImpl
- [ ] Events in api/ package

**Controller**:
- [ ] `InventoryControllerV1` at /api/v1/inventory
- [ ] GET /api/v1/inventory/stock/{productId} - get stock levels across all warehouses
- [ ] POST /api/v1/inventory/reserve - reserve stock
- [ ] POST /api/v1/inventory/release - release reserved stock
- [ ] GET /api/v1/inventory/warehouses - list all warehouses

**DTOs**:
- [ ] `StockLevelResponse`: productId, warehouseId, warehouseName, location, quantity
- [ ] `ReserveStockRequest`: productId, quantity
- [ ] `ReleaseStockRequest`: productId, quantity

**Documentation**:
- [ ] Swagger annotations

**Testing**:
- [ ] Controller unit tests
- [ ] Integration tests verify events published

**Files to create**:
```
deployables/economique/inventory/src/main/kotlin/com/economique/inventory/api/
  - StockReserved.kt, StockReleased.kt
deployables/economique/inventory/src/main/kotlin/com/economique/inventory/rest/v1/
  - (controller, DTOs, mappers)
deployables/economique/inventory/src/test/kotlin/com/economique/inventory/rest/v1/
  - InventoryControllerV1Test.kt
```

---

### - [ ] 24. Inventory Worldview Data & Feature Tests
**Value**: Realistic multi-warehouse inventory scenarios for testing logistics flows.

**What This Delivers**: Named warehouses and inventory covering in-stock, low-stock, and out-of-stock scenarios.

**Acceptance Criteria**:

**Worldview Data**:
- [ ] `WorldviewWarehouse` object:
    - [ ] amsterdamWarehouse (Netherlands)
    - [ ] berlinWarehouse (Germany)
- [ ] `WorldviewInventory` object:
    - [ ] Stock for all worldview products distributed across warehouses
    - [ ] Some products in-stock (100+ units)
    - [ ] Some products low-stock (5 units)
    - [ ] Some products out-of-stock (0 units)
    - [ ] Some products only in specific warehouses

**Builders**:
- [ ] `buildWarehouse()`, `buildInventoryItem()` in testFixtures

**Feature Tests**:
- [ ] `inventory-management.feature`
- [ ] Scenario: Check product stock availability
    - [ ] Given product exists in warehouse
    - [ ] When check stock level
    - [ ] Then correct quantity returned
- [ ] Scenario: Reserve stock for order
    - [ ] Given sufficient stock exists
    - [ ] When reserve stock
    - [ ] Then stock quantity decreases
    - [ ] And StockReserved event published
- [ ] Scenario: Release reserved stock
- [ ] Scenario: Handle insufficient stock
    - [ ] Given product has 5 units
    - [ ] When attempt to reserve 10 units
    - [ ] Then reservation fails

**Step Definitions**:
- [ ] InventorySteps.kt in testFixtures

**Files to create**:
```
deployables/economique/worldview-loader/src/main/kotlin/com/economique/worldview/
  - WorldviewWarehouse.kt
  - WorldviewInventory.kt
deployables/economique/inventory/src/testFixtures/kotlin/com/economique/inventory/
  - InventoryBuilders.kt
  - steps/InventorySteps.kt
deployables/economique/test/src/test/resources/features/
  - inventory-management.feature
```

---

## Phase 6: Shipping Module (0/4 Complete)

### - [ ] 25. Shipping Domain Model & Persistence
**Value**: Track shipments from creation through delivery with proper status management.

**What This Delivers**: Shipment lifecycle tracking with status transitions.

**Acceptance Criteria**:

**Domain Model**:
- [ ] `Shipment` entity: ShipmentId, orderId, productId (ProductId), warehouseId (WarehouseId), status, trackingNumber, createdAt
- [ ] `ShipmentId` value class
- [ ] `TrackingNumber` value class (format: SHIP-{UUID})
- [ ] `ShipmentStatus` enum: PENDING, IN_TRANSIT, DELIVERED, CANCELLED
- [ ] Validation: orderId not blank, valid status transitions

**Persistence**:
- [ ] `ShipmentRepository`: save(), findById(), findByOrderId(), findByStatus(), findAll()
- [ ] `ShipmentRepositoryImpl` with Spring Data JDBC
- [ ] `ShipmentEntity` (internal) with mappers
- [ ] Flyway migration creates shipping.shipments table

**Testing**:
- [ ] Unit tests: domain validation, status transitions
- [ ] Integration tests: repository operations

**Files to create**:
```
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/model/
  - Shipment.kt, ShipmentId.kt, TrackingNumber.kt, ShipmentStatus.kt
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/persistence/
  - (repository files)
deployables/economique/application/src/main/resources/db/migration/shipping/
  - V1__create_shipment_tables.sql
deployables/economique/shipping/src/test/kotlin/com/economique/shipping/
  - (tests)
```

---

### - [ ] 26. Shipping Service with Product Integration
**Value**: Shipments use accurate product data (weight) for shipping calculations.

**What This Delivers**: Shipping service that integrates with Products to get product details for logistics.

**Why This Matters**: Demonstrates synchronous cross-module dependency - Shipping depends on Products API to get weight for shipping cost calculations.

**Acceptance Criteria**:

**Service**:
- [ ] `ShipmentService` interface: createShipment(), updateStatus(), findShipment(), calculateShippingCost()
- [ ] `ShipmentServiceImpl` (internal)
- [ ] Depends on ProductService (constructor injection)
- [ ] createShipment() calls productService.findProduct() to get weight
- [ ] Returns Result.failure if product doesn't exist
- [ ] Generates tracking number automatically
- [ ] calculateShippingCost() based on product weight

**Shipping Cost Logic**:
- [ ] 0-1 kg: €3.50
- [ ] 1-5 kg: €6.50
- [ ] 5+ kg: €12.00
- [ ] Cross-border (non-NL): multiply by 1.5

**Gradle Dependency**:
- [ ] shipping/build.gradle.kts depends on `:deployables:economique:products`

**Testing**:
- [ ] Unit tests with mocked ProductService:
    - [ ] Create shipment succeeds when product exists
    - [ ] Create shipment fails when product doesn't exist
    - [ ] Shipping cost calculated correctly by weight
- [ ] Integration tests with real ProductService

**Files to create**:
```
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/api/
  - ShipmentService.kt
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/service/
  - ShipmentServiceImpl.kt
  - ShippingCostCalculator.kt
deployables/economique/shipping/build.gradle.kts (add products dependency)
deployables/economique/shipping/src/test/kotlin/com/economique/shipping/service/
  - ShipmentServiceImplTest.kt
  - ShippingCostCalculatorTest.kt
```

---

### - [ ] 27. Shipping-Inventory Integration
**Value**: Shipments originate from correct warehouse based on inventory location.

**What This Delivers**: Coordination between Shipping and Inventory to determine fulfillment source.

**Why This Matters**: Shows synchronous cross-module call where Shipping queries Inventory to determine which warehouse has stock.

**Acceptance Criteria**:

**Service Enhancement**:
- [ ] ShipmentServiceImpl depends on InventoryService (constructor injection)
- [ ] createShipment() calls inventoryService.getStockLevel() to find warehouses with stock
- [ ] Selects warehouse based on:
    - [ ] Closest to destination country (if multiple have stock)
    - [ ] Any warehouse with stock (if only one)
- [ ] Returns Result.failure if no warehouse has stock
- [ ] Reserves stock during shipment creation (synchronous call)

**Gradle Dependency**:
- [ ] shipping/build.gradle.kts depends on `:deployables:economique:inventory`

**Testing**:
- [ ] Unit tests with mocked InventoryService:
    - [ ] Selects correct warehouse when multiple available
    - [ ] Fails when no warehouse has stock
    - [ ] Reserves stock successfully
- [ ] Integration tests:
    - [ ] Real ProductService, InventoryService, ShipmentService
    - [ ] Verify warehouse selection logic
    - [ ] Verify stock reservation

**Files to create**:
```
deployables/economique/shipping/build.gradle.kts (add inventory dependency)
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/service/
  - WarehouseSelector.kt (helper for warehouse selection logic)
deployables/economique/shipping/src/test/kotlin/com/economique/shipping/service/
  - ShipmentServiceInventoryIntegrationTest.kt
```

---

### - [ ] 28. Shipping Events, REST API, Worldview & Feature Tests
**Value**: Shipment creation tracked, API accessible, realistic test data, end-to-end scenarios verified.

**What This Delivers**: Complete Shipping module with events, REST API, worldview data, and Cucumber tests.

**Acceptance Criteria**:

**Events**:
- [ ] `ShipmentCreated` event: shipmentId, orderId, productId, warehouseId, trackingNumber, timestamp
- [ ] Published by ShipmentServiceImpl
- [ ] Events in api/ package

**Controller**:
- [ ] `ShipmentControllerV1` at /api/v1/shipments
- [ ] POST /api/v1/shipments - create shipment
- [ ] GET /api/v1/shipments/{id} - get shipment
- [ ] GET /api/v1/shipments?orderId={orderId} - find by order
- [ ] GET /api/v1/shipments/{id}/tracking - get tracking info
- [ ] PUT /api/v1/shipments/{id}/status - update status

**DTOs**:
- [ ] `CreateShipmentRequest`: orderId, productId, destinationCountry
- [ ] `ShipmentResponseV1`: id, orderId, productId, status, trackingNumber, estimatedCost
- [ ] `UpdateStatusRequest`: status

**Documentation**:
- [ ] Swagger annotations

**Worldview Data**:
- [ ] `WorldviewShipment` object with scenarios:
    - [ ] pendingShipment (PENDING)
    - [ ] inTransitShipment (IN_TRANSIT, tracking updates)
    - [ ] deliveredShipment (DELIVERED)
- [ ] Shipments linked to worldview products, warehouses, orders

**Builder**:
- [ ] `buildShipment()` in testFixtures

**Feature Tests**:
- [ ] `shipment-management.feature`
- [ ] Scenario: Create shipment for product
    - [ ] Given product in stock at warehouse
    - [ ] When create shipment for order
    - [ ] Then shipment created with tracking number
    - [ ] And stock reserved from correct warehouse
    - [ ] And ShipmentCreated event published
- [ ] Scenario: Calculate shipping from correct warehouse
    - [ ] Given product in multiple warehouses
    - [ ] When create shipment to Germany
    - [ ] Then shipment originates from Berlin warehouse (closest)
- [ ] Scenario: Handle shipment for out-of-stock product
    - [ ] Given product out of stock
    - [ ] When attempt to create shipment
    - [ ] Then shipment creation fails

**Step Definitions**:
- [ ] ShipmentSteps.kt in testFixtures

**Testing**:
- [ ] Controller unit tests
- [ ] Integration tests verify events
- [ ] All Cucumber scenarios pass

**Files to create**:
```
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/api/
  - ShipmentCreated.kt
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/rest/v1/
  - (controller, DTOs, mappers)
deployables/economique/worldview-loader/src/main/kotlin/com/economique/worldview/
  - WorldviewShipment.kt
deployables/economique/shipping/src/testFixtures/kotlin/com/economique/shipping/
  - ShipmentBuilders.kt
  - steps/ShipmentSteps.kt
deployables/economique/test/src/test/resources/features/
  - shipment-management.feature
deployables/economique/shipping/src/test/kotlin/com/economique/shipping/rest/v1/
  - ShipmentControllerV1Test.kt
```

---

## Phase 7: Integration (0/5 Complete)

### - [ ] 29. Payment-to-Shipping Event Flow
**Value**: Completed payments automatically trigger shipment creation without manual intervention.

**What This Delivers**: Asynchronous event-driven integration between Payment and Shipping modules.

**Why This Matters**: Demonstrates asynchronous cross-module communication via domain events. Payment completion automatically starts fulfillment process, showing loose coupling between modules.

**Acceptance Criteria**:

**Event Listener**:
- [ ] `PaymentCompletedListener` in shipping module (event/ package, internal)
- [ ] Annotated with @EventListener
- [ ] Listens to PaymentCompleted event from payment module
- [ ] Calls ShipmentService.createShipment() with order details
- [ ] Logs successful shipment creation
- [ ] Handles failures gracefully (logs error, doesn't throw)
- [ ] Idempotent (checks if shipment already exists for order)

**Error Handling**:
- [ ] If shipment creation fails (e.g., out of stock), logs error but doesn't fail payment
- [ ] Could publish ShipmentCreationFailed event for monitoring

**Testing**:
- [ ] Unit test: listener calls ShipmentService with correct data
- [ ] Integration test: publish PaymentCompleted event → verify shipment created
- [ ] Integration test: failed payment → no shipment created
- [ ] Integration test: listener handles ShipmentService failure gracefully
- [ ] Integration test: idempotency - duplicate event doesn't create duplicate shipment

**Feature Test**:
- [ ] Enhance `checkout-flow.feature` with payment-to-shipment scenario
- [ ] Scenario: Payment completion triggers shipment
    - [ ] Given product in stock
    - [ ] When payment completes successfully
    - [ ] Then shipment automatically created
    - [ ] And shipment status is PENDING

**Files to create**:
```
deployables/economique/shipping/src/main/kotlin/com/economique/shipping/event/
  - PaymentCompletedListener.kt
deployables/economique/shipping/src/test/kotlin/com/economique/shipping/event/
  - PaymentCompletedListenerTest.kt (unit)
  - PaymentCompletedListenerIntegrationTest.kt (integration)
deployables/economique/test/src/test/resources/features/
  - checkout-flow.feature (enhance existing or create new)
```

---

### - [ ] 30. Shipment Stock Reservation Flow
**Value**: Creating shipments automatically reserves inventory, preventing overselling.

**What This Delivers**: Synchronous coordination ensuring shipments only created when stock available.

**Why This Matters**: Already implemented in story 27, this story adds comprehensive testing and failure scenarios.

**Acceptance Criteria**:

**Enhanced Testing**:
- [ ] Integration test: successful shipment creation reserves stock
    - [ ] Given product with 10 units in stock
    - [ ] When create shipment
    - [ ] Then stock reduced to 9 units
    - [ ] And StockReserved event published
- [ ] Integration test: insufficient stock prevents shipment
    - [ ] Given product with 2 units in stock
    - [ ] When attempt to create shipment for 5 units
    - [ ] Then shipment creation fails
    - [ ] And stock remains unchanged
    - [ ] And no StockReserved event published
- [ ] Integration test: shipment failure releases stock
    - [ ] Given stock reserved for shipment
    - [ ] When shipment cancelled
    - [ ] Then stock released back to inventory

**Feature Test**:
- [ ] `stock-reservation.feature`
- [ ] Scenario: Shipment reserves available stock
    - [ ] Given product "Organic Cotton T-Shirt" has 10 units in Amsterdam warehouse
    - [ ] When create shipment for 1 unit
    - [ ] Then shipment created successfully
    - [ ] And stock in Amsterdam warehouse is 9 units
- [ ] Scenario: Shipment fails when stock insufficient
    - [ ] Given product has 2 units in stock
    - [ ] When attempt to create shipment for 5 units
    - [ ] Then shipment creation fails with "Insufficient stock" error
    - [ ] And stock level unchanged

**Step Definitions**:
- [ ] Enhance ShipmentSteps.kt and InventorySteps.kt for stock verification

**Files to create**:
```
deployables/economique/shipping/src/test/kotlin/com/economique/shipping/service/
  - ShipmentStockReservationIntegrationTest.kt
deployables/economique/test/src/test/resources/features/
  - stock-reservation.feature
```

---

### - [ ] 31. End-to-End Checkout Flow
**Value**: Complete customer journey verified from order creation through shipment.

**What This Delivers**: Comprehensive acceptance test covering entire order fulfillment flow across all modules.

**Why This Matters**: Validates complete system integration. This is the "happy path" that proves all modules work together correctly. Uses worldview data throughout for realistic scenarios.

**Acceptance Criteria**:

**Feature Test**:
- [ ] `complete-checkout-flow.feature`
- [ ] Scenario: Customer orders eco-product successfully
    - [ ] Given customer "John Doe" is registered (WorldviewUser)
    - [ ] And product "Organic Cotton T-Shirt" exists (WorldviewProduct)
    - [ ] And product has stock in "Amsterdam Warehouse" (WorldviewInventory)
    - [ ] When customer creates order for product
    - [ ] And creates payment for €29.99
    - [ ] And payment completes successfully (PSP returns success)
    - [ ] Then payment status is COMPLETED
    - [ ] And PaymentCompleted event was published
    - [ ] And shipment is automatically created
    - [ ] And shipment originates from Amsterdam warehouse
    - [ ] And shipment status is PENDING
    - [ ] And stock is reserved (quantity decreased by 1)
    - [ ] And ShipmentCreated event was published
    - [ ] And tracking number is generated
- [ ] Scenario: Customer cannot order out-of-stock product
    - [ ] Given product is out of stock (0 units)
    - [ ] When customer attempts to create shipment
    - [ ] Then shipment creation fails
    - [ ] And appropriate error message returned: "Insufficient stock"
    - [ ] And no payment created
    - [ ] And no stock reserved

**Step Definitions**:
- [ ] Uses steps from all modules' testFixtures:
    - [ ] UserSteps (setup customer)
    - [ ] ProductSteps (verify product exists)
    - [ ] InventorySteps (setup stock, verify reservation)
    - [ ] PaymentSteps (create/complete payment, verify events)
    - [ ] ShipmentSteps (verify shipment created, check tracking)

**Testing Infrastructure**:
- [ ] @SpringBootTest with Testcontainers
- [ ] All modules' databases initialized
- [ ] Worldview data loaded
- [ ] Event capturing for verification

**Files to create**:
```
deployables/economique/test/src/test/resources/features/
  - complete-checkout-flow.feature
deployables/economique/test/src/test/kotlin/com/economique/test/
  - CheckoutFlowTestConfiguration.kt (event capturing setup)
```

---

### - [ ] 32. Cross-Border Shipping Scenarios
**Value**: International logistics rules verified for warehouse selection and cost calculation.

**What This Delivers**: Acceptance tests for multi-country scenarios with warehouse optimization.

**Why This Matters**: Tests complex business rule: select closest warehouse for international orders. Verifies shipping cost calculation differs for domestic vs international.

**Acceptance Criteria**:

**Feature Test**:
- [ ] `cross-border-shipping.feature`
- [ ] Scenario: German customer orders from Netherlands warehouse
    - [ ] Given customer "Hans Müller" lives in Germany (WorldviewUser)
    - [ ] And product "Bamboo Toothbrush Set" exists (WorldviewProduct)
    - [ ] And product stocked in Amsterdam Warehouse (Netherlands)
    - [ ] And product also stocked in Berlin Warehouse (Germany)
    - [ ] When customer completes order
    - [ ] Then shipment originates from Berlin Warehouse (closest to Germany)
    - [ ] And shipping cost includes cross-border rate (€3.50 * 1.5 = €5.25)
- [ ] Scenario: Local customer orders from local warehouse
    - [ ] Given customer "John Doe" lives in Netherlands
    - [ ] And product stocked in Amsterdam Warehouse only
    - [ ] When customer completes order
    - [ ] Then shipment uses Amsterdam Warehouse
    - [ ] And shipping cost is domestic rate (€3.50)
- [ ] Scenario: Product only in foreign warehouse
    - [ ] Given German customer
    - [ ] And product only stocked in Netherlands
    - [ ] When customer completes order
    - [ ] Then shipment originates from Netherlands warehouse
    - [ ] And shipping cost includes international multiplier

**Step Definitions**:
- [ ] Reuse existing steps
- [ ] Add shipping cost verification steps

**Files to create**:
```
deployables/economique/test/src/test/resources/features/
  - cross-border-shipping.feature
deployables/economique/shipping/src/testFixtures/kotlin/com/economique/shipping/steps/
  - ShipmentSteps.kt (enhance with cost verification)
```

---

### - [ ] 33. Multi-Product Order Scenarios
**Value**: Complex order scenarios with multiple products and partial stock availability verified.

**What This Delivers**: Acceptance tests for multi-item orders with various stock conditions.

**Why This Matters**: Tests transaction boundaries - multi-product orders are all-or-nothing. Verifies no partial stock reservation happens.

**Acceptance Criteria**:

**Feature Test**:
- [ ] `multi-product-orders.feature`
- [ ] Scenario: Order with products from different categories
    - [ ] Given customer "John Doe" is registered
    - [ ] And product "Organic Cotton T-Shirt" (CLOTHING) has 10 units
    - [ ] And product "Bamboo Toothbrush Set" (HOUSEHOLD) has 15 units
    - [ ] And product "Natural Shampoo Bar" (BEAUTY) has 20 units
    - [ ] When customer creates order for all 3 products
    - [ ] And completes payment for total amount
    - [ ] Then 3 separate shipments created
    - [ ] And stock reduced for all 3 products
    - [ ] And total shipping cost calculated correctly
- [ ] Scenario: Partial stock availability fails entire order
    - [ ] Given customer orders 3 products
    - [ ] And product A has 10 units in stock
    - [ ] And product B has 5 units in stock
    - [ ] And product C has 0 units (out of stock)
    - [ ] When customer attempts to complete order
    - [ ] Then order creation fails
    - [ ] And no inventory reserved for any product
    - [ ] And no payment created
    - [ ] And error message indicates which product is unavailable
- [ ] Scenario: Order split across multiple warehouses
    - [ ] Given customer orders 2 products
    - [ ] And product A only available in Amsterdam
    - [ ] And product B only available in Berlin
    - [ ] When customer completes order
    - [ ] Then 2 shipments created from different warehouses
    - [ ] And customer sees both tracking numbers

**Step Definitions**:
- [ ] Multi-product order steps in testFixtures
- [ ] Stock verification across multiple products

**Files to create**:
```
deployables/economique/test/src/test/resources/features/
  - multi-product-orders.feature
deployables/economique/test/src/test/kotlin/com/economique/test/steps/
  - MultiProductOrderSteps.kt
```

---

## Phase 8: Production Readiness (0/3 Complete)

### - [ ] 34. Observability & Monitoring
**Value**: Production issues can be quickly detected and diagnosed through structured logs and health checks.

**What This Delivers**: Comprehensive logging, health indicators, and metrics for production monitoring.

**Why This Matters**: Without observability, production issues are invisible. Structured logging with correlation IDs enables tracing requests across modules. Health checks enable automated monitoring and alerts.

**Acceptance Criteria**:

**Structured Logging**:
- [ ] Logback configuration with JSON format
- [ ] Correlation ID (request ID) added to MDC in filter
- [ ] Correlation ID propagated across all module logs
- [ ] Log levels configurable per module: com.economique.products=DEBUG
- [ ] Request/response logging in controllers (excluding sensitive data)
- [ ] Business event logging (payment completed, shipment created)
- [ ] Error logging with stack traces

**Health Indicators**:
- [ ] Spring Actuator enabled at /actuator
- [ ] Custom health indicators per module:
    - [ ] DatabaseHealthIndicator per schema (products, payment, shipping, inventory, users)
    - [ ] PspHealthIndicator (checks mock PSP availability)
- [ ] Health endpoint shows status of each component: /actuator/health
- [ ] Detailed health visible only to operators: /actuator/health?details=true

**Metrics**:
- [ ] Micrometer metrics enabled
- [ ] Prometheus endpoint: /actuator/prometheus
- [ ] Custom metrics:
    - [ ] Payment success/failure rates
    - [ ] Shipment creation rate
    - [ ] Stock reservation attempts
    - [ ] API endpoint response times
- [ ] JVM metrics (heap, threads, GC)
- [ ] Database connection pool metrics

**Log Aggregation**:
- [ ] JSON log format compatible with ELK stack
- [ ] Log to stdout (Docker/Kubernetes compatible)
- [ ] No PII in logs (mask email, address)

**Testing**:
- [ ] Integration test verifies correlation ID in logs
- [ ] Integration test verifies health checks return correct status
- [ ] Integration test verifies metrics endpoint accessible
- [ ] Test database health check fails when database down

**Documentation**:
- [ ] README section on monitoring
- [ ] Grafana dashboard examples (JSON)
- [ ] Alert definitions for critical metrics

**Files to create**:
```
deployables/economique/application/src/main/resources/
  - logback-spring.xml
deployables/economique/application/src/main/kotlin/com/economique/application/observability/
  - CorrelationIdFilter.kt
  - DatabaseHealthIndicator.kt
  - PspHealthIndicator.kt
deployables/economique/application/src/test/kotlin/com/economique/application/observability/
  - ObservabilityIntegrationTest.kt
docs/
  - monitoring.md
  - grafana-dashboard.json
  - alerts.md
```

---

### - [ ] 35. Module Documentation
**Value**: New developers can quickly understand system architecture and module responsibilities.

**What This Delivers**: Comprehensive documentation per module with diagrams showing dependencies.

**Why This Matters**: Good documentation reduces onboarding time. Module docs explain what each module does, why it exists, how it fits in the system, and how to work with it.

**Acceptance Criteria**:

**Per-Module Documentation** (`docs/modules/`):
- [ ] `products.md`:
    - [ ] Purpose: Product catalog management
    - [ ] Bounded Context: Products
    - [ ] Key Entities: Product, ProductCategory, Weight
    - [ ] API Endpoints: List with examples
    - [ ] Dependencies: common-money, common-country
    - [ ] Events Published: ProductCreated
    - [ ] Events Consumed: None
    - [ ] Database Schema: products.products table
    - [ ] Getting Started: How to add new product categories
- [ ] `users.md`:
    - [ ] Purpose, entities (User, Address), endpoints, dependencies
- [ ] `payment.md`:
    - [ ] Purpose, entities (Payment, PaymentStatus), endpoints, PSP integration
    - [ ] Events: PaymentCompleted, PaymentFailed
- [ ] `inventory.md`:
    - [ ] Purpose, entities (Warehouse, InventoryItem), endpoints
    - [ ] Cross-module dependencies: Products (productId validation)
- [ ] `shipping.md`:
    - [ ] Purpose, entities (Shipment, ShipmentStatus), endpoints
    - [ ] Cross-module dependencies: Products (weight), Inventory (warehouse)
    - [ ] Event subscriptions: PaymentCompleted

**Integration Documentation**:
- [ ] `integration-flows.md`:
    - [ ] Diagram: Payment → Shipping (async)
    - [ ] Diagram: Shipping → Inventory (sync)
    - [ ] Diagram: Shipping → Products (sync)
    - [ ] Sequence diagram: Complete checkout flow
    - [ ] Explanation of synchronous vs asynchronous patterns

**Getting Started Guide**:
- [ ] `docs/getting-started.md`:
    - [ ] Prerequisites (JDK, Docker, Just)
    - [ ] Clone and setup
    - [ ] Start docker, run application
    - [ ] Access Swagger UI
    - [ ] Run tests
    - [ ] Module structure explanation
    - [ ] How to add new module
    - [ ] Code review checklist

**Architecture Overview**:
- [ ] `docs/architecture-overview.md`:
    - [ ] Modular monolith explanation
    - [ ] DDD principles applied
    - [ ] Module boundaries diagram
    - [ ] Communication patterns
    - [ ] Links to all ADRs

**Worldview Documentation**:
- [ ] `docs/worldview.md`:
    - [ ] What is worldview pattern
    - [ ] List of all worldview entities with descriptions
    - [ ] How to add new worldview data
    - [ ] How worldview used in tests

**Diagrams**:
- [ ] Module dependency diagram (using Mermaid or similar)
- [ ] Database schema diagram per module
- [ ] Event flow diagram
- [ ] Deployment architecture

**Files to create**:
```
docs/
  - getting-started.md
  - architecture-overview.md
  - worldview.md
  - modules/
    - products.md
    - users.md
    - payment.md
    - inventory.md
    - shipping.md
    - integration-flows.md
  - diagrams/
    - module-dependencies.mmd
    - event-flows.mmd
    - checkout-sequence.mmd
```

---

### - [ ] 36. Deployment Pipeline
**Value**: Changes can be automatically deployed to staging, reducing manual deployment effort and errors.

**What This Delivers**: Automated CI/CD pipeline from commit to staging deployment.

**Why This Matters**: Manual deployments are error-prone and slow. Automated pipeline ensures every main branch commit is built, tested, packaged, and deployed consistently.

**Acceptance Criteria**:

**Docker Image**:
- [ ] Dockerfile in application module
- [ ] Multi-stage build (build stage + runtime stage)
- [ ] Uses Eclipse Temurin JDK 21
- [ ] Exposes port 8080
- [ ] HEALTHCHECK instruction using /actuator/health
- [ ] Image tagged with git commit SHA and "latest"

**GitHub Actions Workflow** (`.github/workflows/deploy.yml`):
- [ ] Triggers on push to main branch
- [ ] Steps:
    - [ ] Checkout code
    - [ ] Setup JDK 21
    - [ ] Run all tests (unit, integration, Cucumber)
    - [ ] Build application (./gradlew :deployables:economique:application:bootJar)
    - [ ] Build Docker image
    - [ ] Push to Docker registry (GitHub Container Registry)
    - [ ] Deploy to staging environment
    - [ ] Run smoke tests
- [ ] Fails if any step fails
- [ ] Slack/email notification on failure

**Staging Environment**:
- [ ] Docker Compose for staging (docker/staging-compose.yml)
- [ ] PostgreSQL database
- [ ] Application container
- [ ] Environment variables for staging configuration
- [ ] Separate from development environment

**Smoke Tests**:
- [ ] Health check responds 200
- [ ] Can create product via API
- [ ] Can retrieve worldview data
- [ ] Database connectivity verified
- [ ] Basic end-to-end flow works

**Rollback**:
- [ ] Documentation on rollback procedure
- [ ] Keep previous 5 Docker images
- [ ] Manual approval step for production (future)

**Secrets Management**:
- [ ] Database credentials in GitHub Secrets
- [ ] Docker registry credentials in Secrets
- [ ] No secrets in code or logs

**Documentation**:
- [ ] `docs/deployment.md`:
    - [ ] Deployment architecture
    - [ ] How CI/CD pipeline works
    - [ ] How to deploy manually (emergency)
    - [ ] How to rollback deployment
    - [ ] Environment variables documentation
    - [ ] Troubleshooting common deployment issues

**Testing**:
- [ ] Verify workflow runs on test branch
- [ ] Verify Docker image builds
- [ ] Verify smoke tests run

**Files to create**:
```
deployables/economique/application/
  - Dockerfile
docker/
  - staging-compose.yml
.github/workflows/
  - deploy.yml
docs/
  - deployment.md
deployables/economique/test/src/test/kotlin/com/economique/test/smoke/
  - SmokeTests.kt
```

---

## Summary

**Total: 36 stories across 8 phases**

### Phase Summary:
- **Phase 1 (Foundation)**: 8 stories - Project setup, tooling, common types
- **Phase 2 (Products)**: 5 stories - Complete Products module with worldview and tests
- **Phase 3 (Users)**: 3 stories - Complete Users module with worldview and tests
- **Phase 4 (Payment)**: 4 stories - Complete Payment module with PSP, worldview, and tests
- **Phase 5 (Inventory)**: 4 stories - Complete Inventory module with multi-warehouse support and tests
- **Phase 6 (Shipping)**: 4 stories - Complete Shipping module with logistics and tests
- **Phase 7 (Integration)**: 5 stories - Event flows, cross-module integration, E2E tests
- **Phase 8 (Production)**: 3 stories - Observability, documentation, deployment

### Testing Approach Clarified:
- **Unit Tests**: In each module's `src/test/`, test domain/service/controller in isolation
- **Integration Tests**: In each module's `src/test/`, test with Testcontainers
- **Step Definitions**: In each module's `testFixtures`, reusable across features
- **Feature Files**: In `:deployables:economique:test` module, use worldview data
- **Worldview**: Defined in `worldview-loader`, loaded on startup, used in all feature tests

### Key Improvements:
1. **Smaller stories**: Each 1-8 days, independently deliverable
2. **Complete information**: No detail lost from original backlog
3. **Clear testing**: Unit vs integration vs feature tests explicitly stated
4. **Worldview pattern**: Consistently applied across all modules
5. **Cross-module dependencies**: Explicitly called out in acceptance criteria
6. **Why it matters**: Each story explains business/technical value
7. **Files to create**: Concrete list of files per story

Each story delivers working, tested functionality that can be demonstrated.