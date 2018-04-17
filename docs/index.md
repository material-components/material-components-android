# Material Components for Android

## Getting Started

### How to use Material Components for Android

Material Components for Android is available through Google's Maven repository.
To use it:

1. Open the `build.gradle` file for your application.
2. Make sure that the `repositories` section includes a maven section with the
`"https://maven.google.com"` endpoint. For example:

  ```groovy
    allprojects {
      repositories {
        jcenter()
        maven {
          url "https://maven.google.com"
        }
      }
    }
  ```
3. Add the library to the `dependencies` section:

  ```groovy
    dependencies {
      // ...
      compile 'com.google.android.material:material:[Library version code]'
      // ...
    }
  ```

### Building from source

If you'll be contributing to the library, or need a version newer than what has
been released in the Android support libraries, Material Components for Android
can also be built from source. To do so:

Clone the repository:

```sh
git clone https://github.com/material-components/material-components-android.git
```

Then, build the library's AARs using Gradle:

```sh
./gradlew uploadArchives -PmavenRepoUrl="file://localhost/<path_to_aars>"
```

This will output AARs and Maven artifacts for each of the library's modules
to the path on your machine, e.g., `$HOME/Desktop/material_aars`.

To use the AARs in your app locally, copy the output from your AAR directory
into your local Maven repository (`~/.m2/repository`). Then add `mavenLocal()`
as a repository in your project's top-level `build.gradle` file. Finally, add
the Design Library dependency as you would normally, using the version
specified as `mdcLibraryVersion` in the library's top-level `build.gradle`
file.

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


