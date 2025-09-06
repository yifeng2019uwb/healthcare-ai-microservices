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

**Note**: Account creation handled by Gateway orchestration (calls Supabase Auth + Patient Service).

## 📚 **Definitions & Glossary**

### **Key Terms**
- **HIPAA**: Health Insurance Portability and Accountability Act - federal law protecting patient health information
- **Patient Profile**: Complete patient information including personal details and health data
- **Medical History**: Record of patient's past health conditions, treatments, and procedures
- **Demographics**: Basic patient information like age, gender, location, contact details

## 👥 **User Case**

### **Primary User Types**
- **Patients**: Individuals who use the platform to manage their own health information

### **User Case**

#### **User Case 1: Account Creation**
Patients need to create an account to access the healthcare platform. They should be able to register with basic information like name, email, and password to get started with managing their health information.

**Note**: Account creation handled by Gateway orchestration (calls Supabase Auth + Patient Service).

#### **User Case 2: Profile Management**
Patients need to view and update their personal health profile. They want to see their complete health information in one place and easily update their contact details, add new allergies, or modify their health information. This helps them keep their profile current and accurate.

#### **User Case 3: Medical History Review**
Patients want to view their complete medical history including past conditions, treatments, and medications. This helps them understand their health journey better and have more informed conversations with their healthcare providers.


## 🔧 **Solution Alternatives**

### **Shared Infrastructure**
*Reference: System Design Document for complete infrastructure details*

**Key Infrastructure**: PostgreSQL Database, Spring Boot Framework, Shared Data Layer Module, Authentication Service, API Gateway, Docker, Railway Deployment

### **Patient Service Design Approach**
**Description**: Practical patient service that meets current scope while allowing future scaling.

**Database Tables**:
- `user_profiles` - Core user information
- `patient_profiles` - Patient-specific data



**Account-to-Patient Linking Strategy**:
- **Auto-Create Patient ID During Registration**
  - User registration automatically creates both user profile and patient record
  - Immediate patient access after registration
  - Simple implementation for MVP project
  - Best for direct-to-consumer healthcare platforms

**Note**: Registration flow handled by Gateway orchestration (calls Supabase Auth + Patient Service).

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
                    │ • patient_profiles │
                    │ • audit_logs    │
                    └─────────────────┘
```

```
Key Data Flow - Account Creation:
User → Gateway → Auth → Patient Service → Database
  │      │        │         │            │
  │      │        │         │            └── Create user_profiles + patient_profiles
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
- Reasonable complexity for MVP
- Professional structure without over-engineering
- Easy to extend with additional features later

**Cons**:
- Moderate development time
- Requires some architectural planning
- More complex than minimal solution

### **Solution 2: Minimal Patient Service for MVP**
**Description**: A focused, simple patient service that covers only the essential functionality needed for the MVP project while keeping implementation straightforward.

**Database**:
- **Existing System Tables** (already available):
  - `user_profiles` table: Basic user information (ID, username, name, role, status)
- **Patient Service Uses** (existing table):
  - `patient_profiles` table: Patient-specific data (medical history, allergies, emergency contacts)
- **No New Tables Required**: Uses existing database schema

**Account-to-Patient Linking Strategy**:
- **Option 1: Auto-Create Patient ID During Registration**
  - Simple auto-creation during registration
  - Minimal complexity for MVP
  - Immediate patient functionality

**Workflow**:
- Simple account creation with basic validation
- Basic CRUD operations for patient profiles and medical history
- Simple search by name or email
- Basic error handling

**Pros**:
- Fastest development and implementation
- Easy to understand and maintain
- Perfect for building Spring Boot services
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
  - `patient_profiles` table: Patient-specific data (created only when needed)
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
- Over-engineered for MVP

### **Final Decision**
**Chosen Solution**: [To be decided after discussion]

**Why**: [To be filled after decision based on MVP goals and timeline preferences]

