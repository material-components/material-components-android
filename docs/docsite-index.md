<!--docs:
# This file is used by the docsite to generate the platform index page.
title: "Material Components for Android"
layout: "homepage"
path: /
-->

# Material Components for Android

Material Components for Android provides modular and customizable UI components
to help developers easily create beautiful apps.

## Usage

1. To use the Material Components library with the Gradle build system, include
the library in the build.gradle dependencies for your app.

  ```groovy
  dependencies {
    compile 'com.android.support:design:[Library version code]'
  }
  ```
2. Add a reference to the component (widget) that you want to use in your XML
layout. (You can also dynamically instantiate a widget in Java.)

  ```xml
  <android.support.design.widget.FloatingActionButton android:id="@id/fab" />
  ```

2. You can then reference that widget in your Java class.

  ```java
  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
  ```

3. Make sure you have an import statement for the component. Most IDEs add and
organize import statements by default.

  ```java
  import android.support.design.widget.FloatingActionButton;
  ```

## What's next?

*   [View the components](https://github.com/material-components/material-components-android/tree/master/lib/)
*   [Contributing](g3doc/contributing.md)
*   [Class documentation](https://developer.android.com/reference/android/support/design/widget/package-summary.html)
*   [MDC-Android on Stack Overflow](https://www.stackoverflow.com/questions/tagged/material-components+android)
*   [Android Developer’s Guide](https://developer.android.com/training/material/index.html)
*   [Material.io](https://www.material.io)
*   [Material Design Guidelines](https://material.google.com)
