# Healthcare AI Microservices - System Design Document

## 🏗️ **System Architecture Overview**

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
                    │   Spring Cloud Gateway │
                    │        (Port 8000)     │
                    └────────────┬───────────┘
                                 │
                    ┌────────────▼────────────┐
                    │      Auth Service       │
                    │        (Port 8001)      │
                    │   JWT Validation        │
                    └────────────┬───────────-┘
                                 │
         ┌───────────────────────┼────────────────────────┐
         │                       │                        │
    ┌────▼────┐   ┌──────────┐   ┌────▼────--┐    ┌──────────┐
    │ Patient │   │Provider  │   │Appointment│    │   AI     │
    │Service  │   │Service   │   │ Service   │    │ Service  │
    │ 8002    │   │ 8003     │   │ 8004      │    │ 8005     │
    └────┬────┘   └────┬─────┘   └────┬────--┘    └────┬─────┘
         │             │              │                │
         └──────────── ┼─────────────-┼────────────----┘
                       │              │
              ┌───────-▼─────────────-▼────────┐
              │      Shared Data Layer         │
              │     (Database Access)          │
              └──────────────┬───────────────--┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
    ┌────▼────---┐   ┌─────--▼────--┐   ┌─────---▼────┐
    │ Neon DB    │   │   S3         │   │File Storage │
    │(PostgreSQL)│   │(File Storage)│   │ Service     │
    │            │   │              │   │ 8006        │
    └──────────--┘   └──────────----┘   └──────────---┘
```

## 🎯 **Project Scope & Philosophy**

### **Learning-Focused Approach** (Keep it Simple!)
- **Spring Boot Mastery**: Deep dive into Spring Boot microservices
- **AI Integration**: Learn ML/AI integration with healthcare
- **Microservices Patterns**: Clean architecture and service design
- **Progressive Complexity**: Start simple, add features incrementally
- **Avoid Over-Engineering**: Skip Kafka, Redis, complex monitoring for now

### **Core Principles**
- **Service Independence**: No inter-service calls, direct data layer access
- **Shared Infrastructure**: Common data access patterns across services
- **Clear Boundaries**: Each service has distinct, focused responsibility
- **Scalable Foundation**: Architecture supports future growth

### **Hybrid Architecture Strategy**
- **Java Services (6)**: Enterprise-grade Spring Boot microservices for core business logic
- **Python Service (1)**: AI Service leveraging Python's rich ML/AI ecosystem
- **Technology Agnostic**: Services communicate via REST APIs regardless of implementation language
- **Best of Both Worlds**: Java's enterprise strength + Python's AI capabilities

## 🔐 **Authentication & Authorization Architecture**

### **Internal Auth Service (Spring Boot)**
- **JWT Token Management**: Secure authentication across all services
- **User Management**: Registration, login, profile management
- **Role-Based Access**: Patient, Provider, Admin roles with permissions
- **Session Management**: Token validation and refresh

### **User Roles & Permissions**
```
┌─────────────-┐    ┌─────────────-┐    ┌─────────────┐
│   Patient    │    │  Provider    │    │    Admin    │
│              │    │              │    │             │
├─────────────-┤    ├─────────────-┤    ├─────────────┤
│• View own    │    │• View        │    │• Full       │
│  data        │    │  patients    │    │  access     │
│• Update      │    │• Manage      │    │• User       │
│  profile     │    │  schedules   │    │  management │
│• Book        │    │• View        │    │• System     │
│  appointments│    │  appointments│    │  monitoring │
└─────────────-┘    └─────────────-┘    └─────────────┘
```

## 🗄️ **Data Layer Strategy**

### **Primary Database: Neon PostgreSQL**
- **Single Database**: All business data in one Neon PostgreSQL instance
- **Multiple Schemas**: Separate schemas for different services (patient, provider, appointment, ai, file_metadata)
- **Business Data**: Patient records, appointments, provider information, AI analysis results
- **No Auth Tables**: Authentication data is not stored in database

### **File Storage: AWS S3**
- **Medical Documents**: Patient files, medical images, reports
- **Cross-Service Access**: All services can access files through S3
- **Secure Storage**: Healthcare-compliant file storage and access controls

### **Authentication: Stateless JWT**
- **No Database Tables**: Auth Service is purely stateless
- **JWT Validation**: Verify token signature and expiration
- **User Context Extraction**: Parse user ID, roles from token
- **No User Storage**: No user accounts or credentials stored

### **Data Access Pattern**
- **Direct Database Access**: Services connect directly to Neon PostgreSQL
- **Shared Data Layer**: Common database access patterns in shared module
- **No Inter-Service Calls**: Services don't call each other, only the database
- **Consistent Data**: Single source of truth for all application data

## 🚀 **Service Architecture**

### **Backend Service Responsibilities**
- **Auth Service**: JWT token validation only, no business logic
- **Gateway**: Routes validated requests to appropriate business services
- **Business Services**: Can call each other internally when needed for business operations
- **Data Layer**: Primary data access method for all services

### **Service Communication Patterns**
- **External Requests**: Client → Gateway (8080) → Business Service
- **Internal Service Calls**: Business Service → Business Service (when business logic requires it)
- **Data Access**: All services → Data Layer → Neon PostgreSQL
- **Authentication**: Gateway validates JWT with Auth Service before routing

### **Port Management Strategy**
- **External Access**: Only port 8080 (API Gateway) exposed to the internet
- **Internal Communication**: Services communicate internally on their assigned ports
- **Gateway Routing**: All external requests go through Gateway, which routes to appropriate services
- **Simplified Deployment**: Single external port to manage and secure

### **Service Ports & Communication**
| Service | Port | External Access | Internal Communication | Technology |
|---------|------|----------------|----------------------|------------|
| **API Gateway** | 8080 | ✅ **EXTERNAL** | Routes to internal services | Java/Spring Boot |
| **Auth Service** | 8001 | ❌ Internal Only | JWT validation for Gateway | Java/Spring Boot |
| **Patient Service** | 8002 | ❌ Internal Only | Can call other services when needed | Java/Spring Boot |
| **Provider Service** | 8003 | ❌ Internal Only | Can call other services when needed | Java/Spring Boot |
| **Appointment Service** | 8004 | ❌ Internal Only | Can call other services when needed | Java/Spring Boot |
| **AI Service** | 8005 | ❌ Internal Only | Can call other services when needed | Python/FastAPI |
| **File Storage Service** | 8006 | ❌ Internal Only | Can call other services when needed | Java/Spring Boot |

### **Communication Flow**
```
External Request → Port 8080 (Gateway) → Business Service → Other Services (if needed)
     ↓                    ↓                    ↓                    ↓
