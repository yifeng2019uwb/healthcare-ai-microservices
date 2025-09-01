# Healthcare AI Microservices - Patient Service Design

## 📋 **Document Information**

- **Document Title**: Patient Service Design
- **Version**: 1.0
- **Date**: [Current Date]
- **Author**: Healthcare AI Team
- **Status**: Draft
- **Service**: Patient Service
- **Port**: 8002

## 🎯 **Overview**

### **What This Service Is**
The Patient Service manages all patient-related data and operations, including patient profiles, medical history, demographics, and health information. It serves as the central hub for patient data management in the healthcare platform.

### **Business Value & Impact**
Patient data is the core of any healthcare system. This service ensures secure, compliant, and efficient management of patient information while providing the foundation for other services like appointments, medical records, and AI analysis.

### **Scope**
- **In Scope**: Account creation, patient profile management, medical history viewing
- **Out of Scope**: Appointment creation and management (handled by Appointment Service), medical records content, billing information, provider management, user authentication (handled by Auth Service)

**Note**: Account creation scope needs clarification - may be handled by Gateway orchestration or external auth provider.

## 📚 **Definitions & Glossary**

### **Key Terms**
- **HIPAA**: Health Insurance Portability and Accountability Act - federal law protecting patient health information
- **Patient Profile**: Complete patient information including personal details and health data
- **Medical History**: Record of patient's past health conditions, treatments, and procedures
- **Demographics**: Basic patient information like age, gender, location, contact details

## 👥 **User Stories**

### **Primary User Types**
- **Patients**: Individuals who use the platform to manage their own health information

### **User Stories**

#### **User Case 1: Account Creation**
Patients need to create an account to access the healthcare platform. They should be able to register with basic information like name, email, and password to get started with managing their health information.

**Note**: This may be handled by Gateway orchestration or external auth provider - needs discussion.

#### **User Case 2: Profile Management**
Patients need to view and update their personal health profile. They want to see their complete health information in one place and easily update their contact details, add new allergies, or modify their health information. This helps them keep their profile current and accurate.

#### **User Case 3: Medical History Review**
Patients want to view their complete medical history including past conditions, treatments, and medications. This helps them understand their health journey better and have more informed conversations with their healthcare providers.


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

### **Patient Service Design Approach**
**Description**: Practical patient service that meets current scope while allowing future scaling.

**Database Tables**:
- `user_profiles` - Core user information
- `patients` - Patient-specific data



**Account-to-Patient Linking Strategy**:
- **Auto-Create Patient ID During Registration**
  - User registration automatically creates both user profile and patient record
  - Immediate patient access after registration
  - Simple implementation for learning project
  - Best for direct-to-consumer healthcare platforms

**Note**: Registration flow needs discussion - may be orchestrated by Gateway or handled by external auth provider.

**Core Workflow**:
- Account creation with proper validation and security
- Profile management with data integrity checks
- Medical history viewing with proper data organization
- Basic search functionality for patient lookup
- Proper error handling and validation throughout

## 🏗️ **High-Level Design**

### **Core Concept**
Spring Boot service managing patient data and operations, ensuring HIPAA compliance and data integrity while providing self-service capabilities for patients.

### **Key Components**
- **Patient Service**: Patient data management and operations (Port 8002)
- **Profile Management**: Patient profile CRUD operations
- **Medical History**: Patient medical data viewing
- **Data Validation**: Business rule validation and integrity checks

### **Data Flow**
1. **Account Creation**: User registration → Patient profile creation → Data validation → Success response
2. **Profile Management**: Patient updates profile → Validation → Database update → Success response
3. **Medical History**: Patient requests history → Authorization check → Data retrieval → Response
4. **Search Operations**: Provider searches patients → Name/DOB lookup → Patient data retrieval → Response

**Data Flow Diagrams**:

```
Patient Service Component Architecture:
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Auth Service   │    │  Patient Service│
│                 │    │                 │    │                 │
│ • Route requests│    │ • JWT validation│    │ • Account mgmt  │
│ • Load balance  │    │ • User context  │    │ • Profile mgmt  │
│ • Rate limiting │    │ • Role checking │    │ • Medical data  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Shared Database│
                    │                 │
                    │ • user_profiles │
                    │ • patients      │
                    │ • audit_logs    │
                    └─────────────────┘
```

