# Auth Service Design

> **Stateless JWT Validation Service** - Internal authentication for healthcare AI microservices

## 📋 **Document Information**

- **Document Title**: Auth Service Design
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Draft

## 🎯 **Overview**

### **What This Service Is**
The Auth Service is a **stateless JWT validation service** positioned between the Gateway and backend services. It only validates JWT tokens issued by external authentication providers (Supabase Auth) and extracts user context for business services. No authentication data management - external providers handle all user authentication complexity.

### **Business Value & Impact**
Secure authentication is critical for healthcare applications to protect patient data, ensure proper access control, and maintain HIPAA compliance. This simplified approach leverages proven external authentication providers while keeping the Auth Service lightweight and focused on core business services.

### **Scope**
- **In Scope**: JWT token validation, user context extraction, role-based access control
- **Out of Scope**: User login/logout, user registration, password management, authentication data storage, complex authorization rules, enterprise SSO integration

**Note**: Registration flow orchestration needs discussion - Auth Service may need to integrate with external auth provider.

## 📚 **Definitions & Glossary**

- **JWT**: JSON Web Token - secure, stateless authentication mechanism
- **HIPAA**: Health Insurance Portability and Accountability Act - healthcare data protection regulations
- **Stateless JWT Validation**: Core JWT validation doesn't require database access
- **External Auth Provider**: Third-party service (Supabase Auth) handling all user authentication
- **JWT Token**: Secure token issued by external provider containing user information
- **External Auth Provider**: Third-party service handling user login/registration (Supabase Auth, Auth0, etc.)

## 👥 **User Stories**

#### **User Case 1: JWT Token Validation**
When a user makes a request to any service, the Auth Service validates their JWT token to ensure they are authenticated and authorized to access the requested resource. This happens transparently for every API call.

#### **User Case 2: Role-Based Access Control**
The Auth Service extracts user roles from JWT tokens and provides role information to business services, enabling them to enforce appropriate access controls based on user permissions (Patient, Provider, Admin).

#### **User Case 3: User Context Provision**
Business services receive validated user context from the Auth Service, including user ID, username, role, and permissions, allowing them to make authorization decisions and provide personalized responses.



## 🔧 **Solution Alternatives**

### **Current System Infrastructure & Data (Available to All Services)**
- **Neon PostgreSQL**: Single database for all business data
- **Spring Cloud Gateway**: Central routing and middleware
- **JWT Tokens**: Issued by external authentication provider
- **External Auth Provider**: Handles user login/registration (Supabase Auth, Auth0, etc.)

### **Solution 1: Stateless JWT Validation Only**
**Infrastructure**: Lightweight Spring Boot service with JWT validation libraries
**Database**: No database tables - completely stateless
**Workflow**:
1. Gateway receives request with JWT token
2. Gateway calls Auth Service to validate token
3. Auth Service validates JWT signature and expiration
4. Auth Service extracts user context from JWT claims
5. Auth Service returns user context if valid
6. Gateway forwards request with user context to business service

**External Auth Integration**:
1. Users authenticate via Supabase Auth (login/registration)
2. Supabase Auth issues JWT tokens with user information
3. Our Auth Service validates these JWT tokens
4. Business services receive validated user context

**Pros**:
- Simple and lightweight implementation
- No database dependencies or complexity
- Leverages proven external authentication
- Fast token validation
- Easy to scale horizontally
- Focus on core business services

**Cons**:
- Depends on external authentication provider
- Limited control over authentication flow
- No internal authentication data management

### **Solution 2: Internal User Management**
**Infrastructure**: Full authentication service with user database
**Database**: User tables, password hashing, session management
**Workflow**: Complete user lifecycle management internally

**Pros**:
- Full control over user management
- No external dependencies
- Complete authentication solution

**Cons**:
- Complex implementation
- Database overhead
- Security responsibility
- Over-engineered for learning project

### **Solution 3: Hybrid Approach**
**Infrastructure**: JWT validation + minimal user context storage
**Database**: Basic user context tables for caching
**Workflow**: JWT validation with optional user context caching

**Pros**:
- Balance of simplicity and functionality
- Some user context caching
- Moderate complexity

**Cons**:
- Still complex for learning
- Database dependency
- Potential data inconsistency

### **Final Decision**
**Chosen Solution**: Solution 1 - Stateless JWT Validation Only

**Why**: Perfect for learning project - simple, lightweight, and focused. Leverages proven external authentication (Supabase Auth) while keeping our Auth Service stateless and fast. Allows us to focus on core business services (Patient, Provider, AI) without authentication complexity.

## 🏗️ **High-Level Design**

### **Core Concept**
Internal Auth Service positioned between the Gateway and backend services, validating JWT tokens issued by external authentication providers (Supabase Auth). The Auth Service is completely stateless - no database, no user storage, no credential validation, just JWT validation logic.

**User Authentication Flow**:
1. **External Authentication**: Users authenticate via Supabase Auth (login/registration)
2. **JWT Token**: Supabase Auth issues JWT token with user info and roles
3. **Auth Service**: Validates JWT tokens, extracts user context
4. **Business Services**: Use validated user context for business logic
5. **User Data**: Stored in Neon database for business services (Patient, Provider, etc.)

