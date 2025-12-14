CREATE TABLE products (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(2000) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    price_amount DECIMAL(19, 2) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    weight_grams INTEGER NOT NULL,
    sustainability_rating VARCHAR(10) NOT NULL,
    carbon_footprint_kg DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category ON products(category_code);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_sustainability ON products(sustainability_rating);
