-- Change id column from VARCHAR to UUID
-- Drop existing constraints first
ALTER TABLE products DROP CONSTRAINT products_pkey;

-- Create a new UUID column
ALTER TABLE products ADD COLUMN id_new UUID;

-- Since existing data has VARCHAR IDs, we need to recreate them as UUIDs
-- For existing records, generate new UUIDs (this is acceptable during migration)
UPDATE products SET id_new = uuid_in(md5(random()::text || clock_timestamp()::text)::cstring);

-- Drop the old id column and rename the new one
ALTER TABLE products DROP COLUMN id;
ALTER TABLE products RENAME COLUMN id_new TO id;

-- Add back the primary key constraint
ALTER TABLE products ADD PRIMARY KEY (id);
