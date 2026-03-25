# Telemedicine Service (Video)

This service depends on:
- **MongoDB** (default: `localhost:27017`, DB: `telemed-db`)
- **RabbitMQ** (default: `localhost:5672`) — optional in dev (user-events disabled by default)

## Quick start (recommended)

From this folder:

```bash
docker compose up -d mongo rabbitmq
```

Then run the app from your IDE or:

```bash
mvn spring-boot:run
```

## Run everything in Docker

```bash
docker compose up --build
```

## Config overrides

You can override defaults via environment variables:
- `SPRING_DATA_MONGODB_URI` (e.g. `mongodb://localhost:27017/telemed-db`)
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
- `APPOINTMENT_SERVICE_URL`
- `USER_EVENTS_ENABLED` (`true` to enable RabbitMQ consumer)
