ALTER TABLE orders
    ADD COLUMN payment_id UUID;

CREATE INDEX idx_orders_payment_id ON orders (payment_id);
