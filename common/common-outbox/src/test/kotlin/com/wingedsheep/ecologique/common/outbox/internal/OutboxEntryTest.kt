package com.wingedsheep.ecologique.common.outbox.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class OutboxEntryTest {

    @Test
    fun `markAsProcessed should set status to PROCESSED and update processedAt`() {
        // Given
        val entry = buildEntry()
        val processedAt = Instant.now()

        // When
        val processed = entry.markAsProcessed(processedAt)

        // Then
        assertThat(processed.status).isEqualTo(OutboxStatus.PROCESSED)
        assertThat(processed.processedAt).isEqualTo(processedAt)
    }

    @Test
    fun `markAsFailed should increment retry count and set error`() {
        // Given
        val entry = buildEntry()

        // When
        val failed = entry.markAsFailed("Test error", maxRetries = 5)

        // Then
        assertThat(failed.retryCount).isEqualTo(1)
        assertThat(failed.lastError).isEqualTo("Test error")
        assertThat(failed.status).isEqualTo(OutboxStatus.PENDING)
    }

    @Test
    fun `markAsFailed should set status to FAILED when max retries reached`() {
        // Given
        val entry = buildEntry(retryCount = 4)

        // When
        val failed = entry.markAsFailed("Final error", maxRetries = 5)

        // Then
        assertThat(failed.retryCount).isEqualTo(5)
        assertThat(failed.status).isEqualTo(OutboxStatus.FAILED)
    }

    @Test
    fun `markAsFailed should keep PENDING status when retries not exhausted`() {
        // Given
        val entry = buildEntry(retryCount = 3)

        // When
        val failed = entry.markAsFailed("Error", maxRetries = 5)

        // Then
        assertThat(failed.retryCount).isEqualTo(4)
        assertThat(failed.status).isEqualTo(OutboxStatus.PENDING)
    }

    private fun buildEntry(
        retryCount: Int = 0
    ): OutboxEntry = OutboxEntry(
        id = UUID.randomUUID(),
        eventType = "com.example.TestEvent",
        eventPayload = """{"id":"test"}""",
        aggregateType = "Order",
        aggregateId = "ORDER-123",
        createdAt = Instant.now(),
        processedAt = null,
        retryCount = retryCount,
        lastError = null,
        status = OutboxStatus.PENDING
    )
}
