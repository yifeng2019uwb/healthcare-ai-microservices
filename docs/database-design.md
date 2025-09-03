# Healthcare AI Microservices - Database Design

> **🎯 Professional Healthcare Service: Simple but Compliant**
>
> This document defines the database structure for the healthcare AI microservices platform.
> **Design Philosophy**: Keep it simple while maintaining healthcare compliance requirements.

## 📋 **Document Information**

- **Document Title**: Database Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Final

## 🎯 **Overview**

### **What This Is**
This document defines the database schema design for the healthcare AI microservices platform, focusing on simplicity while maintaining healthcare compliance requirements.

### **Why This Matters**
Database design is foundational for all services. A well-designed schema ensures data integrity, performance, and compliance with healthcare regulations like HIPAA.

### **Scope**
- **In Scope**: Core table structure, relationships, basic field definitions, audit requirements
- **Out of Scope**: Detailed field constraints, indexes, complex medical data, advanced healthcare features

## 🏗️ **High-Level Design**

### **Core Design Principles**
1. **Simplicity First**: Easy to understand and implement
2. **Healthcare Compliance**: Must support HIPAA audit requirements
3. **Clear Separation**: Authentication vs. business logic vs. audit
4. **One Role Per Account**: Patients and providers are separate entities
5. **Extensible Foundation**: Can add complexity later when needed

### **Table Structure Overview**
```
🗄️ Core Tables Structure (6 tables total)

Identity & Profiles (3 tables)
├── user_profiles          - Core identity and common fields for all users
├── patient_profiles       - Medical-specific data (history, allergies, patient number)
└── provider_profiles      - Professional-specific data (specialty, license, qualifications)

Healthcare Operations (2 tables)
├── appointments          - Scheduling between patients and providers
└── medical_records      - Unified medical data per appointment

Support Systems (1 table)
└── audit_logs          - Comprehensive audit trail for HIPAA compliance
```

> **📋 Data Strategy**: See [Data Strategy](data-strategy.md) for complete table relationships and architecture overview.

## 🔗 **Table Relationships & Design Decisions**

### **1. Identity & Authentication Tables**

#### **user_profiles Table**
- **Purpose**: Core user identity and basic information
- **Key Design Decision**: One account = one role (patient OR provider)
- **Why This Approach**:
  - ✅ **Simple**: Clear role boundaries, easy to implement
  - ✅ **Secure**: Clear access control for healthcare data
  - ✅ **Auditable**: Easy to track who accessed what
  - ❌ **Limitation**: Can't be both patient and provider (realistic for most cases)


#### **patients & providers Tables**
- **Purpose**: Role-specific data for patients and providers
- **Key Design Decision**: Separate tables instead of polymorphic approach
- **Why This Approach**:
  - ✅ **Simple**: Clear data structure, easy to query
  - ✅ **Performance**: No complex joins or type checking
  - ✅ **Extensible**: Easy to add role-specific fields
  - ❌ **Limitation**: Can't be both patient and provider (acceptable trade-off)

**Table Purpose Clarification**:
- **`user_profiles`**: Basic identity info (name, email, role, status)
- **`patients`**: Patient service business data (medical history, allergies, assigned providers)
- **`providers`**: Provider service business data (specialties, certifications, ratings)

### **2. Healthcare Operations Tables**

#### **appointments Table**
- **Purpose**: Scheduling between patients and providers
- **Key Design Decision**: Many-to-many relationship between patients and providers
- **Why This Approach**:
  - ✅ **Realistic**: Reflects actual healthcare scheduling
  - ✅ **Flexible**: One patient can see multiple providers, one provider can see multiple patients
  - ✅ **Simple**: Standard M:N relationship pattern

#### **medical_records Table**
- **Purpose**: Basic medical data per appointment
- **Key Design Decision**: Multiple records per appointment, keep it simple
- **Why This Approach**:
  - ✅ **Realistic**: Multiple medical records per visit is normal
  - ✅ **Simple**: Basic structure, easy to implement
  - ✅ **Extensible**: Can add complexity later
  - ❌ **Limited**: No ongoing conditions, medications, lab results (acceptable for MVP)

### **3. Support Systems Tables**

#### **audit_logs Table**
- **Purpose**: Comprehensive audit trail for HIPAA compliance
- **Key Design Decision**: Log everything for compliance
- **Why This Approach**:
  - ✅ **HIPAA Compliance**: Required for healthcare applications
  - ✅ **Security**: Track all data access and modifications
  - ✅ **Debugging**: Helpful for development and troubleshooting

