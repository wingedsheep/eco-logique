package com.wingedsheep.ecologique.common.outbox.internal

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Component
class OutboxRepositoryImpl(
    private val jdbcRepository: OutboxJdbcRepository
) : OutboxRepository {

    override fun save(entry: OutboxEntry): OutboxEntry {
        val entity = entry.toEntity()
        if (jdbcRepository.existsById(entry.id)) {
            entity.markAsExisting()
        }
        return jdbcRepository.save(entity).toDomain()
    }

    override fun findPendingEntries(limit: Int): List<OutboxEntry> {
        return jdbcRepository.findPendingEntries(limit).map { it.toDomain() }
    }

    override fun markAsProcessed(id: UUID, processedAt: Instant) {
        jdbcRepository.markAsProcessed(id, processedAt, OutboxStatus.PROCESSED.name)
    }

    override fun markAsFailed(id: UUID, error: String, newRetryCount: Int, newStatus: OutboxStatus) {
        jdbcRepository.markAsFailed(id, error, newRetryCount, newStatus.name)
    }

    override fun deleteProcessedBefore(before: Instant): Int {
        return jdbcRepository.deleteProcessedBefore(before, OutboxStatus.PROCESSED.name)
    }
}

@Repository
interface OutboxJdbcRepository : CrudRepository<OutboxEntity, UUID> {

    @Query(
        """
        SELECT * FROM outbox.outbox_entries
        WHERE status = 'PENDING'
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """
    )
    fun findPendingEntries(limit: Int): List<OutboxEntity>

    @Modifying
    @Query(
        """
        UPDATE outbox.outbox_entries
        SET status = :status, processed_at = :processedAt
        WHERE id = :id
        """
    )
    fun markAsProcessed(id: UUID, processedAt: Instant, status: String)

    @Modifying
    @Query(
        """
        UPDATE outbox.outbox_entries
        SET last_error = :error, retry_count = :retryCount, status = :status
        WHERE id = :id
        """
    )
    fun markAsFailed(id: UUID, error: String, retryCount: Int, status: String)

    @Modifying
    @Query(
        """
        DELETE FROM outbox.outbox_entries
        WHERE status = :status AND processed_at < :before
        """
    )
    fun deleteProcessedBefore(before: Instant, status: String): Int
}
