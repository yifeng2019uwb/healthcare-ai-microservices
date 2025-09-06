# Healthcare Database Infrastructure - Terraform

> **🎯 Database Table Creation with Terraform**
>
> This directory contains Terraform configurations to create and manage our healthcare database tables using Neon PostgreSQL.

## 📁 **File Structure**

```
terraform/
├── main.tf                    # Core infrastructure configuration
├── variables.tf               # Variable definitions
├── outputs.tf                 # Output values for database connection
├── neon-project.tf            # Database connection configuration
├── terraform.tfvars.example   # Example configuration file
├── tables/                    # Database table definitions
│   ├── main.tf               # Tables main configuration
│   ├── users.tf              # User profiles table
│   ├── patient_profiles.tf   # Patient profiles table
│   ├── provider_profiles.tf  # Provider profiles table
│   ├── appointments.tf       # Appointments table
│   ├── medical_records.tf    # Medical records table
│   ├── audit_logs.tf         # Audit logs table
│   └── README.md             # Tables documentation
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
1. **Neon Account** - Sign up at [neon.tech](https://neon.tech)
2. **Neon Database** - Create a new database project
3. **Terraform** - Install Terraform CLI
4. **Credentials** - Neon API key + database connection details

### **Step 1: Get Neon Credentials**

#### **A. Neon API Key (Required)**
1. Go to [Neon Console](https://console.neon.tech)
2. Navigate to **Settings** → **API Keys**
3. Click **Create API Key**
4. Copy the API key (starts with `neon_`)

#### **B. Database Connection Details**
1. In Neon Console, go to your project
2. Navigate to **Dashboard** → **Connection Details**
3. Copy these details:
   - **Host**: `your-project.neon.tech`
   - **Database**: `neondb` (or your custom name)
   - **Username**: `your-username`
   - **Password**: `your-password`
   - **Port**: `5432`

### **Step 2: Configure Credentials**

#### **A. Set Neon API Key (Environment Variable)**
```bash
# Set the Neon API key as environment variable
export NEON_API_KEY="neon_your_actual_api_key_here"

# Verify it's set
echo $NEON_API_KEY
```

#### **B. Configure Database Connection**
```bash
# Copy example variables file
cp terraform.tfvars.example terraform.tfvars

# Edit with your actual Neon database details
nano terraform.tfvars
```

**Fill in `terraform.tfvars`:**
```hcl
# Neon database connection details
neon_host     = "your-project.neon.tech"
neon_port     = 5432
neon_database = "neondb"
neon_username = "your-username"
neon_password = "your-password"
```

### **Step 3: Deploy Database Schema**

#### **Option A: Deploy All Tables**
```bash
# Initialize Terraform
terraform init

# Review deployment plan
terraform plan

# Deploy all tables and indexes
terraform apply
```

#### **Option B: Deploy Single Table**
```bash
# Deploy only user profiles table
terraform apply -target=null_resource.create_database_schema

# Deploy only appointments table
terraform apply -target=null_resource.create_appointments_table
```

#### **Option C: Use Deployment Script**
```bash
# Deploy all tables
../scripts/deploy-neon.sh

# Deploy single table
../scripts/deploy-neon.sh -s user_profiles

# List available tables
../scripts/deploy-neon.sh -l
```

### **Step 4: Verify Deployment**
```bash
# Get database connection URL
terraform output neon_database_url

# Connect to database
psql "$(terraform output -raw neon_database_url)"

# List tables
\dt
```

### **Database Connection**
After applying, you'll get:
- **Database URL** - Complete connection string
- **Host/Port** - Database connection details
- **Table List** - All created tables

## 🔧 **Configuration**

### **Environment Variables**
- `terraform.tfvars` - Database connection details (required)
- **Location**: Copy from `../examples/terraform.tfvars.example`

### **Customization**
- **Database Connection** - Update variables in `terraform.tfvars`
- **Table Structure** - Edit individual table files in `tables/`
- **Security** - All credentials stored in `../config/` directory

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

## 🔧 **Troubleshooting**

### **Common Issues**

#### **1. Neon API Key Issues**
```bash
# Error: "No API key found"
# Solution: Set the NEON_API_KEY environment variable
export NEON_API_KEY="neon_your_actual_api_key_here"

# Verify it's set
echo $NEON_API_KEY
```

#### **2. Database Connection Issues**
```bash
# Error: "connection refused" or "authentication failed"
# Solution: Check your terraform.tfvars file
cat terraform.tfvars

# Verify connection details in Neon Console
# - Host: your-project.neon.tech
# - Username: your-username
# - Password: your-password
# - Database: neondb
```

#### **3. Permission Issues**
```bash
# Error: "permission denied for table"
# Solution: Ensure your Neon user has CREATE privileges
# Check in Neon Console → Settings → Database Users
```

#### **4. Terraform State Issues**
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
echo "API Key: $NEON_API_KEY"
echo "Database: $(grep neon_database terraform.tfvars)"

# Test database connection
psql "$(terraform output -raw neon_database_url)" -c "\dt"
```

## 🔄 **Next Steps**

1. **Apply Terraform** - Create the database tables
2. **Test Connection** - Verify database connectivity
3. **Insert Test Data** - Add sample data for testing
4. **Build Services** - Start building Spring Boot services
5. **Database Integration** - Connect services to database

---

*This Terraform configuration creates the complete database infrastructure for our healthcare AI microservices platform.*