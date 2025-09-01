# Provider Service Design

> **🎯 Learning Focus: Provider Management for Healthcare Microservices**
>
> This document defines the design for the Provider Service.
> **Design Philosophy**: Comprehensive provider management with healthcare compliance.

## 📋 **Document Information**

- **Document Title**: Provider Service Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Draft
- **Service**: Provider Service
- **Port**: 8003

## 🎯 **Overview**

### **What This Service Is**
The Provider Service manages all provider-related data and operations, including doctor profiles, medical specialties, schedules, availability, and provider-patient relationships. It serves as the central hub for healthcare provider management in the platform.

### **Business Value & Impact**
Provider data is essential for healthcare operations, enabling appointment scheduling, patient-provider matching, and clinical decision support. This service ensures secure, compliant, and efficient management of healthcare provider information.

### **Scope**
- **In Scope**: Provider registration, profile management, medical specialties, credentials, provider discovery, medical records management
- **Out of Scope**: Appointment scheduling (handled by Appointment Service), availability management (handled by Appointment Service), billing and payments, user authentication (handled by Auth Service)

## 📚 **Definitions & Glossary**

### **Key Terms**
- **HIPAA**: Health Insurance Portability and Accountability Act - federal law protecting patient health information
- **Provider**: Healthcare professional (doctor, nurse, specialist) who provides medical care
- **Medical Specialty**: Specific area of medical expertise (cardiology, pediatrics, etc.)
- **Medical Records**: Clinical documentation of patient care and treatment
- **Provider-Patient Relationship**: Professional relationship between provider and patient

## 👥 **User Stories**

### **Primary User Types**
- **Providers**: Healthcare professionals who manage their profiles and patient care
- **Patients**: Individuals who need to find and connect with providers
- **System Administrators**: Platform administrators who manage provider accounts

### **User Stories**

#### **User Case 1: Provider Registration**
Healthcare providers need to register on the platform to offer their services. They should be able to create a professional profile with their credentials, specialties, and practice information to start accepting patients.

#### **User Case 2: Profile Management**
Providers need to maintain their professional profiles, including updating their specialties, certifications, and practice information. This helps patients find the right provider for their healthcare needs.

#### **User Case 3: Provider Discovery**
Patients need to find and discover healthcare providers based on specialties, location, and credentials. This helps patients make informed decisions about their healthcare providers.

#### **User Case 4: Medical Records Management**
Providers need to create, view, and update medical records for their patients, including diagnoses, treatments, medications, and clinical notes to maintain comprehensive patient care documentation.

#### **User Case 5: Credential Verification**
System administrators need to verify provider credentials and professional licenses to ensure only qualified healthcare professionals are active on the platform.

## 🔧 **Solution Alternatives**

### **Current System Infrastructure & Data (Available to All Services)**

#### **Shared Infrastructure**
- **PostgreSQL Database**: Central database shared across all microservices
- **Spring Boot Framework**: Standard framework for all Java services
- **Shared Data Layer Module**: Common data access layer with standard patterns
- **Authentication Service**: JWT-based authentication and authorization
- **API Gateway**: Central routing and request handling
- **Docker Containerization**: Standard deployment approach
- **Railway Deployment Platform**: Cloud hosting and deployment

#### **Shared Database Tables**
- **`user_profiles`**: Core user identity and basic information
- **`patients`**: Patient-specific business data
- **`providers`**: Provider-specific business data
- **`appointments`**: Scheduling and appointment data
- **`medical_records`**: Clinical medical data
- **`audit_logs`**: System-wide audit trail

#### **Shared Services**
- **Auth Service**: JWT validation and user context
- **AI Service**: Python-based AI and ML capabilities
- **Shared Exception Handling**: Standard error patterns
- **Shared Logging**: Structured logging across services

### **Provider Service Design Approach**
**Description**: Practical provider service that meets current scope while allowing future scaling.

**Database Tables**:
- `user_profiles` - Core user information
- `providers` - Provider-specific data
- `medical_records` - Clinical medical data



