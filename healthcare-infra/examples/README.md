# Configuration Examples

> **📋 Example Configuration Files**
>
> This directory contains example configuration files for setting up the healthcare platform.
> Copy these files to the `config/` directory and fill in your actual values.

## 📁 **Available Examples**

- **`terraform.tfvars.example`** - Terraform variables for database connection
- **`gcp-credentials.json.example`** - GCP service account template
- **`neon-connection.env.example`** - Neon database connection template

## 🚀 **Setup Instructions**

### **1. Copy Example Files**
```bash
# Copy examples to config directory
cp healthcare-infra/examples/*.example healthcare-infra/config/

# Rename files (remove .example extension)
cd healthcare-infra/config
mv terraform.tfvars.example terraform.tfvars
mv gcp-credentials.json.example gcp-credentials.json
mv neon-connection.env.example neon-connection.env
```

### **2. Fill in Your Values**
Edit each file with your actual credentials:
- **terraform.tfvars** - Your Neon database connection details
- **gcp-credentials.json** - Your GCP service account JSON
- **neon-connection.env** - Your Neon database environment variables

### **3. Security Notes**
- The `config/` directory is completely ignored by git
- Never commit actual credential files
- Use these examples as templates only

---

*These example files are safe to commit to version control as they contain only placeholder values.*
