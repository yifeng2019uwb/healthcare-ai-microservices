# Neon Database Deployment Scripts

## Overview

This directory contains scripts for deploying the healthcare database infrastructure to Neon PostgreSQL.

## Scripts

### `deploy-neon.sh`

Deploy database tables to Neon PostgreSQL with flexible options.

#### Usage

```bash
# Deploy all tables (default)
./scripts/deploy-neon.sh

# Deploy all tables explicitly
./scripts/deploy-neon.sh -a
./scripts/deploy-neon.sh --all

# Deploy a single table
./scripts/deploy-neon.sh -s user_profiles
./scripts/deploy-neon.sh --single appointments

# List available tables
./scripts/deploy-neon.sh -l
./scripts/deploy-neon.sh --list

# Deploy specific resource (advanced)
./scripts/deploy-neon.sh -t null_resource.create_database_schema

# Show help
./scripts/deploy-neon.sh -h
./scripts/deploy-neon.sh --help
```

#### Available Tables

| Table Name | Description | Terraform Resource |
|------------|-------------|-------------------|
| `user_profiles` | User profiles and authentication | `null_resource.create_database_schema` |
| `patient_profiles` | Patient information and medical history | `null_resource.create_patient_profiles_table` |
| `provider_profiles` | Healthcare provider information | `null_resource.create_provider_profiles_table` |
| `appointments` | Appointment scheduling and management | `null_resource.create_appointments_table` |
| `medical_records` | Medical records and documentation | `null_resource.create_medical_records_table` |
| `audit_logs` | System audit and compliance logging | `null_resource.create_audit_logs_table` |

#### Examples

```bash
# Deploy all tables at once
./scripts/deploy-neon.sh

# Deploy only the user profiles table
./scripts/deploy-neon.sh -s user_profiles

# Deploy only the appointments table
./scripts/deploy-neon.sh -s appointments

# List all available tables
./scripts/deploy-neon.sh -l
```

#### Prerequisites

1. **Neon API Key**: Set the `NEON_API_KEY` environment variable
   ```bash
   export NEON_API_KEY='napi_your_api_key_here'
   ```

2. **Terraform Variables**: Ensure `terraform/terraform.tfvars` is configured
   ```bash
   cp terraform/terraform.tfvars.example terraform/terraform.tfvars
   nano terraform/terraform.tfvars
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
- Missing API keys
- Invalid table names
- Terraform configuration issues
- Database connection problems

## Directory Structure

```
healthcare-infra/
├── scripts/
│   ├── deploy-neon.sh          # Main deployment script
│   └── README.md               # This file
└── terraform/
    ├── main.tf                 # Terraform configuration
    ├── variables.tf            # Variable definitions
    ├── terraform.tfvars        # Variable values
    ├── outputs.tf              # Output definitions
    ├── users.tf                # User profiles table
    ├── patient_profiles.tf     # Patient profiles table
    ├── provider_profiles.tf    # Provider profiles table
    ├── appointments.tf         # Appointments table
    ├── medical_records.tf      # Medical records table
    └── audit_logs.tf           # Audit logs table
```

## Troubleshooting

### Common Issues

1. **API Key Not Set**
   ```
   ❌ Error: NEON_API_KEY environment variable not set
   ```
   **Solution**: Set your Neon API key as an environment variable

2. **Invalid Table Name**
   ```
   ❌ Unknown table: invalid_table
   ```
   **Solution**: Use `./scripts/deploy-neon.sh -l` to list available tables

3. **Terraform Configuration Missing**
   ```
   ❌ Error: terraform.tfvars not found
   ```
   **Solution**: Copy and configure `terraform.tfvars.example`

4. **Database Connection Failed**
   ```
   Error: connection to server failed
   ```
   **Solution**: Check your Neon connection details in `terraform.tfvars`

### Getting Help

- Run `./scripts/deploy-neon.sh --help` for usage information
- Run `./scripts/deploy-neon.sh --list` to see available tables
- Check the Terraform logs for detailed error information
