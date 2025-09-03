# Provider Service Design

> **🎯 Professional Focus: Provider Management for Healthcare Microservices**
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

## 👥 **User Case**

### **Primary User Types**
- **Providers**: Healthcare professionals who manage their profiles and patient care
- **Patients**: Individuals who need to find and connect with providers


### **User Case**

#### **User Case 1: Provider Registration**
Healthcare providers need to register on the platform to offer their services. They should be able to create a professional profile with their credentials, specialties, and practice information to start accepting patients.

#### **User Case 2: Profile Management**
Providers need to maintain their professional profiles, including updating their specialties, certifications, and practice information. This helps patients find the right provider for their healthcare needs.

#### **User Case 3: Provider Discovery**
Patients need to find and discover healthcare providers based on specialties, location, and credentials. This helps patients make informed decisions about their healthcare providers.



## 🔧 **Solution Alternatives**

### **Shared Infrastructure**
*Reference: System Design Document for complete infrastructure details*

**Key Infrastructure**: PostgreSQL Database, Spring Boot Framework, Shared Data Layer Module, Authentication Service, API Gateway, Docker, Railway Deployment

### **Provider Service Design Approach**
**Description**: Practical provider service that meets current scope while allowing future scaling.

**Database Tables**:
- `user_profiles` - Core user information
- `provider_profiles` - Provider-specific data
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
                    │ • provider_profiles │
                    │ • audit_logs    │
                    └─────────────────┘
```

```
Key Data Flow - Provider Registration:
Provider → Gateway → Auth → Provider Service → Database
  │         │        │         │                │
  │         │        │         │                └── Create user_profiles + provider_profiles
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
  │        │        │         │                └── Query provider_profiles table
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
  │         │        │         │                │   (with appointment_id, record_type, content, etc.)
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
  │        │        │         │                │   (JOIN appointments WHERE patient_id = JWT.patient_id)
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

### **Provider Management APIs**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/providers` | Create provider profile | Yes |
| GET | `/api/providers/profile` | Get my provider profile | Yes |
| PUT | `/api/providers/profile` | Update my provider profile | Yes |
| GET | `/api/providers` | List providers (patient access) | Yes |

### **Provider Discovery APIs**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/providers/search` | Search providers by specialty/location | Yes |
| GET | `/api/providers/{providerId}` | Get provider details | Yes |



## 🛠️ **Detailed API Design**

### **1. Provider Registration API**

#### **Endpoint**: `POST /api/providers`
**Description**: Register a new healthcare provider with professional credentials

#### **Authentication**:
- **Required**: None (Public registration endpoint)
- **Note**: JWT validation will be handled by external auth service

#### **Request Body**:
```json
{
  "externalUserId": "supabase-uuid-from-auth",
  "firstName": "Dr. Sarah",
  "lastName": "Johnson",
  "email": "dr.sarah.johnson@email.com",
  "phone": "+1-555-0123",
  "dateOfBirth": "1985-03-15",
  "gender": "FEMALE",
  "officeStreetAddress": "456 Medical Plaza",
  "officeCity": "Boston",
  "officeState": "MA",
  "officePostalCode": "02115",
  "officeCountry": "USA",
  "npiNumber": "1234567890",
  "specialty": "Cardiology",
  "licenseNumbers": "MD123456",
  "qualifications": "MD from Harvard Medical School, Board Certified in Cardiology",
  "bio": "Dr. Sarah Johnson is a board-certified cardiologist with over 10 years of experience in treating cardiovascular diseases.",
  "officePhone": "+1-555-0124"
}
```