**Account-to-Provider Linking Strategy**:
- **Auto-Create Provider ID During Registration**
  - User registration automatically creates both user profile and provider record
  - Immediate provider access after registration
  - Professional credential verification process
  - Best for healthcare provider onboarding

**Core Workflow**:
- Provider registration with credential verification
- Profile management with specialty and certification updates
- Medical records creation and management
- Patient relationship management

## 🏗️ **High-Level Design**

### **Core Concept**
Spring Boot service managing healthcare provider data and operations, ensuring HIPAA compliance and professional healthcare standards.

### **Key Components**
- **Provider Service**: Provider data management and operations (Port 8003)
- **Medical Records Management**: Clinical documentation and patient care records
- **Provider-Patient Relationships**: Professional relationship management
- **Credential Verification**: Professional license and certification validation

### **Data Flow**
1. **Provider Registration**: User registration → Provider profile creation → Credential verification
2. **Profile Management**: Provider updates profile → Validation → Database update
3. **Provider Discovery**: Patient searches providers → Filter by specialties → Provider list retrieval
4. **Medical Records Management**: Provider creates/updates records → Appointment validation → Database storage
5. **Patient Medical Records Access**: Patient requests records → JWT validation → Secure data retrieval

**Data Flow Diagrams**:

```
Provider Service Component Architecture:
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Auth Service   │    │  Provider Svc   │
│                 │    │                 │    │                 │
│ • Route requests│    │ • JWT validation│    │ • Profile mgmt  │
│ • Load balance  │    │ • User context  │    │ • Credentials   │
│ • Rate limiting │    │ • Role checking │    │ • Verification  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Shared Database│
                    │                 │
                    │ • user_profiles │
                    │ • providers     │
                    │ • audit_logs    │
                    └─────────────────┘
```

```
Key Data Flow - Provider Registration:
Provider → Gateway → Auth → Provider Service → Database
  │         │        │         │                │
  │         │        │         │                └── Create user_profiles + providers
  │         │        │         │                │   (with license verification)
  │         │        │         │                │
  │         │        │         │                └── Set is_verified = false
  │         │        │         │
  │         │        │         └── Return registration confirmation
  │         │        │
  │         │        └── Validate JWT + provider role
  │         │        │
  │         │        └── If validation fails → Return 401/403 error
  │         │
  │         └── Route registration request
  │
  └── Submit registration
```

```
Key Data Flow - Provider Discovery:
Patient → Gateway → Auth → Provider Service → Database
  │        │        │         │                │
  │        │        │         │                └── Query providers table
  │        │        │         │                │   (filter by specialties, verified status)
  │        │        │         │                │
  │        │        │         │                └── Join with user_profiles for basic info
  │        │        │         │
  │        │        │         └── Return provider list
  │        │        │
  │        │        └── Validate JWT + patient role
  │        │        │
  │        │        └── If validation fails → Return 401/403 error
  │        │
  │        └── Route search request
  │
  └── Search for providers
```

```
Key Data Flow - Medical Records Management:
Provider → Gateway → Auth → Provider Service → Database
  │         │        │         │                │
  │         │        │         │                └── Create/update medical_records table
  │         │        │         │                │   (with patient_id, provider_id, appointment_id, diagnosis, etc.)
  │         │        │         │                │
  │         │        │         │                └── Validate appointment exists and provider owns it
  │         │        │         │
  │         │        │         └── Return record confirmation
  │         │        │
  │         │        └── Validate JWT + provider role
  │         │        │
  │         │        └── If validation fails → Return 401/403 error
  │         │
  │         └── Route to /api/medical/records
  │
  └── Submit medical record
```

