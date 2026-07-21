#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

echo "Running unit tests..."
chmod +x gradlew
./gradlew testDebugUnitTest

echo "Tests passed!"
echo "Report: $PROJECT_ROOT/app/build/reports/tests/testDebugUnitTest/index.html"
