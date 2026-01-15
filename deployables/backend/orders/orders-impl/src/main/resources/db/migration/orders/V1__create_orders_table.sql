CREATE TABLE orders
(
    id          UUID PRIMARY KEY,
    user_id     VARCHAR(255)   NOT NULL,
    status      VARCHAR(50)    NOT NULL,
    subtotal    DECIMAL(19, 2) NOT NULL,
    grand_total DECIMAL(19, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);

CREATE TABLE order_lines
(
    id           SERIAL PRIMARY KEY,
    order_id     UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   VARCHAR(255)   NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    unit_price   DECIMAL(19, 2) NOT NULL,
    quantity     INTEGER        NOT NULL,
    line_total   DECIMAL(19, 2) NOT NULL
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);
