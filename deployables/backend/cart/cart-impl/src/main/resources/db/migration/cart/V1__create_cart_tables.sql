CREATE TABLE carts
(
    user_id    VARCHAR(255) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items
(
    id           SERIAL PRIMARY KEY,
    user_id      VARCHAR(255)   NOT NULL REFERENCES carts (user_id) ON DELETE CASCADE,
    product_id   VARCHAR(255)   NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    unit_price   DECIMAL(19, 2) NOT NULL,
    quantity     INTEGER        NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, product_id)
);

CREATE INDEX idx_cart_items_user_id ON cart_items (user_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);