## 🔌 **API Design**

### **Endpoints**
| Method |            Endpoint             | Description                | Auth |
|--------|---------------------------------|----------------------------|------|
|  GET   | `/health`                       | Health check               | No   |
|  POST  | `/api/patients`                 | Create patient account     | Yes  |
|  GET   | `/api/patients/profile`         | Get my patient profile     | Yes  |
|  PUT   | `/api/patients/profile`         | Update my patient profile  | Yes  |
|  PUT   | `/api/patients/patient-info`    | Update patient info        | Yes  |
|  GET   | `/api/patients/medical-history` | Get my medical history     | Yes  |

## 🔌 **API Documentation**

### **1. Create Patient Account**
**Endpoint**: `POST /api/patients`
**Description**: Create a new patient account (orchestrated by Gateway)
**Authentication**: Required (JWT token)

#### **Request Body**:
```json
{
  "externalUserId": "supabase-uuid-from-auth",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1-555-0123",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "streetAddress": "123 Main St",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1-555-0124"
}
```

#### **Request Field Specifications**:

| Field | Type | Required | Pattern/Format | Description |
|-------|------|----------|----------------|-------------|
| `externalUserId` | String | ✅ Required | UUID format | Supabase user ID from authentication |
| `firstName` | String | ✅ Required | 2-100 characters, letters only | User's first name |
| `lastName` | String | ✅ Required | 2-100 characters, letters only | User's last name |
| `email` | String | ✅ Required | Valid email format | Contact email address |
| `phone` | String | ✅ Required | E.164 format (+1-555-0123) | Contact phone number |
| `dateOfBirth` | String | ✅ Required | YYYY-MM-DD, not future date | User's date of birth |
| `gender` | String | ✅ Required | MALE, FEMALE, OTHER, UNKNOWN | User's gender |
| `streetAddress` | String | ❌ Optional | 1-255 characters | Street address |
| `city` | String | ❌ Optional | 1-100 characters | City name |
| `state` | String | ❌ Optional | 1-50 characters | State or province |
| `postalCode` | String | ❌ Optional | 1-20 characters | Postal or ZIP code |
| `country` | String | ❌ Optional | 1-50 characters | Country name |
| `emergencyContactName` | String | ❌ Optional | 1-100 characters | Emergency contact name |
| `emergencyContactPhone` | String | ❌ Optional | E.164 format (+1-555-0124) | Emergency contact phone |

#### **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Account created successfully"
}
```

#### **Future Enhancement: Email Verification Flow**:

##### **Planned Registration Process**:
1. **User submits registration** → Account created with `status: "INACTIVE"` (future default)
2. **Email sent** → Verification email sent to user's email address
3. **User clicks link** → Email verification endpoint called
4. **Status updated** → Account status changed to `ACTIVE`
5. **Full access** → User can now access all protected endpoints

##### **Status Values**:
- **`INACTIVE`** - New registration, email verification pending (future default)
- **`ACTIVE`** - Email verified, full access granted (current default)
- **`SUSPENDED`** - Account suspended by admin

##### **Current vs Future Behavior**:
- **Current**: New accounts default to `ACTIVE` status
- **Future**: New accounts will default to `INACTIVE` until email verification
- **Authorization**: Status will be checked during login and JWT generation

#### **Error Responses**:

##### **400 Bad Request**:
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data or missing required fields"
}
```

##### **409 Conflict**:
```json
{
  "error": "CONFLICT",
  "message": "User or email already exists"
}
```

##### **401 Unauthorized**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token"
}
```

##### **500 Internal Server Error**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while creating patient account"
}
```

### **2. Get Patient Profile**
**Endpoint**: `GET /api/patients/profile`
**Description**: Get current patient's profile information
**Authentication**: Required (JWT token)

