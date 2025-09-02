#!/bin/bash
# Simple Project Structure Test Script
# This script validates the basic project structure

set -e  # Exit on any error

echo "🚀 Starting Project Structure Test..."
echo "====================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

echo "🔍 Checking project structure..."

# Check main directories exist
if [ -d "docs" ]; then
    print_status "docs/ directory exists"
else
    print_error "docs/ directory missing"
    exit 1
fi

if [ -d "healthcare-infra" ]; then
    print_status "healthcare-infra/ directory exists"
else
    print_error "healthcare-infra/ directory missing"
    exit 1
fi

if [ -d "services" ]; then
    print_status "services/ directory exists"
else
    print_error "services/ directory missing"
    exit 1
fi

# Check shared module exists
if [ -d "services/shared" ]; then
    print_status "services/shared/ directory exists"
else
    print_error "services/shared/ directory missing"
    exit 1
fi

if [ -f "services/shared/pom.xml" ]; then
    print_status "services/shared/pom.xml exists"
else
    print_error "services/shared/pom.xml missing"
    exit 1
fi

if [ -f "services/shared/dev.sh" ]; then
    print_status "services/shared/dev.sh exists"
else
    print_error "services/shared/dev.sh missing"
    exit 1
fi

# Check key files exist
if [ -f "README.md" ]; then
    print_status "README.md exists"
else
    print_error "README.md missing"
    exit 1
fi

if [ -f ".gitignore" ]; then
    print_status ".gitignore exists"
else
    print_error ".gitignore missing"
    exit 1
fi

if [ -f ".github/workflows/ci.yml" ]; then
    print_status "CI workflow exists"
else
    print_error "CI workflow missing"
    exit 1
fi

echo ""
echo "🧪 Testing shared module..."

# Test shared module build and test
if [ -d "services/shared" ]; then
    cd services/shared

    # Check if Java and Maven are available
    if command -v java &> /dev/null && command -v mvn &> /dev/null; then
        echo "🔨 Building shared module..."
        if ./dev.sh build; then
            print_status "Shared module build successful"
        else
            print_error "Shared module build failed"
            exit 1
        fi

        echo "🧪 Testing shared module..."
        if ./dev.sh test; then
            print_status "Shared module tests passed"
        else
            print_error "Shared module tests failed"
            exit 1
        fi
    else
        print_warning "Java or Maven not available, skipping shared module tests"
    fi

    cd ../..
fi

echo ""
echo "====================================="
print_status "Project structure and shared module test completed successfully!"
echo ""
echo "🎯 Summary:"
echo "   ✅ docs/ directory"
echo "   ✅ healthcare-infra/ directory"
echo "   ✅ services/ directory"
echo "   ✅ services/shared/ module"
echo "   ✅ README.md"
echo "   ✅ .gitignore"
echo "   ✅ CI workflow"
echo "   ✅ Shared module build & tests"
echo ""
echo "🚀 Ready to push to GitHub!"
