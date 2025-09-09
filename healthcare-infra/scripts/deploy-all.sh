#!/bin/bash
# Deploy Full Healthcare Infrastructure

set -e

echo "🚀 Deploying Full Healthcare Infrastructure..."
echo "================================================"

# Check prerequisites
echo "🔍 Checking prerequisites..."

# Note: Database is now managed by Supabase
# See healthcare-infra/terraform/supabase/ for database deployment

# Check if GCP credentials exist
if [ ! -f "config/gcp-credentials.json" ]; then
    echo "❌ Error: GCP credentials not found"
    echo "   Download from: https://console.cloud.google.com/iam-admin/serviceaccounts"
    echo "   Save as: config/gcp-credentials.json"
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Deploy GCP infrastructure
echo "☁️ Step 1: Deploying GCP Infrastructure..."
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
echo "   🗄️ Database: Supabase PostgreSQL (see healthcare-infra/terraform/supabase/)"
echo "   ☁️ Cloud: GCP Cloud Run + Storage"
echo "   🔗 Gateway: API Gateway service"
echo ""
echo "🧪 Test your deployment:"
echo "   Database: See healthcare-infra/terraform/supabase/ for connection details"
echo "   Gateway: Check Cloud Run console for service URL"
echo ""
echo "🔧 Next steps:"
echo "   1. Deploy your Spring Boot services"
echo "   2. Configure service-to-service communication"
echo "   3. Set up monitoring and alerting"
echo "   4. Configure CI/CD pipelines"
