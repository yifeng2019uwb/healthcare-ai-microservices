# Database Setup Guide

## Overview

This project uses PostgreSQL for both development and production, ensuring consistency between test and production environments. H2 is no longer used to avoid enum handling differences.

## Database Configurations

### 1. Development (`application.yml`)
- **Database**: PostgreSQL
- **Host**: localhost:5432
- **Database**: healthcare_dev_db
- **Username**: healthcare_dev
- **Password**: dev_password
- **DDL**: create-drop (recreates schema on startup)

### 2. Testing (`application-test.yml`)
- **Database**: PostgreSQL
- **Host**: localhost:5432
- **Database**: healthcare_test_db
- **Username**: healthcare_test
- **Password**: test_password
- **DDL**: create-drop (recreates schema for each test)

### 3. Production (`application-prod.yml`)
- **Database**: PostgreSQL
- **Host**: ${DB_HOST:localhost}:${DB_PORT:5432}
- **Database**: ${DB_NAME:healthcare_prod_db}
- **Username**: ${DB_USERNAME:healthcare_prod}
- **Password**: ${DB_PASSWORD} (environment variable)
- **DDL**: validate (never auto-create)

## Setup Instructions

### 1. Install PostgreSQL
```bash
# macOS with Homebrew
brew install postgresql
brew services start postgresql

# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib
sudo systemctl start postgresql

# Windows
# Download from https://www.postgresql.org/download/windows/
```

### 2. Create Databases and Users
```sql
-- Connect to PostgreSQL as superuser
psql -U postgres

-- Create development database and user
CREATE DATABASE healthcare_dev_db;
CREATE USER healthcare_dev WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE healthcare_dev_db TO healthcare_dev;

-- Create test database and user
CREATE DATABASE healthcare_test_db;
CREATE USER healthcare_test WITH PASSWORD 'test_password';
GRANT ALL PRIVILEGES ON DATABASE healthcare_test_db TO healthcare_test;

-- Create production database and user
CREATE DATABASE healthcare_prod_db;
CREATE USER healthcare_prod WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE healthcare_prod_db TO healthcare_prod;
```

### 3. Run Tests
```bash
# Run tests with PostgreSQL test database
./dev.sh test

# Or with Maven
mvn test -Dspring.profiles.active=test
```

### 4. Run Development Server
```bash
# Start development server
./dev.sh run

# Or with Maven
mvn spring-boot:run
```

## Environment Variables for Production

Set these environment variables in your production environment:

```bash
export DB_HOST=your-postgres-host
export DB_PORT=5432
export DB_NAME=healthcare_prod_db
export DB_USERNAME=healthcare_prod
export DB_PASSWORD=your_secure_password
```

## Enum Handling

PostgreSQL enums are created in the schema files:
- `test-schema.sql` - For testing
- `schema.sql` - For development/production

All enum values match between test and production:
- `status_enum`: ACTIVE, INACTIVE, SUSPENDED
- `gender_enum`: MALE, FEMALE, OTHER, UNKNOWN
- `role_enum`: PATIENT, PROVIDER
- `appointment_status_enum`: AVAILABLE, SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
- `appointment_type_enum`: REGULAR_CONSULTATION, FOLLOW_UP, NEW_PATIENT_INTAKE, PROCEDURE_CONSULTATION
- `record_type_enum`: DIAGNOSIS, TREATMENT, SUMMARY, LAB_RESULT, PRESCRIPTION, NOTE, OTHER
- `action_type_enum`: CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT
- `resource_type_enum`: USER_PROFILE, PATIENT_PROFILE, PROVIDER_PROFILE, APPOINTMENT, MEDICAL_RECORD
- `outcome_enum`: SUCCESS, FAILURE

## Troubleshooting

### Connection Issues
1. Ensure PostgreSQL is running: `brew services list | grep postgresql`
2. Check database exists: `psql -U postgres -l | grep healthcare`
3. Verify user permissions: `psql -U postgres -c "\du"`

### Enum Issues
1. Ensure enums are created before tables
2. Check enum values match between test and production
3. Verify JPA annotations use `@Enumerated(EnumType.STRING)`

### Test Failures
1. Ensure test database is clean: `DROP DATABASE healthcare_test_db; CREATE DATABASE healthcare_test_db;`
2. Check test profile is active: `mvn test -Dspring.profiles.active=test`
3. Verify test data is loaded correctly
