# Healthcare AI Microservices - Standard Error Responses

> **Standard Error Response Format** - Reference for all API error responses

## 🎯 **Overview**

All APIs across all services return errors in a consistent format. This document defines the standard error response structure and exception mappings.

**For API documentation**: Only list the exception names that each endpoint can throw. Do not repeat the full error response format in each API doc.

## 🔧 **Standard Error Response Format**

All error responses follow this structure:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message"
}
```

## 📋 **Exception Types and HTTP Status Codes**

### **400 Bad Request** - `ValidationException`
**Error Code**: `VALIDATION_ERROR`
**Thrown When**: Invalid input data, missing required fields, format validation fails

**Example Response**:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid request data or missing required fields"
}
```

### **401 Unauthorized** - Auth Service Error
**Error Code**: `UNAUTHORIZED`
**Thrown When**: Missing or invalid JWT token, expired token, invalid authentication

**Example Response**:
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired JWT token"
}
```

### **403 Forbidden** - Authorization Error
**Error Code**: `FORBIDDEN`
**Thrown When**: Insufficient permissions, account suspended, role not allowed

**Example Response**:
```json
{
  "error": "FORBIDDEN",
  "message": "Insufficient permissions or account is suspended"
}
```

### **404 Not Found** - `ResourceNotFoundException`
**Error Code**: `RESOURCE_NOT_FOUND`
**Thrown When**: Requested resource doesn't exist

**Example Response**:
```json
{
  "error": "RESOURCE_NOT_FOUND",
  "message": "Patient profile not found or account is inactive"
}
```

### **409 Conflict** - `ConflictException`
**Error Code**: `CONFLICT_ERROR`
**Thrown When**: Business rule violation, resource already exists, duplicate entry

**Example Response**:
```json
{
  "error": "CONFLICT_ERROR",
  "message": "User or email already exists"
}
```

### **500 Internal Server Error** - `InternalException`
**Error Code**: `INTERNAL_ERROR`
**Thrown When**: Unexpected server error, database failure, system error

**Example Response**:
```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred while processing the request"
}
```

## 📝 **Exception Class Mapping**

| Exception Class | Error Code | HTTP Status | Use Case |
|----------------|------------|-------------|----------|
| `ValidationException` | `VALIDATION_ERROR` | 400 | Invalid input data |
| `ConflictException` | `CONFLICT_ERROR` | 409 | Resource conflict, duplicate |
| `ResourceNotFoundException` | `RESOURCE_NOT_FOUND` | 404 | Resource doesn't exist |
| `InternalException` | `INTERNAL_ERROR` | 500 | Unexpected server error |
| Auth Service | `UNAUTHORIZED` | 401 | Authentication failure |
| Auth Service | `FORBIDDEN` | 403 | Authorization failure |

## 📚 **Usage in API Documentation**

### **Do This** ✅
In API documentation, simply list which exceptions an endpoint can throw:

```
**Throws**:
- `ValidationException` (400) - Invalid request data
- `ConflictException` (409) - User or email already exists
- `InternalException` (500) - Server error
```

### **Don't Do This** ❌
Do not repeat the full error response format for each endpoint. Reference this document instead.

## 🚀 **Implementation**

### **Java Services**
- Exception classes defined in shared module
- Global exception handler (`@ControllerAdvice`) maps exceptions to standard format
- All services use same error response structure

### **Python AI Service**
- Create matching exception classes
- Use FastAPI exception handlers
- Return same error response format

## 📊 **Logging**

- Log all errors with request path and user info
- No sensitive patient data in error messages or logs
- Include exception stack trace in server logs (not in response)

---

**Reference**: See exception classes in `services/shared/src/main/java/com/healthcare/exception/`