```
Key Data Flow - Account Creation:
User → Gateway → Auth → Patient Service → Database
  │      │        │         │            │
  │      │        │         │            └── Create user_profiles + patients
  │      │        │         │
  │      │        │         └── Return success response
  │      │        │
  │      │        └── Validate JWT
  │      │        │
  │      │        └── If validation fails → Return 401/403 error
  │      │
  │      └── Route request
  │
  └── Submit registration
```

**Pros**:
- Covers all required functionality in our scope
- Good code quality and architecture practices
- Reasonable complexity for learning
- Professional structure without over-engineering
- Easy to extend with additional features later

**Cons**:
- Moderate development time
- Requires some architectural planning
- More complex than minimal solution

### **Solution 2: Minimal Patient Service for Learning**
**Description**: A focused, simple patient service that covers only the essential functionality needed for the learning project while keeping implementation straightforward.

**Database**:
- **Existing System Tables** (already available):
  - `user_profiles` table: Basic user information (ID, username, name, role, status)
- **Patient Service Uses** (existing table):
  - `patients` table: Patient-specific data (medical history, allergies, assigned providers)
- **No New Tables Required**: Uses existing database schema

**Account-to-Patient Linking Strategy**:
- **Option 1: Auto-Create Patient ID During Registration**
  - Simple auto-creation during registration
  - Minimal complexity for learning
  - Immediate patient functionality

**Workflow**:
- Simple account creation with basic validation
- Basic CRUD operations for patient profiles and medical history
- Simple search by name or email
- Basic error handling

**Pros**:
- Fastest development and implementation
- Easy to understand and maintain
- Perfect for learning Spring Boot basics
- Low infrastructure requirements
- Simple deployment and testing

**Cons**:
- Limited functionality
- May need significant expansion later
- Basic features only
- Not representative of production systems

### **Solution 3: Advanced Patient Service with Just-in-Time Creation**
**Description**: An optimized patient service that creates patient records only when needed, focusing on efficiency and scalability.

**Database**:
- **Existing System Tables** (already available):
  - `user_profiles` table: Core user information
- **Patient Service Uses** (existing table):
  - `patients` table: Patient-specific data (created only when needed)
- **No New Tables Required**: Uses existing database schema

**Account-to-Patient Linking Strategy**:
- **Option 2: Create Patient ID on First Appointment**
  - User registration creates only user profile
  - Patient record created when first appointment is booked
  - More efficient database usage
  - Better for production systems

**Workflow**:
- Account creation with user profile only
- Patient record creation triggered by first appointment
- Complex booking logic with patient creation
- Advanced error handling and rollback

**Pros**:
- Efficient database usage
- No unused patient records
- Production-ready approach
- Scalable architecture

**Cons**:
- More complex implementation
- Complex booking logic
- Potential failure points
- Over-engineered for learning

### **Final Decision**
**Chosen Solution**: [To be decided after discussion]

**Why**: [To be filled after decision based on learning goals and timeline preferences]

## 🔌 **API Design**

### **Endpoints**
| Method |            Endpoint             | Description                | Auth |
|--------|---------------------------------|----------------------------|------|
|  GET   | `/health`                       | Health check               | No   |
|  POST  | `/api/patients`                 | Create patient account     | Yes  |
|  GET   | `/api/patients/profile`         | Get my patient profile     | Yes  |
|  PUT   | `/api/patients/profile`         | Update my patient profile  | Yes  |
|  GET   | `/api/patients/medical-history` | Get my medical history     | Yes  |

