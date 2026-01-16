# Eco-logique Functional Backlog (Demo)

## Progress Overview

* **Foundation & Products**: ✅ Complete
* **Upcoming Features**: **4 Items Remaining**

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

  * [x] `User` entity (ID, keycloakSubject, name, email, default address).
  * [x] `UserService` to handle profile creation/updates.
  * [x] REST API (authenticated; subject derived from token, no userId required):

    * [x] `POST /users` (create profile)
    * [x] `GET /users` (get own profile)
    * [x] `PUT /users/address` (update own address)
  * [x] Email uniqueness validation (within profile store).
  * [x] Error handling: `UserError` sealed hierarchy in `users-api`; controllers map errors to RFC7807 Problem Details.
  * [x] **DB**: ensure `users` schema migrations cover required fields.
  * [x] **Worldview**: Realistic user profiles (e.g., "John Doe" in Amsterdam, "Jane Smith" in Berlin).

---

### 5. Orders (Core Commerce)

**Context**: Persistent order model connecting checkout → payment → shipment and providing order history.
**Module**: `orders-api`, `orders-impl`, `orders-worldview`

* **Description**: Implement ordering domain model and history endpoints.
* **Deliverables**:

  * [x] Entities:

    * [x] `Order` (id, userId, status, totalsSnapshot, createdAt)
    * [x] `OrderLine` (orderId, sku/productId, nameSnapshot, unitPriceSnapshot, quantity, lineTotal)
  * [x] Status model (demo-level):

    * [x] `CREATED`, `RESERVED`, `PAYMENT_PENDING`, `PAID`, `CANCELLED`, `SHIPPED`, `DELIVERED`
  * [x] `OrderService`:

    * [x] `createOrder(userId, lines, totalsSnapshot)`
    * [x] `getOrder(orderId)` (ownership check)
    * [x] `listOrdersForUser(userId)` (order history)
    * [x] Status transition methods used by checkout/payment/shipping (via `orders-api`)
  * [x] REST API:

    * [x] `GET /orders/{id}`
    * [x] `GET /orders` (order history for authenticated user)
  * [x] Error handling: `OrderError` sealed hierarchy in `orders-api`; controllers map errors to RFC7807 Problem Details.
  * [x] **DB**: add `orders` schema + Flyway migrations for orders tables.
  * [x] **Worldview**: A few example orders for seeded users (created + paid + shipped).

---

### 6. Pricing Totals Snapshot

