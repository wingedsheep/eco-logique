# ADR-005: Event-Driven Communication Between Modules

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
- One module shouldn't block another
- Multiple listeners may be interested
- Action is a side effect, not core flow

**Examples**:
- Payment completed → trigger shipment
- Product created → update search index
- User registered → send welcome email

### Use Direct Calls (Synchronous)
- Immediate consistency required
- Operation fails if dependency unavailable
- Need return value for decision
- Part of core business flow

**Examples**:
- Shipping needs product weight
- Inventory check before reservation
- User validation before order

---

## Event Definition

Events are immutable data classes in the publishing module's `-api`:

```kotlin
// payment-api/event/PaymentCompleted.kt
data class PaymentCompleted(
    val paymentId: PaymentId,
    val orderId: OrderId,
    val amount: Money,
    val timestamp: Instant
)

data class PaymentFailed(
    val paymentId: PaymentId,
    val orderId: OrderId,
    val reason: String,
    val timestamp: Instant
)
```

```kotlin
// products-api/event/ProductCreated.kt
data class ProductCreated(
    val productId: ProductId,
    val name: String,
    val category: ProductCategory,
    val timestamp: Instant
)

data class ProductPriceChanged(
    val productId: ProductId,
    val oldPrice: Money,
    val newPrice: Money,
    val timestamp: Instant
)
```

---

## Event Naming

**Pattern**: `<Entity><PastTense>` or `<Entity><Status>Changed`

```kotlin
// ✓ Good - Past tense, clear meaning
PaymentCompleted
ProductCreated
ShipmentDispatched
StockReserved

// ✗ Bad - Present tense or vague
PaymentComplete
CreateProduct
ShipmentEvent
```

---

## Publishing Events

### Setup Event Publisher

```kotlin
// common/event/DomainEventPublisher.kt
interface DomainEventPublisher {
    fun publish(event: Any)
}

@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : DomainEventPublisher {
    override fun publish(event: Any) {
        applicationEventPublisher.publishEvent(event)
    }
}
```

### Publish in Service

```kotlin
// payment-impl/service/PaymentServiceImpl.kt
@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val eventPublisher: DomainEventPublisher
) : PaymentService {

    override fun completePayment(paymentId: PaymentId): Result<Payment> = runCatching {
        val payment = paymentRepository.findById(paymentId)
            ?: throw NoSuchElementException("Payment not found")

        val completedPayment = payment.copy(
            status = PaymentStatus.COMPLETED,
            completedAt = Instant.now()
        )

        paymentRepository.save(completedPayment)

        // Publish event after successful save
        eventPublisher.publish(
            PaymentCompleted(
                paymentId = completedPayment.id,
                orderId = completedPayment.orderId,
                amount = completedPayment.amount,
                timestamp = Instant.now()
            )
        )

        completedPayment
    }
}
```

---

## Listening to Events

### Event Listener

```kotlin
// shipping-impl/event/PaymentCompletedListener.kt
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        logger.info("Payment completed: ${event.paymentId.value}, starting fulfillment")

        shipmentService.startFulfillment(event.orderId)
            .onFailure { error ->
                logger.error("Failed to start fulfillment for order ${event.orderId.value}", error)
            }
    }
}
```

### Async Processing (Optional)

```kotlin
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentService
) {
    @EventListener
    @Async  // Process in background thread
    @Transactional
    fun onPaymentCompleted(event: PaymentCompleted) {
        shipmentService.startFulfillment(event.orderId)
    }
}

// Enable async in configuration
@Configuration
@EnableAsync
class AsyncConfig
```

---

## Event Flow Example

Complete checkout flow using events:

```kotlin
// 1. Payment module publishes
@Service
class PaymentServiceImpl(private val eventPublisher: DomainEventPublisher) {
    fun completePayment(paymentId: PaymentId): Result<Payment> {
        // ... process payment ...
        eventPublisher.publish(PaymentCompleted(paymentId, orderId, amount, now))
    }
}

// 2. Inventory module listens and reserves
@Component
class PaymentCompletedInventoryListener(
    private val inventoryService: InventoryService
) {
    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        inventoryService.reserveForOrder(event.orderId)
        // Publishes StockReserved event
    }
}

// 3. Shipping module listens and creates shipment
@Component
class StockReservedListener(
    private val shipmentService: ShipmentService
) {
    @EventListener
    fun onStockReserved(event: StockReserved) {
        shipmentService.createShipment(event.orderId)
        // Publishes ShipmentCreated event
    }
}
```

---

## Error Handling

### Retry Failed Event Processing