```
Key Data Flow - Patient Medical Records Access:
Patient → Gateway → Auth → Provider Service → Database
  │        │        │         │                │
  │        │        │         │                └── Query medical_records table
  │        │        │         │                │   (WHERE patient_id = JWT.patient_id)
  │        │        │         │                │
  │        │        │         │                └── Validate patient_id matches JWT
  │        │        │         │
  │        │        │         └── Return patient's medical records
  │        │        │
  │        │        └── Validate JWT + patient role
  │        │        │
  │        │        └── If validation fails → Return 401/403 error
  │        │
  │        └── Route to /api/medical/records/patient/{patientId}
  │
  └── Request medical records
```

## 🛠️ **API Design**

### **Endpoints**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/health` | Health check | No |
| POST | `/api/providers` | Create provider account | Yes |
| GET | `/api/providers/profile` | Get my provider profile | Yes |
| PUT | `/api/providers/profile` | Update my provider profile | Yes |
| GET | `/api/providers` | List providers (patient/admin access) | Yes |

### **Medical Records APIs**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/medical/records` | Create medical record | Yes |
| GET | `/api/medical/records` | Get my medical records | Yes |
| PUT | `/api/medical/records/{recordId}` | Update medical record | Yes |
| GET | `/api/medical/records/patient/{patientId}` | Get patient's medical records | Yes |

### **Request/Response Example**
```json
// Create Provider Request
{
  "firstName": "Dr. Sarah",
  "lastName": "Johnson",
  "licenseNumber": "MD123456",
  "specialties": ["Cardiology", "Internal Medicine"],
  "email": "dr.sarah.johnson@email.com",
  "phone": "+1234567890"
}

// Provider Response
{
  "id": "uuid",
  "firstName": "Dr. Sarah",
  "lastName": "Johnson",
  "licenseNumber": "MD123456",
  "specialties": ["Cardiology", "Internal Medicine"],
  "email": "dr.sarah.johnson@email.com",
  "phone": "+1234567890",
  "isVerified": false,
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### **Medical Records Request/Response Example**
```json
// Create Medical Record Request
{
  "patientId": "uuid",
  "appointmentId": "uuid",
  "diagnosis": "Hypertension",
  "treatment": "Prescribed Lisinopril 10mg daily",
  "medications": [
    {
      "name": "Lisinopril",
      "dosage": "10mg",
      "frequency": "daily",
      "duration": "30 days"
    }
  ],
  "notes": "Patient shows improvement. Follow up in 2 weeks.",
  "visitDate": "2024-01-15",
  "recordType": "CONSULTATION",
  "fileUrls": {
    "xrays": ["https://s3.amazonaws.com/medical-files/xray-001.jpg"],
    "lab_reports": ["https://s3.amazonaws.com/medical-files/lab-001.pdf"],
    "prescriptions": ["https://s3.amazonaws.com/medical-files/prescription-001.pdf"]
  }
}

