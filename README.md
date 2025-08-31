# Healthcare AI Microservices Platform

> **Learning-Focused Healthcare AI Platform** - Master Spring Boot, AI Integration, and Microservices Architecture

## 🎯 **What This Project Is**

A comprehensive healthcare AI microservices platform designed for learning Spring Boot, AI integration, and microservices patterns. Built with clean architecture principles and progressive complexity.

## 🏗️ **System Architecture**

### **High-Level Architecture**
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
                    │        (Port 8000)      │
                    └────────────┬───────────-┘
                                 │
                    ┌────────────▼────────────┐
                    │      Auth Service       │
                    │        (Port 8001)      │
                    │   JWT Validation        │
                    └────────────┬───────────-┘
                                 │
         ┌───────────────────────┼────────────────────────┐
         │                       │                        │
    ┌────▼────┐    ┌──────────┐    ┌────▼──────┐    ┌──────────┐
    │ Patient │    │Provider  │    │Appointment│    │   AI     │
    │Service  │    │Service   │    │ Service   │    │ Service  │
    │ 8002    │    │ 8003     │    │ 8004      │    │ 8005     │
    └────┬────┘    └────┬─────┘    └────┬──────┘    └────┬─────┘
         │              │               │                │
         └──────────────┼───────────────┼──────────────┘
                        │               │
              ┌───────--▼───────────────▼────────┐
              │      Shared Data Layer           │
              │     (Database Access)            │
              └──────────────┬───────────────----┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
    ┌────▼───────┐    ┌─────▼────────┐    ┌──────▼──────┐
    │ Neon DB    │    │   S3         │    │File Storage │
    │(PostgreSQL)│    │(File Storage)│    │ Service     │
    │            │    │              │    │ 8006        │
    └────────────┘    └──────────────┘    └─────────────┘
```

## 🚀 **Key Features**

### **🔐 Authentication & Security**
- **Internal Auth Service**: Spring Boot JWT authentication
- **JWT-based Authentication**: Secure token management
- **Role-based Access Control**: Patient, Provider, Admin roles
- **Basic Compliance**: Simple audit trails and data protection

### **💾 Data Management**
- **Shared Data Layer**: Common database access patterns for all services
- **Single Database**: Neon PostgreSQL with service-specific schemas
- **File Storage**: AWS S3 for cross-domain file management
- **Service Independence**: No inter-service calls, direct data access

### **🤖 AI Integration**
- **Centralized AI Service**: All AI capabilities in one service
- **AI Assistant**: Chatbot and patient support
- **Smart Scheduling**: AI-powered appointment optimization
- **Clinical Decision Support**: Treatment recommendations

### **🏥 Healthcare Features**
- **Patient Management**: Complete patient profiles and medical history
- **Provider Management**: Doctor profiles, schedules, availability
- **Appointment System**: Intelligent scheduling and reminders
- **Medical Records**: Secure document storage and retrieval

## 🛠️ **Technology Stack**

### **Backend Services**
- **Framework**: Spring Boot 3.2+ with Java 17
- **API Gateway**: Spring Cloud Gateway (Port 8000)
- **Authentication**: Spring Boot JWT authentication
- **Database**: Neon PostgreSQL with multiple schemas
- **File Storage**: AWS S3 (cost-effective, scalable)
- **Shared Module**: Common data access patterns

### **Frontend Applications**
- **Framework**: React 18+ with TypeScript
- **UI Library**: Material-UI (MUI)
- **State Management**: React Context + Hooks
- **HTTP Client**: Axios
- **Authentication**: JWT token management

### **Infrastructure**
- **Platform**: Railway (deployment and hosting)
- **Containerization**: Docker
- **CI/CD**: GitHub Actions
- **Monitoring**: Railway built-in metrics

## 📚 **Documentation**

- **[System Design](docs/system-design.md)** - Complete system architecture and design decisions
- **[Authentication Design](docs/authentication-design.md)** - JWT authentication and security strategy
- **[Exception Handling](docs/exception-handling-design.md)** - Standard exception handling strategy
- **[Logging Design](docs/logging-design.md)** - Standardized logging strategy
- **[Project Structure](PROJECT_STRUCTURE.md)** - Project organization and structure
- **[Project Setup](docs/project-setup.md)** - Step-by-step setup instructions

## 🔧 **Quick Start**

### **Prerequisites**
- Java 17+
- Maven 3.8+ or Gradle 8+
- Node.js 18+
- Docker
- Git

### **Local Development**
```bash
# Clone the repository
git clone https://github.com/yifeng2019uwb/healthcare-ai-microservices
cd healthcare-ai-microservices

# Follow the setup guide
# See docs/project-setup.md for detailed instructions
```

## 🎯 **Learning Goals**

### **Primary Objectives**
- **Spring Boot Mastery**: Deep dive into microservice development
- **AI Integration**: Learn ML/AI in healthcare context
- **Clean Architecture**: Well-designed, maintainable services
- **Practical Experience**: Working healthcare platform

### **Technical Skills**
- **Microservices Patterns**: Service design and communication
- **Database Design**: PostgreSQL with multiple schemas
- **API Design**: RESTful services and authentication
- **Deployment**: Railway platform and CI/CD

---

*This platform is designed for learning Spring Boot, AI integration, and microservices architecture. Focus on clean design and progressive complexity.*
