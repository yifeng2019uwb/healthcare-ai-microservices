#!/bin/bash

# Healthcare Services - Development Script
# Usage: ./dev.sh [service] [clean|build|test|run|package|coverage]
# Example: ./dev.sh shared test
# Example: ./dev.sh auth-service run
# Example: ./dev.sh all build

set -e

MAVEN_CMD="mvn"
JAVA_VERSION="17"
SERVICES=("shared" "auth-service" "gateway" "patient-service" "provider-service")

echo "🏥 Healthcare Services - Development Script"
echo "==========================================="

# ──────────────────────────────────────────────
# Prerequisites
# ──────────────────────────────────────────────
check_prerequisites() {
    echo "🔍 Checking prerequisites..."

    # Java
    if ! command -v java &>/dev/null; then
        echo "❌ Java not found. Please install Java $JAVA_VERSION+"
        exit 1
    fi

    JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VER" -lt "$JAVA_VERSION" ]; then
        echo "❌ Java $JAVA_VER found, but Java $JAVA_VERSION+ is required"
        exit 1
    fi
    echo "✅ Java $JAVA_VER found"

    # JAVA_HOME — needed by Maven
    if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
        if [ "$(uname)" = "Darwin" ] && command -v /usr/libexec/java_home &>/dev/null; then
            DETECTED=$(/usr/libexec/java_home -v "$JAVA_VERSION" 2>/dev/null || true)
            if [ -n "$DETECTED" ] && [ -x "$DETECTED/bin/java" ]; then
                export JAVA_HOME="$DETECTED"
                echo "✅ JAVA_HOME auto-set to $JAVA_HOME"
            fi
        fi
    fi

    if [ -z "$JAVA_HOME" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
        echo "❌ JAVA_HOME is not set. Run: export JAVA_HOME=\$(/usr/libexec/java_home -v $JAVA_VERSION)"
        exit 1
    fi

    # Maven
    if ! command -v mvn &>/dev/null; then
        echo "❌ Maven not found. Please install Maven"
        exit 1
    fi
    MAVEN_VER=$(mvn -version 2>/dev/null | awk 'NR==1{print $3}')
    echo "✅ Maven $MAVEN_VER found"

    # Must run from services directory
    if [ ! -f "pom.xml" ]; then
        echo "❌ pom.xml not found. Run this script from the services/ directory"
        exit 1
    fi

    echo "✅ All prerequisites met!"
    echo ""
}

# ──────────────────────────────────────────────
# Helpers
# ──────────────────────────────────────────────
list_services() {
    echo "📋 Available services:"
    for s in "${SERVICES[@]}"; do
        [ -f "$s/pom.xml" ] && echo "  - $s"
    done
    echo ""
}

validate_service() {
    local service=$1
    [ "$service" = "all" ] && return 0
    if [ ! -d "$service" ] || [ ! -f "$service/pom.xml" ]; then
        echo "❌ Service '$service' not found"
        list_services
        exit 1
    fi
}

run_maven() {
    local service=$1
    shift
    echo "🔧 Running: mvn $* in $service/"
    (cd "$service" && $MAVEN_CMD "$@")
}

open_coverage_report() {
    local service=$1
    local report="$service/target/site/jacoco/index.html"
    if [ -f "$report" ]; then
        echo "📊 Opening coverage report: $report"
        if [ "$(uname)" = "Darwin" ]; then
            open "$report"
        else
            echo "   Report available at: $report"
        fi
    else
        echo "⚠️  Coverage report not found at $report"
    fi
}

# ──────────────────────────────────────────────
# Main
# ──────────────────────────────────────────────
check_prerequisites

SERVICE=${1:-}
COMMAND=${2:-build}

if [ -z "$SERVICE" ]; then
    echo "Usage: $0 <service|all> [clean|build|test|run|package|coverage]"
    echo ""
    list_services
    exit 1
fi

validate_service "$SERVICE"

# ── all commands ──
if [ "$SERVICE" = "all" ]; then
    case "$COMMAND" in
        "build")
            echo "🔄 Building all services..."
            echo ""
            run_maven "shared" clean install -DskipTests
            echo "✅ shared built"
            echo ""
            for service in auth-service gateway patient-service provider-service; do
                if [ -f "$service/pom.xml" ]; then
                    echo "🔨 Building $service..."
                    if run_maven "$service" clean compile; then
                        echo "✅ $service built"
                    else
                        echo "⚠️  $service build failed (skipped)"
                    fi
                    echo ""
                fi
            done
            echo "✅ All services built"
            ;;

        "test")
            echo "🧪 Testing all services..."
            echo ""
            for service in shared auth-service gateway patient-service provider-service; do
                if [ -f "$service/pom.xml" ]; then
                    echo "🧪 Testing $service..."
                    if run_maven "$service" test; then
                        echo "✅ $service tests passed"
                    else
                        echo "⚠️  $service tests failed (skipped)"
                    fi
                    echo ""
                fi
            done
            echo "✅ All service tests completed"
            ;;

        "coverage")
            echo "📊 Coverage for all services..."
            echo ""
            for service in shared auth-service gateway patient-service provider-service; do
                if [ -f "$service/pom.xml" ]; then
                    echo "📊 Coverage for $service..."
                    run_maven "$service" clean test jacoco:report
                    open_coverage_report "$service"
                    echo ""
                fi
            done
            ;;

        *)
            echo "Usage: $0 all [build|test|coverage]"
            exit 1
            ;;
    esac

# ── single service commands ──
else
    case "$COMMAND" in
        "clean")
            echo "🧹 Cleaning $SERVICE..."
            run_maven "$SERVICE" clean
            echo "✅ Clean completed"
            ;;

        "build")
            echo "🔨 Building $SERVICE..."
            # Install shared first if building a dependent service
            if [ "$SERVICE" != "shared" ] && [ -f "shared/pom.xml" ]; then
                echo "📦 Installing shared module first..."
                run_maven "shared" install -DskipTests -q
            fi
            run_maven "$SERVICE" compile
            echo "✅ Build completed"
            ;;

        "test")
            echo "🧪 Testing $SERVICE..."
            if [ "$SERVICE" != "shared" ] && [ -f "shared/pom.xml" ]; then
                echo "📦 Installing shared module first..."
                run_maven "shared" install -DskipTests -q
            fi
            run_maven "$SERVICE" test
            echo "✅ Tests completed"
            ;;

        "run")
            echo "🚀 Starting $SERVICE..."
            # Install shared first
            if [ "$SERVICE" != "shared" ] && [ -f "shared/pom.xml" ]; then
                echo "📦 Installing shared module first..."
                run_maven "shared" install -DskipTests -q
            fi
            run_maven "$SERVICE" spring-boot:run
            ;;

        "package")
            echo "📦 Packaging $SERVICE..."
            run_maven "$SERVICE" package -DskipTests
            echo "✅ Package completed"
            ;;

        "coverage")
            echo "📊 Running coverage for $SERVICE..."
            run_maven "$SERVICE" clean test jacoco:report
            open_coverage_report "$SERVICE"
            echo "✅ Coverage completed"
            ;;

        *)
            echo "Usage: $0 <service> [clean|build|test|run|package|coverage]"
            echo "       $0 all [build|test|coverage]"
            echo ""
            echo "Commands:"
            echo "  clean     — Remove build artifacts"
            echo "  build     — Compile (default)"
            echo "  test      — Run tests"
            echo "  run       — Start with Spring Boot"
            echo "  package   — Build JAR"
            echo "  coverage  — Run tests + open JaCoCo report"
            echo ""
            list_services
            exit 1
            ;;
    esac
fi

echo ""
echo "🎉 Done!"