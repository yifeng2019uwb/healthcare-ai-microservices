#!/bin/bash

# GCP Deployment Script for Patient Service
# Make sure you have gcloud CLI installed and authenticated

# Configuration
PROJECT_ID="your-gcp-project-id"  # Replace with your actual project ID
SERVICE_NAME="patient-service"
REGION="us-central1"
IMAGE_NAME="gcr.io/$PROJECT_ID/$SERVICE_NAME"

echo "🚀 Starting GCP deployment for Patient Service..."

# Set the project
echo "📋 Setting GCP project to $PROJECT_ID"
gcloud config set project $PROJECT_ID

# Enable required APIs
echo "🔧 Enabling required APIs..."
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable containerregistry.googleapis.com

# Build and push Docker image
echo "🐳 Building and pushing Docker image..."
cd services/patient-service
gcloud builds submit --tag $IMAGE_NAME .

# Deploy to Cloud Run
echo "🚀 Deploying to Cloud Run..."
gcloud run deploy $SERVICE_NAME \
  --image $IMAGE_NAME \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --port 8080 \
  --memory 512Mi \
  --cpu 1 \
  --max-instances 10

echo "✅ Deployment complete!"
echo "🌐 Service URL: https://$SERVICE_NAME-$(echo $PROJECT_ID | cut -d'-' -f1)-uc.a.run.app"