Internet/Client    Spring Cloud Gateway    Primary Service    Supporting Services
```

### **Service Interaction Examples**
- **Appointment Booking**: Appointment Service calls Patient Service to validate patient, Provider Service to check availability
- **Patient Record Access**: Patient Service calls File Storage Service to retrieve medical documents
- **AI Analysis**: AI Service calls Patient Service to get patient data, Appointment Service for appointment history
- **Provider Dashboard**: Provider Service calls Appointment Service for schedule, Patient Service for patient list

### **Benefits of Flexible Internal Communication**
- **Business Logic Support**: Services can collaborate for complex operations
- **Data Consistency**: Coordinated operations across multiple services
- **Efficient Processing**: Avoid unnecessary data duplication
- **Real-time Updates**: Services can notify each other of changes

### **Complete Service Overview**
|      Service             | Port |     Type       |            Responsibility                   | Priority      | Technology |
|--------------------------|------|----------------|---------------------------------------------|---------------|------------|
| **API Gateway**          | 8080 | Infrastructure | Routing, Rate Limiting, Load Balancing      | 🔴 **HIGH**   | Java/Spring Boot |
| **Auth Service**         | 8001 | Infrastructure | JWT Validation, Authentication              | 🔴 **HIGH**   | Java/Spring Boot |
| **Patient Service**      | 8002 | Business       | Patient CRUD, Medical Records, Demographics | 🔴 **HIGH**   | Java/Spring Boot |
| **Provider Service**     | 8003 | Business       | Doctor Profiles, Schedules, Availability    | 🟡 **MEDIUM** | Java/Spring Boot |
| **Appointment Service**  | 8004 | Business       | Booking, Calendar, Notifications            | 🟡 **MEDIUM** | Java/Spring Boot |
| **AI Service**           | 8005 | Business       | All AI Features Centralized                 | 🟢 **LOW**    | Python/FastAPI |
| **File Storage Service** | 8006 | Support        | Cross-domain File Management                | 🟡 **MEDIUM** | Java/Spring Boot |

### **Service Categories**

#### **Infrastructure Services (2)**
|     Service      | Port |         Responsibility                 |  Priority   | Technology |
|------------------|------|----------------------------------------|-------------|------------|
| **API Gateway**  | 8080 | Routing, Rate Limiting, Load Balancing | 🔴 **HIGH** | Java/Spring Boot |
| **Auth Service** | 8001 | JWT Validation, Authentication         | 🔴 **HIGH** | Java/Spring Boot |

#### **Core Business Services (4)**
|        Service          | Port |          Responsibility                     |   Priority    | Technology |
|-------------------------|------|---------------------------------------------|---------------|------------|
| **Patient Service**     | 8002 | Patient CRUD, Medical Records, Demographics | 🔴 **HIGH**   | Java/Spring Boot |
| **Provider Service**    | 8003 | Doctor Profiles, Schedules, Availability    | 🟡 **MEDIUM** | Java/Spring Boot |
| **Appointment Service** | 8004 | Booking, Calendar, Notifications            | 🟡 **MEDIUM** | Java/Spring Boot |
| **AI Service**          | 8005 | All AI Features Centralized                 | 🟢 **LOW**    | Python/FastAPI |

#### **Support Services (1)**
|        Service           | Port |     Responsibility           |   Priority    | Technology |
|--------------------------|------|------------------------------|---------------|------------|
| **File Storage Service** | 8006 | Cross-domain File Management | 🟡 **MEDIUM** | Java/Spring Boot |

### **Service Communication Strategy**
- **No Inter-Service Calls**: Services don't call each other directly
- **Direct Data Access**: Each service calls shared data layer
- **Authentication Layer**: Auth Service validates JWT tokens before requests reach business services
- **Clean Boundaries**: Services operate independently
- **Technology Agnostic**: Java and Python services communicate via REST APIs

## 🔄 **Data Flow Patterns**

### **1. Authentication Flow**
```
Frontend → Gateway (8080) → Auth Service (8001) (JWT Validation) → Business Services
    ↓
