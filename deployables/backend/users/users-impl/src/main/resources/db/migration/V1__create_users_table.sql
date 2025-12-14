CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    keycloak_subject VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    street VARCHAR(255),
    house_number VARCHAR(50),
    postal_code VARCHAR(20),
    city VARCHAR(100),
    country_code VARCHAR(2)
);
