<!--docs:
title: "Using Snapshot Version"
layout: landing
section: docs
path: /docs/using-snapshot-version/
-->

# Using a Snapshot Version of the Library

If you would like to depend on the cutting edge version of the MDC-Android
library, you can use the
[snapshot versions](https://github.com/material-components/material-components-android/packages/81484)
that are published daily via
[GitHub Packages](https://help.github.com/en/packages/publishing-and-managing-packages/about-github-packages).

To do so, you need to
[create a GitHub access token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line#creating-a-token),
and add the following to your `build.gradle` Maven repositories:

```groovy
maven {
    name = "MaterialSnapshots"
    url = uri("https://maven.pkg.github.com/material-components/material-components-android")
    credentials {
        username = <github_username>
        password = <github_access_token>
    }
}
```

Then you can use a snapshot version by adding a
`com.google.android.material:material:1.3.0-dev-<date>` dependency as per usual,
replacing `<date>` with the date of the version you are interested in (see all
versions
[here](https://github.com/material-components/material-components-android/packages/81484/versions)).
See the offical doc on
[Configuring Gradle for use with GitHub Packages](https://help.github.com/en/github/managing-packages-with-github-packages/configuring-gradle-for-use-with-github-packages)
for additional information.

Alternatively, you could use
[JitPack](https://jitpack.io/#material-components/material-components-android)
to generate library releases based on specific commits.

## Useful Links

-   [Getting Started](getting-started.md)
-   [Contributing](contributing.md)
-   [Building From Source](building-from-source.md)
-   [Catalog App](catalog-app.md)
-   [Class documentation](https://developer.android.com/reference/com/google/android/material/classes)
-   [MDC-Android on Stack Overflow](https://www.stackoverflow.com/questions/tagged/material-components+android)
-   [Android Developer’s Guide](https://developer.android.com/training/material/index.html)
-   [Material.io](https://www.material.io)
-   [Material Design Guidelines](https://material.google.com)
