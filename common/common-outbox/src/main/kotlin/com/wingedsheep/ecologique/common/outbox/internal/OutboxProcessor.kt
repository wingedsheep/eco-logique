package com.wingedsheep.ecologique.common.outbox.internal

import com.wingedsheep.ecologique.common.outbox.OutboxProperties
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Processes pending outbox entries by deserializing and publishing them
 * to Spring's event system.
 */
open class OutboxProcessor(
    private val outboxRepository: OutboxRepository,
    private val eventSerializer: OutboxEventSerializer,
    private val eventPublisher: ApplicationEventPublisher,
    private val properties: OutboxProperties
) {
    private val logger = LoggerFactory.getLogger(OutboxProcessor::class.java)

    /**
     * Polls for pending outbox entries and processes them.
     * Runs at the configured poll interval.
     */
    @Scheduled(fixedDelayString = "\${outbox.poll-interval-ms:1000}")
    @Transactional
    open fun processPendingEntries() {
        val entries = outboxRepository.findPendingEntries(properties.batchSize)

        for (entry in entries) {
            processEntry(entry)
        }
    }

    private fun processEntry(entry: OutboxEntry) {
        try {
            val event = eventSerializer.deserialize(entry.eventType, entry.eventPayload)
            eventPublisher.publishEvent(event)
            outboxRepository.markAsProcessed(entry.id, Instant.now())
            logger.debug("Successfully processed outbox entry: {}", entry.id)
        } catch (e: Exception) {
            val errorMessage = "${e::class.simpleName}: ${e.message}"
            val newRetryCount = entry.retryCount + 1
            val newStatus = if (newRetryCount >= properties.maxRetries) {
                OutboxStatus.FAILED
            } else {
                OutboxStatus.PENDING
            }
            outboxRepository.markAsFailed(entry.id, errorMessage, newRetryCount, newStatus)
            logger.warn(
                "Failed to process outbox entry {} (attempt {}/{}): {}",
                entry.id,
                newRetryCount,
                properties.maxRetries,
                errorMessage
            )
        }
    }

    /**
     * Cleans up processed entries older than the retention period.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    open fun cleanupProcessedEntries() {
        val cutoff = Instant.now().minus(properties.retentionDays, ChronoUnit.DAYS)
        val deleted = outboxRepository.deleteProcessedBefore(cutoff)
        if (deleted > 0) {
            logger.info("Cleaned up {} processed outbox entries older than {} days", deleted, properties.retentionDays)
        }
    }
}