```kotlin
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 1000, multiplier = 2.0)
    )
    fun onPaymentCompleted(event: PaymentCompleted) {
        shipmentService.startFulfillment(event.orderId)
            .onFailure { error ->
                logger.error("Failed to start fulfillment", error)
                throw error  // Trigger retry
            }
    }

    @Recover
    fun recover(e: Exception, event: PaymentCompleted) {
        logger.error("Failed after retries for payment ${event.paymentId.value}", e)
        // Send to dead letter queue or alert
    }
}
```

### Idempotent Listeners

```kotlin
@Component
class PaymentCompletedListener(
    private val shipmentService: ShipmentService,
    private val shipmentRepository: ShipmentRepository
) {
    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        // Check if already processed
        val existing = shipmentRepository.findByOrderId(event.orderId)
        if (existing != null) {
            logger.info("Shipment already exists for order ${event.orderId.value}")
            return
        }

        shipmentService.startFulfillment(event.orderId)
    }
}
```

---

## Event Ordering

Events are processed in order they're published **within the same transaction**. Across transactions, no ordering guarantee.

```kotlin
// Events published in this order
eventPublisher.publish(PaymentStarted(...))
eventPublisher.publish(PaymentCompleted(...))

// Listeners receive in same order
@EventListener fun onPaymentStarted(...) { }   // Called first
@EventListener fun onPaymentCompleted(...) { } // Called second
```

For strict ordering across modules, use event chain with explicit dependencies:

```
PaymentCompleted → StockReserved → ShipmentCreated
```

---

## Testing Events

### Test Event Publishing

```kotlin
@SpringBootTest
class PaymentServiceImplTest {
    @MockBean
    private lateinit var eventPublisher: DomainEventPublisher

    @Autowired
    private lateinit var paymentService: PaymentService

    @Test
    fun `completePayment should publish PaymentCompleted event`() {
        // Given
        val paymentId = PaymentId("PAY-001")

        // When
        paymentService.completePayment(paymentId)

        // Then
        verify(eventPublisher).publish(
            argThat<PaymentCompleted> {
                it.paymentId == paymentId
            }
        )
    }
}
```

### Test Event Listening

```kotlin
@SpringBootTest
class PaymentCompletedListenerTest {
    @MockBean
    private lateinit var shipmentService: ShipmentService

    @Autowired
    private lateinit var listener: PaymentCompletedListener

    @Test
    fun `onPaymentCompleted should start fulfillment`() {
        // Given
        val event = PaymentCompleted(
            paymentId = PaymentId("PAY-001"),
            orderId = OrderId("ORD-001"),
            amount = Money(BigDecimal("100"), EUR),
            timestamp = Instant.now()
        )
        whenever(shipmentService.startFulfillment(any())).thenReturn(Result.success(Unit))

        // When
        listener.onPaymentCompleted(event)

        // Then
        verify(shipmentService).startFulfillment(OrderId("ORD-001"))
    }
}
```

### Integration Test with Real Events

```kotlin
@SpringBootTest
class CheckoutFlowIntegrationTest {
    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var shipmentRepository: ShipmentRepository

    @Test
    fun `payment completion should trigger shipment creation`() {
        // Given
        val paymentId = PaymentId("PAY-001")
        val orderId = OrderId("ORD-001")

        // When
        paymentService.completePayment(paymentId)

        // Then - Wait for async event processing
        await().atMost(Duration.ofSeconds(5)).untilAsserted {
            val shipment = shipmentRepository.findByOrderId(orderId)
            assertThat(shipment).isNotNull
            assertThat(shipment?.status).isEqualTo(ShipmentStatus.PENDING)
        }
    }
}
```

---

## Consequences

### Positive
- Loose coupling between modules
- Easy to add new event listeners
- Modules don't block each other
- Natural fit for eventual consistency
- Supports scaling (can move to message queue later)

### Negative
- Harder to debug (async flow)
- Eventual consistency challenges
- Need idempotency handling
- No automatic ordering guarantees
- Error handling more complex

---

## Migration to Message Queue

Current implementation uses Spring's in-memory event bus. Can migrate to RabbitMQ/Kafka later:

```kotlin
// Current: Spring ApplicationEventPublisher
eventPublisher.publishEvent(PaymentCompleted(...))

// Future: RabbitMQ
rabbitTemplate.convertAndSend("payment.completed", PaymentCompleted(...))

// Listener unchanged (use @RabbitListener instead of @EventListener)
@RabbitListener(queues = ["payment.completed"])
fun onPaymentCompleted(event: PaymentCompleted) { ... }
```

Events defined in `-api` stay the same. Only infrastructure changes.
