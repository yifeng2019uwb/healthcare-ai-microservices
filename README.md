# Healthcare AI Microservices Platform

> **Learning-Focused Healthcare AI Platform** - Master Spring Boot, AI Integration, and Microservices Architecture

## 🎯 **What This Project Is**

A comprehensive healthcare AI microservices platform designed for learning Spring Boot, AI integration, and microservices patterns. Built with clean architecture principles and progressive complexity.

## 🏗️ **System Architecture**

### **High-Level Overview**
This project implements a **hybrid Java + Python microservices architecture** for healthcare AI applications. The system uses **Neon PostgreSQL as the single primary database** for all business data and authentication, with **AWS S3 for file storage**.

### **Backend Architecture**
- **API Gateway (Port 8080)**: Single external entry point, orchestrates registration and routes to business services
- **Auth Service (Port 8001)**: JWT validation only, no business logic
- **Business Services (Ports 8002-8005)**: Handle business logic, can call each other internally when needed
- **Data Layer**: Primary data access method for all services

### **Port Management Strategy**
- **External Access**: Only port 8080 (API Gateway) exposed to the internet
- **Internal Services**: All business services run internally on ports 8001-8005
- **Single Entry Point**: All external traffic goes through the Gateway for routing and security

### **Technology Distribution**
- **Java Services (4)**: Gateway, Auth, Patient, Provider, Appointment
- **Python Service (1)**: AI Service for ML/AI capabilities
- **Database**: Neon PostgreSQL (single instance, multiple schemas)
- **File Storage**: AWS S3 for medical documents and images
- **Frontend**: React applications for Patient and Provider users

## 🚀 **Key Features**

### **🔐 Authentication & Security**
- **Internal Auth Service**: Spring Boot JWT validation only
- **External Authentication**: Supabase Auth handles login/registration
- **JWT-based Authentication**: Secure token management
- **Role-based Access Control**: Patient and Provider roles only
- **Basic Compliance**: Simple audit trails and data protection

### **💾 Data Management**
- **Shared Data Layer**: Common database access patterns for all services
- **Single Database**: Neon PostgreSQL with service-specific schemas
- **File Storage**: AWS S3 for cross-domain file management
- **Service Communication**: Services can call each other internally when needed for business logic

### **🤖 AI Integration**
- **Centralized AI Service**: All AI capabilities in one service
- **Healthcare Data Analysis**: Patient data analysis and insights
- **Predictive Analytics**: Health outcome predictions and trends
- **Clinical Decision Support**: AI-powered insights for healthcare decisions

### **🏥 Healthcare Features**
- **Patient Management**: Complete patient profiles and medical history viewing
- **Provider Management**: Doctor profiles, specialties, and medical records management
- **Appointment System**: Scheduling, management, and lifecycle tracking
- **Medical Records**: Secure document storage and retrieval through Provider Service

## 🛠️ **Technology Stack**

### **Backend Services**
- **Framework**: Spring Boot 3.2+ with Java 17
- **API Gateway**: Spring Cloud Gateway (Port 8080)
- **Authentication**: Spring Boot JWT validation + Supabase Auth
- **Database**: Neon PostgreSQL with multiple schemas
- **File Storage**: AWS S3 (cost-effective, scalable)
- **Shared Module**: Common data access patterns

### **Frontend Applications**
- **Framework**: React 18+ with TypeScript
- **UI Library**: Material-UI (MUI)
- **State Management**: React Context + Hooks
- **HTTP Client**: Axios
- **Authentication**: JWT token management
- **User Portals**: Patient Portal and Provider Portal only

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
- **[Implementation Backlog](docs/BACKLOG.md)** - Complete task planning and roadmap
- **[Daily Work Log](docs/DAILY_WORK_LOG.md)** - Daily progress tracking template
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
- **Hybrid Architecture**: Java + Python microservices integration

### **Technical Skills**
- **Microservices Patterns**: Service design and communication
- **Database Design**: PostgreSQL with multiple schemas
- **API Design**: RESTful services and authentication
- **Deployment**: Railway platform and CI/CD

---

*This platform is designed for learning Spring Boot, AI integration, and microservices architecture. Focus on clean design and progressive complexity.*
