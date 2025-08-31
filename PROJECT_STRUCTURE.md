# Healthcare AI Microservices - Project Structure

## 📁 **Directory Overview**

```
healthcare-ai-microservices/
├── .github/                    # GitHub Actions CI/CD workflows
├── config/                     # Configuration files and templates
├── docker/                     # Docker configurations and compose files
├── docs/                       # Project documentation (already exists)
├── frontend/                   # React frontend applications
│   ├── patient-portal/         # Patient-facing application
│   ├── provider-portal/        # Healthcare provider application
│   └── admin-portal/           # Administrative interface
├── services/                   # Backend microservices (Hybrid: Java + Python)
│   ├── shared/                 # Shared Java modules and utilities
│   ├── gateway/                # Spring Cloud Gateway (Port 8000) - Java
│   ├── auth-service/           # Authentication service (Port 8001) - Java
│   ├── patient-service/        # Patient management (Port 8002) - Java
│   ├── provider-service/       # Provider management (Port 8003) - Java
│   ├── appointment-service/    # Appointment management (Port 8004) - Java
│   ├── ai-service/             # AI features (Port 8005) - Python/FastAPI
│   └── file-storage-service/   # File management (Port 8006) - Java
├── terraform/                  # Infrastructure as Code
│   ├── environments/           # Environment-specific configurations
│   ├── modules/                # Reusable Terraform modules
│   └── main.tf                 # Main Terraform configuration
├── kubernetes/                 # Kubernetes deployment manifests
│   ├── base/                   # Base manifests for all services
│   └── overlays/               # Environment-specific overrides
├── monitoring/                 # Monitoring and observability setup
├── scripts/                    # Deployment and utility scripts
├── integration_tests/          # API testing and integration tests
├── .gitignore                  # Git ignore rules
├── README.md                   # Main project documentation
├── deploy.sh                   # Main deployment script
└── PROJECT_STRUCTURE.md        # This file
```

## 🎯 **Implementation Phases**

### **Phase 1: Foundation (Weeks 1-2)**
- [ ] Project structure setup ✅
- [ ] Basic deployment scripts ✅
- [ ] Shared data layer module
- [ ] Auth service implementation
- [ ] Basic database schema

### **Phase 2: Core Services (Weeks 3-4)**
- [ ] Patient service
- [ ] Provider service
- [ ] Basic appointment management
- [ ] Frontend integration

### **Phase 3: Support Services (Weeks 5-6)**
- [ ] File storage service
- [ ] AI service basic implementation
- [ ] Advanced features and optimization

## 🏗️ **Architecture Alignment**

- **Clear separation** of concerns
- **Environment-specific** configurations
- **Infrastructure as Code** with Terraform
- **Container orchestration** with Kubernetes
- **Comprehensive testing** structure
- **Security-first** approach

## 🔧 **Technology Stack**

### **Java Services (6)**
- **Framework**: Spring Boot 3.2+ with Java 17
- **Build Tool**: Maven (multi-module project)
- **Services**: Gateway, Auth, Patient, Provider, Appointment, File Storage

### **Python Service (1)**
- **Framework**: FastAPI with Python 3.11+
- **AI Libraries**: TensorFlow, PyTorch, scikit-learn, healthcare AI
- **Service**: AI Service for ML/AI capabilities

### **Shared Infrastructure**
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Infrastructure**: Terraform
- **Database**: Neon PostgreSQL
- **File Storage**: AWS S3

## 🚀 **Next Steps**

1. **Review structure** - Ensure this aligns with your vision
2. **Start with shared module** - Build the data access layer inside services/
3. **Implement Auth Service** - First Java microservice
4. **Add frontend portals** - React applications
5. **Infrastructure setup** - Terraform and Kubernetes
6. **AI Service** - Python FastAPI implementation

## 📚 **Documentation**

- **System Design**: `docs/system-design.md`
- **Project Setup**: `docs/project-setup.md`
- **API Documentation**: Will be added as services are implemented
- **Deployment Guide**: Will be added as infrastructure is set up
