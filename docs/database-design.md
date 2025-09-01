# Healthcare AI Microservices - Database Design

> **🎯 Learning Focus: Simple but Healthcare-Compliant**
>
> This document defines the database structure for the healthcare AI microservices platform.
> **Design Philosophy**: Keep it simple for learning while maintaining healthcare compliance requirements.

## 📋 **Document Information**

- **Document Title**: Database Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Final

## 🎯 **Overview**

### **What This Is**
This document defines the database schema design for the healthcare AI microservices platform, focusing on simplicity for learning while maintaining healthcare compliance requirements.

### **Why This Matters**
Database design is foundational for all services. A well-designed schema ensures data integrity, performance, and compliance with healthcare regulations like HIPAA.

### **Scope**
- **In Scope**: Core table structure, relationships, basic field definitions, audit requirements
- **Out of Scope**: Detailed field constraints, indexes, complex medical data, advanced healthcare features

## 🏗️ **High-Level Design**

### **Core Design Principles**
1. **Simplicity First**: Easy to understand and implement for learning
2. **Healthcare Compliance**: Must support HIPAA audit requirements
3. **Clear Separation**: Authentication vs. business logic vs. audit
4. **One Role Per Account**: Patients and providers are separate entities
5. **Extensible Foundation**: Can add complexity later when needed

### **Table Structure Overview**
```
🗄️ Core Tables Structure (6 tables total)

Identity & Authentication (2 tables)
├── user_profiles          - Basic user information and role
├── patients              - Patient-specific data (if role = PATIENT)
└── providers             - Provider-specific data (if role = PROVIDER)

Healthcare Operations (2 tables)
├── appointments          - Scheduling between patients and providers
└── medical_records      - Basic medical data per appointment

Support Systems (2 tables)
├── file_metadata        - File storage with ownership tracking
└── audit_logs          - Comprehensive audit trail for HIPAA compliance
```

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
  - ❌ **Limited**: No ongoing conditions, medications, lab results (acceptable for learning)

### **3. Support Systems Tables**

#### **file_metadata Table**
- **Purpose**: File storage with ownership tracking
- **Key Design Decision**: Flexible ownership with owner_type field
- **Why This Approach**:
  - ✅ **Flexible**: Files can belong to patients, providers, or appointments
  - ✅ **Auditable**: Track file ownership and access
  - ✅ **Simple**: Single table for all file metadata

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
```
id (UUID PK)
username (email, unique)
notification_email
first_name, last_name
role (PATIENT/PROVIDER/ADMIN)
status (ACTIVE/INACTIVE/SUSPENDED)
created_at, updated_at
```



### **Table 2: patients**
```
id (UUID PK)
user_id (UUID FK → user_profiles.id)
patient_number (unique)
medical_history (JSON)
allergies (JSON)
assigned_providers (UUID array)
health_goals (JSON)
risk_factors (JSON)
created_at, updated_at
```

### **Table 3: providers**
```
id (UUID PK)
user_id (UUID FK → user_profiles.id)
license_number (unique)
specialties (array)
certifications (JSON)
rating, total_reviews
is_verified (boolean)
created_at, updated_at
```

### **Table 4: appointments**
```
id (UUID PK)
patient_id (UUID FK → patients.id)
provider_id (UUID FK → providers.id)
scheduled_at
status (SCHEDULED/COMPLETED/CANCELLED)
duration_minutes
location (JSON)
notes
created_at, updated_at
```

### **Table 5: medical_records**
```
id (UUID PK)
patient_id (UUID FK → patients.id)
provider_id (UUID FK → providers.id)
appointment_id (UUID FK → appointments.id)
diagnosis
treatment
notes
created_at, updated_at
```

### **Table 6: file_metadata**
```
id (UUID PK)
owner_id (UUID FK → user_profiles.id)
owner_type (PATIENT/PROVIDER/APPOINTMENT)
file_path
file_type
file_size
upload_date
created_at, updated_at
```

### **Table 7: audit_logs**
```
id (UUID PK)
user_id (UUID FK → user_profiles.id)
resource_type (PATIENT/PROVIDER/APPOINTMENT/MEDICAL_RECORD/FILE)
resource_id (UUID)
action (CREATE/READ/UPDATE/DELETE)
timestamp
ip_address
user_agent
details (JSON)
```

## 🔄 **Future Scaling & Refactoring Considerations**

### **Short Term (Learning Phase)**
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
- **File Ownership**: owner_type string field may be fragile

### **Mitigation Strategies**
- **Regular Reviews**: Review schema design as requirements evolve
- **Audit Archiving**: Archive old audit logs to maintain performance
- **Validation**: Add constraints to prevent invalid owner_type values

## 📋 **Success Criteria**

- [ ] All 7 tables can be created in Neon PostgreSQL
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
1. **Simplicity**: Easy to understand and implement for learning
2. **Healthcare Compliance**: Supports HIPAA audit requirements
3. **Clear Structure**: Logical separation of concerns
4. **Extensible**: Can add complexity when needed
5. **Realistic**: Reflects actual healthcare business logic

### **Trade-offs Made**
- **One Role Per Account**: Simplicity over flexibility
- **Basic Medical Records**: Learning focus over medical complexity
- **Simple Audit**: Basic logging over advanced event sourcing
- **Flexible File Ownership**: Simple approach over complex ownership models

### **Future Considerations**
- **Monitor Performance**: Watch for slow queries as data grows
- **Plan Refactoring**: Identify when schema needs to change
- **Add Complexity Gradually**: Don't over-engineer early
- **Keep Learning Focus**: Remember this is for learning microservices

---

*This database design provides a solid, compliant foundation that's simple enough to implement but realistic enough for healthcare applications.*
