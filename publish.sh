#!/bin/bash
#
# a2ui-4k Maven Central Publishing Script
#
# This script publishes the a2ui-4k library to Maven Central via Sonatype Portal.
# It uses JReleaser for signing and uploading artifacts.
#
# Required Environment Variables:
#   JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME - Sonatype Portal user token (not password)
#   JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD - Sonatype Portal token password
#   JRELEASER_GPG_PASSPHRASE - GPG key passphrase
#   JRELEASER_GPG_PUBLIC_KEY - GPG public key (armored)
#   JRELEASER_GPG_SECRET_KEY - GPG private key (armored)
#
# Usage:
#   ./publish.sh           # Publish to Maven Central
#   ./publish.sh --dry-run # Test without uploading
#

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Parse arguments
DRY_RUN=false
for arg in "$@"; do
    case $arg in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
    esac
done

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

# Get version from build.gradle.kts
VERSION=$(grep "^version = " build.gradle.kts | sed 's/version = "\(.*\)"/\1/')
echo -e "Publishing version: ${GREEN}${VERSION}${NC}"
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

# Step 3: Build and stage artifacts
echo -e "${YELLOW}Step 3: Building and staging artifacts...${NC}"
./gradlew :a2ui-4k:publish --no-daemon
echo -e "${GREEN}✓ Artifacts staged${NC}"
echo ""

# Step 4: Deploy to Maven Central
if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}Step 4: Running JReleaser in dry-run mode...${NC}"
    ./gradlew :a2ui-4k:jreleaserDeploy --dry-run --no-daemon
    echo -e "${GREEN}✓ Dry-run complete${NC}"
else
    echo -e "${YELLOW}Step 4: Deploying to Maven Central...${NC}"
    ./gradlew :a2ui-4k:jreleaserDeploy --no-daemon
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
    echo "Next steps:"
    echo "  1. Check deployment status at: https://central.sonatype.com/publishing"
    echo "  2. Artifacts will be validated automatically"
    echo "  3. Publishing typically completes in 10-30 minutes"
fi
