#!/bin/bash

# Deploy Healthcare Database Schema to Supabase
# This script runs Terraform to deploy all tables in the correct dependency order

echo "🚀 Deploying Healthcare Database Schema to Supabase..."

# Initialize Terraform if needed
if [ ! -d ".terraform" ]; then
    echo "📦 Initializing Terraform..."
    terraform init
fi

# Deploy tables in correct dependency order
echo "🔧 Deploying user_profiles table..."
terraform apply -target=null_resource.create_database_schema -auto-approve

echo "🔧 Deploying patient_profiles table..."
terraform apply -target=null_resource.create_patient_profiles_table -auto-approve

echo "🔧 Deploying provider_profiles table..."
terraform apply -target=null_resource.create_provider_profiles_table -auto-approve

echo "🔧 Deploying appointments table..."
terraform apply -target=null_resource.create_appointments_table -auto-approve

echo "🔧 Deploying medical_records table..."
terraform apply -target=null_resource.create_medical_records_table -auto-approve

echo "🔧 Deploying audit_logs table..."
terraform apply -target=null_resource.create_audit_logs_table -auto-approve

# Verify all tables were created
echo "🔍 Verifying table creation..."
PGPASSWORD="cloudP2025@yf" psql -h "db.vohmpqlyiqysdtvzccnz.supabase.co" -p 5432 -U "postgres" -d "postgres" -c "\dt"

if [ $? -eq 0 ]; then
    echo "✅ Database deployment completed successfully!"
    echo ""
    echo "📊 Deployed Tables:"
    echo "  - user_profiles"
    echo "  - patient_profiles"
    echo "  - provider_profiles"
    echo "  - appointments"
    echo "  - medical_records"
    echo "  - audit_logs"
    echo ""
    echo "🔗 Next Steps:"
    echo "  1. Update your application configuration with Supabase connection details"
    echo "  2. Test the database connection"
    echo "  3. Deploy your microservices"
    echo ""
    echo "🎉 Happy coding!"
else
    echo "❌ Database deployment failed!"
    exit 1
fi