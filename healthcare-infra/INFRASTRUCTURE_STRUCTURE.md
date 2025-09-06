# Healthcare Infrastructure Structure

## 🏗️ **Multi-Infrastructure Setup**

### **Current Structure**
```
healthcare-infra/
├── terraform/           # Neon PostgreSQL Database
│   ├── main.tf
│   ├── variables.tf
│   ├── terraform.tfvars
│   └── tables/
├── gcp/                 # Google Cloud Platform
│   ├── cloud-run/
│   ├── cloud-storage/
│   └── scripts/
└── config/              # Shared configuration
    ├── gcp-credentials.json.example
    ├── neon-connection.env.example
    └── terraform.tfvars.example
```

### **Recommended Multi-Infrastructure Structure**
```
healthcare-infra/
├── environments/        # Environment-specific configs
│   ├── dev/
│   │   ├── neon/
│   │   └── gcp/
│   ├── staging/
│   │   ├── neon/
│   │   └── gcp/
│   └── prod/
│       ├── neon/
│       └── gcp/
├── modules/             # Reusable Terraform modules
│   ├── neon-database/
│   ├── gcp-cloud-run/
│   └── gcp-storage/
├── scripts/             # Deployment scripts
│   ├── deploy-neon.sh
│   ├── deploy-gcp.sh
│   └── deploy-all.sh
└── config/              # Shared configuration
    ├── credentials/
    └── variables/
```

## 🚀 **Deployment Scripts**

### **1. Neon Database Deployment**
```bash
#!/bin/bash
# scripts/deploy-neon.sh

set -e

echo "🚀 Deploying Neon Database..."

# Check if NEON_API_KEY is set
if [ -z "$NEON_API_KEY" ]; then
    echo "❌ Error: NEON_API_KEY environment variable not set"
    echo "   Get your API key from: https://console.neon.tech/settings/api-keys"
    exit 1
fi

# Check if terraform.tfvars exists
if [ ! -f "terraform/terraform.tfvars" ]; then
    echo "❌ Error: terraform.tfvars not found"
    echo "   Copy terraform.tfvars.example and fill in your Neon details"
    exit 1
fi

cd terraform/

# Initialize and deploy
terraform init
terraform plan
terraform apply -auto-approve

echo "✅ Neon database deployed successfully!"
echo "📊 Database URL: $(terraform output -raw neon_database_url)"
```

### **2. GCP Infrastructure Deployment**
```bash
#!/bin/bash
# scripts/deploy-gcp.sh

set -e

echo "🚀 Deploying GCP Infrastructure..."

# Check if GCP credentials exist
if [ ! -f "config/gcp-credentials.json" ]; then
    echo "❌ Error: GCP credentials not found"
    echo "   Download from: https://console.cloud.google.com/iam-admin/serviceaccounts"
    exit 1
fi

# Set GCP credentials
export GOOGLE_APPLICATION_CREDENTIALS="config/gcp-credentials.json"

cd gcp/

# Deploy Cloud Run services
echo "📦 Deploying Cloud Run services..."
gcloud run deploy gateway-service --source . --platform managed --region us-central1

# Deploy Cloud Storage
echo "🗄️ Deploying Cloud Storage..."
gsutil mb gs://healthcare-ai-storage || true
gsutil cors set cloud-storage/cors.json gs://healthcare-ai-storage

echo "✅ GCP infrastructure deployed successfully!"
```

### **3. Full Infrastructure Deployment**
```bash
#!/bin/bash
# scripts/deploy-all.sh

set -e

echo "🚀 Deploying Full Healthcare Infrastructure..."

# Deploy Neon database first
./scripts/deploy-neon.sh

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 10

# Deploy GCP infrastructure
./scripts/deploy-gcp.sh

echo "✅ Full infrastructure deployed successfully!"
echo "🌐 Your healthcare platform is ready!"
```

## 🔧 **Environment-Specific Configuration**

### **Development Environment**
```bash
# environments/dev/neon/terraform.tfvars
neon_host     = "ep-dev-123456.us-east-1.aws.neon.tech"
neon_database = "healthcare_dev"
neon_username = "dev_user"
neon_password = "dev_password"

# environments/dev/gcp/terraform.tfvars
project_id    = "healthcare-dev-123456"
region        = "us-central1"
environment   = "dev"
```

### **Production Environment**
```bash
# environments/prod/neon/terraform.tfvars
neon_host     = "ep-prod-789012.us-east-1.aws.neon.tech"
neon_database = "healthcare_prod"
neon_username = "prod_user"
neon_password = "prod_password"

# environments/prod/gcp/terraform.tfvars
project_id    = "healthcare-prod-789012"
region        = "us-central1"
environment   = "prod"
```

## 📋 **Next Steps**

1. **Set up your Neon credentials** in `terraform/terraform.tfvars`
2. **Create deployment scripts** for automated deployment
3. **Set up GCP credentials** for cloud services
4. **Test deployment** with development environment
5. **Create production environment** when ready
