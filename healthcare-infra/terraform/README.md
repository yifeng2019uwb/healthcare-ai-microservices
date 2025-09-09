# Healthcare Database Infrastructure - Terraform

> **🎯 Database Table Creation with Terraform**
>
> This directory contains Terraform configurations to create and manage our healthcare database tables using PostgreSQL.

## 📁 **File Structure**

```
terraform/
├── main.tf                    # Core infrastructure configuration
├── variables.tf               # Variable definitions
├── supabase/                  # Supabase-specific configurations
│   ├── main.tf               # Supabase provider configuration
│   ├── variables.tf          # Supabase variables
│   ├── 01_users.tf           # User profiles table
│   ├── 02_patient_profiles.tf # Patient profiles table
│   ├── 03_provider_profiles.tf # Provider profiles table
│   ├── 04_appointments.tf    # Appointments table
│   ├── 05_medical_records.tf # Medical records table
│   ├── 06_audit_logs.tf      # Audit logs table
│   └── config/               # Configuration files (gitignored)
└── README.md                 # This file
```

## 🗄️ **Database Tables**

### **Core Tables (6 total)**
1. **user_profiles** - User authentication and basic profile data
2. **patient_profiles** - Patient profiles and medical information
3. **provider_profiles** - Healthcare provider profiles and credentials
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
1. **PostgreSQL Database** - Any PostgreSQL provider (Supabase, AWS RDS, etc.)
2. **Terraform** - Install Terraform CLI
3. **Credentials** - Database connection details

### **Step 1: Configure Database Connection**

#### **A. Copy Configuration Template**
```bash
# Copy example variables file
cp supabase/terraform.tfvars.example supabase/config/terraform.tfvars
```

#### **B. Edit Configuration**
```bash
# Edit with your actual database details
nano supabase/config/terraform.tfvars
```

**Fill in `terraform.tfvars`:**
```hcl
# Database connection details
supabase_host     = "your-db-host.com"
supabase_port     = 5432
supabase_database = "postgres"
supabase_username = "your-username"
supabase_password = "your-password"
```

### **Step 2: Deploy Database Schema**

#### **Deploy All Tables**
```bash
# Navigate to supabase directory
cd supabase

# Initialize Terraform
terraform init

# Review deployment plan
terraform plan

# Deploy all tables and indexes
terraform apply
```

#### **Deploy Single Table**
```bash
# Deploy only user profiles table
terraform apply -target=null_resource.create_users_table

# Deploy only appointments table
terraform apply -target=null_resource.create_appointments_table
```

### **Step 3: Verify Deployment**
```bash
# Connect to database
psql -h your-db-host.com -p 5432 -U your-username -d postgres

# List tables
\dt
```

## 🔧 **Configuration**

### **Environment Variables**
- `supabase/config/terraform.tfvars` - Database connection details (required)
- **Location**: Copy from `supabase/terraform.tfvars.example`

### **Customization**
- **Database Connection** - Update variables in `terraform.tfvars`
- **Table Structure** - Edit individual table files
- **Security** - All credentials stored in `config/` directory (gitignored)

## 📊 **Database Schema Overview**

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│user_profiles│    │patient_profiles│ │provider_profiles│
│             │    │             │    │             │
│ • id (PK)   │◄───┤ • user_id   │    │ • user_id   │
│ • email     │    │ • first_name│    │ • specialty │
│ • role      │    │ • last_name │    │ • license   │
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
# Connect to database
psql -h your-db-host.com -p 5432 -U your-username -d postgres

# Test table creation
\dt

# Test data insertion
INSERT INTO user_profiles (email, role) VALUES ('test@test.com', 'PATIENT');
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

## 🔧 **Troubleshooting**

### **Common Issues**

#### **1. Database Connection Issues**
```bash
# Error: "connection refused" or "authentication failed"
# Solution: Check your terraform.tfvars file
cat supabase/config/terraform.tfvars

# Verify connection details
# - Host: your-db-host.com
# - Username: your-username
# - Password: your-password
# - Database: postgres
```

#### **2. Permission Issues**
```bash
# Error: "permission denied for table"
# Solution: Ensure your database user has CREATE privileges
```

#### **3. Terraform State Issues**
```bash
# Error: "state file not found"
# Solution: Initialize Terraform first
terraform init

# If state is corrupted, remove and reinitialize
rm -rf .terraform
terraform init
```

### **Verification Commands**
```bash
# Check Terraform version
terraform version

# Check if credentials are set
cat supabase/config/terraform.tfvars

# Test database connection
psql -h your-db-host.com -p 5432 -U your-username -d postgres -c "\dt"
```

## 🔄 **Next Steps**

1. **Apply Terraform** - Create the database tables
2. **Test Connection** - Verify database connectivity
3. **Insert Test Data** - Add sample data for testing
4. **Build Services** - Start building Spring Boot services
5. **Database Integration** - Connect services to database

---

*This Terraform configuration creates the complete database infrastructure for our healthcare AI microservices platform.*