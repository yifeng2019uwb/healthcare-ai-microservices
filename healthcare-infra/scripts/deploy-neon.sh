#!/bin/bash
# Deploy Neon Database Infrastructure

set -e

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -t, --target RESOURCE    Apply only specific resource"
    echo "  -f, --file FILE         Apply only resources from specific file"
    echo "  -a, --all               Deploy all tables (default)"
    echo "  -s, --single TABLE      Deploy single table by name"
    echo "  -l, --list              List available tables"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Deploy all tables"
    echo "  $0 -a                                 # Deploy all tables"
    echo "  $0 -s user_profiles                   # Deploy only user_profiles table"
    echo "  $0 -s appointments                    # Deploy only appointments table"
    echo "  $0 -l                                 # List available tables"
    echo "  $0 -t null_resource.create_database_schema  # Apply specific resource"
    echo ""
    echo "Available tables:"
    echo "  - user_profiles        # User profiles and authentication"
    echo "  - patient_profiles     # Patient information and medical history"
    echo "  - provider_profiles    # Healthcare provider information"
    echo "  - appointments         # Appointment scheduling and management"
    echo "  - medical_records      # Medical records and documentation"
    echo "  - audit_logs          # System audit and compliance logging"
}

# Parse command line arguments
TARGET=""
SINGLE_TABLE=""
DEPLOY_ALL=true
LIST_TABLES=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--target)
            TARGET="$2"
            DEPLOY_ALL=false
            shift 2
            ;;
        -f|--file)
            FILE="$2"
            DEPLOY_ALL=false
            shift 2
            ;;
        -a|--all)
            DEPLOY_ALL=true
            shift
            ;;
        -s|--single)
            SINGLE_TABLE="$2"
            DEPLOY_ALL=false
            shift 2
            ;;
        -l|--list)
            LIST_TABLES=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            echo "❌ Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Handle list tables option
if [ "$LIST_TABLES" = true ]; then
    echo "📋 Available tables for deployment:"
    echo ""
    echo "  user_profiles        # User profiles and authentication"
    echo "  patient_profiles     # Patient information and medical history"
    echo "  provider_profiles    # Healthcare provider information"
    echo "  appointments         # Appointment scheduling and management"
    echo "  medical_records      # Medical records and documentation"
    echo "  audit_logs          # System audit and compliance logging"
    echo ""
    echo "Usage: $0 -s TABLE_NAME"
    exit 0
fi

# Map table names to Terraform resources
get_table_resource() {
    case "$1" in
        "user_profiles")
            echo "null_resource.create_database_schema"
            ;;
        "patient_profiles")
            echo "null_resource.create_patient_profiles_table"
            ;;
        "provider_profiles")
            echo "null_resource.create_provider_profiles_table"
            ;;
        "appointments")
            echo "null_resource.create_appointments_table"
            ;;
        "medical_records")
            echo "null_resource.create_medical_records_table"
            ;;
        "audit_logs")
            echo "null_resource.create_audit_logs_table"
            ;;
        *)
            echo "❌ Unknown table: $1"
            echo "   Use '$0 -l' to list available tables"
            exit 1
            ;;
    esac
}

# Set target based on single table option
if [ -n "$SINGLE_TABLE" ]; then
    TARGET=$(get_table_resource "$SINGLE_TABLE")
fi

# Determine deployment mode
if [ "$DEPLOY_ALL" = true ]; then
    echo "🚀 Deploying ALL Neon Database Tables..."
    DEPLOY_MODE="all"
elif [ -n "$SINGLE_TABLE" ]; then
    echo "🎯 Deploying SINGLE table: $SINGLE_TABLE"
    DEPLOY_MODE="single"
elif [ -n "$TARGET" ]; then
    echo "🎯 Deploying specific resource: $TARGET"
    DEPLOY_MODE="resource"
else
    echo "🚀 Deploying ALL Neon Database Tables..."
    DEPLOY_MODE="all"
fi

# Note: No API key needed - using direct psql connection to Neon database