**Context**: Demo pricing, but orders must store a snapshot (totals shouldn't change if product price changes).
**Module**: `orders-impl` (domain + application)

* **Description**: Compute and persist order totals snapshot at checkout time.
* **Deliverables**:

  * [x] Totals snapshot value object on `Order`:

    * [x] `subtotal`, `grandTotal` (demo: same value), `currency`
  * [x] Calculation rule (demo):

    * [x] `subtotal = sum(orderLine.unitPriceSnapshot * quantity)`
  * [x] Ensure snapshots stored on `OrderLine` too (name + unit price at purchase time).
  * [x] Tests verifying snapshot consistency if product price changes later.

---

### 7. Cart

**Context**: Users collect items before checkout.
**Module**: `cart-api`, `cart-impl`, `cart-worldview`

* **Description**: Manage a user cart with add/remove/update operations.
* **Deliverables**:

  * [x] Entities:

    * [x] `Cart` (userId)
    * [x] `CartItem` (sku/productId, quantity)
  * [x] `CartService`:

    * [x] `addItem(userId, sku, qty)`
    * [x] `updateItem(userId, sku, qty)`
    * [x] `removeItem(userId, sku)`
    * [x] `getCart(userId)`
  * [x] REST API:

    * [x] `GET /cart`
    * [x] `POST /cart/items` (add)
    * [x] `PUT /cart/items/{sku}` (update qty)
    * [x] `DELETE /cart/items/{sku}`
  * [x] Error handling: `CartError` sealed hierarchy in `cart-api`; controllers map errors to RFC7807 Problem Details.
  * [x] **DB**: add `cart` schema + Flyway migrations for cart tables.
  * [x] **Worldview**: Pre-filled carts for demo users.

---

### 8. Checkout (Create Order → Reserve Inventory → Initiate Payment)

**Context**: Orchestration: cart → order (+ price snapshot) → inventory reservation → payment initiation.
**Module**: `checkout-api`, `checkout-impl`

* **Description**: Implement a simple checkout flow for demo (no separate DB schema unless persistence is required).
* **Deliverables**:

  * [x] `CheckoutService`:

    * [x] Load cart items (via `cart-api`) + product snapshots (via `products-api`)
    * [x] Create `Order` + `OrderLines` + totals snapshot (via `orders-api`)
    * [x] Reserve stock via `InventoryService.reserveStock(sku, qty)` (via `inventory-api`)
    * [x] Initiate payment via `PaymentService` (via `payment-api`)
    * [x] Clear cart on success (or keep for demo—choose one and test)
  * [x] REST API:

    * [x] `POST /checkout` → returns `{ orderId, paymentId, status }`
  * [x] Error handling: `CheckoutError` sealed hierarchy in `checkout-api`; controllers map errors to RFC7807 Problem Details.
  * [x] Tests: happy flow + stock failure.

---

### 9. Inventory Management

**Context**: Don't sell products that aren't available.
**Module**: `inventory-api`, `inventory-impl`, `inventory-worldview`

* **Description**: Track stock levels across warehouses.
* **Deliverables**:

  * [x] `Warehouse` and `InventoryItem` entities (with database persistence, `StockReservation` for tracking).
  * [x] `InventoryService` with `checkStock(sku)` and `reserveStock(sku, quantity)` methods.
  * [ ] REST API:

    * [ ] `GET /inventory/{sku}`
    * [ ] `POST /inventory/reserve`
  * [x] **Constraint**: Inventory cannot go below zero.
  * [x] Error handling: `InventoryError` sealed hierarchy in `inventory-api`; controllers map errors to RFC7807 Problem Details.
  * [x] Domain events: `InventoryReserved` event published on successful reservation.
  * [ ] **Worldview**: Stock levels for all existing Worldview Products in different warehouses.

---

### 10. Payment Processing

**Context**: Capture money for orders before initiating shipment.
**Module**: `payment-api`, `payment-impl`, `payment-worldview`

* **Description**: Handle payment transactions and trigger fulfillment.
* **Deliverables**:

  * [x] `Payment` entity (Transaction ID, Order ID, Amount, Status).
  * [x] `PaymentService` to simulate interaction with a PSP.
  * [x] **Event**: Publish `PaymentCompleted` domain event upon success (in `payment-api`).
  * [x] REST API:

    * [x] `POST /payments` (initiate payment) *(also used by checkout orchestration)*
  * [x] On success: update `Order` status to `PAID` via `orders-api`
  * [x] On failure: order stays in `PAYMENT_PENDING` (customer can retry)
  * [x] Error handling: `PaymentError` sealed hierarchy in `payment-api`; controllers map errors to RFC7807 Problem Details.
  * [x] **Worldview**: Successful (Credit Card) and failed (Insufficient Funds) payment scenarios.

---

### 11. Shipment Creation (Core Fulfillment)

**Context**: After payment, prepare a physical shipment.
**Module**: `shipping-api`, `shipping-impl`, `shipping-worldview`

* **Description**: React to completed payments to create shipment records.
* **Deliverables**:

  * [x] `Shipment` entity (tracking number, status, address, weight).
  * [x] **Event Listener**: Listen to `PaymentCompleted` to generate a `Shipment`.
  * [x] Assign shipment to warehouse (based on Country code).
  * [x] Update `Order` status to `SHIPPED` via `orders-api` when shipment created (demo-level).
  * [x] Error handling: `ShippingError` sealed hierarchy in `shipping-api`; controllers map errors to RFC7807 Problem Details.
  * [x] **Worldview**: Pre-defined shipment scenarios for testing.

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
