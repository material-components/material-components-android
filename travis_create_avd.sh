#!/usr/bin/env bash

set -e

echo "TEST_TYPE=$TEST_TYPE"
echo "EMULATOR_TAG=$EMULATOR_TAG"
echo "EMULATOR_API=$EMULATOR_API"
echo "ABI=$ABI"

echo y | android update sdk --no-ui --all --filter "android-$EMULATOR_API"
android-update-sdk --components="sys-img-$ABI-$EMULATOR_TAG-$EMULATOR_API" --accept-licenses='android-sdk-license-[0-9a-f]{8}'
android list targets
echo no | android create avd --force -n test -t $EMULATOR_TAG-$EMULATOR_API --abi $ABI
emulator -avd test -no-audio -netfast -no-window &

exit 0
