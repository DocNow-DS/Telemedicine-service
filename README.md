# Telemedicine Service

Video consultation microservice for the healthcare platform.

## Prerequisites
- Java 17
- Maven
- Docker Desktop

## Dependencies
- MongoDB
- RabbitMQ (optional for local flows where event consumer is disabled)

## Local Setup
From this folder:

```bash
docker compose up -d mongo rabbitmq
mvnw.cmd clean install -DskipTests
mvnw.cmd spring-boot:run
```

## Default Runtime
- Service URL: http://localhost:8083

## Environment Variables
- `SPRING_DATA_MONGODB_URI` (example: `mongodb://localhost:27017/telemed-db`)
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
- `APPOINTMENT_SERVICE_URL`
- `USER_EVENTS_ENABLED`

## Docker Run
```bash
mvnw.cmd clean package -DskipTests
docker compose up --build
```

## Stop
```bash
docker compose down
```
