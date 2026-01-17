package com.wingedsheep.ecologique.common.outbox

/**
 * Marker interface for events that should be routed through the transactional outbox.
 *
 * Events implementing this interface will be:
 * 1. Serialized to JSON
 * 2. Stored in the outbox table within the same transaction as business data
 * 3. Processed asynchronously by the outbox processor
 * 4. Delivered to Spring's event listeners
 *
 * Use [aggregateType] and [aggregateId] to enable ordering guarantees for events
 * belonging to the same aggregate.
 */
interface OutboxEvent {
    /**
     * Optional aggregate type for ordering guarantees.
     * Events with the same aggregate type and ID will be processed in order.
     */
    val aggregateType: String?
        get() = null

    /**
     * Optional aggregate ID for ordering guarantees.
     * Events with the same aggregate type and ID will be processed in order.
     */
    val aggregateId: String?
        get() = null
}
