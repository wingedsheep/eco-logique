-- Convert orders.id from VARCHAR to UUID
-- First drop the foreign key constraint on order_lines
ALTER TABLE order_lines DROP CONSTRAINT order_lines_order_id_fkey;

-- Convert order_lines.order_id to UUID
ALTER TABLE order_lines
    ALTER COLUMN order_id TYPE UUID USING order_id::uuid;

-- Convert orders.id to UUID (must be done after foreign key is dropped)
ALTER TABLE orders
    ALTER COLUMN id TYPE UUID USING id::uuid;

-- Re-add the foreign key constraint
ALTER TABLE order_lines
    ADD CONSTRAINT order_lines_order_id_fkey
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;
