-- Change the id column from VARCHAR(255) to UUID type
-- This removes the "USER-" prefix and stores raw UUIDs

-- Drop the existing id column constraint and recreate as UUID
ALTER TABLE users ALTER COLUMN id TYPE UUID USING SUBSTRING(id FROM 6)::UUID;
