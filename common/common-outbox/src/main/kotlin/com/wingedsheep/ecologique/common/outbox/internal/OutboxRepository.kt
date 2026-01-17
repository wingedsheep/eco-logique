package com.wingedsheep.ecologique.common.outbox.internal

import java.time.Instant
import java.util.UUID

/**
 * Repository interface for outbox entries.
 */
interface OutboxRepository {
    /**
     * Saves an outbox entry.
     */
    fun save(entry: OutboxEntry): OutboxEntry

    /**
     * Finds pending entries up to the specified limit.
     * Uses FOR UPDATE SKIP LOCKED for concurrent processing safety.
     */
    fun findPendingEntries(limit: Int): List<OutboxEntry>

    /**
     * Marks an entry as processed.
     */
    fun markAsProcessed(id: UUID, processedAt: Instant)

    /**
     * Marks an entry as failed with the given error message.
     */
    fun markAsFailed(id: UUID, error: String, newRetryCount: Int, newStatus: OutboxStatus)

    /**
     * Deletes processed entries older than the specified timestamp.
     */
    fun deleteProcessedBefore(before: Instant): Int
}