// Medical Record Response
{
  "id": "uuid",
  "patientId": "uuid",
  "providerId": "uuid",
  "appointmentId": "uuid",
  "diagnosis": "Hypertension",
  "treatment": "Prescribed Lisinopril 10mg daily",
  "medications": [...],
  "notes": "Patient shows improvement. Follow up in 2 weeks.",
  "visitDate": "2024-01-15",
  "recordType": "CONSULTATION",
  "fileUrls": {...},
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## 🗄️ **Database Schema Design**

### **Provider Service Database Tables**

#### **Providers Table (providers)**
| Column | Type | Constraints | Indexes | Description |
|--------|------|-------------|---------|-------------|
| id | UUID | PRIMARY KEY | PRIMARY | Unique provider record identifier |
| user_id | UUID | FOREIGN KEY, NOT NULL | INDEX | Reference to user_profiles.id |
| license_number | VARCHAR(50) | UNIQUE, NOT NULL | UNIQUE | Professional license number |
| specialties | TEXT[] | NOT NULL | - | Array of medical specialties |
| certifications | JSONB | NULL | - | Professional certifications and credentials |
| rating | DECIMAL(3,2) | NULL | INDEX | Average provider rating (1.00-5.00) |
| total_reviews | INTEGER | NOT NULL, DEFAULT 0 | - | Total number of reviews |
| is_verified | BOOLEAN | NOT NULL, DEFAULT FALSE | INDEX | Professional credential verification status |
| practice_info | JSONB | NULL | - | Practice location, hours, contact info |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | INDEX | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | - | Last update timestamp |

**Indexes**:
- `PRIMARY KEY` (id) - Unique provider identifier
- `UNIQUE` (license_number) - For license verification
- `INDEX` (user_id) - For user profile lookup

#### **Medical Records Table (medical_records)**
| Column | Type | Constraints | Indexes | Description |
|--------|------|-------------|---------|-------------|
| id | UUID | PRIMARY KEY | PRIMARY | Unique medical record identifier |
| patient_id | UUID | FOREIGN KEY, NOT NULL | INDEX | Reference to patients.id |
| provider_id | UUID | FOREIGN KEY, NOT NULL | INDEX | Reference to providers.id |
| appointment_id | UUID | FOREIGN KEY, NOT NULL | INDEX | Reference to appointments.id |
| diagnosis | TEXT | NULL | - | Medical diagnosis |
| treatment | TEXT | NULL | - | Treatment plan and recommendations |
| medications | JSONB | NULL | - | Prescribed medications |
| notes | TEXT | NULL | - | Clinical notes and observations |
| file_urls | JSONB | NULL | - | Medical file URLs (xrays, lab_reports, prescriptions) |
| visit_date | DATE | NOT NULL | INDEX | Date of medical visit |
| record_type | VARCHAR(50) | NOT NULL | INDEX | Type of medical record (consultation, follow-up, etc.) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | INDEX | Record creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | - | Last update timestamp |

**Medical Records Indexes**:
- `PRIMARY KEY` (id) - Unique medical record identifier
- `INDEX` (patient_id) - For patient record lookup
- `INDEX` (provider_id) - For provider record lookup
- `INDEX` (appointment_id) - For appointment-linked records

## ❓ **Q&A**

### **Common Questions**
**Q**: How do we handle provider credential verification?
**A**: Provider credentials are verified through professional license databases and certification validation processes.

**Q**: What happens when a provider's license expires?
**A**: The system tracks license expiration dates and can suspend provider access when licenses are not renewed.

**Q**: How do we ensure medical record privacy?
**A**: JWT validation ensures providers can only access their own records and patients can only access their own records. All access is logged for audit purposes.

**Q**: Why is appointment_id required for medical records?
**A**: Medical records must be linked to actual appointments to ensure data integrity and provide proper context for both providers and patients.

**Q**: Why can't medical records be deleted?
**A**: Medical records are immutable for compliance and legal requirements. Records can be updated but never deleted to maintain audit trail.

## 🔍 **Discussion Points**

### **1. Registration Flow Responsibility**
**Question**: Who handles provider registration?
- **Option A**: Gateway orchestrates (calls Provider Service for validation, creates Supabase account)
- **Option B**: External auth provider handles (Provider Service only manages business data)
- **Option C**: Provider Service handles (integrates with external auth)
- **Decision Needed**: Clear responsibility assignment

### **2. Medical Records Management**
**Question**: How should medical records be managed?
- **Provider Service**: Primary management of medical records
- **Patient Service**: Read-only access to patient's own records
- **Integration**: How to coordinate between services?
- **Decision Needed**: Medical records ownership and access patterns

### **3. Credential Verification**
**Question**: How to implement professional credential verification?
- **Manual Verification**: Admin reviews and approves credentials
- **Automated Verification**: Integration with professional databases
- **Hybrid Approach**: Automated + manual review for complex cases
- **Decision Needed**: Verification strategy and implementation

## 📚 **References**

- [System Design](system-design.md)
- [Database Design](database-design.md)
- [Patient Service Design](patient-service-design.md)
- [Service Design Template](service-design-template.md)

---

*This Provider Service design provides comprehensive healthcare provider management capabilities while maintaining clear service boundaries and healthcare compliance.*
