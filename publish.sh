#!/bin/bash
#
# a2ui-4k Maven Central Publishing Script
#
# This script publishes the a2ui-4k library to Maven Central via Sonatype Portal.
# It uses JReleaser CLI for signing and uploading artifacts.
#
# Required Environment Variables:
#   JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME - Sonatype Portal user token (not password)
#   JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD - Sonatype Portal token password
#   JRELEASER_GPG_PASSPHRASE - GPG key passphrase
#   JRELEASER_GPG_PUBLIC_KEY - GPG public key (armored)
#   JRELEASER_GPG_SECRET_KEY - GPG private key (armored)
#
# Usage:
#   ./publish.sh                    # Publish stable release from main
#   ./publish.sh --channel alpha    # Publish alpha pre-release (from any branch)
#   ./publish.sh --channel beta     # Publish beta pre-release (from any branch)
#   ./publish.sh --dry-run          # Test without uploading
#   ./publish.sh --channel alpha --dry-run
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Parse arguments
DRY_RUN=false
CHANNEL=""
for arg in "$@"; do
    case $arg in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --channel)
            shift
            CHANNEL="$1"
            shift
            ;;
        --channel=*)
            CHANNEL="${arg#*=}"
            shift
            ;;
    esac
done

# Validate channel
if [ -n "$CHANNEL" ] && [ "$CHANNEL" != "alpha" ] && [ "$CHANNEL" != "beta" ]; then
    echo -e "${RED}Error: Invalid channel '${CHANNEL}'. Must be 'alpha' or 'beta'.${NC}"
    exit 1
fi

echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}  a2ui-4k Maven Central Publishing Script${NC}"
echo -e "${GREEN}================================================${NC}"
echo ""

# Validate required environment variables
echo -e "${YELLOW}Checking required environment variables...${NC}"
MISSING_VARS=()

if [ -z "$JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME" ]; then
    MISSING_VARS+=("JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME")
fi
if [ -z "$JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD" ]; then
    MISSING_VARS+=("JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD")
fi
if [ -z "$JRELEASER_GPG_PASSPHRASE" ]; then
    MISSING_VARS+=("JRELEASER_GPG_PASSPHRASE")
fi
if [ -z "$JRELEASER_GPG_PUBLIC_KEY" ]; then
    MISSING_VARS+=("JRELEASER_GPG_PUBLIC_KEY")
fi
if [ -z "$JRELEASER_GPG_SECRET_KEY" ]; then
    MISSING_VARS+=("JRELEASER_GPG_SECRET_KEY")
fi

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo -e "${RED}Error: Missing required environment variables:${NC}"
    for var in "${MISSING_VARS[@]}"; do
        echo -e "${RED}  - $var${NC}"
    done
    echo ""
    echo "Please set these variables before running this script."
    echo "See: https://central.sonatype.com/account for Sonatype credentials"
    exit 1
fi

echo -e "${GREEN}✓ All required environment variables are set${NC}"
echo ""

# Get base version from build.gradle.kts
BASE_VERSION=$(grep "^version = " build.gradle.kts | sed 's/version = .*"\(.*\)"/\1/')
BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Compute the full version based on channel
if [ -n "$CHANNEL" ]; then
    # Count commits ahead of main to generate a build number
    # This auto-increments as commits are added to the branch
    if git rev-parse --verify origin/main >/dev/null 2>&1; then
        BUILD_NUMBER=$(git rev-list --count origin/main..HEAD 2>/dev/null || echo "0")
    elif git rev-parse --verify main >/dev/null 2>&1; then
        BUILD_NUMBER=$(git rev-list --count main..HEAD 2>/dev/null || echo "0")
    else
        BUILD_NUMBER="0"
    fi

    # Ensure build number is at least 1 for pre-release versions
    if [ "$BUILD_NUMBER" -eq "0" ]; then
        BUILD_NUMBER=1
    fi

    VERSION="${BASE_VERSION}-${CHANNEL}.${BUILD_NUMBER}"