#### **Response (200 OK)**:
```json
{
  "userProfile": {
    "externalUserId": "supabase-uuid-from-auth",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "streetAddress": "123 Main St",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA",
    "role": "PATIENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "patientProfile": {
    "patientNumber": "P20240115001",
    "medicalHistory": {
      "conditions": ["Hypertension", "Type 2 Diabetes"],
      "surgeries": ["Appendectomy (2010)"],
      "hospitalizations": ["Emergency visit (2023-05-15)"]
    },
    "allergies": {
      "penicillin": {
        "severity": "SEVERE",
        "reaction": "Anaphylaxis",
        "diagnosedDate": "2015-06-10"
      },
      "shellfish": {
        "severity": "MODERATE",
        "reaction": "Hives and swelling",
        "diagnosedDate": "2018-03-22"
      }
    },
    "insuranceProvider": "Blue Cross Blue Shield",
    "insurancePolicyNumber": "BC123456789",
    "primaryCarePhysician": "Dr. Sarah Johnson",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+1-555-0124",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

#### **Error Responses**:

##### **401 Unauthorized**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token"
}
```

##### **403 Forbidden**:
```json
{
  "error": "FORBIDDEN",
  "message": "Account is suspended"
}
```

##### **404 Not Found**:
```json
{
  "error": "NOT_FOUND",
  "message": "Patient profile not found or account is inactive"
}
```

##### **500 Internal Server Error**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while retrieving profile"
}
```

### **3. Update Personal Profile**
**Endpoint**: `PUT /api/patients/profile`
**Description**: Update personal information (name, contact, address)
**Authentication**: Required (JWT token)

#### **Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1-555-0123",
  "streetAddress": "456 Oak Ave",
  "city": "Brooklyn",
  "state": "NY",
  "postalCode": "11201",
  "country": "USA",
  "emergencyContactName": "Jane Doe",
  "emergencyContactPhone": "+1-555-0124"
}
```

#### **Response (200 OK)**:
```json
{
  "userProfile": {
    "externalUserId": "supabase-uuid-from-auth",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-0123",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "streetAddress": "456 Oak Ave",
    "city": "Brooklyn",
    "state": "NY",
    "postalCode": "11201",
    "country": "USA",
    "role": "PATIENT",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T14:45:00Z"
  },
  "patientProfile": {
    "patientNumber": "P20240115001",
    "emergencyContactName": "Jane Doe",
    "emergencyContactPhone": "+1-555-0124",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T14:45:00Z"
  }
}
```

#### **Request Field Specifications**:

| Field | Type | Required | Pattern/Format | Description |
|-------|------|----------|----------------|-------------|
| `firstName` | String | ❌ Optional | 2-100 characters, letters only | User's first name |
| `lastName` | String | ❌ Optional | 2-100 characters, letters only | User's last name |
| `phone` | String | ❌ Optional | E.164 format (+1-555-0123) | Contact phone number |
| `streetAddress` | String | ❌ Optional | 1-255 characters | Street address |
| `city` | String | ❌ Optional | 1-100 characters | City name |
| `state` | String | ❌ Optional | 1-50 characters | State or province |
| `postalCode` | String | ❌ Optional | 1-20 characters | Postal or ZIP code |
| `country` | String | ❌ Optional | 1-50 characters | Country name |
| `emergencyContactName` | String | ❌ Optional | 1-100 characters | Emergency contact name |
| `emergencyContactPhone` | String | ❌ Optional | E.164 format (+1-555-0124) | Emergency contact phone |

#### **Error Responses**:

##### **400 Bad Request**:
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data or phone number format is invalid"
}
```

##### **401 Unauthorized**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token"
}
```

##### **404 Not Found**:
```json
{
  "error": "NOT_FOUND",
  "message": "Patient profile not found or account is inactive"
}
```

