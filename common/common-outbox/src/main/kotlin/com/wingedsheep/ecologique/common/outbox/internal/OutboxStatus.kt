package com.wingedsheep.ecologique.common.outbox.internal

/**
 * Status of an outbox entry.
 * Note: This class is in the internal package to signal it's an implementation detail.
 */
enum class OutboxStatus {
    /**
     * Entry is waiting to be processed.
     */
    PENDING,

    /**
     * Entry has been successfully processed and delivered.
     */
    PROCESSED,

    /**
     * Entry has failed after exhausting all retry attempts.
     */
    FAILED
}
