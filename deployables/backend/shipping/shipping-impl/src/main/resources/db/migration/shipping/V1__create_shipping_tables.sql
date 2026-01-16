CREATE TABLE shipments
(
    id               UUID PRIMARY KEY,
    order_id         UUID         NOT NULL,
    tracking_number  VARCHAR(100) NOT NULL UNIQUE,
    status           VARCHAR(50)  NOT NULL,
    recipient_name   VARCHAR(255) NOT NULL,
    street           VARCHAR(255) NOT NULL,
    house_number     VARCHAR(50)  NOT NULL,
    postal_code      VARCHAR(20)  NOT NULL,
    city             VARCHAR(255) NOT NULL,
    country_code     VARCHAR(3)   NOT NULL,
    warehouse_id     UUID         NOT NULL,
    weight_kg        DECIMAL(10, 3),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipped_at       TIMESTAMP,
    delivered_at     TIMESTAMP,
    CONSTRAINT uq_shipment_order UNIQUE (order_id)
);

CREATE INDEX idx_shipments_order_id ON shipments (order_id);
CREATE INDEX idx_shipments_tracking_number ON shipments (tracking_number);
CREATE INDEX idx_shipments_status ON shipments (status);
CREATE INDEX idx_shipments_warehouse_id ON shipments (warehouse_id);
