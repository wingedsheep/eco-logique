# Ecologique

A modular monolith e-commerce application for eco-friendly products.

## Prerequisites

- JDK 21
- Docker & Docker Compose
- (Optional) Just command runner

## Quick Start

### Using Just (Recommended)

```bash
# Start Docker services (PostgreSQL, RabbitMQ, Keycloak)
just docker-up

# Run the application
just run

# Run tests
just test
```

### Using Gradle Directly

```bash
# Start Docker services
cd docker && docker-compose up -d

# Run the application
cd deployables/backend && ./gradlew :application:bootRun

# Run tests
cd deployables/backend && ./gradlew test
```

## Endpoints

- Health: http://localhost:8080/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Database

PostgreSQL is available at:

- Host: localhost
- Port: 5432
- Database: ecologique
- Username: user
- Password: password

## Authentication

The application uses Keycloak for authentication via OAuth2/JWT.

### Keycloak Admin Console

- URL: http://localhost:8180
- Username: admin
- Password: admin

### Demo Users

| Username | Email         | Password | Roles                     |
|----------|---------------|----------|---------------------------|
| john     | john@demo.com | password | ROLE_CUSTOMER             |
| jane     | jane@demo.com | password | ROLE_ADMIN, ROLE_CUSTOMER |

### Getting an Access Token

```bash
# Get token for john (customer)
curl -X POST http://localhost:8180/realms/ecologique/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ecologique-web" \
  -d "username=john" \
  -d "password=password"

# Get token for jane (admin)
curl -X POST http://localhost:8180/realms/ecologique/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ecologique-web" \
  -d "username=jane" \
  -d "password=password"
```

### Using the Token

```bash
# Extract the access_token from the response and use it in requests
TOKEN="<your-access-token>"

# Example: Create a product (requires ROLE_ADMIN)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Eco Water Bottle",
    "description": "Reusable stainless steel bottle",
    "category": "HOUSEHOLD",
    "priceAmount": 24.99,
    "priceCurrency": "EUR",
    "weightGrams": 350,
    "carbonFootprintKg": 1.5
  }'

# Example: Get products (public, no auth required)
curl http://localhost:8080/api/v1/products
```

## Development

See [backlog.md](backlog.md) for current development tasks and [AGENTS.md](AGENTS.md) for architecture overview.

## Worldview Data

When running locally (not in `prod` or `test` profiles), the application automatically seeds demo data:

**Products**: 7 eco-friendly products across categories (clothing, electronics, food, etc.)

**Orders**: 4 sample orders in various statuses (created, paid, shipped, delivered)

This data is useful for local development and demos. It's idempotent - restarting the application won't create
duplicates.