##### **500 Internal Server Error**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while updating profile"
}
```

---

### **4. Update Patient Info**
**Endpoint**: `PUT /api/patients/patient-info`
**Description**: Update patient info (medical history, allergies, insurance, physician)
**Authentication**: Required (JWT token)
**Note**: Patients can update medical information like allergies and insurance, but not medical records. Emergency contacts are updated via the user profile endpoint.

#### **Request Body**:
```json
{
  "medicalHistory": {
    "conditions": ["Hypertension", "Type 2 Diabetes"],
    "surgeries": ["Appendectomy (2010)"],
    "hospitalizations": ["Emergency visit (2023-05-15)"]
  },
  "allergies": {
    "penicillin": {
      "severity": "SEVERE",
      "reaction": "Anaphylaxis",
      "diagnosedDate": "2015-06-10"
    },
    "shellfish": {
      "severity": "MODERATE",
      "reaction": "Hives and swelling",
      "diagnosedDate": "2018-03-22"
    }
  },
  "insuranceProvider": "Blue Cross Blue Shield",
  "insurancePolicyNumber": "BC123456789",
  "primaryCarePhysician": "Dr. Sarah Johnson"
}
```

#### **Response (200 OK)**:
```json
{
  "patientProfile": {
    "patientNumber": "P20240115001",
    "medicalHistory": {
      "conditions": ["Hypertension", "Type 2 Diabetes"],
      "surgeries": ["Appendectomy (2010)"],
      "hospitalizations": ["Emergency visit (2023-05-15)"]
    },
    "allergies": {
      "penicillin": {
        "severity": "SEVERE",
        "reaction": "Anaphylaxis",
        "diagnosedDate": "2015-06-10"
      },
      "shellfish": {
        "severity": "MODERATE",
        "reaction": "Hives and swelling",
        "diagnosedDate": "2018-03-22"
      }
    },
    "insuranceProvider": "Blue Cross Blue Shield",
    "insurancePolicyNumber": "BC123456789",
    "primaryCarePhysician": "Dr. Sarah Johnson",
    "updatedAt": "2024-01-15T14:45:00Z"
  }
}
```

#### **Request Field Specifications**:

| Field | Type | Required | Pattern/Format | Description |
|-------|------|----------|----------------|-------------|
| `medicalHistory` | Object | ❌ Optional | JSON object | Medical history and conditions |
| `allergies` | Object | ❌ Optional | JSON object | Known allergies and reactions |
| `insuranceProvider` | String | ❌ Optional | 1-100 characters | Insurance provider name |
| `insurancePolicyNumber` | String | ❌ Optional | 1-50 characters | Insurance policy number |
| `primaryCarePhysician` | String | ❌ Optional | 1-100 characters | Primary care physician name |

#### **Error Responses**:

##### **400 Bad Request**:
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid patient info data format"
}
```

##### **401 Unauthorized**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token"
}
```

##### **403 Forbidden**:
```json
{
  "error": "FORBIDDEN",
  "message": "Insufficient permissions to update patient information"
}
```

##### **404 Not Found**:
```json
{
  "error": "NOT_FOUND",
  "message": "Patient profile not found or account is inactive"
}
```

##### **500 Internal Server Error**:
```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred while updating patient information"
}
```

#### **Access Control for `updatedBy` Field:**

##### **Patient API Responses:**
- **`updatedBy` field NOT exposed** to patients in any API response
- **Internal audit field** - Used only for server-side audit trails
- **Patient privacy** - Patients don't need to know who updated their data
- **Clean interface** - Simpler API responses for patient clients

##### **Provider API Responses:**
- **`updatedBy` field IS exposed** to providers in API responses
- **Clinical workflow** - Providers need to know who updated patient data
- **Audit transparency** - Providers can see audit trail information
- **Professional responsibility** - Important for healthcare team coordination

##### **Implementation Logic:**
```java
// Patient API - Hide updatedBy field
if (userRole == "PATIENT") {
    response.removeField("updatedBy");
}

