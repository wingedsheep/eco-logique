CREATE TABLE warehouses
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    country_code VARCHAR(3)   NOT NULL
);

CREATE TABLE inventory_items
(
    id                SERIAL PRIMARY KEY,
    product_id        UUID    NOT NULL,
    warehouse_id      UUID    NOT NULL REFERENCES warehouses (id),
    quantity_on_hand  INTEGER NOT NULL DEFAULT 0,
    quantity_reserved INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventory_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT chk_quantity_on_hand_non_negative CHECK (quantity_on_hand >= 0),
    CONSTRAINT chk_quantity_reserved_non_negative CHECK (quantity_reserved >= 0),
    CONSTRAINT chk_reserved_not_exceed_on_hand CHECK (quantity_reserved <= quantity_on_hand)
);

CREATE INDEX idx_inventory_items_product_id ON inventory_items (product_id);
CREATE INDEX idx_inventory_items_warehouse_id ON inventory_items (warehouse_id);

CREATE TABLE stock_reservations
(
    id             UUID PRIMARY KEY,
    product_id     UUID         NOT NULL,
    warehouse_id   UUID         NOT NULL REFERENCES warehouses (id),
    quantity       INTEGER      NOT NULL,
    correlation_id VARCHAR(255) NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_stock_reservations_product_id ON stock_reservations (product_id);
CREATE INDEX idx_stock_reservations_correlation_id ON stock_reservations (correlation_id);
CREATE INDEX idx_stock_reservations_status ON stock_reservations (status);
