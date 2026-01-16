-- Add address columns to warehouses table
ALTER TABLE warehouses
    ADD COLUMN street VARCHAR(255),
    ADD COLUMN house_number VARCHAR(50),
    ADD COLUMN postal_code VARCHAR(20),
    ADD COLUMN city VARCHAR(255),
    ADD COLUMN country_code_address VARCHAR(3);
