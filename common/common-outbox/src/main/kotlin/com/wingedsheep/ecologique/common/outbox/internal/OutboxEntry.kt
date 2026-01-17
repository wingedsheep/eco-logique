package com.wingedsheep.ecologique.common.outbox.internal

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing an entry in the transactional outbox.
 */
data class OutboxEntry(
    val id: UUID,
    val eventType: String,
    val eventPayload: String,
    val aggregateType: String?,
    val aggregateId: String?,
    val createdAt: Instant,
    val processedAt: Instant?,
    val retryCount: Int,
    val lastError: String?,
    val status: OutboxStatus
) {
    fun markAsProcessed(processedAt: Instant): OutboxEntry = copy(
        status = OutboxStatus.PROCESSED,
        processedAt = processedAt
    )

    fun markAsFailed(error: String, maxRetries: Int): OutboxEntry {
        val newRetryCount = retryCount + 1
        return copy(
            retryCount = newRetryCount,
            lastError = error,
            status = if (newRetryCount >= maxRetries) OutboxStatus.FAILED else OutboxStatus.PENDING
        )
    }
}
