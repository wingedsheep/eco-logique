# justfile

# List all available commands
default:
    @just --list

# Build the project
build:
    cd deployables/backend && ./gradlew build

# Run the application
run:
    cd deployables/backend && ./gradlew :application:bootRun

# Run tests
test:
    cd deployables/backend && ./gradlew test

# Clean build artifacts
clean:
    cd deployables/backend && ./gradlew clean

# Start Docker services
docker-up:
    cd docker && docker-compose up -d

# Stop Docker services
docker-down:
    cd docker && docker-compose down

# View Docker logs
docker-logs:
    cd docker && docker-compose logs -f

# Full rebuild
rebuild: clean build

# Run with Docker environment
dev: docker-up run
