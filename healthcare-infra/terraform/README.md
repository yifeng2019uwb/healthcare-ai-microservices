# Healthcare Database Infrastructure - Terraform

> **🎯 Database Table Creation with Terraform**
>
> This directory contains Terraform configurations to create and manage our healthcare database tables using Neon PostgreSQL.

## 📁 **File Structure**

```
terraform/
├── main.tf                    # Provider configuration and Neon project setup
├── schema.tf                  # Database schema creation
├── outputs.tf                 # Output values for database connection
├── tables/                    # Individual table definitions
│   ├── users.tf              # Users table (authentication)
│   ├── patients.tf           # Patients table (patient profiles)
│   ├── providers.tf          # Providers table (healthcare providers)
│   ├── appointments.tf       # Appointments table (scheduling)
│   ├── medical_records.tf    # Medical records table (patient records)
│   └── audit_logs.tf         # Audit logs table (compliance tracking)
└── README.md                 # This file
```

## 🗄️ **Database Tables**

### **Core Tables (6 total)**
1. **users** - User authentication and basic profile data
2. **patients** - Patient profiles and medical information
3. **providers** - Healthcare provider profiles and credentials
4. **appointments** - Appointment scheduling and management
5. **medical_records** - Patient medical records and documents
6. **audit_logs** - Audit trail for all data changes and compliance

### **Key Features**
- **UUID Primary Keys** - All tables use UUID for unique identification
- **Foreign Key Relationships** - Proper referential integrity
- **Indexes** - Optimized for common query patterns
- **JSONB Fields** - Flexible data storage for complex structures
- **Timestamps** - Created/updated tracking on all tables

## 🚀 **Usage**

### **Prerequisites**
1. **Existing Neon Database** - You already have `medconnect-healthcase` database
2. **Database Connection Details** - Host, port, username, password
3. **Terraform** - Install Terraform CLI

### **Setup**
```bash
# Copy the example variables file from config directory
cp ../config/terraform.tfvars.example terraform.tfvars

# Edit terraform.tfvars with your actual database connection details
# neon_host     = "your-neon-host.neon.tech"
# neon_port     = 5432
# neon_database = "medconnect-healthcase"
# neon_username = "your-username"
# neon_password = "your-password"

# Initialize Terraform
terraform init

# Plan the infrastructure
terraform plan

# Apply the infrastructure
terraform apply
```

### **Database Connection**
After applying, you'll get:
- **Database URL** - Complete connection string
- **Host/Port** - Database connection details
- **Table List** - All created tables

## 🔧 **Configuration**

### **Environment Variables**
- `terraform.tfvars` - Database connection details (required)
- **Location**: Copy from `../config/terraform.tfvars.example`

### **Customization**
- **Database Connection** - Update variables in `terraform.tfvars`
- **Table Structure** - Edit individual table files in `tables/`
- **Security** - All credentials stored in `../config/` directory

## 📊 **Database Schema Overview**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    users    │    │  patients   │    │  providers  │
│             │    │             │    │             │
│ • id (PK)   │◄───┤ • user_id   │    │ • user_id   │
│ • email     │    │ • first_name│    │ • specialty │
│ • user_type │    │ • last_name │    │ • license   │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│appointments │    │medical_records│   │ audit_logs  │
│             │    │             │    │             │
│ • patient_id│    │ • patient_id│    │ • table_name│
│ • provider_id│   │ • provider_id│   │ • record_id │
│ • date/time │    │ • record_type│   │ • operation │
└─────────────┘    └─────────────┘    └─────────────┘
```

## 🧪 **Testing Database**

### **Direct Database Testing**
```bash
# Connect to database using output URL
psql "$(terraform output -raw neon_database_url)"

# Test table creation
\dt

# Test data insertion
INSERT INTO users (email, user_type) VALUES ('test@test.com', 'PATIENT');
```

### **Table Validation**
```sql
-- Check all tables exist
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public';

-- Check foreign key relationships
SELECT * FROM information_schema.table_constraints
WHERE constraint_type = 'FOREIGN KEY';
```

## 🔄 **Next Steps**

1. **Apply Terraform** - Create the database tables
2. **Test Connection** - Verify database connectivity
3. **Insert Test Data** - Add sample data for testing
4. **Build Services** - Start building Spring Boot services
5. **Database Integration** - Connect services to database

---

*This Terraform configuration creates the complete database infrastructure for our healthcare AI microservices platform.*