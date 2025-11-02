# Ecologique

A modular monolith e-commerce application for eco-friendly products.

## Prerequisites

- JDK 21
- Docker & Docker Compose
- (Optional) Just command runner

## Quick Start

### Using Just (Recommended)

```bash
# Start Docker services
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
./gradlew :deployables:ecologique:bootRun

# Run tests
./gradlew test
```

## Endpoints

- Health: http://localhost:8080/health
- Actuator: http://localhost:8080/actuator/health

## Database

PostgreSQL is available at:
- Host: localhost
- Port: 5432
- Database: economique
- Username: user
- Password: password

## Development

See [backlog.md](backlog.md) for current development tasks and [AGENTS.md](AGENTS.md) for architecture overview.