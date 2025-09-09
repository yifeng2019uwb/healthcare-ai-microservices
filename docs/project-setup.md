# Healthcare AI Microservices - Project Setup

## 📋 **Prerequisites**

### **Required Software**
- [ ] **Java 17+** (for backend development)
- [ ] **Maven 3.8+** or **Gradle 8+**
- [ ] **Docker** (for local development)
- [ ] **Git** (for version control)
- [ ] **Node.js 18+** (for frontend development)

### **Required Accounts**
- [ ] **Supabase Account** (https://supabase.com)
- [ ] **Railway Account** (https://railway.app)

## 🏗️ **Project Structure**

```
healthcare-ai-microservices/
├── services/                 # Microservices
│   ├── shared/              # Shared components
│   ├── patient-service/     # Patient management
│   ├── provider-service/    # Provider management
│   └── appointment-service/ # Appointment scheduling
├── frontend/                # React applications
├── healthcare-infra/        # Infrastructure as Code
└── docs/                   # Documentation
```

## 🚀 **Quick Start**

### **1. Clone and Setup**

```bash
# Clone the repository
git clone <your-repo-url>
cd healthcare-ai-microservices

# Copy environment template
cp .env.example .env
# Edit .env with your database credentials
```

### **2. Run with Docker**

```bash
# Start patient service
cd docker/patient
./run-local.sh
```

### **3. Verify Setup**

- [ ] Database project created
- [ ] Database connection working
- [ ] Environment variables set

## 🔧 **Development**

### **Backend Services**
- Spring Boot microservices
- PostgreSQL database
- Docker containerization

### **Frontend Applications**
- React with TypeScript
- Patient and Provider portals
- Responsive design

### **Infrastructure**
- Terraform for database setup
- Docker for local development
- Railway for deployment

## 📚 **Documentation**

- **System Design**: `docs/system-design.md`
- **API Documentation**: Available at `/swagger-ui.html`
- **Database Schema**: `healthcare-infra/terraform/supabase/`

## 🆘 **Troubleshooting**

### **Common Issues**
1. **Database Connection**: Check database credentials in `.env`
2. **Service Startup**: Check Docker logs
3. **Frontend Issues**: Check API endpoints and CORS

### **Getting Help**
- Check the `docs/` directory for detailed guides
- Create GitHub issues for bugs
- Review the project README

---

**Happy Coding! 🚀**