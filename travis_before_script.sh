#!/usr/bin/env bash

set -e

# Start emulator in the background if necessary.

if [ "TEST_TYPE" == "instrumentation" ] ; then
    echo "Starting AVD for API $EMULATOR_API"
    ./travis_create_adb.sh &
fi

exit 0
