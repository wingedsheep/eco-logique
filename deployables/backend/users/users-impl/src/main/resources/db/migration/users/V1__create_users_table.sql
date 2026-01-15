CREATE TABLE users
(
    id               UUID PRIMARY KEY,
    external_subject VARCHAR(255) NOT NULL UNIQUE,
    name             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    street           VARCHAR(255),
    house_number     VARCHAR(50),
    postal_code      VARCHAR(20),
    city             VARCHAR(100),
    country_code     VARCHAR(50),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_external_subject ON users (external_subject);
CREATE INDEX idx_users_email ON users (email);
