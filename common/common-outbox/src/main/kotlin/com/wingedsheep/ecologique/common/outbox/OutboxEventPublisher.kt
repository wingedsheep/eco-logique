package com.wingedsheep.ecologique.common.outbox

import com.wingedsheep.ecologique.common.outbox.internal.OutboxEntry
import com.wingedsheep.ecologique.common.outbox.internal.OutboxEventSerializer
import com.wingedsheep.ecologique.common.outbox.internal.OutboxRepository
import com.wingedsheep.ecologique.common.outbox.internal.OutboxStatus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Event publisher that routes [OutboxEvent] instances through the transactional outbox
 * for reliable delivery, while delegating other events directly to Spring's [ApplicationEventPublisher].
 *
 * When publishing an [OutboxEvent]:
 * 1. The event is serialized to JSON
 * 2. Stored in the outbox table within the current transaction
 * 3. Later processed by [OutboxProcessor] which delivers it to Spring event listeners
 *
 * This ensures that events are only published if the transaction commits successfully,
 * providing at-least-once delivery semantics.
 *
 * **Important**: This publisher requires an active transaction when publishing [OutboxEvent] instances.
 * If called outside a transaction, an exception will be thrown.
 */
open class OutboxEventPublisher(
    private val outboxRepository: OutboxRepository,
    private val eventSerializer: OutboxEventSerializer,
    private val fallbackPublisher: ApplicationEventPublisher
) {

    /**
     * Publishes an event. If the event implements [OutboxEvent], it will be routed through
     * the transactional outbox. Otherwise, it will be published directly via Spring's
     * [ApplicationEventPublisher].
     *
     * @param event The event to publish
     * @throws IllegalStateException if publishing an [OutboxEvent] outside a transaction
     */
    @Transactional(propagation = Propagation.MANDATORY)
    open fun publishEvent(event: OutboxEvent) {
        val serialized = eventSerializer.serialize(event)
        val entry = OutboxEntry(
            id = UUID.randomUUID(),
            eventType = serialized.eventType,
            eventPayload = serialized.payload,
            aggregateType = event.aggregateType,
            aggregateId = event.aggregateId,
            createdAt = Instant.now(),
            processedAt = null,
            retryCount = 0,
            lastError = null,
            status = OutboxStatus.PENDING
        )
        outboxRepository.save(entry)
    }

    /**
     * Publishes a non-outbox event directly via Spring's event publisher.
     * Use this for events that don't need transactional guarantees.
     */
    fun publishEventDirect(event: Any) {
        fallbackPublisher.publishEvent(event)
    }
}
