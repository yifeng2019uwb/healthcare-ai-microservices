#!/bin/bash
# Deploy Full Healthcare Infrastructure

set -e

echo "🚀 Deploying Full Healthcare Infrastructure..."
echo "================================================"

# Check prerequisites
echo "🔍 Checking prerequisites..."

# Check if NEON_API_KEY is set
if [ -z "$NEON_API_KEY" ]; then
    echo "❌ Error: NEON_API_KEY environment variable not set"
    echo "   Get your API key from: https://console.neon.tech/settings/api-keys"
    echo "   Then run: export NEON_API_KEY='neon_your_api_key_here'"
    exit 1
fi

# Check if GCP credentials exist
if [ ! -f "config/gcp-credentials.json" ]; then
    echo "❌ Error: GCP credentials not found"
    echo "   Download from: https://console.cloud.google.com/iam-admin/serviceaccounts"
    echo "   Save as: config/gcp-credentials.json"
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Deploy Neon database first
echo "🗄️ Step 1: Deploying Neon Database..."
./scripts/deploy-neon.sh

if [ $? -eq 0 ]; then
    echo "✅ Neon database deployed successfully!"
else
    echo "❌ Neon database deployment failed!"
    exit 1
fi

echo ""

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 10

# Deploy GCP infrastructure
echo "☁️ Step 2: Deploying GCP Infrastructure..."
./scripts/deploy-gcp.sh

if [ $? -eq 0 ]; then
    echo "✅ GCP infrastructure deployed successfully!"
else
    echo "❌ GCP infrastructure deployment failed!"
    exit 1
fi

echo ""
echo "🎉 Full infrastructure deployed successfully!"
echo "================================================"
echo ""
echo "📊 Your healthcare platform is ready:"
echo "   🗄️ Database: Neon PostgreSQL with all tables"
echo "   ☁️ Cloud: GCP Cloud Run + Storage"
echo "   🔗 Gateway: API Gateway service"
echo ""
echo "🧪 Test your deployment:"
echo "   Database: psql \"\$(cd terraform && terraform output -raw neon_database_url)\""
echo "   Gateway: Check Cloud Run console for service URL"
echo ""
echo "🔧 Next steps:"
echo "   1. Deploy your Spring Boot services"
echo "   2. Configure service-to-service communication"
echo "   3. Set up monitoring and alerting"
echo "   4. Configure CI/CD pipelines"
