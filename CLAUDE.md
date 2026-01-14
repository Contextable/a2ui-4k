# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Reference Documentation

When the user says "Refer to A2UI", use the deepwiki MCP server to access the `google/A2UI` repo. This is the canonical source of truth for the A2UI protocol.

## Project Overview

A2UI is a rendering engine for Kotlin Multiplatform (KMP).

## Environment Setup

JDK 21 is installed via SDKMAN. Android SDK is installed at ~/android-sdk. Initialize with:
```bash
source /home/developer/.sdkman/bin/sdkman-init.sh
export ANDROID_HOME=/home/developer/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export CHROME_BIN=/usr/bin/chromium  # Required for JS browser tests
```

For ARM64 Linux, x86_64 binary execution is enabled via multiarch:
```bash
sudo dpkg --add-architecture amd64
sudo apt-get update
sudo apt-get install -y libc6:amd64
```

## Build Commands

Build (skipping iOS targets on non-macOS):
```bash
./gradlew build -x compileKotlinIosArm64 -x compileKotlinIosX64 -x compileKotlinIosSimulatorArm64 -x commonizeNativeDistribution
```

Run all tests:
```bash
./gradlew allTests
```

## Architecture

*To be documented as the codebase grows.*