## 📊 **Detailed Table Structure**

> **📅 Note**: For future archive strategy implementation, consider adding these fields to high-volume tables:
> - `is_archived BOOLEAN DEFAULT FALSE`
> - `archived_at TIMESTAMP WITH TIME ZONE`
> - `archived_by VARCHAR(255)`
>
> See [Data Archive Strategy](data-archive-strategy.md) for detailed implementation plan.

### **Table 1: user_profiles**

| Column Name      | Data Type | Constraints         | Index           | Description |
|------------------|-----------|---------------------|-----------------|-------------|-
| id               | UUID         | PK, NOT NULL     | PRIMARY KEY     | Primary key identifier |
| auth_id          | VARCHAR(255) | NOT NULL         | UNIQUE INDEX | External auth provider ID (Supabase, Auth0, etc.) |
| first_name       | VARCHAR(100) | NOT NULL         | COMPOSITE INDEX | User's first name |
| last_name        | VARCHAR(100) | NOT NULL         | COMPOSITE INDEX | User's last name |
| email            | VARCHAR(255) | UNIQUE, NOT NULL | UNIQUE INDEX    | User's email address |
| phone            | VARCHAR(20)  | NOT NULL         | INDEX           | User's phone number |
| date_of_birth    | DATE         | NOT NULL         | COMPOSITE INDEX | User's date of birth |
| gender           | ENUM         | NOT NULL         | -               | User's gender (MALE, FEMALE, OTHER, UNKNOWN) |
| street_address   | VARCHAR(255) | -                | -               | Street address |
| city             | VARCHAR(100) | -                | -               | City name |
| state            | VARCHAR(50)  | -                | -               | State/Province |
| postal_code      | VARCHAR(20)  | -                | -               | Postal/ZIP code |
| country          | VARCHAR(50)  | -                | -               | Country name |
| role             | ENUM         | NOT NULL         | -               | PATIENT or PROVIDER |
| status           | ENUM         | NOT NULL         | -               | ACTIVE, INACTIVE, SUSPENDED |
| custom_data      | JSONB        | -                | -               | Flexible data storage |
| created_at       | TIMESTAMPTZ  | NOT NULL         | -               | Record creation timestamp (timezone-aware) |
| updated_at       | TIMESTAMPTZ  | NOT NULL         | -               | Record update timestamp (timezone-aware) |-

#### **Composite Index Definition:**
```sql
CREATE INDEX idx_user_name_dob ON user_profiles (last_name, first_name, date_of_birth);
```

