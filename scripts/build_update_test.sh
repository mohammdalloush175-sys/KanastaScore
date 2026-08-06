#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

chmod +x gradlew
./gradlew --no-daemon clean test assembleDebug

mkdir -p dist
cp app/build/outputs/apk/debug/*.apk dist/
printf '\nBuilt APK(s):\n'
ls -lh dist/*.apk
