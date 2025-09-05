#!/bin/bash

# Healthcare Shared Module - Simple Development Script
# Usage: ./dev.sh [clean|build|test]

set -e

MAVEN_CMD="mvn"
JAVA_VERSION="17"

echo "🏥 Healthcare Shared Module - Development Script"
echo "================================================"

# Function to check prerequisites
check_prerequisites() {
    echo "🔍 Checking prerequisites..."

    # Check Java version
    if command -v java &> /dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VER" -ge "$JAVA_VERSION" ]; then
            echo "✅ Java $JAVA_VER found (required: $JAVA_VERSION+)"
        else
            echo "❌ Java $JAVA_VER found, but Java $JAVA_VERSION+ is required"
            exit 1
        fi
    else
        echo "❌ Java not found. Please install Java $JAVA_VERSION+"
        exit 1
    fi

    # Check Maven
    if command -v mvn &> /dev/null; then
        MAVEN_VER=$(mvn -version | head -n 1 | cut -d' ' -f3)
        echo "✅ Maven $MAVEN_VER found"
    else
        echo "❌ Maven not found. Please install Maven"
        exit 1
    fi

    # Check if we're in the right directory
    if [ ! -f "pom.xml" ]; then
        echo "❌ pom.xml not found. Please run this script from the shared module directory"
        exit 1
    fi

    echo "✅ All prerequisites met!"
    echo ""
}

# Always check prerequisites first
check_prerequisites

case "${1:-build}" in
    "clean")
        echo "🧹 Cleaning project..."
        $MAVEN_CMD clean
        echo "✅ Clean completed"
        ;;
    "build")
        echo "🔨 Building project..."
        $MAVEN_CMD compile
        echo "✅ Build completed"
        ;;
    "test")
        echo "🧪 Running tests..."
        $MAVEN_CMD test
        echo "✅ Tests completed"
        ;;
    "test-class")
        if [ -z "$2" ]; then
            echo "❌ Please specify a test class name"
            echo "Usage: $0 test-class <ClassName>"
            echo "Example: $0 test-class AuditLogEntityTest"
            exit 1
        fi
        echo "🧪 Running test class: $2"
        $MAVEN_CMD test -Dtest="$2"
        echo "✅ Test class completed"
        ;;
    "coverage")
        echo "📊 Running tests with coverage..."
        $MAVEN_CMD clean test jacoco:report
        echo "✅ Coverage report generated in target/site/jacoco/index.html"
        ;;
    *)
        echo "Usage: $0 [clean|build|test|test-class|coverage]"
        echo "  clean       - Clean previous builds"
        echo "  build       - Compile the project (default)"
        echo "  test        - Run all tests"
        echo "  test-class  - Run specific test class (e.g., test-class AuditLogEntityTest)"
        echo "  coverage    - Run tests with coverage report"
        exit 1
        ;;
esac

echo "🎉 Script completed successfully!"
