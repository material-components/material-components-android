#!/usr/bin/env bash

set -e

echo "TEST_TYPE=$TEST_TYPE"
echo "EMULATOR_TAG=$EMULATOR_TAG"
echo "EMULATOR_API=$EMULATOR_API"
echo "ABI=$ABI"

if [ "$TEST_TYPE" == "unit" ]; then
  echo "This is unit test run, skipping instrumentation tests."
elif [ "$TEST_TYPE" == "instrumentation" ]; then
  echo "Actually running instrumentation tests..."
  ./gradlew connectedCheck -PdisablePreDex;
else
  echo "Unknown test type"
  exit 1
fi
