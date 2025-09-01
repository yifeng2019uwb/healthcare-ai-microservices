# Healthcare AI Microservices - Service Design Template

> **🔧 Standard Template for Service Design Documents**
>
> This template ensures consistency across all service design documents.
> **Use this template** for Patient, Provider, Appointment, AI, and File Storage services.

## 📋 **Document Information**

- **Document Title**: [Service Name] Service Design
- **Version**: 1.0
- **Date**: [Current Date]
- **Author**: Healthcare AI Team
- **Status**: Draft
- **Service**: [Service Name]
- **Port**: [Port Number]

## 🎯 **Overview**

### **What This Service Is**
Brief description of what this service does and its main purpose.

### **Business Value & Impact**
Explanation of why this service is important and what problems it solves.

### **Scope**
- **In Scope**: What this service will handle
- **Out of Scope**: What this service will NOT handle

## 📚 **Definitions & Glossary**

### **Key Terms**
- **HIPAA**: Health Insurance Portability and Accountability Act - federal law protecting patient health information
- **[Term 2]**: Brief definition if needed
- **[Term 3]**: Brief definition if needed

## 👥 **User Case**

### **Primary User Types**
- **User Type 1**: Description of who this user is
- **User Type 2**: Description of who this user is
- **User Type 3**: Description of who this user is

### **User Stories**

#### **User Case 1: [Basic Functionality]**
[Write a simple, natural description of what users need and why it's important. Make it easy to understand like telling a story.]

#### **User Case 2: [Core Feature]**
[Write a simple, natural description of what users need and why it's important. Make it easy to understand like telling a story.]


## 🔧 **Solution Alternatives**

### **Solution 1: [Primary Approach]**
**Description**: Brief description of this solution approach

**Infrastructure**: What infrastructure is needed
**Database**: What tables and fields are needed
**Workflow**: How the solution works step by step

**Pros**: [Advantages]
**Cons**: [Disadvantages]

### **Solution 2: [Alternative Approach]**
**Description**: Brief description of this solution

**Infrastructure**: What infrastructure is needed
**Database**: What tables and fields are needed
**Workflow**: How the solution works step by step

**Pros**: [Advantages]
**Cons**: [Disadvantages]

### **Final Decision**
**Chosen Solution**: [Which solution was selected]

**Why**: [Primary reason for selection]

## 🔌 **API Design**

### **Endpoints**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/health` | Health check | No |
| GET | `/` | List resources | Yes |
| POST | `/` | Create resource | Yes |
| GET | `/{id}` | Get resource | Yes |
| PUT | `/{id}` | Update resource | Yes |
| DELETE | `/{id}` | Delete resource | Yes |

### **Request/Response Example**
```json
// Request
{
  "field1": "value1",
  "field2": "value2"
}

// Response
{
  "id": "uuid",
  "field1": "value1",
  "field2": "value2",
  "created_at": "timestamp"
}
```

## ❓ **Q&A**

### **Common Questions**
**Q**: [Question about the design]
**A**: [Clear answer]

**Q**: [Another question]
**A**: [Clear answer]

## 📚 **References**
- [System Design](system-design.md)
- [Database Design](database-design.md)