# Eco-logique Functional Backlog (Demo)

## Progress Overview

* **Foundation & Products**: ✅ Complete
* **Upcoming Features**: **9 Items Remaining**

---

## ✅ Completed

### 1. Foundation & Architecture

* **Scope**: Project structure, Gradle build logic, Docker environment, and Common libraries.
* **Status**: Implemented modular monolith structure with `common-money`, `common-time`, `common-country`, and `common-result`.

### 2. Products Management

* **Scope**: Product catalog management with sustainability ratings.
* **Status**: Fully implemented (`products-api`, `products-impl`, `products-worldview`).

  * [x] CRUD operations for Products
  * [x] Automatic sustainability rating calculation
  * [x] REST API with Swagger documentation
  * [x] Database schema and persistence
  * [x] Worldview data for testing

---

## 📋 To Do

### 3. Authentication & Authorization (Keycloak)

**Context**: Demo auth with real JWT flows; no custom auth.
**Module**: Application wiring (Spring Security config + Docker)

* **Description**: Integrate Keycloak and secure APIs with JWT.
* **Deliverables**:

  * [x] Add Keycloak to Docker Compose (dev realm + client config).
  * [x] Spring Security integration (Resource Server JWT).
  * [x] Roles (demo-level): `ROLE_CUSTOMER`, `ROLE_ADMIN`.
  * [x] Protect endpoints:

    * [x] Public: health + product catalog read (`GET /products`, `GET /products/{id}`)
    * [x] Authenticated customer: cart/checkout/orders/users
    * [x] Admin (optional): product write endpoints
  * [x] Swagger/OpenAPI configured with Bearer auth.
  * [x] Seed demo users in Keycloak (e.g., `john@demo.com`, `jane@demo.com`) + roles.

---

### 4. User Registration & Profile

**Context**: Identity comes from JWT; we store user profile + delivery preferences linked to the Keycloak subject.
**Module**: `users-api`, `users-impl`, `users-worldview`

* **Description**: Store customer profile (linked to Keycloak subject).
* **Deliverables**:

  * [ ] `User` entity (ID, keycloakSubject, name, email, default address).
  * [ ] `UserService` to handle profile creation/updates.
  * [ ] REST API (authenticated; subject derived from token, no userId required):

    * [ ] `POST /users` (create profile)
    * [ ] `GET /users` (get own profile)
    * [ ] `PUT /users/address` (update own address)
  * [ ] Email uniqueness validation (within profile store).
  * [ ] Error handling: `UserError` sealed hierarchy in `users-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] **DB**: ensure `users` schema migrations cover required fields.
  * [ ] **Worldview**: Realistic user profiles (e.g., "John Doe" in Amsterdam, "Jane Smith" in Berlin).

---

### 5. Orders (Core Commerce)

**Context**: Persistent order model connecting checkout → payment → shipment and providing order history.
**Module**: `orders-api`, `orders-impl`, `orders-worldview`

* **Description**: Implement ordering domain model and history endpoints.
* **Deliverables**:

  * [ ] Entities:

    * [ ] `Order` (id, userId, status, totalsSnapshot, createdAt)
    * [ ] `OrderLine` (orderId, sku/productId, nameSnapshot, unitPriceSnapshot, quantity, lineTotal)
  * [ ] Status model (demo-level):

    * [ ] `CREATED`, `RESERVED`, `PAYMENT_PENDING`, `PAID`, `CANCELLED`, `SHIPPED`, `DELIVERED`
  * [ ] `OrderService`:

    * [ ] `createOrder(userId, lines, totalsSnapshot)`
    * [ ] `getOrder(orderId)` (ownership check)
    * [ ] `listOrdersForUser(userId)` (order history)
    * [ ] Status transition methods used by checkout/payment/shipping (via `orders-api`)
  * [ ] REST API:

    * [ ] `GET /orders/{id}`
    * [ ] `GET /orders` (order history for authenticated user)
  * [ ] Error handling: `OrderError` sealed hierarchy in `orders-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] **DB**: add `orders` schema + Flyway migrations for orders tables.
  * [ ] **Worldview**: A few example orders for seeded users (created + paid + shipped).

---

### 6. Pricing Totals Snapshot

**Context**: Demo pricing, but orders must store a snapshot (totals shouldn’t change if product price changes).
**Module**: `orders-impl` (domain + application)

* **Description**: Compute and persist order totals snapshot at checkout time.
* **Deliverables**:

  * [ ] Totals snapshot value object on `Order`:

    * [ ] `subtotal`, `grandTotal` (demo: same value), `currency`
  * [ ] Calculation rule (demo):

    * [ ] `subtotal = sum(orderLine.unitPriceSnapshot * quantity)`
  * [ ] Ensure snapshots stored on `OrderLine` too (name + unit price at purchase time).
  * [ ] Tests verifying snapshot consistency if product price changes later.

---

### 7. Cart

**Context**: Users collect items before checkout.
**Module**: `cart-api`, `cart-impl`, `cart-worldview`

