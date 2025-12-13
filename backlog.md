# Eco-logique Functional Backlog

## Progress Overview
- **Foundation & Products**: ✅ Complete
- **Upcoming Features**: 5 Items Remaining

---

## ✅ Completed

### 1. Foundation & Architecture
- **Scope**: Project structure, Gradle build logic, Docker environment, and Common libraries.
- **Status**: Implemented modular monolith structure with `common-money`, `common-time`, `common-country`, and `common-result`.

### 2. Products Management
- **Scope**: Product catalog management with sustainability ratings.
- **Status**: Fully implemented (`products-api`, `products-impl`, `products-worldview`).
    - [x] CRUD operations for Products
    - [x] Automatic sustainability rating calculation
    - [x] REST API with Swagger documentation
    - [x] Database schema and persistence
    - [x] Worldview data for testing

---

## 📋 To Do

### 3. User Registration & Profile
**Context**: We need to identify customers to handle shipping addresses and order history.
**Module**: `:users`

- **Description**: Implement user management to store customer identity and delivery preferences.
- **Deliverables**:
    - [ ] `User` entity (ID, name, email, default address).
    - [ ] `UserService` to handle registration and address updates.
    - [ ] REST API: `POST /users` (register), `GET /users/{id}`, `PUT /users/{id}/address`.
    - [ ] Email uniqueness validation.
    - [ ] **Worldview**: Realistic user profiles (e.g., "John Doe" in Amsterdam, "Jane Smith" in Berlin).

### 4. Inventory Management
**Context**: We need to ensure we don't sell products that aren't physically available.
**Module**: `:inventory`

- **Description**: Track stock levels of products across different warehouses.
- **Deliverables**:
    - [ ] `Warehouse` and `InventoryItem` entities.
    - [ ] `InventoryService` with `checkStock(sku)` and `reserveStock(sku, quantity)` methods.
    - [ ] REST API: `GET /inventory/{sku}`, `POST /inventory/reserve`.
    - [ ] **Constraint**: Inventory cannot go below zero.
    - [ ] **Worldview**: Stock levels for all existing Worldview Products in different warehouses.

### 5. Payment Processing
**Context**: Capture money for orders before initiating shipment.
**Module**: `:payment`

- **Description**: Handle payment transactions and act as the trigger for the fulfillment process.
- **Deliverables**:
    - [ ] `Payment` entity (Transaction ID, Order ID, Amount, Status).
    - [ ] `PaymentService` to simulate interaction with a PSP (Payment Service Provider).
    - [ ] **Event**: Publish `PaymentCompleted` domain event upon success.
    - [ ] REST API: `POST /payments` (initiate payment).
    - [ ] **Worldview**: Scenarios for successful (Credit Card) and failed (Insufficient Funds) payments.

### 6. Shipment Creation (Core Fulfillment)
**Context**: Once payment is confirmed, a physical package must be prepared.
**Module**: `:shipping`

- **Description**: React to completed payments to create shipment records.
- **Deliverables**:
    - [ ] `Shipment` entity (tracking number, status, address, weight).
    - [ ] **Event Listener**: Listen to `PaymentCompleted` to automatically generate a `Shipment`.
    - [ ] Logic to assign the shipment to a specific warehouse (based on Country code).
    - [ ] **Worldview**: Pre-defined shipment scenarios for testing.

### 7. External Delivery Status Integration (Queue Consumer)
**Context**: We use external carriers (DHL, PostNL) who push status updates to us asynchronously. We need to consume these updates to keep our system in sync.
**Module**: `:shipping` (Infrastructure layer)

- **Description**: Implement a RabbitMQ consumer to process delivery status updates from external logistics providers.
- **Technical Context**: Use the existing RabbitMQ instance in Docker.
- **Deliverables**:
    - [ ] Define `DeliveryStatusUpdated` message contract (JSON: `trackingNumber`, `newStatus`, `timestamp`).
    - [ ] Implement `RabbitListener` in `shipping-impl`.
    - [ ] Logic: Find shipment by tracking number -> Update status (e.g., `IN_TRANSIT` -> `DELIVERED`).
    - [ ] Integration Test: Publish a message to the queue and verify the database state changes.
