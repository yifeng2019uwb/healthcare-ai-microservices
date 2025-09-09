#!/bin/bash

# Healthcare AI Microservices - Local Development
# This script starts the entire stack locally using Docker Compose

echo "🏥 Starting Healthcare AI Microservices locally..."

# Navigate to docker directory
cd "$(dirname "$0")"

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Build and start services
echo "🚀 Building and starting services..."
docker-compose up --build

echo "✅ Services started!"
echo "🌐 Patient Service: http://localhost:8080"
echo "🗄️  PostgreSQL: localhost:5432"
echo "📊 Database: healthcare_dev (user: postgres, password: password)"
