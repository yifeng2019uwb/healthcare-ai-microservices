# Appointment Service Design

> **🎯 Professional Focus: Appointment Scheduling for Healthcare Microservices**
>
> This document defines the design for the Appointment Service.
> **Design Philosophy**: Comprehensive appointment management with clear APIs and scheduling logic.

## 📋 **Document Information**

- **Document Title**: Appointment Service Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Draft
- **Service**: Appointment Service
- **Port**: 8004

## 🎯 **Overview**

### **What This Service Is**
The Appointment Service manages all appointment-related operations, including scheduling, availability management, calendar operations, reminders, and appointment lifecycle management. It serves as the central hub for healthcare appointment coordination.

### **Business Value & Impact**
Appointment scheduling is critical for healthcare operations, enabling efficient patient-provider coordination, reducing wait times, and improving healthcare access. This service ensures smooth appointment management and patient experience.

### **Scope**
- **In Scope**: Appointment management, status changes, rescheduling, check-in, completion
- **Out of Scope**: Availability management (will be implemented in future), notifications (will be implemented in future), medical records (handled by Provider Service), billing and payments, user authentication (handled by Auth Service)

### **Service Responsibilities**
- **✅ Appointment Service**: All appointment-related functionality
  - Provider availability windows and slot generation
  - Patient appointment booking and management
  - Appointment lifecycle and status management
  - Scheduling logic and conflict prevention
- **❌ Provider Service**: Provider profiles, credentials, medical records
- **❌ Patient Service**: Patient profiles and medical history viewing



## 📚 **Definitions & Glossary**

### **Key Terms**
- **HIPAA**: Health Insurance Portability and Accountability Act - federal law protecting patient health information
- **Appointment**: Scheduled meeting between patient and healthcare provider
- **Availability**: Provider's available time slots for appointments
- **Calendar**: Provider's schedule and appointment management
- **Appointment Lifecycle**: Complete journey from booking to completion
- **Reminders**: Automated notifications for upcoming appointments

## 👥 **User Case**

### **Primary User Types**
- **Patients**: Individuals who book and manage appointments
- **Providers**: Healthcare professionals who manage their schedules


### **User Case**

#### **User Case 1: Appointment Management**
Patients and providers need to view and manage appointments. Patients can see their upcoming appointments, and providers can see their patient appointments.

#### **User Case 2: Appointment Status Changes**
Providers need to update appointment status (confirm, cancel, complete) with notes to track the appointment lifecycle and communicate with patients.

#### **User Case 3: Appointment Rescheduling**
Patients and providers need to reschedule appointments when needed, updating the scheduled time while maintaining appointment history.

#### **User Case 4: Patient Check-in**
Patients need to check in for their appointments, and providers need to mark appointments as complete after the visit.

## 🔧 **Solution Alternatives**

### **Shared Infrastructure**
*Reference: System Design Document for complete infrastructure details*

**Key Infrastructure**: PostgreSQL Database, Spring Boot Framework, Shared Data Layer Module, Authentication Service, API Gateway, Docker, Railway Deployment

### **Appointment Service Design Approach**
**Description**: Practical appointment service that meets current scope while allowing future scaling.

**Database Tables**:
- `user_profiles` - Core user information
- `patient_profiles` - Patient-specific data
- `provider_profiles` - Provider-specific data
- `appointments` - Unified slots and appointments (provider_id + slot_time as composite PK)

**Core Workflow**:
- View appointments (patient and provider views)
- Update appointment status with notes
- Reschedule appointments when needed
- Patient check-in and appointment completion

## 🏗️ **High-Level Design**

### **Core Concept**
Spring Boot service managing healthcare appointment scheduling, availability, and lifecycle management. Handles all appointment-related functionality including provider availability windows and patient booking.

### **Key Components**
- **Appointment Service**: Appointment scheduling and management (Port 8004)
- **Availability Management**: Provider availability windows and time slot generation
- **Booking System**: Patient appointment booking and management
- **Appointment Lifecycle**: Scheduling, confirmation, completion, cancellation

### **Data Flow**
1. **Provider Availability**: Provider sets availability window → System generates 30-minute slots → Slots stored as AVAILABLE
2. **Patient Discovery**: Patient searches for providers → Views available time slots → Books specific appointment
3. **Appointment Lifecycle**: Booking → Confirmation → Check-in → Completion → History
4. **Slot Management**: Available slots → Booked appointments → Status updates

**Data Flow Diagrams**:

