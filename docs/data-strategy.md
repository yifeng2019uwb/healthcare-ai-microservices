# Healthcare AI Microservices - Data Strategy

> **Clear, Consistent Data Architecture** - Resolving all contradictions

## 📋 **Document Information**

- **Document Title**: Data Strategy for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2025-09-01
- **Author**: Healthcare AI Team
- **Status**: Final

## 🎯 **Executive Summary**

### **Data Strategy Overview**
Our healthcare AI microservices platform uses a clear, industry-standard data architecture:
- ✅ **Single Neon PostgreSQL** for all business data
- ✅ **Supabase for authentication** (external auth provider)
- ✅ **No auth tables in our database** (auth handled externally)
- ✅ **Industry-standard separate profile tables** for scalability

## 🏗️ **Consistent Data Strategy**

### **1. Database Strategy**
- **Primary Database**: Neon PostgreSQL for all business data
- **User-Related Tables**: Business user data (profiles, roles, permissions) stored in Neon
- **No Authentication Tables**: Login credentials, passwords, auth sessions NOT stored
- **Clear Separation**: Auth ≠ User Data

### **2. Data Storage Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    HEALTHCARE PLATFORM                      │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │              BUSINESS DATA IN NEON DB                   │ │
│ │                                                         │ │
│ │ • User profiles (common identity)                      │ │
│ │ • Patient profiles (medical-specific)                  │ │
│ │ • Provider profiles (professional-specific)            │ │
│ │ • Appointments (scheduling)                            │ │
│ │ • Medical records (unified medical data)               │ │

│ │ • Audit logs (compliance tracking)                     │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🗄️ **Database Schema Strategy**

### **What Goes in Neon Database**
✅ **User Business Data**:
- User profiles (name, contact info, preferences)
- User roles and permissions for business logic
- Patient records and medical history
- Provider profiles and schedules
- Appointment data and scheduling
- Medical records
- Audit logs and business events

❌ **What Does NOT Go in Neon Database**:
- User login credentials (passwords)
- Authentication sessions
- JWT token storage
- Login/logout history
- Password reset tokens
- MFA secrets

### **Database Schema Structure (Industry Standard)**

#### **Table Relationships & Interactions:**
```
┌─────────────────────────────────────────────────────────────────┐
│                    USER PROFILES (Core Identity)                │
│  • id (UUID PK)                                                │
│  • Common fields: name, email, phone, address, role             │
│  • Flexible data storage (see strategy below)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴───────────────┐
                    │ 1:1                     │ 1:1
                    │ user_id:patient_id      │ user_id:provider_id
                    │                         │
        ┌───────────▼───────────┐      ┌▼─────────────────────┐
        │   PATIENT PROFILES    │      │  PROVIDER PROFILES   │
        │  • id (UUID PK)       │      │  • id (UUID PK)      │
        │  • user_id (FK)       │      │  • user_id (FK)      │
        │  • Medical history    │      │  • Specialty         │
        │  • Allergies          │      │  • License number    │
        │  • Patient number     │      │  • Qualifications    │
        └───────────────────────┘      └─────────────────────┘
                    │ 1:M                       │ 1:M
                    │ patient_id                │ provider_id
                    │                           │
                    └───────────┬───────────────┘
                                │
                    ┌───────────▼───────────┐
                    │     APPOINTMENTS      │
                    │  • id (UUID PK)       │
                    │  • patient_id (FK)    │
                    │  • provider_id (FK)   │
                    │  • Scheduling         │
                    │  • Status tracking    │
                    └───────────────────────┘
                                │ 1:1 or 1:M
                                │ appointment_id
                    ┌───────────▼───────────┐
                    │   MEDICAL RECORDS     │
                    │  • id (UUID PK)       │
                    │  • appointment_id (FK)│
                    │  • patient_id (FK)    │
                    │  • provider_id (FK)   │
                    │  • record_type (enum) │
                    │  • content (JSON/text)│
                    │  • is_patient_visible │
                    │  • release_date       │
                    └───────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                    AUDIT LOGS (HIPAA Compliance)                │
│  • id (UUID PK)                                                │
│  • timestamp (TIMESTAMPTZ) - Event time with timezone          │
│  • user_id (UUID FK) - Who performed the action                │
│  • action_type (ENUM) - Standardized action description        │
│  • resource_type (VARCHAR) - Type of resource acted upon       │
│  • resource_id (UUID) - Specific resource identifier           │
│  • outcome (ENUM) - Success/failure result                     │
│  • details (JSONB) - Additional unstructured details           │
│  • source_ip (CIDR) - Request origin IP address                │
│  • application (VARCHAR) - Microservice/application name       │
└─────────────────────────────────────────────────────────────────┘
```

#### **Core Tables:**
- **`user_profiles`**: Core identity and common fields for all users
- **`patient_profiles`**: Medical-specific data (history, allergies, patient number)
- **`provider_profiles`**: Professional-specific data (specialty, license, qualifications)
- **`appointments`**: Scheduling between patients and providers
- **`medical_records`**: Medical data per appointment with record types (diagnosis, treatment, summary, lab_result, etc.)

