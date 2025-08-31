# Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the Healthcare AI microservices.

## Deployment Structure

- `base/` - Base manifests for all services
- `overlays/dev/` - Development environment overrides
- `overlays/staging/` - Staging environment overrides
- `overlays/prod/` - Production environment overrides

## Services

- **API Gateway**: Spring Cloud Gateway
- **Auth Service**: JWT validation service
- **Business Services**: Patient, Provider, Appointment, AI
- **Support Services**: File Storage

## Implementation Status

- [ ] Base manifests
- [ ] Development environment
- [ ] Staging environment
- [ ] Production environment

## Usage

```bash
# Deploy to development
kubectl apply -k overlays/dev

# Deploy to production
kubectl apply -k overlays/prod

# Check status
kubectl get pods -n healthcare-ai
```
