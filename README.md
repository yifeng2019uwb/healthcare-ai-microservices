# Healthcare AI Microservices Platform

> **Learning-Focused Healthcare AI Platform** - Master Spring Boot, AI Integration, and Microservices Architecture

## 📋 **Overview**

A comprehensive healthcare AI microservices platform designed for learning Spring Boot, AI integration, and microservices patterns. Built with clean architecture principles and progressive complexity.

### **Key Features**
- **🔐 Authentication**: JWT-based authentication with Supabase Auth
- **🏥 Healthcare Services**: Patient, Provider, and Appointment management
- **🤖 AI Integration**: Healthcare analytics and clinical insights
- **📊 Data Management**: Neon PostgreSQL with AWS S3 file storage
- **🚀 Microservices**: Spring Boot services with API Gateway

## 🏗️ **Project Structure**

```
healthcare-ai-microservices/
├── docs/                           # Design documentation
│   ├── system-design.md           # System architecture
│   ├── authentication-design.md   # Auth strategy
│   ├── database-design.md         # Database schema
│   └── BACKLOG.md                 # Implementation planning
├── healthcare-infra/              # Infrastructure as Code
│   ├── terraform/                 # Database tables
│   └── config/                    # Credentials (ignored by git)
├── services/                      # Backend services
│   ├── gateway/                   # API Gateway (Port 8080)
│   ├── auth/                      # Auth Service (Port 8001)
│   ├── patient/                   # Patient Service (Port 8002)
│   ├── provider/                  # Provider Service (Port 8003)
│   ├── appointment/               # Appointment Service (Port 8004)
│   └── ai/                        # AI Service (Port 8005)
├── frontend/                      # React applications
│   ├── patient-portal/            # Patient web app
│   └── provider-portal/           # Provider web app
├── scripts/                       # Development scripts
└── .github/workflows/             # CI/CD pipelines
```

## 🚀 **Installation / Setup**

### **Prerequisites**
- **Java 17+** - For Spring Boot services
- **Maven 3.8+** - Build tool
- **Node.js 18+** - For React frontend
- **Docker** - Containerization
- **Git** - Version control

### **Quick Setup**
```bash
# Clone the repository
git clone https://github.com/yifeng2019uwb/healthcare-ai-microservices
cd healthcare-ai-microservices

# Setup development environment
./scripts/setup-dev.sh

# Test project structure
./scripts/test-ci.sh
```

### **Database Setup**
```bash
# Copy configuration template
cp healthcare-infra/examples/terraform.tfvars.example healthcare-infra/config/terraform.tfvars

# Edit with your Neon database credentials
# Then create database tables
cd healthcare-infra/terraform
terraform init
terraform apply
```

## 💻 **Usage**

### **Development Workflow**
```bash
# Test locally before pushing
./scripts/test-ci.sh

# If tests pass, commit and push
git add .
git commit -m "Your changes"
git push
```

### **System Architecture**
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
                    └────────────┬───────────┘
                                 │
         ┌───────────────────────┼────────────────────────┐
         │                       │                        │
    ┌────▼────┐   ┌──────────┐   ┌────▼────┐    ┌──────────┐
    │ Patient │   │Provider  │   │Appointment│    │   AI     │
    │Service  │   │Service   │   │ Service   │    │ Service  │
    │ 8002    │   │ 8003     │   │ 8004      │    │ 8005     │
    └────┬────┘   └────┬─────┘   └────┬────┘    └────┬─────┘
         │             │              │               │
         └─────────────┼──────────────┼───────────────┘
                       │              │
              ┌────────▼──────────────▼────────┐
              │      Shared Data Layer         │
              │     (Database Access)          │
              └──────────────┬─────────────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
    ┌────▼────┐   ┌─────────▼────────┐   ┌──────▼────┐
    │ Neon DB │   │   S3             │   │   Shared  │
    │(PostgreSQL)│   │(File Storage)   │   │   Data    │
    │            │   │                 │   │   Layer   │
    └───────────┘   └─────────────────┘   └───────────┘
```

### **Service Architecture**
| Service          | Port | Responsibility                     |
| ---------------- | ---- | ---------------------------------- |
| API Gateway      | 8080 | Routes requests to all services    |
| Auth Service     | 8001 | JWT validation                     |
| Patient Service  | 8002 | Patient profiles, medical history  |
| Provider Service | 8003 | Provider profiles, medical records |
| Appointment      | 8004 | Scheduling & availability          |
| AI Service       | 8005 | Analytics & insights               |

## 🎯 **Technology Stack**

**Backend**: Spring Boot 3.2+, Java 17
**Database**: Neon PostgreSQL
**File Storage**: AWS S3
**Frontend**: React 18+ with TypeScript
**Authentication**: Supabase Auth + JWT
**Deployment**: Railway
**CI/CD**: GitHub Actions

## 🏥 **Planned Features**

- **Authentication Service** (JWT validation)
- **Patient Service** (profiles, medical history)
- **Provider Service** (provider data, medical records)
- **Appointment Service** (scheduling, availability)
- **AI Service** (analytics, clinical insights)
- **API Gateway** (single entry point, request routing)

## 🚧 **Current Status**

- ✅ Initial project design
- ✅ Infrastructure setup (Terraform + DB)
- ✅ Shared module (100% test coverage, PostgreSQL)
- 🔄 Database schema deployment
- ⏳ Repository layer implementation
- ⏳ API Gateway skeleton
- ⏳ Auth Service MVP
- ⏳ Core healthcare services
- ⏳ AI service integration
- ⏳ Frontend portals

## 🧪 **Testing**

### **Local Testing**
```bash
# Test project structure
./scripts/test-ci.sh

# Run specific service tests
cd services/patient
mvn test

# Run all tests
cd services
mvn test
```

### **CI/CD Pipeline**
- **Automatic**: Runs on every push and pull request
- **Validation**: Checks project structure and builds
- **Reports**: Test results and build artifacts
- **Status**: View in GitHub Actions tab

## 📚 **Documentation**

### **Design Documents**
- **[System Design](docs/system-design.md)** - Complete architecture overview
- **[Authentication Design](docs/authentication-design.md)** - JWT and security strategy
- **[Database Design](docs/database-design.md)** - Schema and data modeling
- **[Gateway Service Design](docs/gateway-service-design.md)** - API Gateway routing and configuration
- **[Patient Service Design](docs/patient-service-design.md)** - Patient management APIs
- **[Provider Service Design](docs/provider-service-design.md)** - Provider and medical records APIs
- **[Appointment Service Design](docs/appointment-service-design.md)** - Scheduling and availability APIs
- **[AI Service Design](docs/ai-service-design.md)** - Healthcare analytics and insights
- **[Service Design Template](docs/service-design-template.md)** - Standard format for service designs

### **Implementation**
- **[Backlog](BACKLOG.md)** - Task planning and roadmap
- **[Daily Work Log](DAILY_WORK_LOG.md)** - Progress tracking
- **[Project Setup](docs/project-setup.md)** - Detailed setup instructions

### **Infrastructure**
- **[Infrastructure Design](healthcare-infra/README.md)** - Database and deployment setup
- **[Terraform Config](healthcare-infra/terraform/)** - Database table definitions