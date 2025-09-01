# Appointment Service Design

> **🎯 Learning Focus: Appointment Scheduling for Healthcare Microservices**
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

### **Implementation Phases**
- **Phase 1**: Core functionality (availability, basic booking, viewing)
- **Phase 2**: Advanced features (management, lifecycle, updates)

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
- `patients` - Patient-specific data
- `providers` - Provider-specific data
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
                    │ • patients      │
                    │ • providers     │
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

## 🛠️ **API Design**

### **Provider Availability Management**
| Method | Endpoint | Description | Auth | Phase |
|--------|----------|-------------|------|-------|
| POST   | `/api/appointments/availability` | Set my available slots | Provider | **Phase 1** |
| GET    | `/api/appointments/availability` | Get my availability | Provider | **Phase 1** |
| PUT    | `/api/appointments/availability/{slotId}` | Update slot | Provider | **Phase 2** |
| DELETE | `/api/appointments/availability/{slotId}` | Remove slot | Provider | **Phase 2** |

### **Patient Appointment Booking**
| Method | Endpoint | Description | Auth | Phase |
|--------|----------|-------------|------|-------|
| GET    | `/api/appointments/available-slots` | Find available slots | Public | **Phase 1** |
| POST   | `/api/appointments` | Book appointment | Patient | **Phase 1** |
| GET    | `/api/appointments` | View my appointments | Patient | **Phase 1** |

### **Appointment Management APIs**
| Method | Endpoint | Description | Auth | Phase |
|--------|----------|-------------|------|-------|
| GET    | `/api/appointments/provider/{providerId}` | Get provider's appointments | Provider | **Phase 2** |
| PUT    | `/api/appointments/{id}/status` | Change appointment status | Provider | **Phase 2** |
| PUT    | `/api/appointments/{id}/reschedule` | Reschedule appointment | Patient/Provider | **Phase 2** |
| DELETE | `/api/appointments/{id}` | Cancel appointment | Patient/Provider | **Phase 2** |

### **Appointment Lifecycle APIs**
| Method | Endpoint | Description | Auth | Phase |
|--------|----------|-------------|------|-------|
| POST  | `/api/appointments/{id}/checkin` | Patient check-in | Patient | **Phase 2** |
| POST  | `/api/appointments/{id}/complete` | Mark appointment complete | Provider | **Phase 2** |



### **Request/Response Examples**

#### **1. Provider Sets Available Slots**
```json
POST /api/appointments/availability
{
    "dayOfWeek": 1,           // Monday
    "startTime": "09:00:00",  // 9 AM
    "endTime": "17:00:00",    // 5 PM
    "durationMinutes": 30,    // 30-minute slots
    "recurringWeeks": 4       // Create for next 4 weeks
}
```

**Response**: System creates 16 available slots (9:00, 9:30, 10:00, ..., 16:30)

#### **2. Patient Finds Available Slots**
```json
GET /api/appointments/available-slots?providerId=uuid&date=2024-01-15
```

**Response**: List of available 30-minute slots for the requested date

#### **3. Patient Books Appointment**
```json
POST /api/appointments
{
    "providerId": "uuid",
    "slotTime": "2024-01-15T10:00:00Z",
    "notes": "Annual checkup"
}
```

**Response**: Appointment confirmation with booking details

## 🗄️ **Database Schema Design**

### **Appointment Service Database Tables**

#### **Appointments Table (appointments)**
| Column       | Type        | Constraints | Indexes | Description |
|--------------|-------------|-------------|---------|-------------|
| provider_id  | UUID        | PRIMARY KEY | PRIMARY | Reference to providers.id |
| slot_time    | TIMESTAMP   | SORT KEY | INDEX | Time slot for appointment |
| slot_id      | UUID        | UNIQUE | INDEX | Unique appointment identifier |
| patient_id   | UUID        | FOREIGN KEY, NULL | INDEX | Reference to patients.id (NULL for available slots) |
| status       | APPOINTMENT_STATUS | NOT NULL, DEFAULT AVAILABLE | INDEX | Appointment status |
| duration_minutes | INTEGER | NOT NULL, DEFAULT 30 | - | Appointment duration in minutes |
| notes        | TEXT        | NULL | - | Appointment notes and status change notes |
| created_at   | TIMESTAMP   | NOT NULL, DEFAULT NOW() | - | Record creation timestamp |
| updated_at   | TIMESTAMP  | NOT NULL, DEFAULT NOW() | - | Last update timestamp |

**ENUM: APPOINTMENT_STATUS**
- `AVAILABLE` - Provider available slot (no patient assigned)
- `SCHEDULED` - Appointment is scheduled
- `CONFIRMED` - Appointment is confirmed
- `COMPLETED` - Appointment is completed
- `CANCELLED` - Appointment is cancelled
- `NO_SHOW` - Patient did not show up

**Indexes**:
- `PRIMARY KEY` (provider_id, slot_time) - Composite primary key
- `UNIQUE` (slot_id) - Unique appointment identifier
- `INDEX` (patient_id) - For patient appointment lookup
- `INDEX` (status) - For status-based queries

## ❓ **Q&A**

### **Common Questions**
**Q**: How do we prevent double-booking appointments?
**A**: The service uses database constraints and real-time availability checking to prevent scheduling conflicts.

**Q**: What happens when a patient cancels an appointment?
**A**: The appointment status is updated to CANCELLED, the time slot becomes available, and notifications are sent to the provider.

**Q**: How do we handle appointment reminders?
**A**: The service automatically sends reminders based on configured timing (24h, 1h before appointment) via email, SMS, or push notifications.

## 🔍 **Discussion Points**

### **1. Availability Management Strategy**
**Question**: How should provider availability be managed?
- **Fixed Schedule**: Providers set fixed weekly schedules
- **Dynamic Availability**: Providers can update availability in real-time
- **Block Booking**: Providers can block specific time periods
- **Decision Needed**: Availability management approach

### **2. Appointment Booking Flow**
**Question**: How should the appointment booking process work?
- **Real-time Booking**: Immediate confirmation and booking
- **Request-based**: Patient requests, provider confirms
- **Hybrid Approach**: Real-time for some types, request-based for others
- **Decision Needed**: Booking flow strategy

### **3. Notification Strategy**
**Question**: How should appointment notifications be handled?
- **Email Only**: Simple email notifications
- **Multi-channel**: Email, SMS, push notifications
- **Configurable**: Providers/patients can choose notification preferences
- **Decision Needed**: Notification approach and channels

## 📚 **References**

- [System Design](system-design.md)
- [Database Design](database-design.md)
- [Patient Service Design](patient-service-design.md)
- [Provider Service Design](provider-service-design.md)
- [Service Design Template](service-design-template.md)

---

*This Appointment Service design provides comprehensive appointment scheduling capabilities with clear APIs and healthcare compliance.*
