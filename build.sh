#!/bin/bash
# Build chestcavity using podman with a Java 21 container
# Builds the jar and extracts it to build/libs/

set -e

CONTAINER_IMAGE="docker.io/eclipse-temurin:21-jdk"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Building chestcavity with podman..."

podman run --rm \
    -v "$PROJECT_DIR":/project:Z \
    -v gradle-cache:/root/.gradle \
    -w /project \
    "$CONTAINER_IMAGE" \
    bash -c "./gradlew build --no-daemon $*"

echo ""
echo "Build complete. Jars:"
ls -la "$PROJECT_DIR"/build/libs/*.jar 2>/dev/null || echo "No jars found in build/libs/"
