# Healthcare AI Microservices - System Design Document

## 🏗️ **System Architecture Overview**

### **High-Level Architecture**
```
┌─────────────────┐    ┌─────────────────┐
│   Patient Web   │    │  Provider Web   │
│    (React)      │    │   (React)       │
└─────────┬───────┘    └─────────┬───────┘
          │                      │
          └──────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Spring Cloud Gateway │
                    │        (Port 8080)     │
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
    │ Supabase   │   │   S3         │   │   Shared    │
    │(PostgreSQL)│   │(File Storage)│   │   Data      │
    │            │   │              │   │   Layer     │
    └──────────--┘   └──────────----┘   └──────────---┘
```

## 🎯 **Project Scope & Philosophy**

### **Professional Healthcare Service** (Keep it Simple!)
- **Spring Boot Services**: Production-ready microservices architecture
- **AI Integration**: ML/AI integration with healthcare data
- **Microservices Patterns**: Clean architecture and service design
- **Progressive Complexity**: Start simple, add features incrementally
- **Avoid Over-Engineering**: Skip Kafka, Redis, complex monitoring for now

### **Core Principles**
- **Shared Infrastructure**: Common data access patterns across services
- **Clear Boundaries**: Each service has distinct, focused responsibility
- **User Type Separation**: Patient and Provider users access appropriate services
- **Scalable Foundation**: Architecture supports future growth

### **Hybrid Architecture Strategy**
- **Java Services (3)**: Enterprise-grade Spring Boot microservices for core business logic
- **Python Service (1)**: AI Service leveraging Python's rich ML/AI ecosystem
- **Technology Agnostic**: Services communicate via REST APIs regardless of implementation language
- **Best of Both Worlds**: Java's enterprise strength + Python's AI capabilities

### **Service Architecture**
- **4 Services Total**: Patient, Provider, Appointment, AI
- **2 User Types**: Patient and Provider only
- **Clear Boundaries**: Each service has focused responsibility

## 🔐 **Authentication & Authorization Architecture**

### **Internal Auth Service (Spring Boot)**
- **JWT Token Management**: Secure authentication across all services
- **User Management**: Registration, login, profile management
- **Role-Based Access**: Patient, Provider roles with permissions
- **Session Management**: Token validation and refresh

### **User Roles & Permissions (Industry Standard)**
```
┌─────────────────────────────────────────────────────────────┐
│                    USER PROFILES (Core Identity)            │
│  • Common fields: name, email, phone, address, role        │
│  • Role: PATIENT, PROVIDER, ADMIN, NURSE, THIRD_PARTY     │
└─────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
        ┌───────────▼───────────┐      ┌▼─────────────────────┐
        │   PATIENT PROFILES    │      │  PROVIDER PROFILES   │
        │  • Medical history    │      │  • Specialty         │
        │  • Allergies          │      │  • License number    │
        │  • Patient number     │      │  • Qualifications    │
        └───────────────────────┘      └─────────────────────┘
                    │                           │
                    └───────────┬───────────────┘
                                │
                    ┌───────────▼───────────┐
                    │     APPOINTMENTS      │
                    │  • id (UUID PK)       │
                    │  • patient_id (FK)    │
                    │  • provider_id (FK)   │
                    │  • Scheduling         │
                    │  • Status tracking    │
                    └───────────────────────┘
                                │ 1:1 or 1:M
                                │ appointment_id
                    ┌───────────▼───────────┐
                    │   MEDICAL RECORDS     │
                    │  • id (UUID PK)       │
                    │  • appointment_id (FK)│
                    │  • record_type (enum) │
                    │  • content (JSON/text)│
                    │  • is_patient_visible │
                    │  • release_date       │
                    └───────────────────────┘
```

## 🗄️ **Data Layer Strategy**

### **Primary Database: PostgreSQL**
- **Single Database**: All business data in one PostgreSQL instance
- **Business Data**: Patient records, appointments, provider information, AI analysis results

### **File Storage: AWS S3**
- **Cross-domain File Management**: Centralized file storage for all services

### **Authentication: External Auth + Stateless JWT**
- **External Auth Provider**: Handles user authentication
- **Auth Service**: JWT validation only

## 🚀 **Service Architecture**

### **Backend Service Responsibilities (Industry Standard)**
- **Auth Service**: JWT token validation
- **Gateway**: Routes requests to business services
- **Patient Service**:
  - `user_profiles` + `patient_profiles` management
  - Medical history viewing (via appointments and medical_records)
  - Patient profile and demographics management
- **Provider Service**:
  - `user_profiles` + `provider_profiles` management
  - Medical records management (via appointments and medical_records)
  - Provider profile and credentials management
- **Appointment Service**: Appointment scheduling, availability management, and lifecycle
- **AI Service**: Healthcare analytics and clinical insights
- **Data Layer**: Data access for all services

### **Service Boundaries (Industry Standard)**
- **Provider Service**:
  - ✅ `user_profiles` + `provider_profiles` management
  - ✅ Provider profile and credentials management
  - ✅ Provider discovery and search functionality
  - ✅ Medical records management (providers create/update medical records; appointments link records via appointment_id)
  - ❌ Appointment slots (handled by Appointment Service)
