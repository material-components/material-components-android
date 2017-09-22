#!/usr/bin/env bash

set -e

# Start emulator in the background if necessary.

echo "TEST_TYPE=$TEST_TYPE"
echo "EMULATOR_TAG=$EMULATOR_TAG"
echo "EMULATOR_API=$EMULATOR_API"
echo "ABI=$ABI"

if [ "$TEST_TYPE" == "instrumentation" ] ; then
  echo "Starting AVD for API $EMULATOR_API"
  ./travis_create_avd.sh &
else
  echo "Skipping AVD for non-instrumentation build"
fi

exit 0
