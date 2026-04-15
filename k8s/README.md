# Telemedicine Kubernetes Deployment

## Prerequisites
- Kubernetes cluster (Minikube, Docker Desktop Kubernetes, or cloud cluster)
- kubectl configured
- Docker image available in cluster runtime

## 1) Build image
From the Telemedicine service root:

```bash
docker build -t telemedicine-service:latest .
```

If using Minikube:

```bash
minikube image load telemedicine-service:latest
```

## 2) Deploy all resources

```bash
kubectl apply -k k8s
```

## 3) Verify

```bash
kubectl get pods -n healthcare
kubectl get svc -n healthcare
```

## 4) Access service
Telemedicine API is exposed via NodePort:
- http://localhost:30083

## 5) Remove deployment

```bash
kubectl delete -k k8s
```
