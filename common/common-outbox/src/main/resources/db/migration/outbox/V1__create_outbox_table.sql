CREATE TABLE outbox_entries
(
    id             UUID PRIMARY KEY,
    event_type     VARCHAR(500) NOT NULL,
    event_payload  TEXT         NOT NULL,
    aggregate_type VARCHAR(255),
    aggregate_id   VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at   TIMESTAMP,
    retry_count    INTEGER      NOT NULL DEFAULT 0,
    last_error     TEXT,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_outbox_pending ON outbox_entries (status, created_at) WHERE status = 'PENDING';
CREATE INDEX idx_outbox_processed ON outbox_entries (processed_at) WHERE status = 'PROCESSED';
CREATE INDEX idx_outbox_aggregate ON outbox_entries (aggregate_type, aggregate_id, created_at) WHERE aggregate_type IS NOT NULL;