else
    # Stable release: warn if not on main
    if [ "$BRANCH" != "main" ] && [ "$BRANCH" != "HEAD" ]; then
        echo -e "${YELLOW}Warning: Publishing a stable release from branch '${BRANCH}' (not main).${NC}"
        echo -e "${YELLOW}Consider using --channel alpha or --channel beta for pre-release versions.${NC}"
        echo ""
        read -p "Continue with stable release? (y/N) " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "Aborted."
            exit 1
        fi
    fi
    VERSION="${BASE_VERSION}"
fi

echo -e "Branch:  ${GREEN}${BRANCH}${NC}"
echo -e "Channel: ${GREEN}${CHANNEL:-stable}${NC}"
echo -e "Version: ${GREEN}${VERSION}${NC}"
echo ""

if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}🔍 DRY-RUN MODE - No artifacts will be uploaded${NC}"
    echo ""
fi

# Step 1: Clean previous builds
echo -e "${YELLOW}Step 1: Cleaning previous builds...${NC}"
./gradlew clean --no-daemon
rm -rf library/build/staging
echo -e "${GREEN}✓ Clean complete${NC}"
echo ""

# Step 2: Run tests
echo -e "${YELLOW}Step 2: Running tests...${NC}"
./gradlew :a2ui-4k:allTests --no-daemon
echo -e "${GREEN}✓ All tests passed${NC}"
echo ""

# Step 3: Build and stage artifacts (pass version override to Gradle)
echo -e "${YELLOW}Step 3: Building and staging artifacts...${NC}"
./gradlew :a2ui-4k:publish -PpublishVersion="${VERSION}" --no-daemon
echo -e "${GREEN}✓ Artifacts staged${NC}"
echo ""

# Step 4: Deploy to Maven Central using JReleaser CLI
echo -e "${YELLOW}Step 4: Installing JReleaser CLI...${NC}"
JRELEASER_VERSION="1.20.0"
if [ ! -f "jreleaser-cli.jar" ]; then
    curl -sL "https://github.com/jreleaser/jreleaser/releases/download/v${JRELEASER_VERSION}/jreleaser-tool-provider-${JRELEASER_VERSION}.jar" -o jreleaser-cli.jar
fi
echo -e "${GREEN}✓ JReleaser CLI ready${NC}"
echo ""

# Override JReleaser project version via environment variable
export JRELEASER_PROJECT_VERSION="${VERSION}"

if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}Step 5: Running JReleaser in dry-run mode...${NC}"
    java -jar jreleaser-cli.jar deploy --dry-run
    echo -e "${GREEN}✓ Dry-run complete${NC}"
else
    echo -e "${YELLOW}Step 5: Deploying to Maven Central...${NC}"
    java -jar jreleaser-cli.jar deploy
    echo -e "${GREEN}✓ Deployment initiated${NC}"
fi
echo ""

# Summary
echo -e "${GREEN}================================================${NC}"
if [ "$DRY_RUN" = true ]; then
    echo -e "${GREEN}  DRY-RUN COMPLETE${NC}"
    echo -e "${GREEN}================================================${NC}"
    echo ""
    echo "No artifacts were uploaded. Run without --dry-run to publish."
else
    echo -e "${GREEN}  PUBLISHING COMPLETE${NC}"
    echo -e "${GREEN}================================================${NC}"
    echo ""
    echo "Published artifacts:"
    echo "  - com.contextable:a2ui-4k:${VERSION} (JVM)"
    echo "  - com.contextable:a2ui-4k-android:${VERSION} (Android)"
    echo "  - com.contextable:a2ui-4k-iosx64:${VERSION} (iOS x64)"
    echo "  - com.contextable:a2ui-4k-iosarm64:${VERSION} (iOS ARM64)"
    echo "  - com.contextable:a2ui-4k-iossimulatorarm64:${VERSION} (iOS Simulator ARM64)"
    echo ""
    if [ -n "$CHANNEL" ]; then
        echo "This is a ${CHANNEL} pre-release. Consumers can depend on it with:"
        echo "  implementation(\"com.contextable:a2ui-4k:${VERSION}\")"
        echo ""
    fi
    echo "Next steps:"
    echo "  1. Check deployment status at: https://central.sonatype.com/publishing"
    echo "  2. Artifacts will be validated automatically"
    echo "  3. Publishing typically completes in 10-30 minutes"
fi
