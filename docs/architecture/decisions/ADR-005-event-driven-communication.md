# ADR-005: Communication Between Modules

**Status**: Accepted

**Date**: 2024-11-02

---

## Decision

Use **domain events** for asynchronous, decoupled communication between modules. Events represent important business occurrences.

---

## When to Use Events

### Use Events (Asynchronous)
- Eventual consistency is acceptable
- Loose coupling desired
- Multiple modules might react
- Action is a side effect, not core flow

**Examples**: Payment completed → trigger shipment, Product created → update search index

### Use Direct Calls (Synchronous)
- Immediate consistency required
- Need return value for decision
- Part of core business flow

**Examples**: Shipping needs product weight, Inventory check before reservation

---

## Event Definition

Events are immutable data classes in the publishing module's `-api`:

```kotlin
// payment-api/event/PaymentCompletedEvent.kt
data class PaymentCompletedEvent(
    val paymentId: String,
    val orderId: String,
    val amount: BigDecimal,
    val currency: String,
    val timestamp: Instant,
)
```

**Naming**: `<Entity><PastTense>` — `PaymentCompleted`, `ProductCreated`, `ShipmentDispatched`

---

## Publishing Events

```kotlin
@Service
internal class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : PaymentServiceApi {

    override fun completePayment(paymentId: String): Result<PaymentDto, PaymentError> {
        val payment = paymentRepository.findById(PaymentId(paymentId))
            ?: return Err(PaymentError.NotFound(paymentId))

        val completed = payment.complete()
        paymentRepository.save(completed)

        eventPublisher.publishEvent(PaymentCompletedEvent(
            paymentId = completed.id.value,
            orderId = completed.orderId.value,
            amount = completed.amount.amount,
            currency = completed.amount.currency.name,
            timestamp = Instant.now(),
        ))

        return Ok(completed.toDto())
    }
}
```

---

## Listening to Events

```kotlin
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentServiceApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onPaymentCompleted(event: PaymentCompletedEvent) {
        logger.info("Payment completed: ${event.paymentId}, starting fulfillment")
        shipmentService.startFulfillment(event.orderId)
    }
}
```

---

## The Dual-Write Problem

If database write succeeds but event publishing fails, the payment is complete but nobody knows. The **transactional outbox** solves this:

1. Write event to an outbox table in the same transaction as business data
2. A separate processor polls the outbox and publishes to listeners
3. If transaction rolls back, outbox entry rolls back too

For in-process Spring events, direct publishing is often sufficient. The outbox becomes important for external message brokers or when event delivery is critical.

---

## Idempotent Listeners

Events may be delivered more than once. Listeners must handle duplicates:

```kotlin
@EventListener
fun onPaymentCompleted(event: PaymentCompletedEvent) {
    val existing = shipmentRepository.findByOrderId(event.orderId)
    if (existing != null) {
        logger.info("Shipment already exists for order ${event.orderId}")
        return
    }
    shipmentService.startFulfillment(event.orderId)
}
```

---

## Consequences

### Positive
- Loose coupling between modules
- Easy to add new listeners
- Modules don't block each other
- Supports scaling (can move to message queue later)

### Negative
- Harder to debug async flows
- Eventual consistency challenges
- Need idempotency handling
