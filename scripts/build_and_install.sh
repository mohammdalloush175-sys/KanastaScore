#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="/home/mohammd/Workspace/Android/kanastaScore"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

cd "$PROJECT_ROOT"

CLEAN=false
if [[ "${1:-}" == "--clean" ]]; then
    CLEAN=true
fi

chmod +x gradlew

if [ "$CLEAN" = true ]; then
    echo "Cleaning and building..."
    ./gradlew clean assembleDebug
else
    echo "Building..."
    ./gradlew assembleDebug
fi

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo "Build successful: $APK_PATH"

    if command -v adb &> /dev/null; then
        DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)
        if [ "$DEVICE_COUNT" -gt 0 ]; then
            echo "Installing on device..."
            adb install -r "$APK_PATH"
            echo "Launching app..."
            adb shell am start -n com.mohammdalloush.kanastascore/com.mohammdalloush.kanastascore.MainActivity
        else
            echo "No device connected via ADB. Skipping installation."
        fi
    else
        echo "ADB not found. Skipping installation."
    fi
else
    echo "Build failed: APK not found."
    exit 1
fi
