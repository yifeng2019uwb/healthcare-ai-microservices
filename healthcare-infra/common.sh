#!/bin/bash
# =============================================================================
# common.sh — Shared utilities for healthcare-infra scripts
# Source this file at the start of each script:
#   source "$(dirname "${BASH_SOURCE[0]}")/../common.sh"
# =============================================================================

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage(){ echo -e "\n${BLUE}=== $1 ===${NC}"; }

# Add PostgreSQL client tools to PATH (Mac Homebrew)
if [[ "$OSTYPE" == "darwin"* ]]; then
  export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"
fi

# Load .env from healthcare-infra/ if local
INFRA_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -z "$CI" ]] && [[ -f "$INFRA_DIR/.env" ]]; then
  source "$INFRA_DIR/.env"
  ok "Loaded .env"
fi