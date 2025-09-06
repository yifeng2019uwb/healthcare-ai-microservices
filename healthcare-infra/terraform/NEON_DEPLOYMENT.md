# Neon Database Deployment with Terraform

This directory contains Terraform configurations for deploying the Healthcare AI Microservices database to Neon using the proper Neon Terraform provider approach.

## Prerequisites

1. **Neon Account**: Sign up at [neon.tech](https://neon.tech)
2. **Neon API Key**: Get your API key from [Neon Console](https://console.neon.tech/app/settings/api-keys)
3. **Terraform**: Install Terraform (version 1.0+)
4. **psql**: Install PostgreSQL client tools

## Setup

### 1. Set Environment Variables

```bash
export NEON_API_KEY="your_neon_api_key_here"
```

### 2. Initialize Terraform

```bash
terraform init
```

### 3. Review the Plan

```bash
terraform plan
```

### 4. Deploy the Infrastructure

```bash
terraform apply
```

## What This Creates

### Neon Infrastructure
- **Project**: `medconnect-healthcare` (existing project)
- **Development Branch**: Creates `healthcare_dev` database
- **Production Branch**: Creates `healthcare_prod` database
- **Branches**: Uses existing `development` and `production` branches

### Database Schema
- **ENUM Types**: `gender_enum`, `role_enum`, `status_enum`
- **Tables**: `user_profiles` (with all indexes)
- **Extensions**: `uuid-ossp` for UUID generation

## Architecture

This follows the **recommended Neon approach**:

1. **Neon Provider**: Manages Neon infrastructure (projects, branches, databases)
2. **null_resource**: Executes SQL scripts for schema creation
3. **local-exec provisioner**: Runs `psql` commands against the Neon database

## Key Files

- `main.tf`: Provider configuration
- `neon-project.tf`: Neon project and database setup
- `users.tf`: Database schema deployment
- `outputs.tf`: Connection details and project information
- `variables.tf`: Input variables (if needed)

## Connection Details

After deployment, you can get connection details:

```bash
# Get development database URL
terraform output development_database_url

# Get production database URL
terraform output production_database_url

# Get individual connection details
terraform output development_database_host
terraform output development_database_port
terraform output development_database_name

# Get project information
terraform output neon_project_name
terraform output neon_project_id
```

## Adding More Tables

To add more tables, update the SQL in `users.tf` within the `null_resource` provisioner:

```sql
-- Add your new table creation SQL here
CREATE TABLE IF NOT EXISTS your_new_table (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- your columns here
);
```

## Troubleshooting

### Common Issues

1. **API Key Not Set**: Make sure `NEON_API_KEY` environment variable is set
2. **psql Not Found**: Install PostgreSQL client tools
3. **Connection Issues**: Check that your IP is whitelisted in Neon (if required)

### Debugging

Enable Terraform debug logging:

```bash
export TF_LOG=DEBUG
terraform apply
```

## Security Notes

- The database URL contains sensitive information and is marked as sensitive in Terraform
- API keys should be stored securely and not committed to version control
- Consider using Terraform Cloud or similar for production deployments

## Next Steps

After successful deployment:

1. Update your application configuration with the new connection details
2. Run database migrations if needed
3. Set up monitoring and backups
4. Configure connection pooling for production use
