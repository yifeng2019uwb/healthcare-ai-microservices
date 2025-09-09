# Infrastructure Configuration

This directory contains configuration templates and examples for the healthcare infrastructure.

## 🔒 Security Notice

**This directory is completely ignored by Git** - no sensitive configuration files will be committed to version control.

## 📁 Configuration Files

### Database Configurations
- `supabase.tfvars.example` - Supabase database configuration template
- `neon.tfvars.example` - Neon database configuration template (legacy)

### Environment Configurations
- `dev.tfvars.example` - Development environment variables
- `prod.tfvars.example` - Production environment variables (future)

## 🚀 Usage

### For Supabase Database Deployment:
```bash
# Copy the Supabase configuration template
cp config/supabase.tfvars.example terraform/supabase/terraform.tfvars

# Edit with your actual Supabase credentials
nano terraform/supabase/terraform.tfvars

# Deploy the database schema
cd terraform/supabase
./deploy-supabase.sh
```

### For Other Services:
```bash
# Copy the appropriate configuration template
cp config/[service].tfvars.example terraform/[service]/terraform.tfvars

# Edit with your actual credentials
nano terraform/[service]/terraform.tfvars
```

## 🔧 Configuration Templates

All configuration templates include:
- ✅ **Required variables** - All necessary configuration parameters
- ✅ **Example values** - Sample values to guide configuration
- ✅ **Documentation** - Comments explaining each variable
- ✅ **Security notes** - Best practices for credential management

## 🛡️ Security Best Practices

1. **Never commit actual credentials** - Only commit `.example` files
2. **Use environment variables** - For production deployments
3. **Rotate credentials regularly** - Change passwords periodically
4. **Use least privilege** - Grant minimum required permissions
5. **Monitor access** - Track who has access to what

## 📚 Related Documentation

- [Supabase Setup Guide](../terraform/supabase/README.md)
- [Infrastructure Design](../INFRASTRUCTURE_DESIGN.md)
- [Security Guidelines](../docs/security-guide.md)

---

*This directory ensures secure configuration management for the Healthcare AI Microservices platform.*