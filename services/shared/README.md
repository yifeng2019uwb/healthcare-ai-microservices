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
- [x] Database entities (7 entities with comprehensive validation)
- [x] Configuration classes (PostgreSQL for all environments)
- [x] Constants and ENUMs (10 enums with full test coverage)
- [x] Exception classes (5 exception classes with tests)
- [x] Validation utilities (ValidationUtils with healthcare patterns)
- [x] Testing utilities (176 unit tests, 100% coverage)
- [ ] Repository interfaces (Next: CRUD operations)
- [ ] Logging utilities (Next: Audit logging)

## 🧪 Testing
- **176 Unit Tests** with 100% code coverage
- **PostgreSQL Test Database** for consistent enum handling
- **Healthcare Validation Patterns** for real-world compatibility
- **Repository tests** with @DataJpaTest (test DAO layer against database) - planned

**Note**: Shared module is **internal** (no HTTP endpoints). API integration tests belong in service modules, not here.

## 🎯 Recent Updates
- **H2 to PostgreSQL Migration**: Consistent enum handling across all environments
- **Healthcare Validation Patterns**: Real-world compatible patterns for patient numbers, insurance policies, medical records
- **100% Test Coverage**: Comprehensive validation and testing across all entities
- **Database Constants**: Centralized column definitions and table names
