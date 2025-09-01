# Configuration Files - Credentials & Secrets

> **🔒 SECURITY NOTICE**
>
> This directory contains configuration files with sensitive credentials.
> **NEVER commit these files to version control!**

## 📁 **Directory Structure**

```
config/
├── README.md                           # This file
├── terraform.tfvars.example           # Example Terraform variables
├── gcp-credentials.json.example       # Example GCP service account
├── neon-connection.env.example        # Example Neon database connection
└── .gitignore                         # Local gitignore for this directory
```

## 🔐 **Credential Management**

### **What Goes Here:**
- **Terraform Variables** - Database connection details
- **GCP Credentials** - Service account JSON files
- **Neon Credentials** - Database connection strings
- **API Keys** - External service API keys
- **Environment Variables** - Development environment settings

### **Security Rules:**
1. **Never commit** actual credential files
2. **Always use** `.example` files for templates
3. **Copy and rename** example files for actual use
4. **Keep credentials** in this directory only
5. **Use environment variables** when possible

## 🚀 **Setup Instructions**

### **1. Terraform Configuration**
```bash
# Copy example file
cp terraform.tfvars.example terraform.tfvars

# Edit with your actual values
# neon_host     = "your-neon-host.neon.tech"
# neon_username = "your-username"
# neon_password = "your-password"
```

### **2. GCP Credentials**
```bash
# Copy example file
cp gcp-credentials.json.example gcp-credentials.json

# Edit with your actual GCP service account JSON
```

### **3. Environment Variables**
```bash
# Copy example file
cp neon-connection.env.example neon-connection.env

# Edit with your actual connection details
```

## ⚠️ **Important Security Notes**

### **DO:**
- ✅ Use example files as templates
- ✅ Keep actual credentials in this directory
- ✅ Use environment variables when possible
- ✅ Rotate credentials regularly
- ✅ Use least-privilege access

### **DON'T:**
- ❌ Commit actual credential files
- ❌ Share credentials in chat/email
- ❌ Use production credentials in development
- ❌ Store credentials in code
- ❌ Use weak passwords

## 🔄 **Credential Rotation**

### **When to Rotate:**
- **Monthly** - Development credentials
- **Quarterly** - Production credentials
- **Immediately** - If compromised
- **When leaving** - Project or company

### **How to Rotate:**
1. **Generate new credentials** in service provider
2. **Update local config files** with new values
3. **Test connection** with new credentials
4. **Update deployed services** if applicable
5. **Delete old credentials** from service provider

---

*This directory is protected by .gitignore to prevent accidental credential exposure.*
