#!/bin/bash
# ─────────────────────────────────────────────────────────────
# App Factory — First-time setup script
#
# Run this once after cloning the repo on a new machine.
# Safe to re-run — all steps are idempotent.
#
# Usage:
#   bash scripts/setup.sh
#
# What this does:
#   1. Checks all required tools are installed
#   2. Warns if JDK version may cause issues and explains the fix
#   3. Creates .env from .env.example if not already present
#   4. Generates the Gradle wrapper
#   5. Starts local Docker services (Postgres + Nango)
#   6. Runs the hard-gate test suite (ArchUnit + domain tests)
#   7. Runs a full build to verify everything compiles
#
# Prerequisites (install before running):
#   - JDK 17 or 21 (NOT 25+): https://adoptium.net
#       brew install --cask temurin@21
#   - Gradle 8.11+: https://gradle.org/install
#       brew install gradle
#   - Docker Desktop: https://docs.docker.com/get-docker/
#   - Android Studio or Android SDK (for Android builds)
#   - Xcode (macOS only, for iOS builds)
# ─────────────────────────────────────────────────────────────
set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

ok()   { echo -e "${GREEN}✓${NC} $1"; }
fail() { echo -e "\n${RED}✗ $1${NC}\n"; exit 1; }
warn() { echo -e "${YELLOW}⚠${NC} $1"; }
step() { echo -e "\n${CYAN}▶ $1${NC}"; }
info() { echo -e "   $1"; }

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}  App Factory — Setup${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# ─────────────────────────────────────────
# 1. CHECK PREREQUISITES
# ─────────────────────────────────────────
step "Checking prerequisites"

command -v java &>/dev/null || fail "Java not found. Install: brew install --cask temurin@21"
ok "Java: $(java -version 2>&1 | head -1)"

DOCKER_AVAILABLE=false
if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
    ok "Docker: $(docker --version | awk '{print $3}' | tr -d ',')"
    DOCKER_AVAILABLE=true
else
    warn "Docker not running — local services will be skipped"
fi

[ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ] && \
    warn "ANDROID_HOME not set — Android builds will fail until configured" || \
    ok "Android SDK: ${ANDROID_HOME:-$ANDROID_SDK_ROOT}"

[[ "$OSTYPE" == "darwin"* ]] && {
    command -v xcodebuild &>/dev/null && \
        ok "Xcode: $(xcodebuild -version 2>/dev/null | head -1)" || \
        warn "Xcode not found — iOS builds unavailable"
}

step "Clearing stale build caches"
rm -rf .gradle build build-logic/build
ok "Caches cleared"

step "Generating Gradle wrapper"
# If gradlew is missing, generate it with system Gradle.
# The wrapper targets 9.x — use whatever 9.x version you have installed.
if [ ! -f "./gradlew" ]; then
    if command -v gradle &>/dev/null; then
        GRADLE_VERSION=$(gradle --version 2>/dev/null | grep '^Gradle' | awk '{print $2}')
        gradle wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin
        ok "Gradle wrapper generated ($GRADLE_VERSION)"
    else
        fail "gradlew not found and system Gradle not installed.\nInstall Gradle: brew install gradle\nThen re-run this script."
    fi
else
    ok "Gradle wrapper present"
fi

# ─────────────────────────────────────────
# 3. ENVIRONMENT FILE
# ─────────────────────────────────────────
step "Environment file"
if [ -f ".env" ]; then
    ok ".env already exists"
else
    cp .env.example .env
    echo ""
    warn ".env created from .env.example"
    info "Open .env and fill in the required values:"
    info ""
    info "  Required for backend:"
    info "    SUPABASE_URL          — from your Supabase project settings"
    info "    SUPABASE_ANON_KEY     — from your Supabase project settings"
    info "    SUPABASE_SERVICE_ROLE_KEY"
    info "    JWT_SECRET            — generate with: openssl rand -base64 32"
    info ""
    info "  Required for connectors (OAuth):"
    info "    NANGO_SECRET_KEY      — from Nango dashboard (or leave empty for local)"
    info ""
    info "The app will compile and run without these values."
    info "Backend and sync features require them at runtime."
    echo ""
fi

# ─────────────────────────────────────────
# 4. LOCAL SERVICES
# ─────────────────────────────────────────
step "Starting local services"
if [ "$DOCKER_AVAILABLE" = true ]; then
    docker compose -f scripts/deploy/docker-compose.yml up -d db
    ok "Local Postgres started on port 5432"
    info "Stop with: docker compose -f scripts/deploy/docker-compose.yml down"
else
    warn "Skipping — Docker not available"
fi

# ─────────────────────────────────────────
# 5. HARD-GATE TESTS
# ─────────────────────────────────────────
step "Running hard-gate tests"
./gradlew :domain:jvmTest :application:jvmTest --no-daemon -q
ok "ArchUnit + domain tests passed"

# ─────────────────────────────────────────
# 6. FULL BUILD
# ─────────────────────────────────────────
step "Full build"
./gradlew build --no-daemon -q
ok "All modules compiled successfully"

# ─────────────────────────────────────────
# DONE
# ─────────────────────────────────────────
echo -e "\n${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  Setup complete ✓${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
echo "  Run targets:"
echo "    Desktop:  ./gradlew :clients:desktop:run"
echo "    Backend:  ./gradlew :backend:run          → http://localhost:8080/health"
echo "    Web:      ./gradlew :clients:web:wasmJsBrowserRun"
echo "    Android:  ./gradlew :clients:android:installDebug"
echo "    Tests:    ./gradlew :domain:jvmTest :application:jvmTest"
echo ""
echo "  iOS setup (requires Xcode + successful build above):"
echo "    See docs/setup-ios.md"
echo ""
echo "  Next: read docs/ai-workflow.md before using AI agents."
echo ""
