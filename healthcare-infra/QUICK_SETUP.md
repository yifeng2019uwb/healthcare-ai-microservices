# 🚀 Quick Setup Guide

## **Step 1: Get Your Neon Host**

1. Go to [Neon Console](https://console.neon.tech)
2. Select your project
3. Go to **Dashboard** → **Connection Details**
4. Copy the **Host** field (looks like: `ep-cool-name-123456.us-east-1.aws.neon.tech`)

## **Step 2: Configure Neon Database**

```bash
# Edit your terraform.tfvars file
nano healthcare-infra/terraform/terraform.tfvars
```

**Fill in with your actual Neon details:**
```hcl
neon_host     = "ep-cool-name-123456.us-east-1.aws.neon.tech"  # Your actual host
neon_port     = 5432
neon_database = "neondb"  # or your custom database name
neon_username = "your-actual-username"
neon_password = "your-actual-password"
```

## **Step 3: Set Neon API Key**

```bash
# Get your API key from: https://console.neon.tech/settings/api-keys
export NEON_API_KEY="neon_your_actual_api_key_here"

# Verify it's set
echo $NEON_API_KEY
```

## **Step 4: Deploy Database**

```bash
# Deploy Neon database
./healthcare-infra/scripts/deploy-neon.sh
```

## **Step 5: (Optional) Deploy GCP Infrastructure**

```bash
# Download GCP credentials from: https://console.cloud.google.com/iam-admin/serviceaccounts
# Save as: healthcare-infra/config/gcp-credentials.json

# Deploy GCP infrastructure
./healthcare-infra/scripts/deploy-gcp.sh
```

## **Step 6: Deploy Everything**

```bash
# Deploy both Neon + GCP
./healthcare-infra/scripts/deploy-all.sh
```

## **Verification**

```bash
# Test database connection
psql "$(cd healthcare-infra/terraform && terraform output -raw neon_database_url)"

# List tables
\dt

# Exit psql
\q
```

## **Troubleshooting**

- **"No API key found"** → Set `NEON_API_KEY` environment variable
- **"Connection refused"** → Check your `terraform.tfvars` values
- **"Permission denied"** → Ensure your Neon user has CREATE privileges
- **"terraform.tfvars not found"** → Copy from `terraform.tfvars.example`
