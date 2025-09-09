#!/bin/bash

# Healthcare AI Microservices - Local Development
# This script starts the entire stack locally using Docker Compose

echo "🏥 Starting Healthcare AI Microservices locally..."

# Navigate to docker directory
cd "$(dirname "$0")"

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "📝 Please copy env.example to .env and configure your database credentials:"
    echo "   cp env.example .env"
    echo "   # Then edit .env with your actual database credentials"
    exit 1
fi

# Load environment variables
source .env

# Validate required environment variables
if [ -z "$SPRING_DATASOURCE_URL" ] || [ -z "$SPRING_DATASOURCE_USERNAME" ] || [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
    echo "❌ Error: Missing required environment variables!"
    echo "📝 Please configure SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, and SPRING_DATASOURCE_PASSWORD in .env"
    exit 1
fi

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Build and start services
echo "🚀 Building and starting services..."
docker-compose up --build

echo "✅ Services started!"
echo "🌐 Patient Service: http://localhost:8080"
echo "🗄️  Database: Configured via environment variables"
