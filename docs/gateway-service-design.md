# Gateway Service Design

> **🎯 Learning Focus: API Gateway for Healthcare Microservices**
>
> This document defines the design for the API Gateway service.
> **Design Philosophy**: Simple routing and orchestration for healthcare microservices.

## 📋 **Document Information**

- **Document Title**: Gateway Service Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Draft

## 🎯 **Overview**

### **What This Service Is**
The Gateway Service is the **single entry point** for all external API requests, handling routing, authentication, and basic request/response processing.

### **Business Value & Impact**
The Gateway provides centralized access control, request routing, and security for all healthcare microservices, ensuring consistent API experience and security compliance.

### **Scope**
- **In Scope**: Request routing, authentication middleware, basic validation, response formatting
- **Out of Scope**: Business logic, data processing, complex orchestration

**Note**: Scope may expand to include orchestration for registration flow - needs discussion.

## 📚 **Definitions & Glossary**

- **API Gateway**: Central entry point for all external API requests
- **Routing**: Directing requests to appropriate backend services
- **Authentication Middleware**: JWT validation and user context extraction
- **Rate Limiting**: Controlling request frequency per user/IP

## 👥 **User Cases**

### **User Case 1: Patient Registration**
When a patient wants to register, the Gateway receives the registration request, validates basic format, calls business services for validation, creates Supabase account, and returns success/error response.

**Note**: This assumes Gateway handles orchestration - needs discussion on implementation approach.

### **User Case 2: Patient Profile Access**
When a patient wants to view their profile, the Gateway validates their JWT token, extracts user context, routes the request to Patient Service, and returns the profile data.

### **User Case 3: Provider Authentication**
When a provider logs in, the Gateway validates their JWT token, extracts role and permissions, and allows access to provider-specific endpoints.

## 🔧 **Solution Alternatives**

### **Solution 1: Simple Routing Gateway**
**Infrastructure**: Spring Cloud Gateway with basic routing
**Database**: No database required
**Workflow**: Route requests to appropriate services based on URL patterns

### **Solution 2: Orchestration Gateway**
**Infrastructure**: Spring Cloud Gateway with service orchestration
**Database**: No database required
**Workflow**: Gateway coordinates multi-service operations (like registration)

**Note**: This approach handles registration flow but increases Gateway complexity.

### **Solution 3: Advanced Gateway**
**Infrastructure**: Spring Cloud Gateway with caching, rate limiting, monitoring
**Database**: Redis for caching and rate limiting
**Workflow**: Full-featured gateway with advanced capabilities

## 🏗️ **High-Level Design**

### **Core Concept**
Spring Cloud Gateway positioned as the single external entry point, routing requests to appropriate backend services while handling authentication and basic validation.

### **Key Components**
- **Gateway Service**: Request routing and orchestration (Port 8080)
- **Authentication Middleware**: JWT validation and user context extraction
- **Route Configuration**: URL-based routing to backend services
- **Response Handling**: Error handling and response formatting

### **Data Flow**
1. **External Request**: Client sends request to Gateway
2. **Authentication**: Gateway validates JWT token via Auth Service
3. **Routing**: Gateway routes request to appropriate backend service
4. **Response**: Gateway returns response to client

## 🛠️ **API Design**

### **Endpoints**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | User registration | No |
| POST | `/api/patients/*` | Patient service routes | Yes |
| POST | `/api/providers/*` | Provider service routes | Yes |
| GET | `/health` | Health check | No |

### **Request/Response Examples**
[To be defined based on solution choice]

## 🗄️ **Database Schema Design**

### **No Database Tables Required**
The Gateway Service is stateless and does not require any database tables.

## ❓ **Q&A**

**Q: How does the Gateway handle authentication?**
A: [To be defined based on solution choice]

**Q: What happens if a backend service is down?**
A: [To be defined based on solution choice]

**Q: How does the Gateway handle rate limiting?**
A: [To be defined based on solution choice]

## 🔍 **Discussion Points**

### **1. Gateway Implementation Strategy**
**Question**: Simple routing vs. orchestration approach?
- **Simple Routing**: Basic request forwarding, minimal complexity
- **Orchestration**: Handles registration flow, more complex but better UX
- **Decision Needed**: Balance learning goals with functionality

### **2. External Auth Integration**
**Question**: How does Gateway work with Supabase Auth and Auth Service?
- **Flow**: Supabase Auth → JWT → Gateway → Auth Service → Business Services
- **Registration**: Gateway orchestrates or separate flow?
- **Decision Needed**: Clear integration pattern

### **3. Service Communication**
**Question**: How should Gateway handle service failures?
- **Error Handling**: Circuit breakers, fallbacks, retries
- **User Experience**: Clear error messages vs. technical details
- **Decision Needed**: Error handling strategy

## 📚 **References**

- [System Design](system-design.md)
- [Authentication Design](authentication-design.md)
- [Patient Service Design](patient-service-design.md)

---

*This Gateway service design provides the foundation for routing and orchestrating requests across healthcare microservices.*