* **Description**: Manage a user cart with add/remove/update operations.
* **Deliverables**:

  * [ ] Entities:

    * [ ] `Cart` (userId)
    * [ ] `CartItem` (sku/productId, quantity)
  * [ ] `CartService`:

    * [ ] `addItem(userId, sku, qty)`
    * [ ] `updateItem(userId, sku, qty)`
    * [ ] `removeItem(userId, sku)`
    * [ ] `getCart(userId)`
  * [ ] REST API:

    * [ ] `GET /cart`
    * [ ] `POST /cart/items` (add)
    * [ ] `PUT /cart/items/{sku}` (update qty)
    * [ ] `DELETE /cart/items/{sku}`
  * [ ] Error handling: `CartError` sealed hierarchy in `cart-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] **DB**: add `cart` schema + Flyway migrations for cart tables.
  * [ ] **Worldview**: Pre-filled carts for demo users.

---

### 8. Checkout (Create Order → Reserve Inventory → Initiate Payment)

**Context**: Orchestration: cart → order (+ price snapshot) → inventory reservation → payment initiation.
**Module**: `checkout-api`, `checkout-impl`

* **Description**: Implement a simple checkout flow for demo (no separate DB schema unless persistence is required).
* **Deliverables**:

  * [ ] `CheckoutService`:

    * [ ] Load cart items (via `cart-api`) + product snapshots (via `products-api`)
    * [ ] Create `Order` + `OrderLines` + totals snapshot (via `orders-api`)
    * [ ] Reserve stock via `InventoryService.reserveStock(sku, qty)` (via `inventory-api`)
    * [ ] Initiate payment via `PaymentService` (via `payment-api`)
    * [ ] Clear cart on success (or keep for demo—choose one and test)
  * [ ] REST API:

    * [ ] `POST /checkout` → returns `{ orderId, paymentId, status }`
  * [ ] Error handling: `CheckoutError` sealed hierarchy in `checkout-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] Tests: happy flow + stock failure.

---

### 9. Inventory Management

**Context**: Don’t sell products that aren’t available.
**Module**: `inventory-api`, `inventory-impl`, `inventory-worldview`

* **Description**: Track stock levels across warehouses.
* **Deliverables**:

  * [ ] `Warehouse` and `InventoryItem` entities.
  * [ ] `InventoryService` with `checkStock(sku)` and `reserveStock(sku, quantity)` methods.
  * [ ] REST API:

    * [ ] `GET /inventory/{sku}`
    * [ ] `POST /inventory/reserve`
  * [ ] **Constraint**: Inventory cannot go below zero.
  * [ ] Error handling: `InventoryError` sealed hierarchy in `inventory-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] **Worldview**: Stock levels for all existing Worldview Products in different warehouses.

---

### 10. Payment Processing

**Context**: Capture money for orders before initiating shipment.
**Module**: `payment-api`, `payment-impl`, `payment-worldview`

* **Description**: Handle payment transactions and trigger fulfillment.
* **Deliverables**:

  * [ ] `Payment` entity (Transaction ID, Order ID, Amount, Status).
  * [ ] `PaymentService` to simulate interaction with a PSP.
  * [ ] **Event**: Publish `PaymentCompleted` domain event upon success (in `payment-api`).
  * [ ] REST API:

    * [ ] `POST /payments` (initiate payment) *(also used by checkout orchestration)*
  * [ ] On success: update `Order` status to `PAID` via `orders-api`
  * [ ] On failure: update `Order` status to `PAYMENT_PENDING` or `CANCELLED` via `orders-api` (choose one for demo)
  * [ ] Error handling: `PaymentError` sealed hierarchy in `payment-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] **Worldview**: Successful (Credit Card) and failed (Insufficient Funds) payment scenarios.

---

### 11. Shipment Creation (Core Fulfillment)

**Context**: After payment, prepare a physical shipment.
**Module**: `shipping-api`, `shipping-impl`, `shipping-worldview`

* **Description**: React to completed payments to create shipment records.
* **Deliverables**:

  * [ ] `Shipment` entity (tracking number, status, address, weight).
  * [ ] **Event Listener**: Listen to `PaymentCompleted` to generate a `Shipment`.
  * [ ] Assign shipment to warehouse (based on Country code).
  * [ ] Update `Order` status to `SHIPPED` via `orders-api` when shipment created (demo-level).
  * [ ] Error handling: `ShippingError` sealed hierarchy in `shipping-api`; controllers map errors to RFC7807 Problem Details.
  * [ ] **Worldview**: Pre-defined shipment scenarios for testing.

---

### 12. External Delivery Status Integration (Queue Consumer)

**Context**: Carriers push status updates asynchronously; consume to keep system in sync.
**Module**: `shipping-impl` (Infrastructure layer)

* **Description**: RabbitMQ consumer for delivery status updates.
* **Technical Context**: Use existing RabbitMQ instance in Docker.
* **Deliverables**:

  * [ ] Define `DeliveryStatusUpdated` message contract (JSON: `trackingNumber`, `newStatus`, `timestamp`).
  * [ ] Implement `RabbitListener` in `shipping-impl`.
  * [ ] Logic: Find shipment by tracking number → Update status (`IN_TRANSIT` → `DELIVERED`).
  * [ ] Update `Order` status to `DELIVERED` via `orders-api` when shipment delivered (demo-level).
  * [ ] Integration Test: Publish message to queue and verify DB state changes.