# Check if terraform.tfvars exists
if [ ! -f "terraform/terraform.tfvars" ]; then
    echo "❌ Error: terraform.tfvars not found"
    echo "   Copy terraform.tfvars.example and fill in your Neon details:"
    echo "   cp terraform/terraform.tfvars.example terraform/terraform.tfvars"
    echo "   nano terraform/terraform.tfvars"
    exit 1
fi

# Validate terraform.tfvars has real values
if grep -q "your-neon-host" terraform/terraform.tfvars; then
    echo "❌ Error: terraform.tfvars still has placeholder values"
    echo "   Please update terraform.tfvars with your actual Neon connection details"
    exit 1
fi

cd terraform/

echo "📋 Initializing Terraform..."
terraform init

# Build terraform command with optional target
if [ "$DEPLOY_MODE" = "all" ]; then
    echo "📊 Planning deployment for ALL tables..."
    terraform plan
    echo "🚀 Applying ALL tables..."
    terraform apply -auto-approve
elif [ "$DEPLOY_MODE" = "single" ] || [ "$DEPLOY_MODE" = "resource" ]; then
    echo "📊 Planning deployment for: $TARGET"
    terraform plan -target="$TARGET"
    echo "🚀 Applying: $TARGET"
    terraform apply -auto-approve -target="$TARGET"
fi

echo "✅ Deployment completed successfully!"

# Show deployment summary
echo ""
if [ "$DEPLOY_MODE" = "all" ]; then
    echo "🎉 ALL TABLES DEPLOYED SUCCESSFULLY!"
    echo ""
    echo "📊 Database Information:"
    echo "   URL: $(terraform output -raw development_database_url 2>/dev/null || echo 'Not available')"
    echo "   Host: $(terraform output -raw development_database_host 2>/dev/null || echo 'Not available')"
    echo "   Database: $(terraform output -raw development_database_name 2>/dev/null || echo 'Not available')"
    echo ""
    echo "📋 Deployed Tables:"
    echo "   ✅ user_profiles        # User profiles and authentication"
    echo "   ✅ patient_profiles     # Patient information and medical history"
    echo "   ✅ provider_profiles    # Healthcare provider information"
    echo "   ✅ appointments         # Appointment scheduling and management"
    echo "   ✅ medical_records      # Medical records and documentation"
    echo "   ✅ audit_logs          # System audit and compliance logging"
    echo ""
    echo "🧪 Test your connection:"
    echo "   psql \"\$(terraform output -raw development_database_url)\""
    echo "   \\dt"
elif [ "$DEPLOY_MODE" = "single" ]; then
    echo "🎯 SINGLE TABLE DEPLOYED: $SINGLE_TABLE"
    echo ""
    case "$SINGLE_TABLE" in
        "user_profiles")
            echo "   ✅ User profiles and authentication table created"
            echo "   📋 Includes: user data, authentication, roles, status"
            ;;
        "patient_profiles")
            echo "   ✅ Patient profiles table created"
            echo "   📋 Includes: patient data, medical history, insurance, emergency contacts"
            ;;
        "provider_profiles")
            echo "   ✅ Provider profiles table created"
            echo "   📋 Includes: provider data, specialties, licenses, NPI numbers"
            ;;
        "appointments")
            echo "   ✅ Appointments table created"
            echo "   📋 Includes: scheduling, status, types, priorities, notes"
            ;;
        "medical_records")
            echo "   ✅ Medical records table created"
            echo "   📋 Includes: medical documentation, types, status, attachments"
            ;;
        "audit_logs")
            echo "   ✅ Audit logs table created"
            echo "   📋 Includes: system audit, compliance, user actions, security"
            ;;
    esac
    echo ""
    echo "🧪 Test your connection:"
    echo "   psql \"\$(terraform output -raw development_database_url)\""
    echo "   \\d $SINGLE_TABLE"
else
    echo "🎯 RESOURCE DEPLOYED: $TARGET"
fi