### **Request/Response Example**
```json
// Create Patient Request
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "email": "john.doe@email.com",
  "phone": "+1234567890"
}

// Patient Response
{
  "id": "uuid",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-01-01",
  "email": "john.doe@email.com",
  "phone": "+1234567890",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### **Future Enhancement - Pre-Visit APIs**
For enhanced patient experience before medical visits, the following APIs can be added:

#### **Pre-Visit Update Endpoints**
| Method | Endpoint | Description | Purpose |
|--------|----------|-------------|---------|
| PUT | `/api/patients/pre-visit/allergies` | Update allergies before visit | Pre-visit preparation |
| PUT | `/api/patients/pre-visit/medications` | Update current medications | Pre-visit preparation |
| PUT | `/api/patients/pre-visit/health-status` | Update recent health status | Pre-visit preparation |
| PUT | `/api/patients/pre-visit/emergency-contact` | Update emergency contact | Pre-visit preparation |

#### **Benefits of Pre-Visit APIs**
- **Specific purpose**: Clear focus on pre-visit data updates
- **Better tracking**: Know what was updated specifically for visits
- **Granular control**: Update specific data types independently
- **Visit preparation**: Streamlined process for patients before appointments

#### **Implementation Notes**
- **Current approach**: Use existing `PUT /api/patients/profile` for all updates
- **Future enhancement**: Add specific pre-visit endpoints when needed
- **Data storage**: Store in existing `patients` table JSON fields
- **Audit logging**: Track all pre-visit updates for compliance

## 🗄️ **Database Schema Design**

### **Patient Service Database Tables**

#### **1. User Profiles Table (user_profiles)**
| Column        | Type         | Constraints                 | Indexes | Description |
|---------------|--------------|-----------------------------|---------|-------------|
| id            | UUID         | PRIMARY KEY                 | PRIMARY | Unique user identifier |
| username      | VARCHAR(255) | UNIQUE, NOT NULL            | UNIQUE | Email address for login |
| first_name    | VARCHAR(100) | NOT NULL                    | - | User's first name |
| last_name     | VARCHAR(100) | NOT NULL                    | - | User's last name |
| date_of_birth | DATE         | NOT NULL                    | - | User's date of birth |
| phone         | VARCHAR(20)  | NULL                        | INDEX | Contact phone number |
| email         | VARCHAR(255) | NOT NULL                    | - | Contact email address |
| role          | ENUM         | NOT NULL, DEFAULT 'PATIENT' | - | User role (PATIENT, PROVIDER, ADMIN) |
| status        | ENUM         | NOT NULL, DEFAULT 'ACTIVE'  | - | Account status (ACTIVE, INACTIVE) |
| created_at    | TIMESTAMP    | NOT NULL, DEFAULT NOW()     | - | Record creation timestamp |
| updated_at    | TIMESTAMP    | NOT NULL, DEFAULT NOW()     | - | Last update timestamp |

**Composite Indexes:**
- `idx_user_profiles_name_dob` (last_name, first_name, date_of_birth) - For exact patient identification

#### **2. Patients Table (patients)**
| Column            | Type      | Constraints             | Indexes | Description                           |
|-------------------|-----------|-------------------------|---------|---------------------------------------|
| id                | UUID      | PRIMARY KEY             | PRIMARY | Unique patient identifier |
| user_id           | UUID      | FOREIGN KEY, NOT NULL   | INDEX   | Reference to user_profiles.id |
| patient_number    | VARCHAR(50) | UNIQUE, NOT NULL      | UNIQUE  | Auto-generated patient number
| allergies         | JSONB     | NULL                    | - | Patient allergies array |
| assigned_providers| UUID[]    | NULL                    | - | Array of provider IDs |
| health_goals      | JSONB     | NULL                    | - | Patient health objectives |
| risk_factors      | JSONB     | NULL                    | - | Health risk factors |
| emergency_contact | JSONB     | NULL                    | - | Emergency contact information |
| created_at        | TIMESTAMP | NOT NULL, DEFAULT NOW() | - | Record creation timestamp |
| updated_at        | TIMESTAMP | NOT NULL, DEFAULT NOW() | - | Last update timestamp |

**Foreign Key Indexes:**
- `idx_patients_user_id` (user_id) - For patient self-service queries (auto-created by FK)

#### **Allergies (JSONB)**
```json
[
  {
    "allergen": "Penicillin",
    "severity": "SEVERE",
    "reaction": "Anaphylaxis",
    "diagnosed_date": "2015-06-10"
  }
]
```

#### **Health Goals (JSONB)**
```json
[
  {
    "goal": "Reduce blood sugar levels",
    "target_date": "2024-12-31",
    "status": "ACTIVE",
    "notes": "Target A1C < 7%"
  }
]
```

#### **Risk Factors (JSONB)**
```json
[
  {
    "factor": "Family history of diabetes",
    "category": "FAMILY_HISTORY",
    "severity": "HIGH",
    "notes": "Both parents have diabetes"
  }
]
```

#### **Emergency Contact (JSONB)**
```json
{
  "name": "John Smith",
  "relationship": "Spouse",
  "phone": "+1-555-0123",
  "email": "john.smith@email.com"
}
```

### **Table Relationships**

#### **Primary Relationships**
- **patients.user_id** → **user_profiles.id** (One-to-One)
- Each patient record belongs to exactly one user profile
- Each user profile can have at most one patient record

#### **Data Access Patterns**
- **Patient Self-Service**: Query by `patients.user_id` (authenticated user)
- **Provider Lookup**: Query by `user_profiles` name/DOB, join to `patients`
- **System Admin**: Query by `user_profiles.role = 'PATIENT'`

### **Data Validation Rules**

#### **User Profile Validation**
| Field | Validation Rules |
|-------|------------------|
| username | Required, valid email format, unique |
| first_name | Required, 2-50 characters |
| last_name | Required, 2-50 characters |
| date_of_birth | Required, valid date, not in future |
| phone | Optional, strict format: +1-XXX-XXX-XXXX (US format), unique if provided |
| email | Required, valid email format |
| role | Must be "PATIENT" for this service |
| status | Must be "ACTIVE" for new registrations |

#### **Patient Data Validation**
| Field | Validation Rules |
|-------|------------------|
| user_id | Required, must reference existing user_profiles.id |
| patient_number | Auto-generated, unique, format: "P" + timestamp |
| medical_history | JSON structure validation |
| allergies | Array validation, allergen names required |
| assigned_providers | Array of valid UUIDs |
| health_goals | Array validation, goal text required |
| risk_factors | Array validation, factor name required |
| emergency_contact | Optional, but if provided must have name and phone |

### **Phone Number Validation Rules**

#### **Strict Format Requirements**
- **Format**: `+1-XXX-XXX-XXXX` (US phone number format)
- **Country Code**: Must start with `+1` for US
- **Area Code**: 3 digits (XXX)
- **Prefix**: 3 digits (XXX)
- **Line Number**: 4 digits (XXXX)
- **Separators**: Hyphens required between sections

#### **Validation Examples**
| Valid Format | Invalid Format | Reason |
|--------------|----------------|---------|
| `+1-555-123-4567` | `555-123-4567` | Missing country code |
| `+1-555-123-4567` | `+15551234567` | Missing hyphens |
| `+1-555-123-4567` | `+1-555-123-456` | Wrong number of digits |
| `+1-555-123-4567` | `+1-555-123-45678` | Too many digits |

#### **Database Constraints**
- **Unique constraint**: Phone numbers must be unique across all patients
- **Index**: Fast lookup by phone number
- **Nullable**: Patients can exist without phone numbers

#### **Business Rules**
- **Optional field**: Patients can register without phone number
- **Unique identification**: If provided, must be unique for accurate searches
- **Search capability**: Providers can find patients by phone number
- **Format consistency**: Ensures reliable search results

### **Service Boundaries**

#### **Patient Service Handles**
- ✅ Patient registration and profile management
- ✅ Patient self-service data (allergies, goals, risk factors)
- ✅ Emergency contact information
- ✅ Basic patient identification

#### **Provider Service Will Handle**
- 🏥 Medical records and diagnoses
- 🏥 Treatment plans and medications
- 🏥 Lab results and imaging
- 🏥 Visit/appointment documentation
- 🏥 Provider-patient relationships
- 🏥 Clinical notes and medical history

#### **Appointment Service Will Handle**
- 📅 Appointment scheduling and booking
- 📅 Visit management and status tracking
- 📅 Calendar integration
- 📅 Reminders and notifications


## ❓ **Q&A**

### **Common Questions**
**Q**: How do we handle patient data privacy and HIPAA compliance?
**A**: All patient data is encrypted, access is logged, and only authorized users can access patient information through proper authentication and role-based access control.

**Q**: What happens when a patient wants to delete their data?
**A**: Patient data deletion follows GDPR and HIPAA requirements - data is anonymized rather than completely deleted to maintain medical record integrity.

## 🔍 **Discussion Points**

### **1. Registration Flow Responsibility**
**Question**: Who handles patient registration?
- **Option A**: Gateway orchestrates (calls Patient Service for validation, creates Supabase account)
- **Option B**: External auth provider handles (Patient Service only manages business data)
- **Option C**: Patient Service handles (integrates with external auth)
- **Decision Needed**: Clear responsibility assignment

### **2. Business Validation Strategy**
**Question**: Where does business rule validation happen?
- **Patient Service**: Validates patient-specific business rules
- **Gateway**: Basic format validation only
- **Integration**: How to coordinate validation between services?
- **Decision Needed**: Validation strategy and coordination

### **3. Service Communication**
**Question**: How should Patient Service communicate with other services?
- **Direct Data Access**: Primary approach for data operations
- **Service Calls**: When business logic requires other service data
- **Error Handling**: How to handle service failures?
- **Decision Needed**: Communication patterns and error handling

## 📚 **References**
- [System Design](system-design.md)
- [Database Design](database-design.md)
- [Data Strategy](data-strategy.md)
