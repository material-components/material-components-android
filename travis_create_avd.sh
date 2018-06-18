#!/usr/bin/env bash

set -e

echo "TEST_TYPE=$TEST_TYPE"
echo "EMULATOR_TAG=$EMULATOR_TAG"
echo "EMULATOR_API=$EMULATOR_API"
echo "ABI=$ABI"

$HOME/android-sdk/tools/bin/sdkmanager "platforms;android-$EMULATOR_API"
$HOME/android-sdk/tools/bin/sdkmanager "system-images;android-$EMULATOR_API;default;$ABI"
$HOME/android-sdk/tools/bin/avdmanager list targets
echo no | $HOME/android-sdk/tools/bin/avdmanager create avd --force -n test -k "system-images;android-$EMULATOR_API;default;$ABI"

$HOME/android-sdk/emulator/emulator -avd test -no-audio -netfast -no-window &

exit 0