- **`audit_logs`**: HIPAA-compliant audit trail with detailed tracking (user actions, resource access, outcomes, IP addresses, timestamps)

#### **Flexible Data Storage Strategy:**
- **Single JSON Column**: `custom_data` for all flexible data storage
- **Healthcare Compliance**: Strict data validation and structured JSON schemas
- **Cost Model**: NULL values cost nothing, only pay for actual data stored
- **Benefits**:
  - Single source of truth for flexible data
  - Consistent validation and schema enforcement
  - Easier to maintain and audit
  - Healthcare-grade data integrity

> **📋 Detailed Schema**: See [Database Design](database-design.md) for complete table definitions, column specifications, and constraints.
> **📅 Data Lifecycle**: See [Data Archive Strategy](data-archive-strategy.md) for future data retention and archiving policies.

### **Industry Standard Design Benefits**

This database design follows healthcare industry standards (Epic, Cerner, Allscripts) with these advantages:

#### **✅ Scalability & Future-Proofing**
- **Easy Role Expansion**: Add new roles (ADMIN, NURSE, THIRD_PARTY) without schema changes
- **Clean Separation**: Profile data vs. visit/medical records clearly separated
- **No Data Duplication**: Each piece of data stored in one place only

#### **✅ Data Organization**
- **`user_profiles`**: Core identity and common fields for all users
- **`patient_profiles`**: Medical-specific data (history, allergies, patient number)
- **`provider_profiles`**: Professional-specific data (specialty, license, qualifications)
- **`appointments`**: Scheduling between patients and providers
- **`medical_records`**: Medical data per appointment with record types


#### **✅ Security & Compliance**
- **Role-Based Access**: Clear separation enables proper RBAC implementation
- **Data Isolation**: Sensitive medical data properly isolated by role
- **HIPAA Audit Trail**: Comprehensive logging of all data access and modifications
  - **User Tracking**: Who accessed what data and when
  - **Resource Tracking**: Specific records and actions performed
  - **Outcome Tracking**: Success/failure of all operations
  - **Source Tracking**: IP addresses and application sources
  - **Forensic Analysis**: Timezone-aware timestamps for compliance reporting
- **HIPAA Compliance Requirements**:
  - **6-Year Retention**: Audit logs must be retained for minimum 6 years
  - **Log Integrity**: Tamper-evident storage with encryption
  - **Regular Review**: Automated monitoring for unauthorized access
  - **Comprehensive Coverage**: All ePHI access must be logged
  - **State Regulations**: Compliance with state-specific health data laws
  - **Data Lifecycle Management**: See [Data Archive Strategy](data-archive-strategy.md) for HIPAA-compliant data retention and archiving policies

#### **✅ Performance**
- **Optimized Queries**: No unnecessary JOINs for basic profile data
- **Focused Indexes**: Each table can have role-specific indexes
- **Efficient Caching**: Profile data can be cached separately from visit data

#### **✅ Flexible Data Storage**
- **Cost-Neutral**: Single JSON column costs nothing when unused (NULL values)
- **Future-Proof**: Can store any data structure without schema changes
- **Healthcare Compliance**: Strict validation and structured JSON schemas
- **API Flexibility**: Support for unknown data structures and requirements
- **Single Source of Truth**: One column per table for all flexible data

## 🏥 **HIPAA Compliance Strategy**

### **Protected Health Information (PHI) Handling**
- **Data Classification**: All medical records, patient profiles, and appointment data classified as ePHI
- **Access Controls**: Role-based access with minimum necessary principle
- **Data Encryption**: All ePHI encrypted at rest and in transit
- **Secure Storage**: Neon PostgreSQL with encryption and access controls

### **Audit & Monitoring Requirements**
- **Comprehensive Logging**: All ePHI access logged in audit_logs table
- **6-Year Retention**: Audit logs retained for minimum 6 years per HIPAA
- **Log Integrity**: Tamper-evident storage with cryptographic hashing
- **Regular Review**: Automated monitoring and alerting for suspicious activity
- **Breach Detection**: Real-time monitoring for unauthorized access patterns

### **Data Minimization & Purpose Limitation**
- **Minimum Necessary**: Only collect data required for healthcare operations
- **Purpose Limitation**: Data used only for stated healthcare purposes
- **Data Retention**: Clear policies for data lifecycle management (see [Data Archive Strategy](data-archive-strategy.md) for detailed retention policies)
- **Right to Access**: Patients can request access to their PHI
- **Right to Amendment**: Patients can request corrections to their PHI

## 🔗 **Data Integration Points**

### **Internal Data Access**
- **Business Services**: Direct access to Neon database with audit logging
- **Shared Data Layer**: Common database access patterns with compliance controls
- **Data Validation**: Strict healthcare compliance requirements and PHI protection

---

## 🔗 **Related Documents**

- [System Design](system-design.md) - Overall architecture
- [Database Design](database-design.md) - Detailed table specifications
- [Data Archive Strategy](data-archive-strategy.md) - Future data lifecycle management and archiving

---

*This document resolves all data strategy contradictions and provides a clear, consistent approach for the healthcare AI microservices platform.*
