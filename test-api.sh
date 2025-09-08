#!/bin/bash

echo "🧪 Manual Integration Testing - Patient Service API"
echo "=================================================="

# Test 1: Health Check (if service starts)
echo "1. Testing Health Check..."
curl -s http://localhost:8080/actuator/health || echo "❌ Service not running"

# Test 2: Create Patient Account (mock data)
echo -e "\n2. Testing Create Patient Account..."
curl -X POST http://localhost:8080/api/patients/create-account \
  -H "Content-Type: application/json" \
  -d '{
    "externalUserId": "test-user-123",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "dateOfBirth": "1990-01-01",
    "gender": "MALE"
  }' || echo "❌ Create account failed"

# Test 3: Get Patient Profile
echo -e "\n3. Testing Get Patient Profile..."
curl -X POST http://localhost:8080/api/patients/profile \
  -H "Content-Type: application/json" \
  -d '{}' || echo "❌ Get profile failed"

echo -e "\n✅ Manual testing completed!"