```
Appointment Service Component Architecture:
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Auth Service   │    │ Appointment Svc │
│                 │    │                 │    │                 │
│ • Route requests│    │ • JWT validation│    │ • Booking mgmt  │
│ • Load balance  │    │ • User context  │    │ • Availability  │
│ • Rate limiting │    │ • Role checking │    │ • Lifecycle     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Shared Database│
                    │                 │
                    │ • appointments  │
                    │ • user_profiles │
                    │ • patient_profiles │
                    │ • provider_profiles │
                    └─────────────────┘
```

```
Key Data Flow - Appointment Booking:
Patient → Gateway → Auth → Appointment Service → Database
  │        │        │         │                │
  │        │        │         │                └── Create appointment record
  │        │        │         │                │   (patient_id + provider_id + slot_time)
  │        │        │         │                │
  │        │        │         │                └── Update slot status to SCHEDULED
  │        │        │         │
  │        │        │         └── Return booking confirmation
  │        │        │
  │        │        └── Validate JWT + patient role
  │        │        │
  │        │        └── If validation fails → Return 401/403 error
  │        │
  │        └── Route booking request
  │
  └── Submit booking request
```

```
Key Data Flow - Provider Availability Management:
Provider → Gateway → Auth → Appointment Service → Database
  │         │        │         │                │
  │         │        │         │                └── Create available slots
  │         │        │         │                │   (provider_id + slot_time + status=AVAILABLE)
  │         │        │         │                │
  │         │        │         │                └── Update existing slots if needed
  │         │        │         │
  │         │        │         └── Return availability confirmation
  │         │        │
  │         │        └── Validate JWT + provider role
  │         │        │
  │         │        └── If validation fails → Return 401/403 error
  │         │
  │         └── Route availability request
  │
  └── Submit availability schedule
```

## 🛠️ **Detailed API Design**

### **1. Book Appointment API**

#### **Endpoint**: `PUT /api/appointments/{id}`
**Description**: Book an existing appointment slot with a healthcare provider

#### **Authentication**:
- **Required**: JWT Token with `role: "PATIENT"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "notes": "Annual checkup and blood pressure review"
}
```

#### **Field Specifications**:
| Field           | Type | Required | Pattern/Validation |
|-----------------|------|----------|-------------------|
| notes           | String | No | Max 1000 characters |

#### **Response (200 OK)**:
```json
{
  "appointment": {
    "id": "appointment-uuid-456",
    "providerName": "Dr. Sarah Johnson",
    "scheduledAt": "2024-01-20T14:00:00Z",
    "checkinTime": null,
    "status": "SCHEDULED",
    "appointmentType": "REGULAR_CONSULTATION",
    "notes": "Annual checkup and blood pressure review",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid request data: Appointment slot is no longer available"
}

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

// 409 Conflict
{
  "error": "CONFLICT",
  "message": "Time slot is no longer available"
}

// 500 Internal Server Error
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred"
}
```

### **2. Search Available Slots API**

#### **Endpoint**: `GET /api/appointments/available-slots`
**Description**: Patients search for available appointment slots

#### **Authentication**:
- **Required**: None (Public endpoint for slot discovery)

#### **Query Parameters**:
| Parameter  | Type | Required | Description |
|------------|------|----------|-------------|
| providerId | String | No | Filter by specific provider (business ID) |
| date       | String | No | Specific date (YYYY-MM-DD) |
| startDate  | String | No | Start date for range (YYYY-MM-DD) |
| endDate    | String | No | End date for range (YYYY-MM-DD) |
| appointmentType | String | No | Filter by appointment type |

#### **Response (200 OK)**:
```json
{
  "availableSlots": [
    {
      "id": "appointment-uuid-001",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T09:00:00Z",
      "appointmentType": "REGULAR_CONSULTATION",
      "status": "AVAILABLE"
    },
    {
      "id": "appointment-uuid-002",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T09:30:00Z",
      "appointmentType": "REGULAR_CONSULTATION",
      "status": "AVAILABLE"
    }
  ],
  "totalSlots": 2
}
```

### **3. Get Patient Appointments API**

#### **Endpoint**: `GET /api/appointments`
**Description**: Get all appointments for the authenticated patient

