# Healthcare AI Microservices - Logging Design

> **Simple, Practical Logging Strategy** - Good observability without enterprise complexity

## 📋 **Document Information**

- **Document Title**: Logging Design for Healthcare AI Microservices
- **Version**: 1.0
- **Date**: 2024-01-15
- **Author**: Healthcare AI Team
- **Status**: Draft

## 🎯 **Overview**

### **What This Is**
This design defines a simple logging approach for healthcare AI microservices that provides good observability without over-engineering. It focuses on practical logging patterns that work immediately and scale easily.

### **Why This Matters**
Good logging is essential for debugging issues, monitoring system health, and maintaining healthcare compliance. This approach gives you 80% of the benefits with 20% of the complexity - perfect for a personal project that demonstrates professional practices.

### **Scope**
- **In Scope**: Basic log format, correlation IDs, service isolation, simple monitoring
- **Out of Scope**: Complex log analytics, enterprise monitoring systems, advanced alerting

## 🏗️ **High-Level Design**

### **Core Concept**
Simple JSON structured logging with correlation IDs, automatic service discovery, and zero configuration for new services.

### **Key Components**
- **BaseLogger**: Common logging library for all services
- **Correlation IDs**: Track requests across service boundaries
- **Service Isolation**: Each service gets its own log file
- **Auto-Discovery**: New services automatically appear in monitoring

### **Data Flow**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Java Services │    │  Python AI      │    │   Frontend      │
│   (Spring Boot) │    │   Service       │    │   (React)       │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   BaseLogger            │
                    │   (Common library)      │
                    └────────────┬───────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Service Log Files     │
                    │   (Auto-created)        │
                    └─────────────────────────┘
