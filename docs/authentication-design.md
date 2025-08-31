# Healthcare AI Microservices - Authentication Design

> **Simple, Secure Authentication Strategy** - Internal Auth Service with JWT tokens

## 📋 **Document Information**

- **Document Title**: Authentication Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Draft

## 🎯 **Overview**

### **What This Is**
This design defines the authentication approach for healthcare AI microservices using an internal Auth Service that **only validates JWT tokens** and provides user context. The Auth Service is purely stateless - no database tables, no user storage, no credential validation, just JWT validation logic.

### **Why This Matters**
Secure authentication is critical for healthcare applications to protect patient data, ensure proper access control, and maintain HIPAA compliance. This stateless approach provides centralized authentication without external dependencies while keeping the Auth Service lightweight and focused.

### **Scope**
- **In Scope**: JWT token validation, user context extraction, role-based access control, basic security
- **Out of Scope**: User login/logout, credential validation, user management operations, database storage, complex authorization rules, enterprise SSO integration

## 🏗️ **High-Level Design**

### **Core Concept**
Internal Auth Service positioned between the Gateway and backend services, **only validating JWT tokens** and providing user context. The Auth Service is completely stateless - no database, no user storage, no credential validation, just JWT validation logic.

**User Authentication Flow**:
1. **External Authentication**: Users authenticate via external provider (Supabase Auth, Auth0, etc.)
2. **JWT Token**: External provider issues JWT token with user info and roles
3. **Auth Service**: Only validates JWT tokens, extracts user context
4. **Business Services**: Use validated user context for business logic
5. **User Data**: Stored in Neon database for business services (Patient, Provider, etc.)

### **Key Components**
- **Auth Service**: JWT validation only, no business logic, no database tables, no user management (Port 8001)
- **JWT Tokens**: Secure, stateless authentication with embedded user info (issued by external provider)
- **Role-Based Access**: User roles extracted from JWT token claims
- **Gateway Integration**: Authentication middleware in Spring Cloud Gateway
- **Business Services**: Handle business logic and can call each other internally
- **External Auth Provider**: Handles user login, registration, password reset (Supabase Auth, Auth0, etc.)

### **Data Flow**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Patient Web   │    │  Provider Web   │    │   Admin Portal  │
│    (React)      │    │   (React)       │    │    (React)      │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                          ┌────────────▼────────────┐
                          │   Spring Cloud Gateway  │
                          │      Port 8080          │
                          │   (EXTERNAL ENTRY)      │
                          └────────────┬───────────┘
                                       │
                          ┌────────────▼────────────┐
                          │      Auth Service       │
                          │        Port 8001        │
                          │   (INTERNAL ONLY)       │
                          └────────────┬───────────┘
                                       │
               ┌───────────────────────┼────────────────────────┐
               │                       │                        │
          ┌────▼────┐    ┌──────────┐    ┌────▼──────┐    ┌──────────┐
          │ Patient │    │Provider  │    │Appointment│    │   AI     │
          │Service  │    │Service   │    │ Service   │    │ Service  │
          │ Port    │    │ Port     │    │ Port      │    │ Port     │
          │ 8002    │    │ 8003     │    │ 8004      │    │ 8005     │
          │(Java)   │    │(Java)    │    │(Java)     │    │(Python)  │
          └─────────┘    └──────────┘    └───────────┘    └──────────┘
