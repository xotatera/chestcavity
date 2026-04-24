#!/bin/bash
# Build chestcavity using podman with a Java 21 container
# Builds the jar and copies it to the mods folder

set -e

CONTAINER_IMAGE="docker.io/eclipse-temurin:21-jdk"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_OUT_DIR="$PROJECT_DIR/jars"

echo "Building chestcavity with podman..."

podman run --rm \
    -v "$PROJECT_DIR":/project:Z \
    -v gradle-cache:/root/.gradle \
    -w /project \
    "$CONTAINER_IMAGE" \
    bash -c "./gradlew build --no-daemon $*"

JAR="$PROJECT_DIR/build/libs/chestcavity-3.0.0.jar"
if [ -f "$JAR" ]; then
    mkdir -p "$JAR_OUT_DIR"
    cp "$JAR" "$JAR_OUT_DIR/chestcavity-3.0.0.jar"
    echo "Copied to $JAR_OUT_DIR/chestcavity-3.0.0.jar"
else
    echo "Build failed - no jar found"
    exit 1
fi
