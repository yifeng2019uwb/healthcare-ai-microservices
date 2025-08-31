# Backend Services

This directory contains all the Java/Spring Boot microservices for the Healthcare AI platform.

## Service Structure

- `shared/` - Common data access layer and utilities
- `gateway/` - Spring Cloud Gateway (Port 8000)
- `auth-service/` - Authentication and JWT validation (Port 8001)
- `patient-service/` - Patient management (Port 8002)
- `provider-service/` - Provider management (Port 8003)
- `appointment-service/` - Appointment management (Port 8004)
- `ai-service/` - AI features (Port 8005)
- `file-storage-service/` - File management (Port 8006)

## Technology Stack

- **Framework**: Spring Boot 3.2+ with Java 17
- **Build Tool**: Maven (multi-module project)
- **API Gateway**: Spring Cloud Gateway
- **Authentication**: Spring Security + JWT
- **Database**: Neon PostgreSQL with shared data layer
- **Containerization**: Docker

## Implementation Status

- [ ] Shared Module (Data Access Layer)
- [ ] Gateway Service
- [ ] Auth Service
- [ ] Patient Service
- [ ] Provider Service
- [ ] Appointment Service
- [ ] AI Service
- [ ] File Storage Service

## Project Structure

```
services/
├── pom.xml                    # Parent POM for all services
├── shared/                    # Shared library module
├── gateway/                   # Spring Cloud Gateway
├── auth-service/              # Authentication service
├── patient-service/           # Patient management
├── provider-service/          # Provider management
├── appointment-service/        # Appointment management
├── ai-service/                # AI features
└── file-storage-service/      # File management
```

## Shared Components

- **Data Access Layer**: Common database access patterns
- **Authentication**: Shared authentication utilities
- **Logging**: Common logging configuration
- **Utilities**: Shared helper functions
- **Models**: Common data models

## Benefits of Java-Based Architecture

- **Unified Technology Stack**: All services use Spring Boot
- **Consistent Patterns**: Same build, test, and deployment approach
- **Shared Dependencies**: Common libraries and versions
- **Easier Integration**: Seamless service communication
- **Strong Ecosystem**: Rich Spring Boot ecosystem and community support
