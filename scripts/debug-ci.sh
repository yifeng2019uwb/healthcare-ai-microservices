#!/bin/bash
# Debug CI/CD Issues
# This script helps identify what's failing in the CI pipeline

set -e

echo "🔍 Debugging CI/CD Issues..."
echo "=============================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check project structure
echo "📁 Checking project structure..."
if [ -f "services/pom.xml" ]; then
    print_status "services/pom.xml exists"
else
    print_error "services/pom.xml not found"
    exit 1
fi

if [ -f ".github/workflows/ci.yml" ]; then
    print_status ".github/workflows/ci.yml exists"
else
    print_error ".github/workflows/ci.yml not found"
    exit 1
fi

# Check Java
echo ""
echo "☕ Checking Java..."
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1)
    print_status "Java found: $java_version"
else
    print_error "Java not found"
    exit 1
fi

# Check Maven
echo ""
echo "🔨 Checking Maven..."
if command -v mvn &> /dev/null; then
    mvn_version=$(mvn -version | head -n 1)
    print_status "Maven found: $mvn_version"
else
    print_error "Maven not found"
    exit 1
fi

# Check services directory structure
echo ""
echo "📂 Checking services structure..."
cd services
if [ -f "pom.xml" ]; then
    print_status "services/pom.xml exists"

    # Check for subdirectories
    subdirs=$(find . -maxdepth 1 -type d -name "*" | grep -v "^\.$" | wc -l)
    if [ "$subdirs" -gt 0 ]; then
        print_status "Found $subdirs service directories:"
        find . -maxdepth 1 -type d -name "*" | grep -v "^\.$" | while read dir; do
            echo "   - $dir"
        done
    else
        print_warning "No service subdirectories found"
    fi
else
    print_error "services/pom.xml not found"
    exit 1
fi

# Try to run Maven commands
echo ""
echo "🧪 Testing Maven commands..."

# Test Maven clean
echo "Testing: mvn clean"
if mvn clean -q; then
    print_status "mvn clean works"
else
    print_error "mvn clean failed"
    exit 1
fi

# Test Maven compile
echo "Testing: mvn compile"
if mvn compile -q; then
    print_status "mvn compile works"
else
    print_error "mvn compile failed"
    exit 1
fi

# Test Maven test (if tests exist)
echo "Testing: mvn test"
if mvn test -q; then
    print_status "mvn test works"
else
    print_warning "mvn test failed (this might be expected if no tests exist yet)"
fi

# Test Maven package
echo "Testing: mvn package"
if mvn package -DskipTests -q; then
    print_status "mvn package works"
else
    print_error "mvn package failed"
    exit 1
fi

cd ..

echo ""
echo "=============================="
print_status "Debug completed!"
echo ""
echo "💡 If all checks passed, the CI should work."
echo "   If not, check the specific error messages above."
