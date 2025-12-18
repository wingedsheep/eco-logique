# Orders Module

The Orders module manages customer orders, including order creation, status tracking, and order history. It validates
product existence before accepting orders and stores price snapshots to ensure historical accuracy.

## Key Features

- **Order Management**: Create and retrieve orders with ownership validation
- **Product Validation**: Validates all products exist before order creation
- **Status Tracking**: Manages order lifecycle through defined status transitions
- **Price Snapshots**: Stores product prices at time of purchase
- **Order History**: Retrieve all orders for a user

## Domain Model

### Order

The core aggregate representing a customer order.

- **Id**: Unique identifier (UUID-based, prefixed with `ORD-`)
- **UserId**: Reference to the user who placed the order (JWT subject)
- **Status**: Current order status
- **Lines**: List of order line items
- **Totals**: Snapshot of subtotal, grand total, and currency
- **CreatedAt**: Timestamp of order creation

### OrderLine

A line item within an order, storing product snapshots.

- **ProductId**: Reference to the product
- **ProductName**: Name of the product at time of purchase
- **UnitPrice**: Price per unit at time of purchase
- **Quantity**: Number of items ordered
- **LineTotal**: Calculated total for this line

### Order Status

Orders follow a defined lifecycle:

```
CREATED → RESERVED → PAYMENT_PENDING → PAID → SHIPPED → DELIVERED
    ↓         ↓            ↓            ↓
CANCELLED ←───┴────────────┴────────────┘
```

| Status            | Description                 |
|-------------------|-----------------------------|
| `CREATED`         | Order has been created      |
| `RESERVED`        | Inventory has been reserved |
| `PAYMENT_PENDING` | Awaiting payment            |
| `PAID`            | Payment completed           |
| `CANCELLED`       | Order cancelled             |
| `SHIPPED`         | Order shipped               |
| `DELIVERED`       | Order delivered             |

## API Usage

All endpoints require authentication. The user's identity is derived from the JWT token.

### Create Order

`POST /api/v1/orders`

Creates a new order for the authenticated user. All products must exist in the catalog.

**Request:**

```json
{
  "lines": [
    {
      "productId": "PROD-001",
      "productName": "Organic Cotton T-Shirt",
      "unitPrice": 29.99,
      "quantity": 2
    }
  ],
  "subtotal": 59.98,
  "grandTotal": 59.98,
  "currency": "EUR"
}
```

**Response (201 Created):**

```json
{
  "id": "ORD-abc123",
  "userId": "user-subject",
  "status": "CREATED",
  "lines": [
    {
      "productId": "PROD-001",
      "productName": "Organic Cotton T-Shirt",
      "unitPrice": 29.99,
      "quantity": 2,
      "lineTotal": 59.98
    }
  ],
  "subtotal": 59.98,
  "grandTotal": 59.98,
  "currency": "EUR",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

**Errors:**

- `400 Bad Request`: Invalid data or product not found
- `401 Unauthorized`: Not authenticated

### Get Order

`GET /api/v1/orders/{id}`

Retrieves an order by ID. Users can only access their own orders.

**Response (200 OK):**

```json
{
  "id": "ORD-abc123",
  "userId": "user-subject",
  "status": "PAID",
  "lines": [
    ...
  ],
  "subtotal": 59.98,
  "grandTotal": 59.98,
  "currency": "EUR",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

**Errors:**

- `403 Forbidden`: Order belongs to another user
- `404 Not Found`: Order does not exist

### List Orders

`GET /api/v1/orders`

Retrieves all orders for the authenticated user, ordered by creation date (newest first).

**Response (200 OK):**

```json
[
  {
    "id": "ORD-abc123",
    "userId": "user-subject",
    "status": "PAID",
    ...
  },
  {
    "id": "ORD-def456",
    "userId": "user-subject",
    "status": "DELIVERED",
    ...
  }
]
```

## Error Responses

All errors follow RFC 7807 Problem Details format:

```json
{
  "type": "urn:problem:order:product-not-found",
  "title": "Product Not Found",
  "status": 400,
  "detail": "Product not found: PROD-INVALID"
}
```

### Error Types

| Type                                          | Status | Description                 |
|-----------------------------------------------|--------|-----------------------------|
| `urn:problem:order:not-found`                 | 404    | Order not found             |
| `urn:problem:order:access-denied`             | 403    | User does not own the order |
| `urn:problem:order:product-not-found`         | 400    | Product does not exist      |
| `urn:problem:order:invalid-status-transition` | 400    | Invalid status change       |
| `urn:problem:order:validation-failed`         | 400    | General validation error    |

## Internal API

The `OrderService.updateStatus()` method is used internally by other modules (checkout, payment, shipping) to update
order status. It is not exposed via REST.

```kotlin
fun updateStatus(orderId: String, newStatus: String): Result<OrderDto, OrderError>
```

## Dependencies

The orders module depends on:

- **products-api**: Validates product existence before order creation

## Events

### OrderCreated

Published when an order is successfully created.

```kotlin
data class OrderCreated(
    val orderId: String,
    val userId: String,
    val grandTotal: BigDecimal,
    val currency: String,
    val timestamp: Instant
)
```
