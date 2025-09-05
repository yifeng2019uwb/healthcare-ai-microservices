# Healthcare Shared Module

## 🎯 Purpose
Internal shared components for database access, utilities, and common functionality across all healthcare microservices.

## 📦 Components

### **Database Layer**
- **Entities**: JPA entities for all 6 core tables
- **Repositories**: Spring Data JPA repository interfaces
- **Config**: Database connection and JPA configuration

### **Common Utilities**
- **Constants**: Database, validation, and business constants
- **ENUMs**: User roles, appointment statuses, medical record types
- **Exceptions**: Standardized exception classes
- **DTOs**: Common data transfer objects
- **Validation**: Custom validators for healthcare data
- **Logging**: Audit and debug logging utilities

## 🚫 What's NOT Included
- API controllers or endpoints
- Business logic services
- Web layer components
- External API integrations

## 🏗️ Package Structure
```
src/main/java/com/healthcare/
├── config/          # Database, JPA, logging config
├── entity/          # JPA entities
├── repository/      # Repository interfaces
├── dto/             # Data transfer objects
├── exception/       # Custom exceptions
├── util/            # Utilities
├── validation/      # Custom validators
├── constants/       # String constants
├── enums/           # ENUMs for variable attributes
└── logging/         # Audit/debug logging utilities
```

## 🚀 Usage
This module is imported as a dependency by individual services (Patient, Provider, Appointment, AI) to access shared database functionality and utilities.

## 📋 Implementation Status
- [x] Maven setup
- [ ] Database entities
- [ ] Repository interfaces
- [ ] Configuration classes
- [ ] Constants and ENUMs
- [ ] Exception classes
- [ ] Validation utilities
- [ ] Logging utilities
- [ ] Testing utilities

## 🧪 Testing
- Unit tests with PostgreSQL test database
- Integration tests with Testcontainers
- Repository tests with @DataJpaTest
