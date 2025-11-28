# Local Development Environment

This directory contains the necessary files to run a local development environment using Docker.

## Prerequisites

- Docker
- Docker Compose

## How to Start

To start the local development environment, run the following command from the `docker` directory:

```bash
docker-compose up -d
```

This will start the following services:

- **PostgreSQL**: A PostgreSQL database with the following schemas:
  - `payment`
  - `products`
  - `shipping`
  - `inventory`
  - `users`
- **RabbitMQ**: A RabbitMQ message broker.

You can connect to the PostgreSQL database from your host machine using the following credentials:

- **Host**: `localhost`
- **Port**: `5432`
- **Username**: `user`
- **Password**: `password`
- **Database**: `ecologique`

The RabbitMQ management interface is available at `http://localhost:15672`.
