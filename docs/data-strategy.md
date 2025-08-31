# Healthcare AI Microservices - Data Strategy

> **Clear, Consistent Data Architecture** - Resolving all contradictions

## 📋 **Document Information**

- **Document Title**: Data Strategy for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Final

## 🎯 **Executive Summary**

### **The Problem**
Our documentation contains multiple conflicting data strategies:
- ❌ "Single Neon PostgreSQL for all business data"
- ❌ "Supabase for authentication"
- ❌ "No auth tables in database"
- ❌ But schema includes user management tables

### **The Solution**
**Option A - Stateless JWT Only with Business User Data**

## 🏗️ **Consistent Data Strategy**

### **1. Authentication Strategy**
- **External Authentication Provider**: Supabase Auth, Auth0, or similar
- **Auth Service**: Purely stateless JWT validation only
- **No Internal User Management**: All user auth handled by external provider
- **No Auth Tables in Our Database**: Authentication data stays with external provider

### **2. Database Strategy**
- **Primary Database**: Neon PostgreSQL for all business data
- **User-Related Tables**: Business user data (profiles, roles, permissions) stored in Neon
- **No Authentication Tables**: Login credentials, passwords, auth sessions NOT stored
- **Clear Separation**: Auth ≠ User Data

### **3. Data Storage Architecture**
```
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL AUTH PROVIDER                   │
│                (Supabase Auth, Auth0, etc.)                │
│                                                             │
│ • User login/logout                                        │
│ • Password management                                      │
│ • JWT token generation                                     │
│ • Authentication sessions                                  │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    OUR HEALTHCARE PLATFORM                  │
│                                                             │
│ ┌─────────────────┐    ┌─────────────────┐                │
│ │   Auth Service  │    │  Business Data  │                │
│ │  (Port 8001)    │    │   in Neon DB    │                │
│ │                 │    │                 │                │
│ │ • JWT validation│    │ • User profiles │                │
│ │ • User context  │    │ • Patient data  │                │
│ │ • No database   │    │ • Provider data │                │
│ │ • No auth data  │    │ • Appointments  │                │
│ └─────────────────┘    │ • File metadata │                │
│                        └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

## 🔐 **Authentication Flow**

### **Step 1: User Authentication**
1. User goes to external auth provider (Supabase Auth, Auth0, etc.)
2. User logs in with credentials
3. External provider validates credentials
4. External provider generates JWT token with user info and roles
5. JWT token returned to frontend

### **Step 2: Request Processing**
1. Frontend sends request with JWT token
2. Gateway receives request and extracts token
3. Gateway calls Auth Service to validate JWT
4. Auth Service validates token (signature, expiration, etc.)
5. If valid, user context extracted and passed to business service
6. If invalid, request rejected with 401

### **Step 3: Business Logic**
1. Business service receives validated user context
2. Business service uses user ID/role for data access
3. Business service queries Neon database for business data
4. Business service returns data to client

## 🗄️ **Database Schema Strategy**

### **What Goes in Neon Database**
✅ **User Business Data**:
- User profiles (name, contact info, preferences)
- User roles and permissions for business logic
- Patient records and medical history
- Provider profiles and schedules
- Appointment data and scheduling
- File metadata and references
- Audit logs and business events

❌ **What Does NOT Go in Neon Database**:
- User login credentials (passwords)
- Authentication sessions
- JWT token storage
- Login/logout history
- Password reset tokens
- MFA secrets

### **Database Schema Structure**
```sql
-- Business user data (NOT authentication)
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_user_id VARCHAR(255) NOT NULL, -- From external auth provider
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role user_role NOT NULL DEFAULT 'PATIENT',
    -- ... other business fields
);

-- Patient business data
CREATE TABLE patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id),
    patient_number VARCHAR(50) UNIQUE NOT NULL,
    medical_history JSONB,
    -- ... other patient fields
);

-- Provider business data
CREATE TABLE providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id),
    provider_number VARCHAR(50) UNIQUE NOT NULL,
    license_number VARCHAR(100) NOT NULL,
    -- ... other provider fields
);
```

## 🔗 **Integration Points**

### **External Auth Provider Integration**
- **Frontend**: Login/logout forms, password reset, registration
- **Backend**: JWT token validation only
- **No Database Sync**: We don't replicate auth data

### **Internal Data Access**
- **Business Services**: Direct access to Neon database
- **Shared Data Layer**: Common database access patterns
- **User Context**: Extracted from JWT, used for business logic

## 📊 **Data Flow Examples**

### **Patient Login Flow**
```
1. User → Supabase Auth → Login → JWT Token
2. Frontend → Gateway (8080) → JWT Token
3. Gateway → Auth Service (8001) → Validate JWT
4. Gateway → Patient Service (8002) → User Context
5. Patient Service → Neon DB → Patient Data
6. Patient Service → Frontend → Patient Records
```

### **Provider Dashboard Flow**
```
1. Provider → Supabase Auth → Login → JWT Token
2. Frontend → Gateway (8080) → JWT Token
3. Gateway → Auth Service (8001) → Validate JWT
4. Gateway → Provider Service (8003) → User Context
5. Provider Service → Neon DB → Provider + Patient Data
6. Provider Service → Frontend → Dashboard Data
```

## ⚠️ **Key Points to Remember**

### **DO**
- ✅ Use external auth provider for login/logout
- ✅ Store business user data in Neon database
- ✅ Use JWT tokens for authentication
- ✅ Validate JWT tokens in Auth Service
- ✅ Keep Auth Service stateless

### **DON'T**
- ❌ Store passwords or auth credentials
- ❌ Implement user login/logout internally
- ❌ Store JWT tokens in database
- ❌ Sync auth data with business data
- ❌ Mix authentication with business logic

## 🔄 **Migration Strategy**

### **Phase 1: Clean Up Documentation**
- [x] Update authentication design (completed)
- [ ] Update system design document
- [ ] Update data layer architecture
- [ ] Remove conflicting information

### **Phase 2: Implementation**
- [ ] Choose external auth provider (Supabase Auth recommended)
- [ ] Implement stateless Auth Service
- [ ] Design Neon database schema for business data
- [ ] Update business services to use user context

### **Phase 3: Testing & Validation**
- [ ] Test external auth integration
- [ ] Validate JWT token flow
- [ ] Test business data access with user context
- [ ] Verify no auth data in our database

## 📋 **Success Criteria**

- [ ] No contradictions in documentation
- [ ] Clear separation between auth and business data
- [ ] External auth provider handles all authentication
- [ ] Auth Service is purely stateless
- [ ] Business services use validated user context
- [ ] Neon database contains only business data

---

## 🔗 **Related Documents**

- [Authentication Design](authentication-design.md) - JWT validation strategy
- [System Design](system-design.md) - Overall architecture
- [Data Layer Architecture](data-layer-architecture.md) - Database design

---

*This document resolves all data strategy contradictions and provides a clear, consistent approach for the healthcare AI microservices platform.*
