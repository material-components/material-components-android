# Material Components for Android

Material Components for Android (MDC-Android) help developers execute
[Material Design](https://www.material.io). Developed by a core team of
engineers and UX designers at Google, these components enable a reliable
development workflow to build beautiful and functional Android apps.

Material Components for Android are based on Android’s Design support library
(DesignLib) which will continue to be released as part of Android’s SDK.
Development will take place on GitHub, and stable versions will be synced to
Android’s SDK based on the SDK’s release schedules.

## Useful Links
- [All Components](lib/)
- [Contributing](CONTRIBUTING.md)
- [Class
  documentation](https://developer.android.com/reference/android/support/design/widget/package-summary.html)
  (external site)
- [MDC-Android on Stack
  Overflow](https://www.stackoverflow.com/questions/tagged/material-components+android)
  (external site)
- [Android Developer’s
  Guide](https://developer.android.com/training/material/index.html)
  (external site)
- [Material.io](https://www.material.io) (external site)
- [Material Design Guidelines](https://material.google.com) (external site)

## Getting Started

### Using the support library version

For most users, the Android Design support library is the best version to use in
their application. The Android SDK contains the latest stable version of this
library. To use it:

1. Make sure you've downloaded the Android Support Repository using the SDK
   Manager.
2. Open the `build.gradle` file for your application.
3. Add the Design support library to the `dependencies` section:

  ```groovy
  dependencies {
    // ...
    compile 'com.android.support:design:25.1.0'
    // ...
  }
  ```

### Building from source

If you'll be contributing to the library, or need a version newer than what has
been released in the Android support libraries, Material Components for Android
can also be built from source. To do so:

Clone this repository:

```sh
git clone https://github.com/material-components/material-components-android.git
```

Then, build an AAR using Gradle:

```sh
./gradlew assembleRelease
```

(the AAR file will be located in `lib/build/outputs/aar/`)

### Running tests

Material Components for Android has JVM tests as well as Emulator tests.

To run the JVM tests, do:

```
./gradlew test
```

To run the emulator tests, ensure you have [a virtual device set
up](https://developer.android.com/studio/run/managing-avds.html) and do:

```
./gradlew connectedAndroidTest
```