```

## 🔧 **Technical Approach**

### **Technology Choice**
- **Why JSON Format**: Machine readable, easy parsing, widely supported
- **Why Service Isolation**: Each service gets its own log file automatically
- **Why Auto-Discovery**: Zero configuration for new services

### **Integration Points**
- **Java Services**: Use BaseLogger from shared module
- **Python Service**: Use BaseLogger Python implementation
- **Frontend**: Send important events to backend for logging

## 📊 **Requirements**

### **Functional Requirements**
- [ ] All services use BaseLogger for consistent logging
- [ ] Automatic correlation ID generation per request
- [ ] Service-specific log files with auto-discovery
- [ ] Basic log aggregation and search
- [ ] Security event logging (failed logins, suspicious activity)

### **Non-Functional Requirements**
- **Performance**: Minimal impact on service performance
- **Security**: No sensitive patient data in logs
- **Scalability**: Handle logs from multiple services automatically

## 🚀 **Implementation Plan**

### **Phase 1: Foundation**
- [ ] Create BaseLogger in shared Java module
- [ ] Implement BaseLogger for Python
- [ ] Set up basic log file structure

### **Phase 2: Service Integration**
- [ ] Add BaseLogger to Auth Service
- [ ] Add BaseLogger to Patient Service
- [ ] Add BaseLogger to Provider Service
- [ ] Add BaseLogger to Python AI Service

### **Phase 3: Monitoring**
- [ ] Basic log aggregation setup
- [ ] Simple log search functionality
- [ ] Basic security event monitoring

## ⚠️ **Risks & Considerations**

### **Technical Risks**
- **Performance Impact**: Excessive logging could slow down services
- **Storage Growth**: Logs can consume significant storage over time
- **Data Privacy**: Accidentally logging sensitive patient information

### **Mitigation Strategies**
- **For Performance**: Use appropriate log levels, avoid logging in hot paths
- **For Storage**: Implement log rotation and retention policies
- **For Privacy**: Never log patient data, use correlation IDs instead

## 📋 **Success Criteria**

- [ ] All services log in consistent format
- [ ] Correlation IDs track requests across services
- [ ] New services require zero configuration
- [ ] Basic log search and filtering works
- [ ] No sensitive data in logs

## 🔗 **Related Documents**

- [Exception Handling Design](exception-handling-design.md)
- [System Design](system-design.md)

---

## 📝 **Standard Log Format**

### **Basic Log Structure**
```json
{
  "timestamp": "2024-01-15T10:30:45Z",
  "level": "INFO",
  "service": "patient-service",
  "request_id": "req-12345",
  "user": "john_doe",
  "action": "patient_retrieved",
  "message": "Patient record retrieved successfully",
  "duration_ms": 45,
  "extra": {
    "patient_id": "12345",
    "endpoint": "/api/patients/12345"
  }
}
```

### **Required Fields**
- **timestamp**: When the log was created (ISO 8601)
- **level**: Log level (INFO, WARN, ERROR)
- **service**: Which service created the log
- **request_id**: Unique request ID for correlation
- **action**: What happened (login, patient_retrieved, etc.)
- **message**: Human-readable log message

### **Optional Fields**
- **user**: Username (if available)
- **duration_ms**: How long the operation took
- **extra**: Additional context (endpoint, method, etc.)

### **Log Levels**
- **ERROR**: Something went wrong, needs immediate attention
- **WARN**: Something unexpected happened, but service continues
- **INFO**: Normal operation information

## 🏗️ **Infrastructure Architecture**

### **Log File Structure (Auto-Generated)**
```
logs/
├── services/
│   ├── auth-service/          # Created when auth service starts
│   │   └── auth-service.log
│   ├── patient-service/       # Created when patient service starts
│   │   └── patient-service.log
│   ├── provider-service/      # Created when provider service starts
│   │   └── provider-service.log
│   ├── ai-service/            # Created when AI service starts
│   │   └── ai-service.log
│   └── any_new_service/       # Created automatically for new services!
│       └── any_new_service.log
```

### **Key Infrastructure Principles**
- **One-Time Setup**: Log aggregation configured once
- **Auto-Discovery**: New service log files automatically detected
- **Zero Configuration**: New services just import BaseLogger and start logging
- **Service Isolation**: Each service gets its own log file and directory

## 🔧 **Service Integration (Zero Configuration)**

### **For Any New Service, Just Do This:**
1. **Import BaseLogger** from shared module
2. **Create logger instance** with service name
3. **Start logging immediately**

### **What Happens Automatically:**
- **Log Directory Created**: `logs/services/my_new_service/`
- **Log File Created**: `logs/services/my_new_service/my_new_service.log`
- **Auto-Detection**: Log aggregation automatically finds new files
- **Immediate Querying**: Service logs available for search immediately

## 📊 **Usage Examples**

### **Patient Service Examples**
```java
// Patient record retrieved
logger.info(
    action="patient_retrieved",
    message="Patient record retrieved successfully",
    user="doctor_smith",
    duration_ms=45,
    extra={"patient_id": "12345", "endpoint": "/api/patients/12345"}
);

// Patient creation failed
logger.error(
    action="patient_creation_failed",
    message="Failed to create patient record",
    extra={"reason": "Invalid data", "validation_errors": ["Missing DOB"]}
);
```

### **Security Events**
```java
// Failed authentication
logger.warn(
    action="auth_failed",
    message="Authentication failed - invalid credentials",
    extra={"username": "attempted_user", "ip": "192.168.1.100"}
);

// Suspicious activity
logger.warn(
    action="suspicious_activity",
    message="Multiple failed login attempts",
    extra={"username": "john_doe", "attempts_in_hour": 10}
);
```

## 🔍 **Basic Log Analysis**

### **Simple Queries**
- **Find all errors**: `grep '"level":"ERROR"' logs/*.json`
- **Find specific actions**: `grep '"action":"patient_retrieved"' logs/*.json`
- **Find user activity**: `grep '"user":"john_doe"' logs/*.json`
- **Find slow requests**: `grep '"duration_ms"' logs/*.json | jq 'select(.duration_ms > 1000)'`

### **Security Monitoring**
- **Failed logins**: `grep '"action":"auth_failed"' logs/*.json`
- **Suspicious activity**: `grep '"action":"suspicious_activity"' logs/*.json`

---

*This logging design provides a simple, practical approach to logging across all healthcare AI microservices with zero configuration for new services and immediate observability.*
