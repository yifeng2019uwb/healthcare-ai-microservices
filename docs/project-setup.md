# Project Setup Guide - Healthcare AI Microservices

## 🚀 **Getting Started**

This guide will help you set up the Healthcare AI Microservices project from scratch, including all necessary infrastructure and services.

## 📋 **Prerequisites**

### **Required Tools**
- [ ] **Java 17+** (OpenJDK or Oracle JDK)
- [ ] **Maven 3.8+** or **Gradle 8+**
- [ ] **Docker** (for local development)
- [ ] **Git** (for version control)
- [ ] **Node.js 18+** (for frontend development)

### **Required Accounts**
- [ ] **Supabase Account** (https://supabase.com)
- [ ] **Railway Account** (https://railway.app)
- [ ] **Neon Account** (https://neon.tech)
- [ ] **Redis Cloud Account** (https://redis.com/redis-enterprise-cloud)
- [ ] **Kafka Account** (Confluent Cloud or self-hosted) - *Not needed for initial phases*

## 🏗️ **Project Structure**

```
healthcare-ai-microservices/
├── docs/                          # Documentation
│   ├── system-design.md          # System architecture
│   ├── data-layer-architecture.md # Data layer details
│   └── project-setup.md          # This file
├── services/                      # Microservices
│   ├── auth-service/             # Authentication service
│   ├── gateway-service/          # API Gateway
│   ├── patient-service/          # Patient management
│   ├── provider-service/         # Provider management
│   ├── appointment-service/      # Appointment management
│   └── ai-service/               # AI insights service
├── shared/                       # Shared components
│   ├── models/                   # Common data models
│   ├── utils/                    # Utility classes
│   └── config/                   # Configuration files
├── frontend/                     # React applications
│   ├── patient-portal/          # Patient web app
│   ├── provider-portal/          # Provider web app

├── infrastructure/               # Infrastructure as code
│   ├── terraform/                # Terraform configurations
│   └── docker/                   # Docker configurations
├── scripts/                      # Build and deployment scripts
└── README.md                     # Project overview
```

## 🔧 **Step 1: Infrastructure Setup**

### **1.1 Supabase Project Setup**

1. **Create Supabase Project**
   ```bash
   # Go to https://supabase.com
   # Click "New Project"
   # Choose organization and region
   # Set project name: healthcare-ai-platform
   # Set database password (save this!)
   ```

2. **Configure Authentication**
   ```bash
   # In Supabase Dashboard:
   # 1. Go to Authentication > Settings
   # 2. Enable Email confirmations
   # 3. Set Site URL: http://localhost:3000
   # 4. Configure SMTP settings
   ```

3. **Get API Keys**
   ```bash
   # In Supabase Dashboard:
   # 1. Go to Settings > API
   # 2. Copy Project URL
   # 3. Copy anon public key
   # 4. Copy service_role key (keep secret!)
   ```

### **1.2 Neon PostgreSQL Setup**

1. **Create Neon Project**
   ```bash
   # Go to https://neon.tech
   # Click "Create Project"
   # Set project name: healthcare-ai-db
   # Choose region (same as Supabase)
   # Set database name: healthcare_ai
   ```

2. **Get Connection Details**
   ```bash
   # In Neon Dashboard:
   # 1. Copy connection string
   # 2. Note database name
   # 3. Save credentials
   ```

### **1.3 Redis Cloud Setup**

1. **Create Redis Database**
   ```bash
   # Go to https://redis.com/redis-enterprise-cloud
   # Click "Try Free"
   # Set database name: healthcare-ai-cache
   # Choose region (same as others)
   ```

2. **Get Connection Details**
   ```bash
   # In Redis Cloud Dashboard:
   # 1. Copy endpoint URL
   # 2. Copy port number
   # 3. Copy password
   ```

### **1.4 Kafka Setup** *(Optional - Phase 3+)*

> **Note**: Kafka is not needed for the initial phases. Focus on core services first.

1. **Option A: Confluent Cloud (Managed)**
   ```bash
   # Go to https://confluent.cloud
   # Click "Get Started Free"
   # Create cluster: healthcare-ai-cluster
   # Choose region (same as others)
   # Create API keys for authentication
   ```

2. **Option B: Self-Hosted (Local Development)**
   ```bash
   # Use Docker Compose (see docs/kafka-implementation.md)
   # Or install Apache Kafka locally
   # Set up Zookeeper and Kafka broker
   ```

3. **Get Connection Details**
   ```bash
   # In Kafka Dashboard:
   # 1. Copy bootstrap servers
   # 2. Copy API keys (if using Confluent Cloud)
   # 3. Note cluster configuration
   ```

## 🚀 **Step 2: Local Development Environment**

### **2.1 Clone and Setup Project**

```bash
# Clone the repository
git clone <your-repo-url>
cd healthcare-ai-microservices

# Create necessary directories
mkdir -p services auth-service gateway-service patient-service provider-service appointment-service ai-service
mkdir -p shared/{models,utils,config}
mkdir -p frontend/{patient-portal,provider-portal}
mkdir -p infrastructure/{terraform,docker}
mkdir -p scripts
```

### **2.2 Environment Configuration**

Create `.env` file in root directory:
```bash
# Supabase Configuration
SUPABASE_URL=your_supabase_project_url
SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key

# Database Configuration
DATABASE_URL=your_neon_connection_string
DATABASE_USERNAME=your_neon_username
DATABASE_PASSWORD=your_neon_password

# Redis Configuration
REDIS_HOST=your_redis_host
REDIS_PORT=your_redis_port
REDIS_PASSWORD=your_redis_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=your_kafka_bootstrap_servers
KAFKA_CONSUMER_GROUP=healthcare-ai-group
KAFKA_API_KEY=your_kafka_api_key
KAFKA_API_SECRET=your_kafka_api_secret

# Email Service
SENDGRID_API_KEY=your_sendgrid_api_key
EMAIL_FROM=noreply@healthcare-ai.com

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=3600
JWT_REFRESH_EXPIRATION=86400

# Application Configuration
APP_ENV=development
APP_PORT=8080
GATEWAY_PORT=8081
AUTH_SERVICE_PORT=8082
PATIENT_SERVICE_PORT=8083
PROVIDER_SERVICE_PORT=8084
APPOINTMENT_SERVICE_PORT=8085
AI_SERVICE_PORT=8086
```

## 🏗️ **Step 3: Service Implementation**

### **3.1 Auth Service Setup**

```bash
cd services/auth-service

# Create Spring Boot project structure
mkdir -p src/main/java/com/healthcare/auth
mkdir -p src/main/resources
mkdir -p src/test/java/com/healthcare/auth
```

Create `pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.healthcare</groupId>
    <artifactId>auth-service</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Supabase SDK -->
        <dependency>
            <groupId>io.github.jan-tennert.supabase</groupId>
            <artifactId>postgrest-kt</artifactId>
            <version>1.4.7</version>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### **3.2 Gateway Service Setup**

```bash
cd ../gateway-service

# Create Spring Cloud Gateway project
mkdir -p src/main/java/com/healthcare/gateway
mkdir -p src/main/resources
```

Create `pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.healthcare</groupId>
    <artifactId>gateway-service</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## 🗄️ **Step 4: Database Setup**

### **4.1 Database Schema Creation**

Create `scripts/init-database.sql`:
```sql
-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create custom types
CREATE TYPE user_role AS ENUM ('PATIENT', 'PROVIDER');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE verification_status AS ENUM ('PENDING', 'VERIFIED', 'REJECTED');
CREATE TYPE appointment_type AS ENUM ('REGULAR_CONSULTATION', 'FOLLOW_UP', 'NEW_PATIENT_INTAKE', 'PROCEDURE_CONSULTATION');
CREATE TYPE appointment_status AS ENUM ('AVAILABLE', 'SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE record_type AS ENUM ('DIAGNOSIS', 'TREATMENT', 'SUMMARY', 'LAB_RESULT', 'PRESCRIPTION', 'NOTE', 'OTHER');
CREATE TYPE insight_type AS ENUM ('SYMPTOM_ANALYSIS', 'RISK_ASSESSMENT', 'TREATMENT_RECOMMENDATION');
CREATE TYPE model_type AS ENUM ('CLASSIFICATION', 'REGRESSION', 'NLP', 'COMPUTER_VISION');
CREATE TYPE audit_action AS ENUM ('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT');
CREATE TYPE resource_type AS ENUM ('USER_PROFILE', 'PATIENT_PROFILE', 'PROVIDER_PROFILE', 'APPOINTMENT', 'MEDICAL_RECORD');
CREATE TYPE compliance_event_type AS ENUM ('DATA_ACCESS', 'DATA_MODIFICATION', 'SECURITY_EVENT');
CREATE TYPE compliance_severity AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE hipaa_category AS ENUM ('PHI_ACCESS', 'DATA_BREACH', 'UNAUTHORIZED_ACCESS');
CREATE TYPE data_classification AS ENUM ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'RESTRICTED');
CREATE TYPE access_type AS ENUM ('VIEW', 'CREATE', 'UPDATE', 'DELETE');
CREATE TYPE data_category AS ENUM ('DEMOGRAPHIC', 'MEDICAL', 'FINANCIAL');
CREATE TYPE access_method AS ENUM ('WEB_APP', 'API', 'MOBILE_APP', 'SYSTEM');

-- Create tables (as defined in data-layer-architecture.md)
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY REFERENCES auth.users(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    role user_role NOT NULL DEFAULT 'PATIENT',
    status user_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login TIMESTAMP WITH TIME ZONE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    verification_status verification_status DEFAULT 'PENDING'
);

-- Add more tables as defined in the architecture document...
```

### **4.2 Run Database Initialization**

```bash
# Connect to your Neon database
psql "your_neon_connection_string"

# Run the initialization script
\i scripts/init-database.sql

# Verify tables were created
\dt
```

## 🔐 **Step 5: Authentication Implementation**

### **5.1 Auth Service Main Class**

Create `services/auth-service/src/main/java/com/healthcare/auth/AuthServiceApplication.java`:
```java
package com.healthcare.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

### **5.2 Supabase Configuration**

Create `services/auth-service/src/main/java/com/healthcare/auth/config/SupabaseConfig.java`:
```java
package com.healthcare.auth.config;

import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.createSupabaseClient;
import io.github.jan.supabase.gotrue.GoTrue;
import io.github.jan.supabase.postgrest.Postgrest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    @Bean
    public SupabaseClient supabaseClient() {
        return createSupabaseClient(supabaseUrl, supabaseAnonKey) {
            install(GoTrue)
            install(Postgrest)
        };
    }
}
```

## 🚀 **Step 6: Testing the Setup**

### **6.1 Build and Run Services**

```bash
# Build auth service
cd services/auth-service
mvn clean install

# Run auth service
mvn spring-boot:run

# In another terminal, build gateway service
cd ../gateway-service
mvn clean install

# Run gateway service
mvn spring-boot:run
```

### **6.2 Test Health Endpoints**

```bash
# Test auth service health
curl http://localhost:8082/actuator/health

# Test gateway service health
curl http://localhost:8081/actuator/health
```

## 📱 **Step 7: Frontend Setup**

### **7.1 Patient Portal Setup**

```bash
cd frontend/patient-portal

# Create React app
npx create-react-app . --template typescript

# Install dependencies
npm install @supabase/supabase-js axios react-router-dom @mui/material @emotion/react @emotion/styled
```

### **7.2 Provider Portal Setup**

```bash
cd ../provider-portal

# Create React app
npx create-react-app . --template typescript

# Install dependencies
npm install @supabase/supabase-js axios react-router-dom @mui/material @emotion/react @emotion/styled
```



```bash


# Create React app
npx create-react-app . --template typescript

# Install dependencies
npm install @supabase/supabase-js axios react-router-dom @mui/material @emotion/react @emotion/styled
```

## 🐳 **Step 8: Docker Setup**

### **8.1 Create Docker Compose**

Create `docker-compose.yml` in root directory:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: healthcare_ai
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  auth-service:
    build: ./services/auth-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - postgres
      - redis

  gateway-service:
    build: ./services/gateway-service
    ports:
      - "8081:8081"
    depends_on:
      - auth-service

volumes:
  postgres_data:
  redis_data:
```

## 🚀 **Step 9: Railway Deployment**

### **9.1 Railway Project Setup**

```bash
# Install Railway CLI
npm install -g @railway/cli

# Login to Railway
railway login

# Initialize Railway project
railway init

# Add services
railway service add auth-service
railway service add gateway-service
railway service add patient-service
railway service add provider-service
railway service add appointment-service
railway service add ai-service
```

### **9.2 Environment Variables in Railway**

Set environment variables in Railway dashboard:
```bash
# Copy from your .env file
SUPABASE_URL=your_supabase_project_url
SUPABASE_ANON_KEY=your_supabase_anon_key
SUPABASE_SERVICE_ROLE_KEY=your_supabase_service_role_key
DATABASE_URL=your_neon_connection_string
REDIS_HOST=your_redis_host
REDIS_PORT=your_redis_port
REDIS_PASSWORD=your_redis_password
```

## 📋 **Step 10: Verification Checklist**

### **Infrastructure**
- [ ] Supabase project created and configured
- [ ] Neon PostgreSQL database set up
- [ ] Redis Cloud instance configured
- [ ] Kafka cluster set up (Confluent Cloud or self-hosted) - *Phase 3+*
- [ ] Email service configured (SendGrid/Resend)

### **Backend Services**
- [ ] Auth service builds and runs locally
- [ ] Gateway service builds and runs locally
- [ ] Database schema created successfully
- [ ] Health endpoints responding

### **Frontend Applications**
- [ ] Patient portal React app created
- [ ] Provider portal React app created

- [ ] Supabase client configured

### **Deployment**
- [ ] Railway project initialized
- [ ] Services deployed to Railway
- [ ] Environment variables configured
- [ ] Health checks passing

## 🆘 **Troubleshooting**

### **Common Issues**

1. **Database Connection Failed**
   ```bash
   # Check connection string format
   # Verify network access
   # Check credentials
   ```

2. **Supabase Authentication Issues**
   ```bash
   # Verify API keys
   # Check project URL
   # Confirm authentication settings
   ```

3. **Service Build Failures**
   ```bash
   # Check Java version (must be 17+)
   # Verify Maven/Gradle installation
   # Check dependency versions
   ```

4. **Port Conflicts**
   ```bash
   # Change ports in application.yml
   # Check if ports are already in use
   # Use different ports for each service
   ```

## 📚 **Next Steps**

After completing this setup:

1. **Implement Business Logic**: Add CRUD operations to services
2. **Add Security Features**: Implement JWT validation and authorization
3. **Create API Endpoints**: Build RESTful APIs for each service
4. **Add Frontend Features**: Implement user interfaces
5. **Testing**: Add unit and integration tests
6. **Monitoring**: Set up logging and monitoring
7. **CI/CD**: Implement automated deployment pipeline

## 📞 **Support**

For issues and questions:
- Check the troubleshooting section above
- Review the system design documents
- Check service logs for error details
- Verify configuration values

---

*This setup guide provides the foundation for your Healthcare AI Microservices platform. Follow each step carefully and verify success before proceeding to the next step.*
