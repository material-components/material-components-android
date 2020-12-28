<!--docs:
title: "Building From Source"
layout: landing
section: docs
path: /docs/building-from-source/
-->

# Building From the Latest Source

If you'll be contributing to the library, or need a version newer than what has
been released, Material Components for Android can also be built from source.
To do so:

Clone the repository:

```sh
git clone https://github.com/material-components/material-components-android.git
```

Then, build the library's AARs using Gradle:

```sh
./gradlew publish -PmavenRepoUrl="file://localhost/<path_to_aars>"
```

This will output AARs and Maven artifacts for each of the library's modules
to the path on your machine, e.g., `$HOME/Desktop/material_aars`.

To use the AARs in your app locally, copy the output from your AAR directory
into your local Maven repository (`~/.m2/repository`). Then add `mavenLocal()`
as a repository in your project's top-level `build.gradle` file. Finally, add
the Design Library dependency as you would normally, using the version
specified as `mdcLibraryVersion` in the library's top-level `build.gradle`
file.

## Useful Links

-   [Getting Started](getting-started.md)
-   [Contributing](contributing.md)
-   [Catalog App](catalog-app.md)
-   [Using Snapshot Version](using-snapshot-version.md)
-   [Class documentation](https://developer.android.com/reference/com/google/android/material/classes)
-   [MDC-Android on Stack Overflow](https://www.stackoverflow.com/questions/tagged/material-components+android)
-   [Android Developer’s Guide](https://developer.android.com/training/material/index.html)
-   [Material.io](https://www.material.io)
-   [Material Design Guidelines](https://material.google.com)
