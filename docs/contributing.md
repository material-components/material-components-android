<!--docs:
title: "Contributing"
layout: landing
section: docs
path: /docs/contributing/
-->

# General Contributing Guidelines

The Material Components contributing policies and procedures can be found in the
main Material Components documentation repository’s
[contributing page](https://github.com/material-components/material-components/blob/develop/CONTRIBUTING.md).

For larger feature requests we might ask you to write a
[Design Doc](https://docs.google.com/document/d/1ISW8sVEQpAs1X-pQ0zf2q3Sbz5NRS8jfjs-jnjo9iWk/edit).

To make a contribution, you'll need to be able to build the library from source
and run our tests.

## Pull Request Process

Because the material-components-android code is stored in two locations (i.e.,
GitHub and Google), PRs are not directly merged into the repository. Instead,
once a PR is complete (i.e., cla signed, CI passing, design reviewed, code
reviewed), the PR will be converted to a commit sourced to the original author
that is synced into the repository. Even though the PR list shows no merged PRs,
we do accept contributions.

## Building From Source

Take a look at our [instructions](building-from-source.md) on how to build the
library from source.

## Running Tests

Material Components for Android has JVM tests as well as Emulator tests.

To run the JVM tests, do:

```sh
./gradlew test
```

To run the emulator tests, ensure you have
[a virtual device set up](https://developer.android.com/studio/run/managing-avds.html)
and do:

```sh
./gradlew connectedAndroidTest
```

## Code Conventions

Since we all want to spend more time coding and less time fiddling with
whitespace, Material Components for Android uses code conventions and styles to
encourage consistency. Code with a consistent style is easier (and less
error-prone!) to review, maintain, and understand.

### Be consistent

If the style guide is not explicit about a particular situation, the cardinal
rule is to **be consistent**. For example, take a look at the surrounding code
and follow its lead, or look for similar cases elsewhere in the codebase.

### Java

We follow the
[Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

### XML

-   2 space indentation
-   Resource naming (including IDs) is `lowercase_with_underscores`
-   Attribute ordering:
    1.  `xmlns:android`
    2.  other `xmlns:`
    3.  `android:id`
    4.  `style`
    5.  `android:layout_` attributes
    6.  `android:padding` attributes
    7.  other `android:` attributes
    8.  `app:` attributes
    9.  `tool:` attributes

## Useful Links

-   [Getting Started](getting-started.md)
-   [Using Snapshot Version](using-snapshot-version.md)
-   [Building From Source](building-from-source.md)
-   [Catalog App](catalog-app.md)
-   [Class documentation](https://developer.android.com/reference/com/google/android/material/classes)
-   [MDC-Android on Stack Overflow](https://www.stackoverflow.com/questions/tagged/material-components+android)
-   [Android Developer’s Guide](https://developer.android.com/training/material/index.html)
-   [Material.io](https://www.material.io)
-   [Material Design Guidelines](https://material.google.com)
