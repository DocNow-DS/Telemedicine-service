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
mvn -DskipTests clean package
docker compose up --build
```

## Docker image (standalone)

Build and run from this folder:

```bash
mvn -DskipTests clean package
docker build -t telemedicine-service:latest .
docker run --rm -p 8083:8083 \
	-e SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal:27017/telemed-db \
	-e RABBITMQ_HOST=host.docker.internal \
	-e USER_EVENTS_ENABLED=false \
	telemedicine-service:latest
```

## Kubernetes

Kubernetes manifests are under `k8s/` and include:
- telemedicine service deployment + service
- mongo deployment + service + PVC
- rabbitmq deployment + service
- config map and secret

### 1) Build image locally

```bash
docker build -t telemedicine-service:latest .
```

If you use Minikube:

```bash
minikube image load telemedicine-service:latest
```

### 2) Apply resources

```bash
kubectl apply -k k8s
```

### 3) Check status

```bash
kubectl get pods -n healthcare
kubectl get svc -n healthcare
```

Telemedicine API is exposed on NodePort `30083`.

## Config overrides

You can override defaults via environment variables:
- `SPRING_DATA_MONGODB_URI` (e.g. `mongodb://localhost:27017/telemed-db`)
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`
- `APPOINTMENT_SERVICE_URL`
- `USER_EVENTS_ENABLED` (`true` to enable RabbitMQ consumer)
