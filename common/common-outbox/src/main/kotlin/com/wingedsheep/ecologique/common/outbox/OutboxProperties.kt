package com.wingedsheep.ecologique.common.outbox

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the transactional outbox.
 */
@ConfigurationProperties(prefix = "outbox")
data class OutboxProperties(
    /**
     * Whether the outbox is enabled. Defaults to true.
     */
    val enabled: Boolean = true,

    /**
     * Interval in milliseconds between polling for pending outbox entries.
     * Defaults to 1000ms (1 second).
     */
    val pollIntervalMs: Long = 1000,

    /**
     * Maximum number of entries to process in a single batch.
     * Defaults to 100.
     */
    val batchSize: Int = 100,

    /**
     * Maximum number of retry attempts for failed entries before giving up.
     * Defaults to 5.
     */
    val maxRetries: Int = 5,

    /**
     * Number of days to retain processed entries before cleanup.
     * Defaults to 7 days.
     */
    val retentionDays: Long = 7
)