```

## 🔧 **Technical Approach**

### **Technology Choice**
- **Why JWT**: Stateless, scalable, widely supported
- **Why Internal Service**: Full control, no external dependencies, healthcare compliance
- **Why Gateway Integration**: Centralized authentication, consistent across all services
- **Why Neon PostgreSQL**: Single database for all data including authentication tables

### **Database Strategy**
- **Auth Service**: No database tables, completely stateless, only JWT validation
- **Business Services**: Use Neon PostgreSQL for all business data including user-related tables
- **User Data Storage**: Patient records, provider profiles, appointments stored in Neon database
- **External Auth Provider**: Handles user authentication, registration, password reset (Supabase Auth, Auth0, etc.)
- **No Internal User Management**: All user management operations handled by external provider

### **Integration Points**
- **Gateway**: Authentication middleware validates tokens
- **Auth Service**: JWT validation and user context
- **Backend Services**: Receive validated user context
- **Frontend**: Login/logout and token management
- **Database**: Neon PostgreSQL for all data storage

## 📊 **Requirements**

### **Functional Requirements**
- [ ] JWT token validation and verification
- [ ] User context extraction from JWT tokens
- [ ] Role-based access control (Patient, Provider, Admin)
- [ ] Token refresh validation
- [ ] User session context management
- [ ] Basic security monitoring

**Note**: User login, logout, registration, and password reset are handled by external authentication provider (Supabase Auth, Auth0, etc.)

### **Non-Functional Requirements**
- **Security**: Secure token storage and transmission
- **Performance**: Minimal authentication overhead
- **Scalability**: Handle multiple concurrent users
- **Compliance**: Healthcare data protection standards

## 🚀 **Implementation Plan**

### **Phase 1: Foundation**
- [ ] Create Auth Service with JWT validation functionality
- [ ] Implement JWT token validation and user context extraction
- [ ] Set up external authentication provider integration (Supabase Auth, Auth0, etc.)

### **Phase 2: Gateway Integration**
- [ ] Add authentication middleware to Gateway
- [ ] Implement token validation in Gateway using Auth Service
- [ ] Pass user context to backend services

### **Phase 3: Service Integration**
- [ ] Update Patient Service to use user context
- [ ] Update Provider Service to use user context
- [ ] Update Appointment Service to use user context
- [ ] Update AI Service to use user context

### **Phase 4: Security & Monitoring**
- [ ] Add role-based access control
- [ ] Implement security event logging
- [ ] Add basic authentication monitoring

## ⚠️ **Risks & Considerations**

### **Technical Risks**
- **Token Security**: JWT tokens could be compromised
- **Performance**: Authentication overhead on every request
- **Scalability**: Auth Service becoming a bottleneck

### **Mitigation Strategies**
- **For Security**: Use short-lived tokens, secure transmission, token rotation
- **For Performance**: Token caching, efficient validation algorithms
- **For Scalability**: Horizontal scaling, load balancing

## 📋 **Success Criteria**

- [ ] All services require valid authentication
- [ ] Role-based access control works correctly
- [ ] JWT tokens are secure and properly validated
- [ ] Authentication performance is acceptable
- [ ] Security events are properly logged

## 🔗 **Related Documents**

- [System Design](system-design.md)
- [Exception Handling Design](exception-handling-design.md)
- [Logging Design](logging-design.md)

---

## 📝 **Authentication Flow**

### **Login Process**
1. **User submits credentials** to external authentication provider (Supabase Auth, Auth0, etc.)
2. **External provider validates credentials** and authenticates user
3. **JWT token generated** by external provider with user info and roles
4. **Token returned** to frontend for storage
5. **Frontend includes token** in subsequent requests

### **Request Authentication**
1. **Frontend sends request** with JWT token in header
2. **Gateway receives request** and extracts token
3. **Gateway validates token** with Auth Service (JWT validation only)
4. **If valid, request proceeds** with user context
5. **If invalid, request rejected** with 401 response

### **Token Validation**
1. **Check token signature** for authenticity
2. **Verify token expiration** (not expired)
3. **Extract user information** and roles from JWT claims
4. **Validate user permissions** for requested resource
5. **Return user context** for service use

**Note**: The Auth Service does not handle user login, registration, or password reset. These operations are managed by the external authentication provider.

## 🔐 **Security Features**

### **JWT Token Security**
- **Short Expiration**: Tokens expire quickly (15-60 minutes)
- **Secure Signing**: Strong secret key for token signing
- **HTTPS Only**: Tokens transmitted only over secure connections
- **Token Rotation**: Regular token refresh for active sessions

### **Access Control**
- **Role-Based**: Patient, Provider, Admin roles
- **Resource-Level**: Users can only access their own data
- **API Protection**: All endpoints require valid authentication
- **Audit Logging**: All authentication events logged

### **Healthcare Compliance**
- **Patient Data Protection**: Users can only access authorized data
- **Audit Trails**: Complete authentication and access logs
- **Session Management**: Proper session timeout and cleanup
- **Security Monitoring**: Failed authentication attempts tracked

## 👥 **User Roles & Permissions**

### **Patient Role**
- **Access**: Own patient records, appointments, medical history
- **Actions**: View records, book appointments, update personal info
- **Restrictions**: Cannot access other patients' data

### **Provider Role**
- **Access**: Assigned patients' records, appointment schedules
- **Actions**: View patient data, manage appointments, update records
- **Restrictions**: Only access assigned patients

### **Admin Role**
- **Access**: System administration, user management, reports
- **Actions**: Manage users, view system logs, generate reports
- **Restrictions**: Cannot access individual patient medical data

## 📊 **Implementation Examples**

### **Auth Service Endpoints**
```java
// JWT validation endpoint (internal use by Gateway)
POST /auth/validate
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response
{
  "valid": true,
  "user": {
    "id": "user-123",
    "username": "john_doe",
    "role": "PROVIDER",
    "expiresAt": "2024-01-15T11:30:00Z"
  }
}
```

### **Gateway Authentication Middleware**
```java
@Component
public class AuthenticationMiddleware {

    public Mono<UserContext> validateToken(String token) {
        // Call Auth Service to validate JWT token
        // Extract user information from validated token
        // Return user context for business services
    }
}
```

### **Service User Context Usage**
```java
@GetMapping("/patients/{id}")
public Patient getPatient(@PathVariable String id,
                         @RequestHeader("Authorization") String token) {
    UserContext user = authService.validateToken(token);

    // Check if user can access this patient
    if (!canAccessPatient(user, id)) {
        throw new AccessDeniedException("Access denied");
    }

    return patientService.getPatient(id);
}
```

**Note**: User login, registration, and password reset are handled by external authentication provider (Supabase Auth, Auth0, etc.). The Auth Service only validates JWT tokens.

## 🔍 **Security Monitoring**

### **Authentication Events**
- **Successful Logins**: Track user logins and locations
- **Failed Logins**: Monitor failed authentication attempts
- **Token Usage**: Track token generation and validation
- **Access Patterns**: Monitor unusual access patterns

### **Security Alerts**
- **Multiple Failed Logins**: Alert on suspicious activity
- **Unusual Access Times**: Flag access outside normal hours
- **Role Escalation**: Monitor for unauthorized role changes
- **Token Abuse**: Detect token sharing or misuse

---

*This authentication design provides a secure, scalable approach to user authentication across all healthcare AI microservices while maintaining compliance and performance.*