Validated Request → Shared Data Layer → Database
```

### **2. Data Access Flow**
```
Service → Shared Data Layer → Database (direct connection)
    ↓
Return Data → Response to Client
```

### **3. File Access Flow**
```
Service → File Storage Service (8006) → S3
    ↓
File URL + Metadata → Response to Client
```

## 🚀 **Infrastructure Requirements**

### **Platform: Railway**
- **Gateway Service**: Spring Cloud Gateway (Port 8080) - Java
- **Core Services**: Java Spring Boot microservices (Ports 8001-8004, 8006)
- **AI Service**: Python FastAPI service (Port 8005) - Python
- **Database**: Neon PostgreSQL with multiple schemas
- **File Storage**: AWS S3 for cross-domain file management
- **Frontend**: React web applications
- **Monitoring**: Railway built-in metrics

### **Technology Stack by Service Type**
- **Java Services**: Spring Boot 3.2+ with Java 17, Maven build system
- **Python Service**: FastAPI with Python 3.11+, pip/poetry for dependencies
- **Shared Infrastructure**: Docker containers, Kubernetes orchestration
- **Data Layer**: PostgreSQL with shared access patterns

### **Deployment Strategy**
- **Service Independence**: One service per Railway project
- **Technology-Specific**: Java services use Maven, Python service uses pip/poetry
- **Health Checks**: Basic service health monitoring for all services
- **Auto-scaling**: Based on CPU and memory usage
- **Rollback**: Manual rollback if needed

## 🔒 **Security & Compliance**

### **Data Protection**
- **Encryption**: TLS for data in transit
- **Access Control**: JWT-based authentication with role permissions
- **Data Isolation**: Service-level data boundaries
- **Audit Trail**: Basic logging for compliance

### **Healthcare Compliance**
- **User Authentication**: Secure login and session management
- **Data Access**: Role-based permissions and data isolation
- **Audit Logging**: Basic access tracking and compliance
- **Privacy**: User consent and data protection measures

## 📊 **Performance & Scalability**

### **Performance Strategy**
- **Simple Caching**: Basic in-memory caching per service (no Redis initially)
- **Database Optimization**: Connection pooling and basic query optimization
- **Frontend State**: Local state management

### **Future Considerations** (Skip for now - learning focus)
- **High Traffic**: Can be designed later when needed
- **Event-Driven**: Can be added when business logic requires it
- **Advanced Caching**: Redis can be added when performance needs it
- **Service Mesh**: Not needed for learning project

## 🧪 **AI Service Integration**

### **Python-Based AI Service**
- **Technology**: Python with FastAPI framework
- **AI Libraries**: TensorFlow, PyTorch, scikit-learn, pandas, numpy
- **Healthcare AI**: MONAI (medical imaging), med7 (medical NLP), healthcare-nlp
- **Deployment**: Docker container with Python base image

### **Centralized AI Features**
- **AI Assistant**: Chatbot and patient support using NLP
- **Smart Scheduling**: AI-powered appointment optimization with ML models
- **Clinical Decision Support**: Treatment recommendations using healthcare AI models
- **Predictive Analytics**: Health risk assessment and trend analysis

### **Implementation Approach**
- **Python Ecosystem**: Leverage Python's extensive ML/AI libraries
- **REST API Integration**: Seamless communication with Java services
- **Model Management**: Basic version control (keep it simple)
- **Healthcare Focus**: Specialized medical AI models and datasets

## 📈 **Basic Monitoring** (Keep it Simple)

### **Health Checks**
- **Service Health**: Basic health endpoints for each service
- **Database Health**: Simple connection check
- **Gateway Health**: Basic routing check

### **Simple Metrics**
- **Response Times**: Basic performance tracking
- **Error Rates**: Simple error counting
- **Resource Usage**: Basic CPU/memory check

## 🔄 **Development Workflow**

### **Git Strategy**
- **Main Branch**: Production-ready code
- **Feature Branches**: Individual feature development
- **Simple Releases**: Tag-based versioning

### **CI/CD Pipeline**
- **Build**: Maven build and testing
- **Test**: Basic unit and integration tests
- **Deploy**: Automated Railway deployment
- **Monitor**: Basic health checks post-deployment

## 📋 **Implementation Roadmap** (Learning Focus)

### **Phase 1: Foundation (Weeks 1-2)**
- [ ] Project setup and basic infrastructure
- [ ] Shared data layer module
- [ ] Auth service (JWT validation only)
- [ ] Basic database schema and Neon setup
- [ ] Gateway service configuration

### **Phase 2: Core Services (Weeks 3-4)**
- [ ] Patient service implementation
- [ ] Provider service implementation
- [ ] Basic appointment management
- [ ] Simple frontend integration

### **Phase 3: Support Services (Weeks 5-6)**
- [ ] File storage service with S3 integration
- [ ] AI service basic implementation
- [ ] Testing and basic optimization
- [ ] **Skip**: Advanced features, complex monitoring, caching layers

## 🎯 **Success Metrics**

### **Learning Goals**
- **Spring Boot Mastery**: Confident microservice development
- **AI Integration**: Understanding ML/AI in healthcare context
- **Clean Architecture**: Well-designed, maintainable services
- **Practical Experience**: Working healthcare platform

### **Learning Goals** (Keep it Simple)
- **Working Services**: All services start up and respond to requests
- **Basic Functionality**: CRUD operations work for each service
- **Simple Integration**: Services can communicate when needed
- **Deployment**: Services can be deployed to Railway

## 🤝 **Contributing**

1. Fork the repository
2. Create a feature branch
3. Implement changes
4. Test thoroughly
5. Submit pull request

## 📄 **License**

This project is licensed under the MIT License.

## 📞 **Support**

For questions and support:
- Check the documentation
- Review the implementation guides
- Open an issue on GitHub

---

*This document provides the high-level system design for a learning-focused healthcare AI microservices platform. Focus on Spring Boot mastery, clean architecture, and progressive complexity.*
