# List all available commands
default:
    @just --list

# Build the project
build:
    ./gradlew build

# Run the application
run:
    ./gradlew :deployables:ecologique:bootRun

# Run tests
test:
    ./gradlew test

# Clean build artifacts
clean:
    ./gradlew clean

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