#### **Field Specifications**:
| Field          | Type | Required | Pattern/Validation |
|----------------|------|----------|-------------------|
| externalUserId | String | Yes | UUID format |
| firstName      | String | Yes | 2-50 characters |
| lastName       | String | Yes | 2-50 characters |
| email          | String | Yes | Valid email format |
| phone          | String | No  | +1-XXX-XXX-XXXX format |
| dateOfBirth    | String | Yes | YYYY-MM-DD format |
| gender         | String | Yes | MALE, FEMALE, OTHER, UNKNOWN |
| officeStreetAddress | String | No  | Max 255 characters |
| officeCity     | String | No  | Max 100 characters |
| officeState    | String | No  | Max 50 characters |
| officePostalCode | String | No  | Max 20 characters |
| officeCountry  | String | No  | Max 50 characters |
| npiNumber      | String | Yes | 10 digits |
| specialty      | String | No  | Max 100 characters |
| licenseNumbers | String | No  | Max 50 characters |
| qualifications | String | No  | Max 1000 characters |
| bio            | String | No  | Max 2000 characters |
| officePhone    | String | No  | +1-XXX-XXX-XXXX format |

#### **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Provider account created successfully",
  "providerId": "PR20240115001"
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data: NPI number must be 10 digits"
}

// 409 Conflict
{
  "error": "CONFLICT",
  "message": "Provider already exists with this NPI number"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

#### **Future Feature: Provider Credential Validation**

**Planned Validation Steps** (Not implemented yet):
1. **NPI Number Validation**
   - Verify NPI number exists in national database
   - Check NPI number format and checksum
   - Validate NPI is active and not expired

2. **License Number Verification**
   - Verify license number with state medical board
   - Check license status (active, suspended, expired)
   - Validate license matches provider name and specialty

3. **Email Domain Verification**
   - Verify email domain belongs to healthcare institution
   - Check for professional email patterns
   - Validate email is not from personal domains

4. **Professional Credential Cross-Reference**
   - Cross-reference NPI with license number
   - Verify specialty matches license type
   - Check for any disciplinary actions

5. **Document Verification**
   - Upload and verify medical degree certificates
   - Verify board certification documents
   - Check continuing education requirements

**Implementation Status**:
- **Current**: Basic format validation only
- **Future**: Full credential verification system
- **Timeline**: TBD based on business requirements

### **2. Get Provider Profile API**

#### **Endpoint**: `GET /api/providers/profile`
**Description**: Retrieve the authenticated provider's profile information

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Response (200 OK)**:
```json
{
  "userProfile": {
    "externalUserId": "supabase-uuid-from-auth",
    "firstName": "Dr. Sarah",
    "lastName": "Johnson",
    "email": "dr.sarah.johnson@email.com",
    "phone": "+1-555-0123",
    "dateOfBirth": "1985-03-15",
    "gender": "FEMALE",
    "role": "PROVIDER",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "providerProfile": {
    "npiNumber": "1234567890",
    "specialty": "Cardiology",
    "licenseNumbers": "MD123456",
    "qualifications": "MD from Harvard Medical School, Board Certified in Cardiology",
    "bio": "Dr. Sarah Johnson is a board-certified cardiologist with over 10 years of experience in treating cardiovascular diseases.",
    "officeStreetAddress": "456 Medical Plaza",
    "officeCity": "Boston",
    "officeState": "MA",
    "officePostalCode": "02115",
    "officeCountry": "USA",
    "officePhone": "+1-555-0124",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
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
  "message": "Account suspended. Contact support"
}

// 404 Not Found
{
  "error": "NOT_FOUND",
  "message": "Provider profile not found"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

### **3. Update Provider Personal Profile API**

#### **Endpoint**: `PUT /api/providers/profile`
**Description**: Update provider's personal information (name, contact, address)

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "firstName": "Dr. Sarah",
  "lastName": "Johnson",
  "phone": "+1-555-0123",
  "officeStreetAddress": "456 Medical Plaza",
  "officeCity": "Boston",
  "officeState": "MA",
  "officePostalCode": "02115",
  "officeCountry": "USA",
  "officePhone": "+1-555-0124"
}
```

#### **Response (200 OK)**:
```json
{
  "userProfile": {
    "externalUserId": "supabase-uuid-from-auth",
    "firstName": "Dr. Sarah",
    "lastName": "Johnson",
    "email": "dr.sarah.johnson@email.com",
    "phone": "+1-555-0123",
    "dateOfBirth": "1985-03-15",
    "gender": "FEMALE",
    "role": "PROVIDER",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T14:45:00Z"
  },
  "providerProfile": {
    "officeStreetAddress": "456 Medical Plaza",
    "officeCity": "Boston",
    "officeState": "MA",
    "officePostalCode": "02115",
    "officeCountry": "USA",
    "officePhone": "+1-555-0124",
    "updatedAt": "2024-01-15T14:45:00Z"
  }
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data: Phone number format invalid"
}

// 401 Unauthorized
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing JWT token"
}

// 404 Not Found
{
  "error": "NOT_FOUND",
  "message": "Provider profile not found"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

### **4. Update Provider Professional Information API**

#### **Endpoint**: `PUT /api/providers/professional-info`
**Description**: Update provider's professional information (NPI, specialty, license)

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "npiNumber": "1234567890",
  "specialty": "Cardiology",
  "licenseNumbers": "MD123456",
  "qualifications": "MD from Harvard Medical School, Board Certified in Cardiology",
  "bio": "Dr. Sarah Johnson is a board-certified cardiologist with over 10 years of experience in treating cardiovascular diseases.",
  "officeStreetAddress": "456 Medical Plaza",
  "officeCity": "Boston",
  "officeState": "MA",
  "officePostalCode": "02115",
  "officeCountry": "USA",
  "officePhone": "+1-555-0124"
}
```

#### **Response (200 OK)**:
```json
{
  "providerProfile": {
    "npiNumber": "1234567890",
    "specialty": "Cardiology",
    "licenseNumbers": "MD123456",
    "qualifications": "MD from Harvard Medical School, Board Certified in Cardiology",
    "bio": "Dr. Sarah Johnson is a board-certified cardiologist with over 10 years of experience in treating cardiovascular diseases.",
    "officeStreetAddress": "456 Medical Plaza",
    "officeCity": "Boston",
    "officeState": "MA",
    "officePostalCode": "02115",
    "officeCountry": "USA",
    "officePhone": "+1-555-0124",
    "updatedAt": "2024-01-15T14:45:00Z",
    "updatedBy": "supabase-uuid-from-auth"
  }
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data: NPI number must be 10 digits"
}

// 401 Unauthorized
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing JWT token"
}

// 403 Forbidden
{
  "error": "FORBIDDEN",
  "message": "Insufficient permissions to update professional information"
}

// 404 Not Found
{
  "error": "NOT_FOUND",
  "message": "Provider profile not found"
}

// 409 Conflict
{
  "error": "CONFLICT",
  "message": "NPI number already exists"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

### **5. Provider Search API**

#### **Endpoint**: `GET /api/providers/search`
**Description**: Search for providers by specialty, location, or other criteria

#### **Authentication**:
- **Required**: JWT Token with `role: "PATIENT"` or `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| specialty | String | No | Medical specialty filter |
| city | String | No | City filter |
| state | String | No | State filter |
| limit | Integer | No | Number of results (default: 20, max: 100) |
| offset | Integer | No | Pagination offset (default: 0) |

#### **Example Request**:
```
GET /api/providers/search?specialty=Cardiology&city=Boston&limit=10
```

#### **Response (200 OK)**:
```json
{
  "providers": [
    {
      "userProfile": {
        "externalUserId": "supabase-uuid-from-auth",
        "firstName": "Dr. Sarah",
        "lastName": "Johnson",
        "email": "dr.sarah.johnson@email.com",
        "phone": "+1-555-0123",
        "role": "PROVIDER",
        "status": "ACTIVE"
      },
      "providerProfile": {
        "npiNumber": "1234567890",
        "specialty": "Cardiology",
        "licenseNumbers": "MD123456",
        "qualifications": "MD from Harvard Medical School, Board Certified in Cardiology",
        "bio": "Dr. Sarah Johnson is a board-certified cardiologist with over 10 years of experience in treating cardiovascular diseases.",
        "officeStreetAddress": "456 Medical Plaza",
        "officeCity": "Boston",
        "officeState": "MA",
        "officePostalCode": "02115",
        "officeCountry": "USA",
        "officePhone": "+1-555-0124"
      }
    }
  ],
  "pagination": {
    "total": 1,
    "limit": 10,
    "offset": 0,
    "hasMore": false
  }
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid query parameters: limit must be between 1 and 100"
}

// 401 Unauthorized
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing JWT token"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

### **6. Create Medical Record API**

#### **Endpoint**: `POST /api/providers/medical-records`
**Description**: Create a new medical record for an appointment

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "appointmentId": "appointment-uuid-456",
  "recordType": "DIAGNOSIS",
  "content": "Hypertension - Stage 1. Patient shows elevated blood pressure readings.",
  "isPatientVisible": true,
  "releaseDate": "2024-01-15T16:00:00Z",
  "customData": {
    "diagnosis": "Hypertension - Stage 1",
    "description": "Patient shows elevated blood pressure readings",
    "severity": "MILD",
    "status": "ACTIVE",
    "clinicalNotes": "BP: 145/95 mmHg. Patient reports occasional headaches. Family history of hypertension."
  }
}
```

#### **Field Specifications**:
| Field | Type | Required | Pattern/Validation |
|-------|------|----------|-------------------|
| appointmentId | String | Yes | UUID format |
| recordType | String | Yes | DIAGNOSIS, TREATMENT, SUMMARY, LAB_RESULT, PRESCRIPTION, NOTE, OTHER |
| content | String | Yes | Max 5000 characters |
| isPatientVisible | Boolean | Yes | true/false |
| releaseDate | String | No | ISO 8601 timestamp format |
| customData | Object | No | Structured JSON object for additional data |

#### **Response (201 Created)**:
```json
{
  "medicalRecord": {
    "id": "record-uuid-123",
    "appointmentId": "appointment-uuid-456",
    "patientId": "P20240115001",
    "patientName": "John Doe",
    "patientAge": 45,
    "patientGender": "MALE",
    "recordType": "DIAGNOSIS",
    "content": "Hypertension - Stage 1. Patient shows elevated blood pressure readings.",
    "customData": {
      "diagnosis": "Hypertension - Stage 1",
      "description": "Patient shows elevated blood pressure readings",
      "severity": "MILD",
      "status": "ACTIVE",
      "clinicalNotes": "BP: 145/95 mmHg. Patient reports occasional headaches. Family history of hypertension."
    },
    "isPatientVisible": true,
    "releaseDate": "2024-01-15T16:00:00Z",
    "createdAt": "2024-01-15T15:30:00Z",
    "updatedAt": "2024-01-15T15:30:00Z",
    "updatedBy": "PR20240115001"
  }
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data: Record type must be valid ENUM value"
}

// 401 Unauthorized
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or missing JWT token"
}

// 403 Forbidden
{
  "error": "FORBIDDEN",
  "message": "Insufficient permissions. Provider role required"
}

// 404 Not Found
{
  "error": "NOT_FOUND",
  "message": "Appointment not found or not accessible"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

### **7. Update Medical Record API**

#### **Endpoint**: `PUT /api/providers/medical-records/{recordId}`
**Description**: Update an existing medical record

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "content": "Hypertension - Stage 1. Patient shows elevated blood pressure readings. Recommended lifestyle changes.",
  "customData": {
    "diagnosis": "Hypertension - Stage 1",
    "description": "Patient shows elevated blood pressure readings. Recommended lifestyle changes.",
    "severity": "MILD",
    "status": "ACTIVE",
    "clinicalNotes": "BP: 145/95 mmHg. Patient reports occasional headaches. Family history of hypertension. Recommended lifestyle changes."
  },
  "isPatientVisible": true,
  "releaseDate": "2024-01-15T16:00:00Z"
}
```

#### **Response (200 OK)**:
```json
{
  "medicalRecord": {
    "id": "record-uuid-123",
    "appointmentId": "appointment-uuid-456",
    "patientId": "P20240115001",
    "patientName": "John Doe",
    "patientAge": 45,
    "patientGender": "MALE",
    "recordType": "DIAGNOSIS",
    "content": "Hypertension - Stage 1. Patient shows elevated blood pressure readings. Recommended lifestyle changes.",
    "customData": {
      "diagnosis": "Hypertension - Stage 1",
      "description": "Patient shows elevated blood pressure readings. Recommended lifestyle changes.",
      "severity": "MILD",
      "status": "ACTIVE",
      "clinicalNotes": "BP: 145/95 mmHg. Patient reports occasional headaches. Family history of hypertension. Recommended lifestyle changes."
    },
    "isPatientVisible": true,
    "releaseDate": "2024-01-15T16:00:00Z",
    "updatedAt": "2024-01-15T16:15:00Z",
    "updatedBy": "PR20240115001"
  }
}
```

### **8. Get Single Medical Record API**

#### **Endpoint**: `GET /api/providers/medical-records/{recordId}`
**Description**: Get a specific medical record by ID

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Response (200 OK)**:
```json
{
  "medicalRecord": {
    "id": "record-uuid-123",
    "appointmentId": "appointment-uuid-456",
    "patientName": "John Doe",
    "recordType": "DIAGNOSIS",
    "content": "Hypertension - Stage 1. Patient shows elevated blood pressure readings.",
    "isPatientVisible": true,
    "releaseDate": "2024-01-15T16:00:00Z",
    "updatedAt": "2024-01-15T15:30:00Z",
    "updatedBy": "PR20240115001"
  }
}
```

#### **Error Responses**:
```json
// 404 Not Found
{
  "error": "NOT_FOUND",
  "message": "Medical record not found"
}

// 403 Forbidden
{
  "error": "FORBIDDEN",
  "message": "Access denied to this medical record"
}
```

### **9. Get Patient Medical Records API**

#### **Endpoint**: `GET /api/providers/patients/{patientId}/medical-records`
**Description**: Get all medical records for a specific patient

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| appointmentId | String | No | Filter by specific appointment |
| recordType | String | No | Filter by record type |
| limit | Integer | No | Number of results (default: 50, max: 100) |
| offset | Integer | No | Pagination offset (default: 0) |

#### **Response (200 OK)**:
```json
{
  "medicalRecords": [
    {
      "id": "record-uuid-123",
      "appointmentId": "appointment-uuid-456",
      "patientName": "John Doe",
      "recordType": "DIAGNOSIS",
      "content": "Hypertension - Stage 1",
      "isPatientVisible": true,
      "releaseDate": "2024-01-15T16:00:00Z",
      "createdAt": "2024-01-15T15:30:00Z",
      "updatedAt": "2024-01-15T15:30:00Z",
      "updatedBy": "PR20240115001"
    }
  ],
  "pagination": {
    "total": 1,
    "limit": 50,
    "offset": 0,
    "hasMore": false
  }
}
```

### **10. Health Check API**

#### **Endpoint**: `GET /health`
**Description**: Service health check endpoint

#### **Response (200 OK)**:
```json
{
  "status": "UP",
  "service": "provider-service",
  "timestamp": "2024-01-15T10:30:00Z",
  "version": "1.0.0"
}
```

## 🔐 **Access Control & Security**

### **JWT Token Requirements**
- **Provider APIs**: Require JWT with `role: "PROVIDER"`
- **Search APIs**: Require JWT with `role: "PATIENT"` or `role: "PROVIDER"`
- **Token Validation**: All requests validated by Auth Service
- **User Context**: JWT contains `sub` field with external user ID

### **Role-Based Access Control**
- **Provider Self-Service**: Providers can only access their own profile data
- **Patient Discovery**: Patients can search and view provider public information
- **Professional Information**: Only providers can update their professional credentials
- **Audit Trail**: All updates tracked with `updated_by` field

### **Data Privacy & HIPAA Compliance**
- **PHI Protection**: Personal health information protected per HIPAA guidelines
- **Audit Logging**: All data access and modifications logged
- **Access Controls**: Role-based permissions enforced at API level
- **Data Minimization**: Only necessary data exposed in API responses

## 📊 **Audit Trail Strategy**

### **Dual Audit Approach**
- **`updated_by` Fields**: Quick audit trail in each table
- **`audit_logs` Table**: Comprehensive audit history
- **JWT Integration**: Automatic population from JWT claims

### **JWT Integration for `updated_by` Field**

**Automatic Population:**
- **JWT Token Claims**: Contains `sub` field with external user ID
- **Server-Side Extraction**: Application automatically extracts user ID from JWT
- **Automatic Setting**: `updated_by` field set to extracted user ID on every update
- **No Client Input**: Client cannot control or fake the `updated_by` value

**JWT Token Structure:**
```json
{
  "sub": "supabase-uuid-from-auth",
  "role": "PROVIDER",
  "status": "ACTIVE",
  "iat": 1640995200,
  "exp": 1641081600
}
```

**Implementation Flow:**
1. **Client Request**: Sends JWT token in Authorization header
2. **JWT Validation**: Server validates token and extracts user ID
3. **Record Update**: `updated_by` automatically set to extracted user ID
4. **Audit Logging**: Action logged to `audit_logs` table
5. **Response**: Updated record returned with `updated_by` field

**Security Benefits:**
- **Audit Integrity**: Guaranteed accurate audit trail
- **No Tampering**: Client cannot modify `updated_by` value
- **HIPAA Compliance**: Meets healthcare audit requirements
- **Automatic Tracking**: No manual intervention required

### **Access Control for `updatedBy` Field**

#### **Provider API Responses:**
- **`updatedBy` field IS exposed** to providers in API responses
- **Clinical workflow** - Providers need to know who updated their data
- **Audit transparency** - Providers can see audit trail information
- **Professional responsibility** - Important for healthcare team coordination

#### **Patient API Responses:**
- **`updatedBy` field NOT exposed** to patients in API responses
- **Patient privacy** - Patients don't need to know who updated provider data
- **Clean interface** - Simpler API responses for patient clients

#### **Implementation Logic:**
```java
// Provider API - Show updatedBy field
if (userRole == "PROVIDER") {
    response.includeField("updatedBy");
}

// Patient API - Hide updatedBy field
if (userRole == "PATIENT") {
    response.removeField("updatedBy");
}
```

## 🔄 **Future Enhancements**

### **Email Verification Flow**
**Planned Feature**: Email verification for new provider registrations
- **Default Status**: New providers start as `INACTIVE`
- **Email Confirmation**: Required to activate account
- **Status Update**: Changed to `ACTIVE` after email verification
- **Implementation**: Future enhancement, not current requirement

### **Credential Verification**
**Planned Feature**: Automated credential verification
- **NPI Validation**: Real-time NPI number verification
- **License Verification**: State medical board integration
- **Certification Tracking**: Professional certification monitoring
- **Expiration Alerts**: Automated license expiration notifications

## ❓ **Q&A**

### **Common Questions**
**Q**: How do we handle provider credential verification?
**A**: Provider credentials are verified through professional license databases and certification validation processes.

**Q**: How do we ensure provider credential validation?
**A**: NPI numbers are validated against national databases, and license numbers are verified with state medical boards.

**Q**: What happens when a provider's license expires?
**A**: The system tracks license expiration dates and can suspend provider access when licenses are not renewed.

## 🔍 **Discussion Points**

### **1. Registration Flow Responsibility**
**Decision**: Gateway orchestrates provider registration (calls Supabase Auth + Provider Service)
- **Gateway**: Orchestrates complete registration process
- **Supabase Auth**: Handles authentication credentials
- **Provider Service**: Creates provider business profile

### **2. Provider Discovery**
**Question**: How should provider search and discovery work?
- **Search Criteria**: Specialty, location, availability, ratings
- **Filtering**: Advanced filters for patient preferences
- **Integration**: How to coordinate with appointment availability?
- **Decision Needed**: Search algorithm and ranking criteria



## 📚 **References**

- [System Design](system-design.md)
- [Database Design](database-design.md)
- [Patient Service Design](patient-service-design.md)
- [Service Design Template](service-design-template.md)

---

*This Provider Service design provides comprehensive healthcare provider management capabilities while maintaining clear service boundaries and healthcare compliance.*