#### **Timezone-Aware Timestamps:**
```sql
-- All timestamp fields use TIMESTAMPTZ for global compatibility
created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**Why TIMESTAMPTZ is Critical:**
- ✅ **Global Application**: Supports users across different time zones
- ✅ **HIPAA Compliance**: Accurate audit trails regardless of user location
- ✅ **Legal Requirements**: Precise timestamps for healthcare records
- ✅ **Data Consistency**: No timezone conversion issues
- ✅ **Future-Proof**: Ready for international expansion

#### **Key Design Decisions:**
- **External Auth Integration**: `auth_id` links to external auth provider
- **Common Profile Fields**: All users share basic identity information
- **Structured Address**: Separate columns for better querying and indexing
- **Role-Agnostic Design**: No patient-specific fields (emergency contacts moved to patient_profiles)
- **Custom Data**: JSONB column for future extensibility
- **No Password Fields**: Authentication handled externally



### **Table 2: patient_profiles**

| Column Name            | Data Type    | Constraints  | Index | Description |
|------------------------|--------------|--------------|-------|-------------|
| id                     | UUID         | PK, NOT NULL | PRIMARY KEY | Primary key identifier |
| user_id                | UUID         | FK, NOT NULL | UNIQUE INDEX | Foreign key to user_profiles.id |
| patient_number         | VARCHAR(50)  | UNIQUE, NOT NULL | UNIQUE INDEX | Unique patient identifier |
| medical_history        | JSONB        | -            | -     | Patient's medical history |
| allergies              | JSONB        | -            | -     | Patient's allergies and reactions |
| emergency_contact_name | VARCHAR(100) | -            | -     | Emergency contact name |
| emergency_contact_phone| VARCHAR(20)  | -            | -     | Emergency contact phone |
| insurance_provider     | VARCHAR(100) | -            | -     | Insurance company name |
| insurance_policy_number| VARCHAR(50)  | -            | -     | Insurance policy number |
| primary_care_physician | VARCHAR(100) | -            | -     | Primary care physician name |
| custom_data            | JSONB        | -            | -     | Flexible data storage |
| created_at             | TIMESTAMPTZ  | NOT NULL     | -     | Record creation timestamp (timezone-aware) |
| updated_at             | TIMESTAMPTZ  | NOT NULL     | -     | Record update timestamp (timezone-aware) |

#### **Foreign Key Constraint:**
```sql
ALTER TABLE patient_profiles
ADD CONSTRAINT fk_patient_user_id
FOREIGN KEY (user_id) REFERENCES user_profiles(id) ON DELETE CASCADE;
```

#### **Key Design Decisions:**
- **1:1 Relationship**: Each user can have only one patient profile
- **Patient-Specific Data**: Medical history, allergies, emergency contacts
- **Healthcare Compliance**: Emergency contacts required for patient care
- **Insurance Information**: Support for insurance verification
- **Flexible Medical Data**: JSONB for complex medical information
- **Timezone-Aware**: All timestamps use TIMESTAMPTZ

### **Table 3: provider_profiles**

| Column Name      | Data Type    | Constraints  | Index           | Description |
|------------------|--------------|--------------|-----------------|-------------|
| id               | UUID         | PK, NOT NULL | PRIMARY KEY     | Primary key identifier |
| user_id          | UUID         | FK, NOT NULL | UNIQUE INDEX    | Foreign key to user_profiles.id |
| license_numbers  | VARCHAR(50)   | -            | -               | State medical license number |
| npi_number       | VARCHAR(10)  | UNIQUE, NOT NULL | UNIQUE INDEX | National Provider Identifier (NPI) |
| specialty        | VARCHAR(100) | -            | INDEX           | Primary medical specialty |
| qualifications   | TEXT         | -            | -               | Medical qualifications and education |
| bio              | TEXT         | -            | -               | Provider biography |
| office_phone     | VARCHAR(20)  | -            | -               | Office phone number |
| custom_data      | JSONB        | -            | -               | Flexible data storage |
| created_at       | TIMESTAMPTZ  | NOT NULL     | -               | Record creation timestamp (timezone-aware) |
| updated_at       | TIMESTAMPTZ  | NOT NULL     | -               | Record update timestamp (timezone-aware) |

#### **Foreign Key Constraint:**
```sql
ALTER TABLE provider_profiles
ADD CONSTRAINT fk_provider_user_id
FOREIGN KEY (user_id) REFERENCES user_profiles(id) ON DELETE CASCADE;
```

#### **Key Design Decisions:**
- **1:1 Relationship**: Each user can have only one provider profile
- **Professional Data**: License, specialty, qualifications, bio
- **Office Contact**: Office phone (location in user_profiles)
- **Availability via Appointments**: Provider availability managed through appointment slots
- **Flexible Data**: JSONB for future extensibility
- **Timezone-Aware**: All timestamps use TIMESTAMPTZ

### **Table 4: appointments**

| Column Name      | Data Type    | Constraints  | Index           | Description |
|------------------|--------------|--------------|-----------------|-------------|
| id               | UUID         | PK, NOT NULL | PRIMARY KEY     | Primary key identifier |
| patient_id       | UUID         | FK, -        | INDEX           | Foreign key to patient_profiles.id (null if not booked) |
| provider_id      | UUID         | FK, NOT NULL | COMPOSITE INDEX | Foreign key to provider_profiles.id |
| scheduled_at     | TIMESTAMPTZ  | NOT NULL     | COMPOSITE INDEX | Appointment date and time (timezone-aware) |
| status           | ENUM         | NOT NULL     | INDEX           | AVAILABLE, SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW |
| appointment_type | ENUM         | NOT NULL     | -               | REGULAR_CONSULTATION (30min), FOLLOW_UP (15min), NEW_PATIENT_INTAKE (60min), PROCEDURE_CONSULTATION (45min) |
| notes            | TEXT         | -            | -               | Appointment notes |
| custom_data      | JSONB        | -            | -               | Flexible data storage |
| created_at       | TIMESTAMPTZ  | NOT NULL     | -               | Record creation timestamp (timezone-aware) |
| updated_at       | TIMESTAMPTZ  | NOT NULL     | -               | Record update timestamp (timezone-aware) |

#### **Foreign Key Constraints:**
```sql
ALTER TABLE appointments
ADD CONSTRAINT fk_appointment_patient_id
FOREIGN KEY (patient_id) REFERENCES patient_profiles(id) ON DELETE CASCADE;