// Provider API - Show updatedBy field
if (userRole == "PROVIDER") {
    response.includeField("updatedBy");
}
```

### **5. Get Medical History**
**Endpoint**: `GET /api/patients/medical-history`
**Description**: Get patient's medical history from completed appointments and released medical records
**Authentication**: Required (JWT token with `role: "PATIENT"`)

#### **Response (200 OK)**:
```json
{
  "appointments": [
    {
      "id": "789e0123-e89b-12d3-a456-426614174002",
      "providerId": "provider-123",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T14:00:00Z",
      "status": "COMPLETED",
      "appointmentType": "REGULAR_CONSULTATION",
      "medicalRecords": [
        {
          "id": "record-123",
          "recordType": "DIAGNOSIS",
          "content": "Hypertension - Stage 1",
          "isPatientVisible": true,
          "releaseDate": "2024-01-20T16:00:00Z",
          "createdAt": "2024-01-20T15:30:00Z"
        },
        {
          "id": "record-124",
          "recordType": "TREATMENT",
          "content": "Prescribed Lisinopril 10mg daily",
          "isPatientVisible": true,
          "releaseDate": "2024-01-20T16:00:00Z",
          "createdAt": "2024-01-20T15:35:00Z"
        }
      ]
    }
  ],
  "summary": {
    "totalAppointments": 1,
    "completedAppointments": 1,
    "upcomingAppointments": 0,
    "lastVisit": "2024-01-20T14:00:00Z"
  }
}
```

#### **Error Responses**:
```json
// 401 Unauthorized
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing JWT token"
}

// 403 Forbidden
{
  "error": "FORBIDDEN",
  "message": "Insufficient permissions. Patient role required"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

#### **Data Access Notes**:
- **Read-Only Access**: Patients can only view medical records marked as `is_patient_visible: true`
- **Release Date Control**: Records are only shown if `release_date` has passed
- **Appointment Context**: Medical records are grouped by appointment/visit
- **Provider Information**: Includes provider name and specialty for context
- **Service Integration**: This endpoint calls Appointment Service to get appointment data and medical records

### **6. Health Check**
**Endpoint**: `GET /health`
**Description**: Service health check
**Authentication**: Not required

#### **Response (200 OK)**:
```json
{
  "status": "UP",
  "service": "patient-service",
  "version": "1.0.0",
  "timestamp": "2024-01-15T12:00:00Z",
  "database": "UP",
  "dependencies": {
    "database": "UP",
    "auth-service": "UP"
  }
}
```

### **Future Enhancement - Pre-Visit APIs**
For enhanced patient experience before medical visits, the following APIs can be added:

#### **Pre-Visit Update Endpoints**
| Method | Endpoint | Description | Purpose |
|--------|----------|-------------|---------|
| PUT | `/api/patients/allergies` | Update allergies before visit | Pre-visit preparation |
| PUT | `/api/patients/medications` | Update current medications | Pre-visit preparation |
| PUT | `/api/patients/health-status` | Update recent health status | Pre-visit preparation |


#### **Benefits of Pre-Visit APIs**
- **Specific purpose**: Clear focus on pre-visit data updates
- **Better tracking**: Know what was updated specifically for visits
- **Granular control**: Update specific data types independently
- **Visit preparation**: Streamlined process for patients before appointments

#### **Implementation Notes**
- **Current approach**: Use existing `PUT /api/patients/profile` for all updates
- **Future enhancement**: Add specific pre-visit endpoints when needed
- **Data storage**: Store in existing `patient_profiles` table JSON fields
- **Audit logging**: Track all pre-visit updates for compliance

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

### **3. Service Communication**
**Question**: How should Patient Service communicate with other services?
- **Direct Data Access**: Primary approach for data operations
- **Service Calls**: When business logic requires other service data
- **Decision Needed**: Communication patterns and error handling

## 📚 **References**
- [System Design](system-design.md)
- [Database Design](database-design.md)
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
