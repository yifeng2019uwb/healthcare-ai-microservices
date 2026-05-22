# Supabase Database Deployment

This directory contains Terraform configurations to deploy the healthcare database schema to Supabase PostgreSQL.

## 🚀 Quick Start

### 1. Get Supabase Credentials
1. Go to your [Supabase Dashboard](https://supabase.com/dashboard)
2. Select your project
3. Go to **Settings** → **Database**
4. Copy the connection details

### 2. Configure Terraform
```bash
# Copy the example configuration from the config directory
cp ../../config/supabase.tfvars.example terraform.tfvars

# Edit with your Supabase credentials
nano terraform.tfvars
```

### 3. Deploy Database Schema
```bash
# Run the deployment script
./deploy-supabase.sh
```

## 📋 Configuration

### Required Variables
- `supabase_host` - Your Supabase database host
- `supabase_password` - Your Supabase database password
- `supabase_port` - Database port (default: 5432)
- `supabase_database` - Database name (default: postgres)
- `supabase_username` - Database username (default: postgres)

### Example terraform.tfvars
```hcl
supabase_host = "db.abcdefghijklmnop.supabase.co"
supabase_password = "your-secure-password-here"
```

## 🗄️ Deployed Tables

The deployment creates 6 core tables:

1. **user_profiles** - Core user identity and basic information
2. **patient_profiles** - Patient-specific medical data
3. **provider_profiles** - Provider-specific professional data
4. **appointments** - Scheduling between patients and providers
5. **medical_records** - Medical data per appointment
6. **audit_logs** - Comprehensive audit trail for HIPAA compliance

## 🔧 Manual Deployment

If you prefer to run Terraform commands manually:

```bash
# Initialize Terraform
terraform init

# Validate configuration
terraform validate

# Plan deployment
terraform plan

# Apply deployment
terraform apply
```

## 🆘 Troubleshooting

### Common Issues

**1. Connection Refused**
- Check if your Supabase project is active
- Verify the host and port are correct
- Ensure your IP is whitelisted (if required)

**2. Authentication Failed**
- Double-check your database password
- Verify the username is correct
- Check if your Supabase project is fully provisioned

**3. Permission Denied**
- Ensure you're using the correct database credentials
- Check if your Supabase project has the required permissions

### Getting Help

1. Check the [Supabase Documentation](https://supabase.com/docs)
2. Verify your connection string format
3. Test connection with `psql` directly

## 🔒 Security Notes

- Never commit `terraform.tfvars` to version control
- Use environment variables for production deployments
- Rotate database passwords regularly
- Enable Row Level Security in Supabase dashboard

## 📚 Next Steps

After successful deployment:

1. **Update Application Configuration** - Point your services to Supabase
2. **Test Database Connection** - Verify all tables are accessible
3. **Deploy Microservices** - Start your healthcare services
4. **Configure Row Level Security** - Set up proper access controls

---

*This deployment creates a production-ready database schema for the Healthcare AI Microservices platform.*