ALTER TABLE appointments
ADD CONSTRAINT fk_appointment_provider_id
FOREIGN KEY (provider_id) REFERENCES provider_profiles(id) ON DELETE CASCADE;
```

#### **Composite Index Definition:**
```sql
CREATE INDEX idx_appointment_provider_schedule ON appointments (provider_id, scheduled_at);
```

#### **Overlap Prevention:**
```sql
-- Prevent overlapping appointments for the same provider
-- Different durations: 15min, 30min, 45min, 60min based on appointment_type
CREATE UNIQUE INDEX idx_no_overlap_appointments
ON appointments (provider_id, scheduled_at)
WHERE status NOT IN ('CANCELLED', 'NO_SHOW');

-- Application logic must validate no time conflicts
-- Check: scheduled_at + appointment_type_duration doesn't overlap with existing appointments
```

#### **Key Design Decisions:**
- **Appointment Types**: ENUM with predefined durations (15, 30, 45, 60 minutes)
- **Appointment Slots**: Providers create available slots (status = 'AVAILABLE', patient_id = null)
- **Patient Booking**: Patients book available slots (status = 'SCHEDULED', patient_id populated)
- **Status Flow**: AVAILABLE → SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED
- **Timezone-Aware Scheduling**: All appointment times use TIMESTAMPTZ
- **Overlap Prevention**: Application logic validates no time conflicts
- **Flexible Data**: JSONB for future extensibility

### **Table 5: medical_records**

| Column Name | Data Type  | Constraints | Index | Description |
|-------------|------------|-------------|-------|-------------|
| id          | UUID       | PK, NOT NULL | PRIMARY KEY | Primary key identifier |
| appointment_id | UUID    | FK, NOT NULL | COMPOSITE INDEX | Foreign key to appointments.id (visit identifier) |
| record_type | ENUM       | NOT NULL | COMPOSITE INDEX | DIAGNOSIS, TREATMENT, SUMMARY, LAB_RESULT, PRESCRIPTION, NOTE, OTHER |
| content     | TEXT       | NOT NULL | - | Medical record content/details |
| is_patient_visible       | BOOLEAN  | NOT NULL | - | Whether patient can view this record |
| release_date | TIMESTAMPTZ | -      | - | When record becomes visible to patient |
| custom_data | JSONB      | -        | - | Flexible data storage |
| created_at | TIMESTAMPTZ | NOT NULL | - | Record creation timestamp (timezone-aware) |
| updated_at | TIMESTAMPTZ | NOT NULL | - | Record update timestamp (timezone-aware) |

#### **Foreign Key Definitions:**
```sql
ALTER TABLE medical_records
ADD CONSTRAINT fk_medical_record_appointment_id
FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE;
```

#### **Index Definition:**
```sql
CREATE INDEX idx_medical_record_appointment_type ON medical_records (appointment_id, record_type);
```

#### **Key Design Decisions:**
- **Visit-Based Records**: All medical records linked to appointments (visits)
- **Patient/Provider via Appointment**: Get patient and provider info through appointment relationship
- **Patient Visibility Control**: `is_patient_visible` and `release_date` for privacy
- **Record Types**: ENUM for standardized medical record categories
- **Simplified Relationships**: Single foreign key to appointments table
- **Timezone-Aware**: All timestamps use TIMESTAMPTZ
- **Flexible Data**: JSONB for future extensibility

### **Table 6: audit_logs**

| Column Name | Data Type | Constraints | Index | Description |
|-------------|-----------|-------------|-------|-------------|
| id          | UUID   | PK, NOT NULL | PRIMARY KEY | Primary key identifier |
| user_id     | UUID   | FK, NOT NULL | INDEX | Foreign key to user_profiles.id |
| action_type | ENUM   | NOT NULL | INDEX | CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT |
| resource_type | ENUM | NOT NULL | INDEX | USER_PROFILE, PATIENT_PROFILE, PROVIDER_PROFILE, APPOINTMENT, MEDICAL_RECORD |
| resource_id | UUID   | - | INDEX | ID of the resource being acted upon (nullable for system actions) |
| outcome     | ENUM   | NOT NULL | INDEX | SUCCESS, FAILURE |
| details     | JSONB  | - | - | Additional action details |
| source_ip   | INET   | - | - | IP address of the request |
| user_agent  | TEXT   | - | - | User agent string |
| created_at  | TIMESTAMPTZ | NOT NULL | INDEX | Action timestamp (timezone-aware) |

#### **Foreign Key Definitions:**
```sql
ALTER TABLE audit_logs
ADD CONSTRAINT fk_audit_user_id
FOREIGN KEY (user_id) REFERENCES user_profiles(id) ON DELETE CASCADE;
```

#### **Key Design Decisions:**
- **Comprehensive Logging**: All user actions logged for HIPAA compliance
- **Resource Tracking**: Track actions on specific resources
- **Outcome Tracking**: Success/failure for security monitoring
- **IP Tracking**: Source IP for security analysis
- **Timezone-Aware**: All timestamps use TIMESTAMPTZ
- **Flexible Details**: JSONB for additional context

## 🔄 **Future Scaling & Refactoring Considerations**

### **Short Term **
- **Keep Current Structure**: Focus on implementing basic CRUD operations
- **Simple Queries**: Use basic SQL, avoid complex optimizations
- **Basic Validation**: Simple field validation, no complex business rules

### **Medium Term (Feature Enhancement)**
- **Add Indexes**: Performance optimization for common queries
- **Enhanced Validation**: Business rule validation and constraints
- **Data Archiving**: Move old data to archive tables (see [Data Archive Strategy](data-archive-strategy.md))
- **Partitioning**: Split large tables by date for performance

### **Long Term (Production Scaling)**
- **Database Sharding**: Split by service or geographic region
- **Read Replicas**: Separate read/write operations
- **Advanced Caching**: Redis for frequently accessed data
- **Event Sourcing**: Kafka for audit and change tracking

### **Potential Refactoring Points**
1. **Role Management**: If need for multiple roles per user
2. **Medical Data**: Add tables for medications, lab results, conditions
3. **File Storage**: Move to dedicated file service with metadata
4. **Audit System**: Implement event sourcing for better audit trails
5. **Data Archiving**: Implement comprehensive archive strategy for performance and compliance

## ⚠️ **Risks & Considerations**

### **Technical Risks**
- **Simple Schema**: May need refactoring as requirements grow
- **Audit Performance**: Large audit_logs table may impact performance
- **File Storage**: File metadata stored in custom_data JSONB may need validation

### **Mitigation Strategies**
- **Regular Reviews**: Review schema design as requirements evolve
- **Audit Archiving**: Archive old audit logs to maintain performance
- **Validation**: Add constraints to prevent invalid custom_data JSONB values

## 📋 **Success Criteria**

- [ ] All 6 tables can be created in Neon PostgreSQL
- [ ] Basic CRUD operations work for each table
- [ ] Foreign key relationships are properly enforced
- [ ] Audit logging captures all data access
- [ ] Schema supports basic healthcare operations

## 🔗 **Related Documents**

- [Data Strategy](data-strategy.md) - Overall data approach
- [System Design](system-design.md) - Overall architecture
- [Authentication Design](authentication-design.md) - Auth strategy

---

## 📝 **Design Decisions Summary**

### **Why We Chose This Design**
1. **Simplicity**: Easy to understand and implement
2. **Healthcare Compliance**: Supports HIPAA audit requirements
3. **Clear Structure**: Logical separation of concerns
4. **Extensible**: Can add complexity when needed
5. **Realistic**: Reflects actual healthcare business logic

### **Trade-offs Made**
- **One Role Per Account**: Simplicity over flexibility
- **Basic Medical Records**: MVP focus over medical complexity
- **Simple Audit**: Basic logging over advanced event sourcing
- **Flexible Data Storage**: Simple JSONB approach over complex structured fields

### **Future Considerations**
- **Monitor Performance**: Watch for slow queries as data grows
- **Plan Refactoring**: Identify when schema needs to change
- **Add Complexity Gradually**: Don't over-engineer early
- **Keep MVP Focus**: Remember this is for building a working healthcare service

---

*This database design provides a solid, compliant foundation that's simple enough to implement but realistic enough for healthcare applications.*
