#!/bin/bash
# Local CI/CD Test Script
# This script simulates the GitHub Actions CI pipeline locally

set -e  # Exit on any error

echo "🚀 Starting Local CI/CD Test..."
echo "=================================="

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

# Check if we're in the right directory
if [ ! -f "services/pom.xml" ]; then
    print_error "services/pom.xml not found. Please run this script from the project root."
    exit 1
fi

print_status "Found project structure"

# Check Java version
echo ""
echo "🔍 Checking Java version..."
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$java_version" -ge 17 ]; then
    print_status "Java version: $(java -version 2>&1 | head -n 1)"
else
    print_error "Java 17+ required. Found: $java_version"
    exit 1
fi

# Check Maven
echo ""
echo "🔍 Checking Maven..."
if command -v mvn &> /dev/null; then
    print_status "Maven version: $(mvn -version | head -n 1)"
else
    print_error "Maven not found. Please install Maven."
    exit 1
fi

# Clean and test
echo ""
echo "🧪 Running unit tests..."
cd services
if mvn clean test -q; then
    print_status "Unit tests passed"
else
    print_error "Unit tests failed"
    exit 1
fi

# Build application
echo ""
echo "🔨 Building application..."
if mvn clean package -DskipTests -q; then
    print_status "Build successful"
else
    print_error "Build failed"
    exit 1
fi

# Check if JAR files were created
echo ""
echo "📦 Checking build artifacts..."
jar_count=$(find . -name "*.jar" -not -path "*/target/original-*" | wc -l)
if [ "$jar_count" -gt 0 ]; then
    print_status "Found $jar_count JAR file(s):"
    find . -name "*.jar" -not -path "*/target/original-*" | while read jar; do
        echo "   - $jar"
    done
else
    print_error "No JAR files found"
    exit 1
fi

# Check test reports
echo ""
echo "📊 Checking test reports..."
if [ -d "target/surefire-reports" ]; then
    test_files=$(find target/surefire-reports -name "*.xml" | wc -l)
    if [ "$test_files" -gt 0 ]; then
        print_status "Found $test_files test report(s)"
    else
        print_warning "No test reports found"
    fi
else
    print_warning "No surefire-reports directory found"
fi

# Go back to project root
cd ..

echo ""
echo "=================================="
print_status "Local CI/CD test completed successfully!"
echo ""
echo "🎯 Summary:"
echo "   ✅ Java version check"
echo "   ✅ Maven availability"
echo "   ✅ Unit tests"
echo "   ✅ Build process"
echo "   ✅ Artifact generation"
echo ""
echo "🚀 Ready to push to GitHub!"
