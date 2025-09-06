#!/bin/bash
# Deploy GCP Infrastructure

set -e

echo "🚀 Deploying GCP Infrastructure..."

# Check if GCP credentials exist
if [ ! -f "config/gcp-credentials.json" ]; then
    echo "❌ Error: GCP credentials not found"
    echo "   Download from: https://console.cloud.google.com/iam-admin/serviceaccounts"
    echo "   Save as: config/gcp-credentials.json"
    exit 1
fi

# Set GCP credentials
export GOOGLE_APPLICATION_CREDENTIALS="config/gcp-credentials.json"

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Error: gcloud CLI not installed"
    echo "   Install from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Authenticate with GCP
echo "🔐 Authenticating with GCP..."
gcloud auth activate-service-account --key-file=config/gcp-credentials.json

# Get project ID from credentials
PROJECT_ID=$(gcloud config get-value project)
if [ -z "$PROJECT_ID" ]; then
    echo "❌ Error: No GCP project ID found"
    echo "   Set project: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi

echo "📦 Project ID: $PROJECT_ID"

# Deploy Cloud Run services
echo "📦 Deploying Cloud Run services..."
cd gcp/cloud-run/

# Deploy Gateway Service
echo "   Deploying Gateway Service..."
gcloud run deploy gateway-service \
    --source . \
    --platform managed \
    --region us-central1 \
    --allow-unauthenticated \
    --project $PROJECT_ID

# Deploy Cloud Storage
echo "🗄️ Deploying Cloud Storage..."
cd ../cloud-storage/

# Create storage bucket
gsutil mb gs://$PROJECT_ID-healthcare-storage || echo "Bucket already exists"

# Set CORS policy
gsutil cors set cors.json gs://$PROJECT_ID-healthcare-storage

echo "✅ GCP infrastructure deployed successfully!"
echo ""
echo "🌐 Services deployed:"
echo "   Gateway Service: https://gateway-service-xxx-uc.a.run.app"
echo "   Storage Bucket: gs://$PROJECT_ID-healthcare-storage"
echo ""
echo "🔧 Next steps:"
echo "   1. Configure your services to use the deployed endpoints"
echo "   2. Set up monitoring and logging"
echo "   3. Configure security policies"