- **Patient Service**:
  - ✅ `user_profiles` + `patient_profiles` management
  - ✅ Patient profile and demographics management
  - ✅ Medical history viewing (via appointments and medical_records)
  - ❌ Appointment booking (handled by Appointment Service)
- **Appointment Service**:
  - ✅ Appointment scheduling and lifecycle management
  - ✅ Provider availability and slot management
  - ❌ Medical records management (handled by Provider Service; appointments may have medical_records set to null when creating)
  - ❌ Provider/patient profile management (handled by respective services)

### **Service Ports**
| Service | Port | External Access | Technology |
|---------|------|----------------|------------|
| **API Gateway** | 8080 | ✅ **EXTERNAL** | Java/Spring Boot |
| **Auth Service** | 8001 | ❌ Internal Only | Java/Spring Boot |
| **Patient Service** | 8002 | ❌ Internal Only | Java/Spring Boot |
| **Provider Service** | 8003 | ❌ Internal Only | Java/Spring Boot |
| **Appointment Service** | 8004 | ❌ Internal Only | Java/Spring Boot |
| **AI Service** | 8005 | ❌ Internal Only | Python/FastAPI |

### **Service Interaction Examples**
- **Appointment Management**: Appointment Service (availability windows, slot generation, booking, lifecycle)
- **Provider Profiles**: Provider Service (profiles, credentials, specialties, medical records)
- **Patient Profiles**: Patient Service (patient data, medical history viewing)
- **AI Analysis**: AI Service (healthcare insights, clinical support)

### **Data Flow Examples**
- **Provider Sets Availability**: Provider → Appointment Service → Creates multiple 30-min slots
- **Patient Books Appointment**: Patient → Appointment Service → Books specific time slot
- **Provider Manages Records**: Provider → Provider Service → Creates/updates medical records

### **Simplified API Structure**
- **Provider Service**: Profile management, medical records
- **Appointment Service**: Availability management, appointment booking
- **Patient Service**: Patient profiles, medical history viewing



## 🔄 **Data Flow Patterns**

### **1. Authentication Flow**
```
Frontend → Gateway (8080) → Auth Service (8001) → Business Services
```

### **2. Data Access Flow**
```
Service → Database → Response to Client
```

## 🚀 **Infrastructure**

### **Platform: Railway**
- **Services**: Java Spring Boot and Python FastAPI microservices
- **Database**: PostgreSQL
- **File Storage**: AWS S3
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

### **Future Considerations** (Skip for now - MVP focus)
- **High Traffic**: Can be designed later when needed
- **Event-Driven**: Can be added when business logic requires it
- **Advanced Caching**: Redis can be added when performance needs it
- **Service Mesh**: Not needed for MVP project

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



## 🎯 **Success Metrics**

### **Project Goals**
- **Spring Boot Services**: Production-ready microservice development
- **AI Integration**: Understanding ML/AI in healthcare context
- **Clean Architecture**: Well-designed, maintainable services
- **Practical Experience**: Working healthcare platform

### **MVP Goals** (Keep it Simple)
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

## 🔍 **Discussion Points & Open Questions**

### **1. Gateway Implementation Strategy**
**Decision**: Gateway handles orchestration for registration and complex flows
- **Simple Routing**: Basic request forwarding to services
- **Orchestration**: Gateway coordinates multi-service operations (registration, complex flows)
- **Implementation**: Balance between functionality and MVP goals

### **2. External Auth Integration Strategy**
**Decision**: Gateway orchestrates registration, Auth Service validates JWT only
- **Flow**: External Auth → JWT Token → Gateway → Auth Service → Business Services
- **Registration**: Gateway orchestrates registration (calls Supabase Auth + business services)
- **Integration**: Clear pattern established with Gateway orchestration

### **3. Service Interaction Strategy**
**Question**: How should services call each other internally?
- **When**: Only when business logic requires it
- **How**: REST APIs, clear contracts, avoid circular dependencies
- **Decision Needed**: Interaction patterns and guidelines

### **4. Registration Flow Strategy**
**Recommended Approach**: Gateway Orchestration for complete registration

**Flow**: User → Gateway → [Supabase Auth + Patient/Provider Service] → Success/Rollback

**Responsibilities**:
- **Supabase**: Username/password storage, authentication
- **Gateway**: Orchestrates complete registration process
- **Business Services**: User profile and business data
- **Auth Service**: Only JWT validation (no registration logic)

**Failure Handling**:
- Gateway coordinates rollback if any step fails
- Transactional approach: All-or-nothing registration
- Clean error messages to user regardless of which step failed

**API Placement**:
- **Registration API Location**: Gateway Service
- `POST /api/auth/register/patient`
- `POST /api/auth/register/provider`
- Gateway endpoint calls Supabase API + Business Service
- Single external endpoint for complete registration

---

*This document provides the high-level system design for a professional healthcare AI microservices platform. Focus on production-ready Spring Boot services, clean architecture, and progressive complexity.*
