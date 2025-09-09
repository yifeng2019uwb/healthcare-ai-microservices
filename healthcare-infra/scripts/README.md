# Database Deployment Scripts

## Overview

This directory contains scripts for deploying the healthcare database infrastructure to PostgreSQL.

## Scripts

### `deploy-all.sh`

Deploy all database tables to PostgreSQL with flexible options.

#### Usage

```bash
# Deploy all tables (default)
./scripts/deploy-all.sh

# Deploy all tables explicitly
./scripts/deploy-all.sh -a
./scripts/deploy-all.sh --all

# Deploy a single table
./scripts/deploy-all.sh -s user_profiles
./scripts/deploy-all.sh --single appointments

# List available tables
./scripts/deploy-all.sh -l
./scripts/deploy-all.sh --list

# Deploy specific resource (advanced)
./scripts/deploy-all.sh -t null_resource.create_database_schema

# Show help
./scripts/deploy-all.sh -h
./scripts/deploy-all.sh --help
```

#### Available Tables

| Table Name | Description | Terraform Resource |
|------------|-------------|-------------------|
| `user_profiles` | User profiles and authentication | `null_resource.create_users_table` |
| `patient_profiles` | Patient information and medical history | `null_resource.create_patient_profiles_table` |
| `provider_profiles` | Healthcare provider information | `null_resource.create_provider_profiles_table` |
| `appointments` | Appointment scheduling and management | `null_resource.create_appointments_table` |
| `medical_records` | Medical records and documentation | `null_resource.create_medical_records_table` |
| `audit_logs` | System audit and compliance logging | `null_resource.create_audit_logs_table` |

#### Examples

```bash
# Deploy all tables at once
./scripts/deploy-all.sh

# Deploy only the user profiles table
./scripts/deploy-all.sh -s user_profiles

# Deploy only the appointments table
./scripts/deploy-all.sh -s appointments

# List all available tables
./scripts/deploy-all.sh -l
```

#### Prerequisites

1. **Database Credentials**: Configure database connection details
   ```bash
   # Copy example configuration
   cp terraform/supabase/terraform.tfvars.example terraform/supabase/config/terraform.tfvars

   # Edit with your database details
   nano terraform/supabase/config/terraform.tfvars
   ```

2. **Terraform**: Install Terraform CLI
   ```bash
   # macOS
   brew install terraform

   # Ubuntu/Debian
   sudo apt-get install terraform
   ```

3. **PostgreSQL Client**: Install `psql` for database operations
   ```bash
   # macOS
   brew install postgresql

   # Ubuntu/Debian
   sudo apt-get install postgresql-client
   ```

#### Output

The script provides detailed output including:
- Deployment progress
- Table creation status
- Database connection information
- Testing commands

#### Error Handling

The script includes comprehensive error handling for:
- Missing database credentials
- Invalid table names
- Terraform configuration issues
- Database connection problems

## Directory Structure

```
healthcare-infra/
├── scripts/
│   ├── deploy-all.sh           # Main deployment script
│   └── README.md               # This file
└── terraform/
    └── supabase/
        ├── main.tf             # Terraform configuration
        ├── variables.tf        # Variable definitions
        ├── config/             # Configuration files (gitignored)
        │   └── terraform.tfvars # Database credentials
        ├── 01_users.tf         # User profiles table
        ├── 02_patient_profiles.tf # Patient profiles table
        ├── 03_provider_profiles.tf # Provider profiles table
        ├── 04_appointments.tf  # Appointments table
        ├── 05_medical_records.tf # Medical records table
        └── 06_audit_logs.tf    # Audit logs table
```

## Troubleshooting

### Common Issues

1. **Database Credentials Not Set**
   ```
   ❌ Error: Database credentials not configured
   ```
   **Solution**: Configure `terraform/supabase/config/terraform.tfvars`

2. **Invalid Table Name**
   ```
   ❌ Unknown table: invalid_table
   ```
   **Solution**: Use `./scripts/deploy-all.sh -l` to list available tables

3. **Terraform Configuration Missing**
   ```
   ❌ Error: terraform.tfvars not found
   ```
   **Solution**: Copy and configure `terraform.tfvars.example`

4. **Database Connection Failed**
   ```
   Error: connection to server failed
   ```
   **Solution**: Check your database connection details in `terraform.tfvars`

### Getting Help

- Run `./scripts/deploy-all.sh --help` for usage information
- Run `./scripts/deploy-all.sh --list` to see available tables
- Check the Terraform logs for detailed error information