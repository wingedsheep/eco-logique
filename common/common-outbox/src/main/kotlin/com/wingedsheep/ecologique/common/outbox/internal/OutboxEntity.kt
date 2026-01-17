package com.wingedsheep.ecologique.common.outbox.internal

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Spring Data JDBC entity for outbox entries.
 */
@Table("outbox_entries", schema = "outbox")
class OutboxEntity(
    @Id private val id: UUID,
    val eventType: String,
    val eventPayload: String,
    val aggregateType: String?,
    val aggregateId: String?,
    val createdAt: Instant,
    val processedAt: Instant?,
    val retryCount: Int,
    val lastError: String?,
    val status: String
) : Persistable<UUID> {

    @Transient
    private var isNewEntity: Boolean = true

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsExisting(): OutboxEntity {
        isNewEntity = false
        return this
    }
}

fun OutboxEntry.toEntity(): OutboxEntity = OutboxEntity(
    id = id,
    eventType = eventType,
    eventPayload = eventPayload,
    aggregateType = aggregateType,
    aggregateId = aggregateId,
    createdAt = createdAt,
    processedAt = processedAt,
    retryCount = retryCount,
    lastError = lastError,
    status = status.name
)

fun OutboxEntity.toDomain(): OutboxEntry = OutboxEntry(
    id = id,
    eventType = eventType,
    eventPayload = eventPayload,
    aggregateType = aggregateType,
    aggregateId = aggregateId,
    createdAt = createdAt,
    processedAt = processedAt,
    retryCount = retryCount,
    lastError = lastError,
    status = OutboxStatus.valueOf(status)
)
