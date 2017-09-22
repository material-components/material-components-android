#!/usr/bin/env bash

set -e

if [ "$TEST_TYPE" == "unit" ]; then
    ./gradlew assemble lint build test -PdisablePreDex;
elif [ "$TEST_TYPE" == "instrumentation" ]; then
    ./gradlew assemble -PdisablePreDex;
    travis_wait android-wait-for-emulator
    adb shell input keyevent 82 &
    # Avoid having it lock itself again.
    adb shell svc power stayon true
    travis_wait ./gradlew connectedCheck -PdisablePreDex;
else
    echo "Unknown test type"
    exit 1
fi