### **Key Components**
- **Auth Service**: JWT validation only, no authentication data management (Port 8001)
- **JWT Tokens**: Secure, stateless authentication with embedded user info (issued by Supabase Auth)
- **Role-Based Access**: User roles extracted from JWT token claims
- **Gateway Integration**: Authentication middleware in Spring Cloud Gateway
- **Business Services**: Handle business logic and can call each other internally
- **Supabase Auth**: Handles all user authentication (login, registration, password management, MFA, etc.)

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

## 🔌 **API Design**

### **Endpoints**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/health` | Health check | No |
| POST | `/api/auth/validate` | Validate JWT token | No |

### **Request/Response Examples**

#### **Token Validation**
```json
// Request
POST /api/auth/validate
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

// Response
{
  "valid": true,
  "user": {
    "id": "user-123",
    "username": "john.doe@email.com",
    "role": "PROVIDER",
    "permissions": ["READ_PATIENT", "WRITE_PATIENT"],
    "expiresAt": "2024-01-15T11:30:00Z"
  }
}
```



#### **Password Change**
```json
// Request
POST /api/auth/change-password
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}

// Response
{
  "success": true,
  "message": "Password changed successfully",
  "requiresReauth": true,
  "passwordChangedAt": "2024-01-15T11:30:00Z"
}
```



## 🗄️ **Database Schema Design**

### **No Database Tables Required**
The Auth Service is completely stateless and does not require any database tables. All authentication data is managed by Supabase Auth, including:
- **User credentials**: Username, password, password history
- **Authentication events**: Login history, failed attempts, account locks
- **Security settings**: MFA, account status, security policies
- **Session management**: Token refresh, session tracking

### **External Authentication Data (Supabase Auth)**
Supabase Auth handles all authentication data management:
- **User accounts**: Registration, login, logout
- **Password management**: Hashing, validation, reset
- **Security features**: MFA, account lockout, failed login tracking
- **Session management**: Token generation, refresh, invalidation

### **JWT Token Structure**
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user-123",
    "username": "john.doe@email.com",
    "role": "PROVIDER",
    "permissions": ["READ_PATIENT", "WRITE_PATIENT"],
    "iat": 1642234567,
    "exp": 1642238167
  },
  "signature": "HMACSHA256(base64UrlEncode(header) + '.' + base64UrlEncode(payload), secret)"
}
```

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

**Note**: User login, registration, password reset, and all authentication management are handled by Supabase Auth. The Auth Service only validates JWT tokens and extracts user context for business services.

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

## ❓ **Q&A**

**Q: How does user login work if Auth Service doesn't handle it?**
A: User login is handled entirely by Supabase Auth. Users authenticate through Supabase Auth, which issues JWT tokens. Our Auth Service only validates these JWT tokens and extracts user context for business services.

**Q: What happens if JWT token expires?**
A: The Auth Service returns validation failure, and the client must re-authenticate with Supabase Auth to get a new JWT token.

**Q: How do we handle user roles and permissions?**
A: Roles and permissions are embedded in JWT token claims by Supabase Auth. The Auth Service extracts and validates this information from the JWT token.

**Q: What about user registration and password reset?**
A: These operations are handled entirely by Supabase Auth. The Auth Service has no involvement in user management - we focus only on JWT validation.

**Q: How do we ensure security compliance?**
A: Supabase Auth handles all security compliance (password hashing, failed login tracking, account locks, MFA). Our Auth Service focuses on JWT validation and user context extraction.

**Q: Where is authentication data stored?**
A: All authentication data (passwords, login history, security settings) is stored and managed by Supabase Auth. Our Auth Service is completely stateless with no database tables.

**Q: What about token refresh?**
A: Token refresh is handled by Supabase Auth. Our Auth Service only validates JWT tokens and doesn't manage refresh tokens or session state.

## 🔍 **Discussion Points**

### **1. External Auth Integration**
**Question**: How does Auth Service integrate with Supabase Auth?
- **JWT Validation**: Validate tokens issued by Supabase Auth
- **User Context**: Extract user information from JWT claims
- **Registration Flow**: How to handle new user registration?
- **Decision Needed**: Integration pattern and responsibilities

### **2. Registration Flow Responsibility**
**Question**: Who orchestrates the registration process?
- **Option A**: Gateway orchestrates (calls business services, creates Supabase account)
- **Option B**: Auth Service handles (integrates with Supabase API)
- **Option C**: Separate registration service
- **Decision Needed**: Clear responsibility assignment

### **3. Service Communication**
**Question**: How should Auth Service communicate with other services?
- **JWT Validation**: Called by Gateway for every request
- **User Context**: Pass user information to business services
- **Error Handling**: How to handle validation failures?
- **Decision Needed**: Communication patterns and error handling

## 📚 **References**

- [System Design](system-design.md)
- [Exception Handling Design](exception-handling-design.md)
- [Logging Design](logging-design.md)
- [Data Strategy](data-strategy.md)

---

*This simplified authentication design leverages Supabase Auth for all user authentication while keeping our Auth Service lightweight and focused on JWT validation and business service integration.*
