# Healthcare Database Tables - Neon PostgreSQL

This directory contains Terraform configurations for deploying healthcare database tables to Neon PostgreSQL.

## 📁 Directory Structure

```
tables/
├── main.tf                    # Main configuration (loads all table files)
├── users.tf                   # User profiles table
├── patient_profiles.tf        # Patient profiles table
├── provider_profiles.tf       # Provider profiles table
├── appointments.tf            # Appointments table
├── medical_records.tf         # Medical records table
├── audit_logs.tf              # Audit logs table
└── README.md                  # This file
```

## 🗄️ Database Tables

### **Core Tables (6 total)**

| Table | Description | Key Features |
|-------|-------------|--------------|
| **user_profiles** | User authentication and basic profile data | External auth integration, roles, status |
| **patient_profiles** | Patient profiles and medical information | Medical history, insurance, emergency contacts |
| **provider_profiles** | Healthcare provider profiles and credentials | NPI numbers, licenses, specialties |
| **appointments** | Appointment scheduling and management | Status tracking, types, priorities |
| **medical_records** | Patient medical records and documents | Record types, attachments, review workflow |
| **audit_logs** | Audit trail for all data changes | Compliance tracking, user actions |

### **Key Features**
- **UUID Primary Keys** - All tables use UUID for unique identification
- **Foreign Key Relationships** - Proper referential integrity between tables
- **Comprehensive Indexes** - Optimized for common query patterns
- **JSONB Fields** - Flexible data storage for complex structures
- **Audit Timestamps** - Created/updated tracking on all tables
- **ENUM Types** - Type-safe status and category fields

## 🚀 Deployment

### **Deploy All Tables**
```bash
# From healthcare-infra/terraform directory
terraform apply

# Or use the deployment script
../scripts/deploy-neon.sh
```

### **Deploy Single Table**
```bash
# Deploy only user profiles
../scripts/deploy-neon.sh -s user_profiles

# Deploy only appointments
../scripts/deploy-neon.sh -s appointments
```

### **List Available Tables**
```bash
../scripts/deploy-neon.sh -l
```

## 🔧 Configuration

### **Prerequisites**
1. **Neon Database** - Existing Neon PostgreSQL database
2. **Connection Details** - Configured in `../terraform.tfvars`
3. **PostgreSQL Client** - `psql` installed locally

### **Required Variables**
```hcl
# Database connection details
neon_host     = "your-host.neon.tech"
neon_port     = 5432
neon_database = "medconnect-healthcare"
neon_username = "your-username"
neon_password = "your-password"
```

## 📊 Database Schema

### **Table Relationships**
```
user_profiles (1) ──┐
                    ├── patient_profiles (1:1)
                    └── provider_profiles (1:1)

patient_profiles (1) ──┐
                       ├── appointments (1:N)
                       └── medical_records (1:N)

provider_profiles (1) ──┐
                        ├── appointments (1:N)
                        └── medical_records (1:N)

appointments (1) ── medical_records (1:N)

user_profiles (1) ── audit_logs (1:N)
```

### **ENUM Types**
- `gender_enum` - MALE, FEMALE, OTHER, UNKNOWN
- `role_enum` - PATIENT, PROVIDER
- `status_enum` - ACTIVE, INACTIVE, SUSPENDED
- `patient_status_enum` - ACTIVE, INACTIVE, SUSPENDED, DECEASED
- `insurance_type_enum` - PRIVATE, MEDICARE, MEDICAID, TRICARE, OTHER
- `provider_type_enum` - DOCTOR, NURSE, ASSISTANT, THERAPIST, ADMIN, OTHER
- `appointment_status_enum` - SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED, NO_SHOW
- `medical_record_type_enum` - CONSULTATION_NOTE, DIAGNOSIS, PRESCRIPTION, LAB_RESULT, etc.
- `audit_action_enum` - CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, etc.

## 🧪 Testing

### **Verify Table Creation**
```sql
-- Connect to database
psql "postgresql://username:password@host:port/database"

-- List all tables
\dt

-- Check table structure
\d user_profiles
\d patient_profiles
```

### **Test Data Insertion**
```sql
-- Insert test user
INSERT INTO user_profiles (external_auth_id, first_name, last_name, email, phone, date_of_birth, gender, role, status)
VALUES ('auth_123', 'John', 'Doe', 'john@example.com', '+1234567890', '1990-01-01', 'MALE', 'PATIENT', 'ACTIVE');

-- Insert test patient
INSERT INTO patient_profiles (user_profile_id, patient_number, date_of_birth, gender, phone, email, status)
VALUES ((SELECT id FROM user_profiles WHERE external_auth_id = 'auth_123'), 'P001', '1990-01-01', 'MALE', '+1234567890', 'john@example.com', 'ACTIVE');
```

## 🔄 Maintenance

### **Adding New Tables**
1. Create new `.tf` file in this directory
2. Follow the `null_resource` pattern from existing files
3. Update the main outputs in `../outputs.tf`
4. Test with `terraform plan` and `terraform apply`

### **Modifying Existing Tables**
1. Edit the appropriate `.tf` file
2. Use `terraform plan` to review changes
3. Apply with `terraform apply`
4. Verify changes in database

## 🚨 Important Notes

- **Data Safety** - Always backup before making schema changes
- **Dependencies** - Tables have foreign key relationships - deploy in order
- **Indexes** - All tables have comprehensive indexes for performance
- **Constraints** - Foreign key constraints ensure data integrity
- **Audit Trail** - All changes are tracked in audit_logs table

---

*This directory manages the complete database schema for the healthcare AI microservices platform.*
