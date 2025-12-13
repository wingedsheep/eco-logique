# ADR-008: Error Handling

**Status**: Accepted

**Date**: 2024-11-04

---

## Decision

We implement a structured error handling strategy using a Global Exception Handler for cross-cutting concerns and Controller-based mapping for domain-specific errors. We adopt the **RFC 7807 Problem Details** format for all error responses using Spring Boot 3's native `ProblemDetail` support.

---

## Context

*   The application has use cases in the service layer.
*   Domain errors should be handled explicitly.
*   The Domain layer should be agnostic of HTTP and DTOs.
*   Controllers act as the boundary, mapping domain results to HTTP responses.

---

## Strategy

### 1. Global Exception Handler
The Global Exception Handler is responsible for unexpected errors and framework-level issues. It does **not** handle business logic errors.

*   **Handles**:
    *   `UnexpectedException` (500 Internal Server Error)
    *   `AuthenticationException` / `AuthorizationException` (401/403)
    *   Argument Mapping errors (400 Bad Request, e.g., JSON parse errors, validation failures)
*   **Response**: Returns a standardized Problem JSON.

### 2. Domain Errors
Domain errors are treated as data, not exceptions.

*   **Sealed Classes**: Service layer methods return `Result<T, E>` (or a similar Result type) where `E` is a sealed class hierarchy of specific domain errors.
*   **Exhaustiveness**: This forces the consumer (the Controller) to handle all possible error cases.
*   **Isolation**: The Domain layer knows nothing about HTTP status codes or DTOs.

### 3. Controller Responsibility
The Controller acts as the translation layer between the Domain and HTTP.

*   **Calls** the service/use case.
*   **Matches** on the `Result`.
*   **On Success**: Returns typed `ResponseEntity<T>` with the DTO.
*   **On Failure (Domain Error)**: Throws `ResponseStatusException` which Spring converts to Problem Details.

### 4. Error Response Structure (RFC 7807 Problem Details)
All error responses follow the [RFC 7807](https://tools.ietf.org/html/rfc7807) standard. Spring Boot 3 automatically converts `ResponseStatusException` to Problem Details format.

*   `type`: URI reference that identifies the problem type.
*   `title`: Short, human-readable summary of the problem type.
*   `status`: The HTTP status code.
*   `detail`: Human-readable explanation specific to this occurrence of the problem.
*   `instance`: URI reference that identifies the specific occurrence of the problem.

#### Example

**Domain Layer**:
```kotlin
sealed class OrderCreateError {
    data object OutOfStock : OrderCreateError()
    data class InvalidAddress(val reason: String) : OrderCreateError()
}

interface OrderService {
    fun createOrder(order: Order): Result<Order, OrderCreateError>
}
```

**Controller Layer**:
```kotlin
@PostMapping
fun createOrder(@RequestBody request: OrderCreateRequest): ResponseEntity<OrderResponse> {
    return orderService.createOrder(request.toDomain())
        .fold(
            onSuccess = { order -> ResponseEntity.ok(order.toResponse()) },
            onFailure = { error -> throw error.toResponseStatusException() }
        )
}
```

**Error Mapper** (in the REST layer, alongside the controller):
```kotlin
// rest/v1/OrderCreateErrorMappers.kt
fun OrderCreateError.toResponseStatusException(): ResponseStatusException =
    when (this) {
        is OrderCreateError.OutOfStock -> ResponseStatusException(
            HttpStatus.CONFLICT,
            "The requested product is out of stock."
        )
        is OrderCreateError.InvalidAddress -> ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            reason
        )
    }
```

---

## Consequences

### Positive
*   **Type-safety**: The compiler enforces handling of all known domain errors. Controllers return typed `ResponseEntity<T>`.
*   **Separation of Concerns**: The domain layer remains pure, while the controller handles HTTP specifics.
*   **Standardization**: Clients receive consistent error responses (Problem JSON) via Spring's built-in handling.
*   **Predictability**: No "magic" exception throwing for control flow in business logic.
*   **Simplicity**: `ResponseStatusException` leverages Spring's native Problem Details support.

### Negative
*   **Hierarchy Maintenance**: Developers must define and maintain error class hierarchies.
*   **Mapper Boilerplate**: Each error type needs a mapper to `ResponseStatusException`.