#### **Authentication**:
- **Required**: JWT Token with `role: "PATIENT"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | String | No | Filter by appointment status |
| limit | Integer | No | Number of results (default: 20, max: 100) |
| offset | Integer | No | Pagination offset (default: 0) |

#### **Response (200 OK)**:
```json
{
  "appointments": [
    {
      "id": "appointment-uuid-456",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T14:00:00Z",
      "checkinTime": null,
      "status": "SCHEDULED",
      "appointmentType": "REGULAR_CONSULTATION",
      "notes": "Annual checkup and blood pressure review",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "total": 1,
    "limit": 20,
    "offset": 0,
    "hasMore": false
  }
}
```

### **4. Patient Check-in API**

#### **Endpoint**: `PUT /api/appointments/{id}/checkin`
**Description**: Patient checks in for their appointment

#### **Authentication**:
- **Required**: JWT Token with `role: "PATIENT"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Response (200 OK)**:
```json
{
  "appointment": {
    "id": "appointment-uuid-456",
    "providerName": "Dr. Sarah Johnson",
    "scheduledAt": "2024-01-20T14:00:00Z",
    "checkinTime": "2024-01-20T14:05:00Z",
    "status": "IN_PROGRESS",
    "appointmentType": "REGULAR_CONSULTATION",
    "notes": "Annual checkup and blood pressure review",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-20T14:05:00Z"
  }
}
```

### **5. Update Appointment Status API**

#### **Endpoint**: `PUT /api/appointments/{id}/status`
**Description**: Provider updates appointment status (confirm, complete, cancel, no-show)

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "status": "COMPLETED",
  "notes": "Patient completed annual checkup. Blood pressure normal. Follow-up in 6 months."
}
```

#### **Field Specifications**:
| Field | Type | Required | Pattern/Validation |
|-------|------|----------|-------------------|
| status | String | Yes | SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW |
| notes | String | No | Max 1000 characters |

#### **Response (200 OK)**:
```json
{
  "appointment": {
    "id": "appointment-uuid-456",
    "providerId": "PR20240115001",
    "providerName": "Dr. Sarah Johnson",
    "patientId": "P20240115001",
    "scheduledAt": "2024-01-20T14:00:00Z",
    "checkinTime": "2024-01-20T14:05:00Z",
    "status": "COMPLETED",
    "appointmentType": "REGULAR_CONSULTATION",
    "notes": "Patient completed annual checkup. Blood pressure normal. Follow-up in 6 months.",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-20T15:30:00Z",
    "updatedBy": "PR20240115001"
  }
}
```

### **6. Get Provider Appointments API**

#### **Endpoint**: `GET /api/appointments/provider/{providerId}`
**Description**: Get all appointments for a specific provider

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | String | No | Filter by appointment status |
| date | String | No | Filter by specific date (YYYY-MM-DD) |
| limit | Integer | No | Number of results (default: 50, max: 100) |
| offset | Integer | No | Pagination offset (default: 0) |

#### **Response (200 OK)**:
```json
{
  "appointments": [
    {
      "id": "appointment-uuid-456",
      "providerId": "PR20240115001",
      "providerName": "Dr. Sarah Johnson",
      "patientId": "P20240115001",
      "patientName": "John Doe",
      "scheduledAt": "2024-01-20T14:00:00Z",
      "checkinTime": "2024-01-20T14:05:00Z",
      "status": "COMPLETED",
      "appointmentType": "REGULAR_CONSULTATION",
      "notes": "Patient completed annual checkup. Blood pressure normal. Follow-up in 6 months.",
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-20T15:30:00Z"
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

### **7. Cancel Appointment API**

#### **Endpoint**: `PUT /api/appointments/{id}/cancel`
**Description**: Cancel an appointment (available to both patients and providers)

#### **Authentication**:
- **Required**: JWT Token with `role: "PATIENT"` or `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "reason": "Patient requested cancellation due to illness"
}
```

#### **Response (200 OK)**:
```json
{
  "appointment": {
    "id": "appointment-uuid-456",
    "providerName": "Dr. Sarah Johnson",
    "scheduledAt": "2024-01-20T14:00:00Z",
    "checkinTime": null,
    "status": "CANCELLED",
    "appointmentType": "REGULAR_CONSULTATION",
    "notes": "Patient requested cancellation due to illness",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-19T16:45:00Z"
  }
}
```

### **8. Set Provider Availability API**

#### **Endpoint**: `POST /api/appointments/availability`
**Description**: Provider sets their availability for a specific date. Business logic automatically creates appointment slots based on appointment type duration.

#### **Authentication**:
- **Required**: JWT Token with `role: "PROVIDER"`
- **Header**: `Authorization: Bearer <jwt_token>`

#### **Request Body**:
```json
{
  "date": "2024-01-20",
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "appointmentType": "REGULAR_CONSULTATION"
}
```

#### **Field Specifications**:
| Field           | Type | Required | Pattern/Validation |
|-----------------|------|----------|-------------------|
| date            | String | Yes | Date in YYYY-MM-DD format |
| startTime       | String | Yes | Time in HH:MM:SS format (24-hour) |
| endTime         | String | Yes | Time in HH:MM:SS format (24-hour) |
| appointmentType | String | Yes | REGULAR_CONSULTATION (30min), FOLLOW_UP (15min), NEW_PATIENT_INTAKE (60min), PROCEDURE_CONSULTATION (45min) |

#### **Business Logic**:
- **REGULAR_CONSULTATION**: Creates 30-minute slots (9:00, 9:30, 10:00, etc.)
- **FOLLOW_UP**: Creates 15-minute slots (9:00, 9:15, 9:30, etc.)
- **NEW_PATIENT_INTAKE**: Creates 60-minute slots (9:00, 10:00, 11:00, etc.)
- **PROCEDURE_CONSULTATION**: Creates 45-minute slots (9:00, 9:45, 10:30, etc.)

#### **Response (201 Created)**:
```json
{
  "appointments": [
    {
      "id": "appointment-uuid-001",
      "providerId": "PR20240115001",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T09:00:00Z",
      "appointmentType": "REGULAR_CONSULTATION",
      "status": "AVAILABLE",
      "createdAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "appointment-uuid-002",
      "providerId": "PR20240115001",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T09:30:00Z",
      "appointmentType": "REGULAR_CONSULTATION",
      "status": "AVAILABLE",
      "createdAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": "appointment-uuid-003",
      "providerId": "PR20240115001",
      "providerName": "Dr. Sarah Johnson",
      "scheduledAt": "2024-01-20T10:00:00Z",
      "appointmentType": "REGULAR_CONSULTATION",
      "status": "AVAILABLE",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

#### **Error Responses**:
```json
// 400 Bad Request
{
  "error": "BAD_REQUEST",
  "message": "Invalid time range: endTime must be after startTime"
}

// 409 Conflict
{
  "error": "CONFLICT",
  "message": "Availability already exists for this date and time range"
}
```

### **9. Health Check API**

#### **Endpoint**: `GET /health`
**Description**: Service health check endpoint

#### **Response (200 OK)**:
```json
{
  "status": "UP",
  "service": "appointment-service",
  "timestamp": "2024-01-15T10:30:00Z",
  "version": "1.0.0"
}
```



## 🔐 **Access Control & Security**

### **JWT Token Requirements**
- **Patient APIs**: Require JWT with `role: "PATIENT"`
- **Provider APIs**: Require JWT with `role: "PROVIDER"`
- **Mixed APIs**: Some endpoints accept both patient and provider roles
- **Token Validation**: All requests validated by Auth Service
- **User Context**: JWT contains `sub` field with external user ID

### **Role-Based Access Control**
- **Patient Self-Service**: Patients can only access their own appointments
- **Provider Management**: Providers can manage appointments for their patients
- **Cross-Role Operations**: Both patients and providers can cancel appointments
- **Audit Trail**: All updates tracked with `updated_by` field

### **Data Privacy & HIPAA Compliance**
- **PHI Protection**: Appointment data protected per HIPAA guidelines
- **Audit Logging**: All appointment actions logged
- **Access Controls**: Role-based permissions enforced at API level
- **Data Minimization**: Only necessary data exposed in API responses

## 📊 **Audit Trail Strategy**

### **Dual Audit Approach**
- **`updated_by` Fields**: Quick audit trail in appointments table
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
  "role": "PATIENT",
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

#### **Patient API Responses:**
- **`updatedBy` field NOT exposed** to patients in API responses
- **Internal audit field** - Used only for server-side audit trails
- **Patient privacy** - Patients don't need to know who updated their appointments
- **Clean interface** - Simpler API responses for patient clients

#### **Provider API Responses:**
- **`updatedBy` field IS exposed** to providers in API responses
- **Clinical workflow** - Providers need to know who updated appointment data
- **Audit transparency** - Providers can see audit trail information
- **Professional responsibility** - Important for healthcare team coordination

#### **Implementation Logic:**
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

## 🔄 **Future Enhancements**

### **Availability Management**
**Planned Feature**: Provider availability and slot management
- **Availability Windows**: Providers set their available time slots
- **Slot Generation**: Automatic generation of appointment slots
- **Conflict Prevention**: Real-time availability checking
- **Recurring Schedules**: Weekly/monthly availability patterns

### **Notification System**
**Planned Feature**: Automated appointment notifications
- **Appointment Reminders**: 24h and 1h before appointment
- **Status Updates**: Notifications for appointment changes
- **Multi-channel**: Email, SMS, push notifications
- **Configurable**: User preference management

## 🗄️ **Database Schema Reference**

### **Appointment Service Database Tables**

**Reference**: See [Database Design](database-design.md) for complete table definitions and relationships.

#### **Key Tables Used by Appointment Service**:
- **`appointments`** - Core appointment data with `checkin_time` field
- **`user_profiles`** - User information for patients and providers
- **`patient_profiles`** - Patient-specific data
- **`provider_profiles`** - Provider-specific data
- **`audit_logs`** - Comprehensive audit trail

#### **Key Features**:
- **`checkin_time`** - Tracks when patient actually arrives vs scheduled time
- **Status Management** - Complete appointment lifecycle tracking
- **Audit Trail** - All changes tracked with `updated_by` field
- **Timezone Support** - All timestamps use `TIMESTAMPTZ`
- **Flexible Data** - `custom_data` JSONB for extensibility

## ❓ **Q&A**

### **Common Questions**
**Q**: How do we prevent double-booking appointments?
**A**: The service uses database constraints and real-time availability checking to prevent scheduling conflicts. The composite index on `(provider_id, status, scheduled_at)` enables efficient conflict detection.

**Q**: What happens when a patient cancels an appointment?
**A**: The appointment status is updated to CANCELLED, the `updated_by` field tracks who cancelled it, and the change is logged in the audit system. The time slot becomes available for rebooking.

**Q**: How does the check-in process work?
**A**: Patients use the `PUT /api/appointments/{id}/checkin` API to check in. The `checkin_time` field is automatically set to the current timestamp, and the status changes to IN_PROGRESS.

**Q**: How do we handle appointment reminders?
**A**: This is a planned future feature. The service will automatically send reminders based on configured timing (24h, 1h before appointment) via email, SMS, or push notifications.

**Q**: What's the difference between `scheduled_at` and `checkin_time`?
**A**: `scheduled_at` is when the appointment was originally scheduled (e.g., 2:00 PM). `checkin_time` is when the patient actually arrived (e.g., 2:15 PM). This allows tracking of no-shows and actual service times.

## 🔍 **Implementation Notes**

### **1. Appointment Validation Rules (Base Cases)**

#### **Provider Slot Creation Rules**
- **Minimum Advance Notice**: Providers must create appointment slots at least 1 day in advance
- **Database Constraint**: `scheduled_at > created_at + INTERVAL '1 day'`
- **Business Logic**: Ensures adequate preparation time for appointments

#### **Patient Booking Rules**
- **Booking Window**: Patients can book appointments 15-30 minutes before scheduled time
- **Maximum Advance**: Patients can book up to 30 days in advance
- **Real-time Validation**: Check availability at booking time

#### **Cancellation Rules**
- **Patient Cancellation**: Must cancel at least 24-48 hours before appointment
- **Provider Cancellation**: Can cancel up to 30 minutes before appointment
- **Emergency Override**: Special handling for urgent medical situations

#### **Validation Methods (Entity Level)**
- **Provider Slot Creation**: Validate 1 day advance notice
- **Patient Booking Window**: Validate 15-30 minute + 30 day window
- **Patient Cancellation**: Validate 24+ hour notice
- **Provider Cancellation**: Validate 30+ minute notice

### **2. Appointment Booking Flow**
**Current Implementation**: Real-time booking with immediate confirmation
- **Direct Booking**: Patients can book available slots immediately
- **Conflict Prevention**: Database constraints prevent double-booking
- **Status Tracking**: Complete appointment lifecycle management
- **Future Enhancement**: Request-based booking for complex appointments

### **2. Check-in and No-Show Management**
**Current Implementation**: Dual timestamp tracking
- **Scheduled Time**: `scheduled_at` for appointment scheduling
- **Actual Arrival**: `checkin_time` for real service time
- **No-Show Detection**: Automatic detection when `checkin_time` is null after grace period
- **Service Analytics**: Track actual vs scheduled service times

### **3. Audit and Compliance**
**Current Implementation**: Comprehensive audit trail
- **Dual Audit**: `updated_by` fields + `audit_logs` table
- **JWT Integration**: Automatic audit field population
- **HIPAA Compliance**: All appointment actions logged
- **Role-Based Access**: Different audit visibility for patients vs providers

## 📚 **References**

- [System Design](system-design.md)
- [Database Design](database-design.md)
- [Patient Service Design](patient-service-design.md)
- [Provider Service Design](provider-service-design.md)
- [Service Design Template](service-design-template.md)

---

*This Appointment Service design provides comprehensive appointment scheduling capabilities with clear APIs and healthcare compliance.*
