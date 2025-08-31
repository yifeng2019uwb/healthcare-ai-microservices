# Backend Services

This directory contains all the Java/Spring Boot microservices and one Python AI service for the Healthcare AI platform.

## Service Structure

- `shared/` - Common data access layer and utilities (Java)
- `gateway/` - Spring Cloud Gateway (Port 8000) - Java
- `auth-service/` - Authentication and JWT validation (Port 8001) - Java
- `patient-service/` - Patient management (Port 8002) - Java
- `provider-service/` - Provider management (Port 8003) - Java
- `appointment-service/` - Appointment management (Port 8004) - Java
- `ai-service/` - AI features (Port 8005) - Python/FastAPI
- `file-storage-service/` - File management (Port 8006) - Java

## Technology Stack

- **Java Services**: Spring Boot 3.2+ with Java 17, Maven build system
- **Python Service**: FastAPI with Python 3.11+, pip/poetry for dependencies
- **Shared Infrastructure**: Docker containers, Kubernetes orchestration
- **Data Layer**: PostgreSQL with shared access patterns

## Project Structure

```
services/
├── pom.xml                    # Parent POM for Java services
├── shared/                    # Shared Java library module
├── gateway/                   # Spring Cloud Gateway
├── auth-service/              # Authentication service
├── patient-service/           # Patient management
├── provider-service/          # Provider management
├── appointment-service/        # Appointment management
├── ai-service/                # AI features (Python)
└── file-storage-service/      # File management
```
