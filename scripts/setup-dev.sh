#!/bin/bash
# Development Environment Setup Script
# This script helps set up the required tools for local development

set -e

echo "🛠️  Setting up Development Environment..."
echo "========================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if we're on macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    print_info "Detected macOS"

    # Check if Homebrew is installed
    if command -v brew &> /dev/null; then
        print_status "Homebrew found"

        # Install Java 17
        echo ""
        print_info "Installing Java 17..."
        if brew list openjdk@17 &> /dev/null; then
            print_status "Java 17 already installed"
        else
            print_info "Installing Java 17 via Homebrew..."
            brew install openjdk@17
        fi

        # Install Maven
        echo ""
        print_info "Installing Maven..."
        if command -v mvn &> /dev/null; then
            print_status "Maven already installed"
        else
            print_info "Installing Maven via Homebrew..."
            brew install maven
        fi

    else
        print_error "Homebrew not found. Please install Homebrew first:"
        echo "  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
        exit 1
    fi

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    print_info "Detected Linux"

    # Update package list
    print_info "Updating package list..."
    sudo apt update

    # Install Java 17
    echo ""
    print_info "Installing Java 17..."
    sudo apt install -y openjdk-17-jdk

    # Install Maven
    echo ""
    print_info "Installing Maven..."
    sudo apt install -y maven

else
    print_error "Unsupported operating system: $OSTYPE"
    print_info "Please install Java 17 and Maven manually"
    exit 1
fi

# Verify installations
echo ""
echo "🔍 Verifying installations..."

# Check Java
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1)
    print_status "Java: $java_version"
else
    print_error "Java installation failed"
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    mvn_version=$(mvn -version | head -n 1)
    print_status "Maven: $mvn_version"
else
    print_error "Maven installation failed"
    exit 1
fi

# Set JAVA_HOME if needed (for macOS with Homebrew)
if [[ "$OSTYPE" == "darwin"* ]]; then
    if [ -z "$JAVA_HOME" ]; then
        print_info "Setting JAVA_HOME..."
        echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
        echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
        print_warning "JAVA_HOME added to ~/.zshrc. Please run: source ~/.zshrc"
    fi
fi

echo ""
echo "========================================"
print_status "Development environment setup complete!"
echo ""
echo "🎯 Next steps:"
echo "   1. Run: source ~/.zshrc (if JAVA_HOME was set)"
echo "   2. Run: ./scripts/debug-ci.sh"
echo "   3. Run: ./scripts/test-ci.sh"
echo ""
echo "🚀 You're ready to develop!"
