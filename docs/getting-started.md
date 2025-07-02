<!--docs:
title: "Getting Started"
layout: landing
section: docs
path: /docs/getting-started/
-->

# Getting started with Material components for Android

## 1. Migration guidance

Take a look at our [guide](https://material.io/blog/migrating-material-3) and
[codelab](https://goo.gle/apply-dynamic-color) to help you migrate your codebase
using Material Components for Android to the new Material 3 system.

Additionally, if you are still using the legacy Design Support Library, take a
look at our
[legacy guide](https://material.io/blog/migrate-android-material-components) to
help you migrate your codebase to Material Components for Android.

## 2. Maven library dependency

Material Components for Android is available through Google's Maven Repository.
To use it:

1.  Open the `build.gradle` file for your application.
2.  Make sure that the `repositories` section includes Google's Maven Repository
    `google()`. For example:

    ```groovy
      allprojects {
        repositories {
          google()
          mavenCentral()
        }
      }
    ```

3.  Add the library to the `dependencies` section:

    ```groovy
      dependencies {
        // ...
        implementation 'com.google.android.material:material:<version>'
        // ...
      }
    ```

Visit
[Google's Maven Repository](https://maven.google.com/web/index.html#com.google.android.material:material)
or
[MVN Repository](https://mvnrepository.com/artifact/com.google.android.material/material)
to find the latest version of the library.

**Note:** In order to use the new `Material3` themes and component styles, you
should depend on version `1.5.0` or later.

**Note:** In order to use the new `Material3Expressive` themes and component
styles, you should depend on version `1.14.0` or later.

### New Namespace and AndroidX

If your app currently depends on the original Design Support Library, you can
make use of the
[`Refactor to AndroidX…`](https://developer.android.com/jetpack/androidx/migrate)
option provided by Android Studio. Doing so will update your app's dependencies
and code to use the newly packaged `androidx` and `com.google.android.material`
libraries.

If you don't want to switch over to the new `androidx` and
`com.google.android.material` packages yet, you can use Material Components via
the `com.android.support:design:28.0.0` dependency.

**Note:** You should not use the `com.android.support` and
`com.google.android.material` dependencies in your app at the same time.

### Non-Transitive R Classes (referencing library resources programmatically)

Starting with version `1.13.0-alpha12`, the Material library is built with AGP
8.7.3 (or later) and `android.nonTransitiveRClass=true`, meaning
[R classes are no longer transitive](https://developer.android.com/build/optimize-your-build#use-non-transitive-r-classes)
and resources must be fully qualified with their library path when used
programmatically.

For example, since `colorPrimary` is defined in the AppCompat library, you must
refer to it as `androidx.appcompat.R.attr.colorPrimary` as opposed to
`com.google.android.material.R.attr.colorPrimary` or `R.attr.colorPrimary`.

For a Material defined resource like `colorOnPrimary`, you must refer to it as
`com.google.android.material.R.attr.colorOnPrimary`.

To opt out of this new behavior, set `android.nonTransitiveRClass=false` in your
`gradle.properties` file. Then you can access any resource without a fully
qualified path (i.e., simply `R.<resource-type>.<resource-name>`).

**Note:** This is relevant for all types of library resources, not just
attribute references.

## 3. Android SDK compilation

In order to use the latest versions of Material Components for Android and the
AndroidX Jetpack libraries, you will have to install the latest version of
Android Studio and update your app to meet the following requirements:

-   `compileSdkVersion` -> `35` or later (see the
    [Android 15 app migration guide](https://developer.android.com/about/versions/15/migration))
-   `minSdkVersion` -> `21` or later

## 4. Java 8 compilation

The latest Material and AndroidX Jetpack libraries now require your app to be
compiled with Java 8. See the
[Java 8 language features and APIs documentation](https://developer.android.com/studio/write/java8-support)
for more information on Java 8 support and how to enable it for your app.

## 5. Gradle, AGP, and Android Studio

When using MDC-Android version `1.13.0` and above, you will need to make sure
your project is built with the following minimum requirements, in order to
support the latest build features such as XML `macro`:

-   [Gradle version 8.9](https://developer.android.com/studio/releases/gradle-plugin#updating-gradle)
-   [Android Gradle Plugin (AGP) version 8.7.3](https://developer.android.com/studio/releases/gradle-plugin#updating-gradle)
-   [Android Studio Ladybug, version 2024.2.1](https://developer.android.com/studio/releases/gradle-plugin#android_gradle_plugin_and_android_studio_compatibility)

## 6. `AppCompatActivity`

Use `AppCompatActivity` to ensure that all the components work correctly. If you
are unable to extend from `AppCompatActivity`, update your activities to use
[`AppCompatDelegate`](https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate).
This will enable the AppCompat or Material versions of components to be inflated
(depending on your theme), among other important things.

## 7. `Material3` theme inheritance

We recommend you perform an app-wide migration by changing your app theme to
inherit from a `Material3` theme. Be sure to test thoroughly afterwards, since
this may change the appearance and behavior of existing layout components.

Check out the new Material Theme Builder which can be used to generate your
`Material3` app theme, with all of the Material Color System roles filled out
based on your brand colors.

-   [Web](https://material.io/material-theme-builder)
-   [Figma](https://goo.gle/material-theme-builder-figma)

**Note:** If you **can't** change your theme, you can continue to inherit from
an `AppCompat` or `MaterialComponents` theme and add some new theme attributes
to your theme. See the
[**AppCompat or MaterialComponents themes**](#appcompat-or-materialcomponents-themes)
section for more details.

### **`Material3Expressive` themes**

**Note:** You must depend on library version `1.14.0-alpha01` or later to use
`Theme.Material3Expressive.*` themes, which are required for
`Widget.Material3Expressive.*` component styles.

Here are the `Material3Expressive` themes you can use to get the latest
component styles and theme-level attributes, as well as their `Material3`
equivalents when applicable.

`Material3Expressive`                                          | `Material3`
-------------------------------------------------------------- | -----------
`Theme.Material3Expressive.Light`                              | `Theme.Material3.Light`
`Theme.Material3Expressive.Light.NoActionBar`                  | `Theme.Material3.Light.NoActionBar`
`Theme.Material3Expressive.Dark`                               | `Theme.Material3.Dark`
`Theme.Material3Expressive.Dark.NoActionBar`                   | `Theme.Material3.Dark.NoActionBar`
`Theme.Material3Expressive.DayNight`                           | `Theme.Material3.DayNight`
`Theme.Material3Expressive.DayNight.NoActionBar`               | `Theme.Material3.DayNight.NoActionBar`
`Theme.Material3Expressive.DynamicColors.Light`                | `Theme.Material3.DynamicColors.Light`
`Theme.Material3Expressive.DynamicColors.Light.NoActionBar`    | `Theme.Material3.DynamicColors.Light.NoActionBar`
`Theme.Material3Expressive.DynamicColors.Dark`                 | `Theme.Material3.DynamicColors.Dark`
`Theme.Material3Expressive.DynamicColors.Dark.NoActionBar`     | `Theme.Material3.DynamicColors.Dark.NoActionBar`
`Theme.Material3Expressive.DynamicColors.DayNight`             | `Theme.Material3.DynamicColors.DayNight`
`Theme.Material3Expressive.DynamicColors.DayNight.NoActionBar` | `Theme.Material3.DynamicColors.DayNight.NoActionBar`

### **`Material3` themes**

Here are the `Material3` themes you can use to get the latest component styles
and theme-level attributes, as well as their `MaterialComponents` equivalents
when applicable.

`Material3`                                          | `MaterialComponents`
---------------------------------------------------- | --------------------
`Theme.Material3.Light`                              | `Theme.MaterialComponents.Light`
`Theme.Material3.Light.NoActionBar`                  | `Theme.MaterialComponents.Light.NoActionBar`
`Theme.Material3.Dark`                               | `Theme.MaterialComponents`
`Theme.Material3.Dark.NoActionBar`                   | `Theme.MaterialComponents.NoActionBar`
`Theme.Material3.DayNight`                           | `Theme.MaterialComponents.DayNight`
`Theme.Material3.DayNight.NoActionBar`               | `Theme.MaterialComponents.DayNight.NoActionBar`
`Theme.Material3.DynamicColors.Light`                | N/A
`Theme.Material3.DynamicColors.Light.NoActionBar`    | N/A
`Theme.Material3.DynamicColors.Dark`                 | N/A
`Theme.Material3.DynamicColors.Dark.NoActionBar`     | N/A
`Theme.Material3.DynamicColors.DayNight`             | N/A
`Theme.Material3.DynamicColors.DayNight.NoActionBar` | N/A
N/A                                                  | `Theme.MaterialComponents.Light.DarkActionBar`
N/A                                                  | `Theme.MaterialComponents.DayNight.DarkActionBar`

Update your app theme to inherit from one of these themes:

```xml
<style name="Theme.MyApp" parent="Theme.Material3.DayNight.NoActionBar">
    <!-- ... -->
</style>
```

For more information on how to set up theme-level attributes for your app, take
a look at our [Theming](theming.md) guide, as well as our
[Dark Theme](theming/Dark.md) guide for why it's important to inherit from the
`DayNight` theme.

`Material3` themes enable a custom view inflater, which replaces default
components with their Material counterparts. Currently, this replaces the
following XML components:

*   `<Button` → [`MaterialButton`](components/Button.md)
*   `<CheckBox` → [`MaterialCheckBox`](components/Checkbox.md)
*   `<RadioButton` → [`MaterialRadioButton`](components/RadioButton.md)
*   `<TextView` → [`MaterialTextView`](components/MaterialTextView.md)
*   `<AutoCompleteTextView` →
    [`MaterialAutoCompleteTextView`](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/textfield/MaterialAutoCompleteTextView.java)

### **`AppCompat` or `MaterialComponents` Themes**

You can incrementally test new Material components without changing your app
theme. This allows you to keep your existing layouts looking and behaving the
same, while introducing new components to your layout one at a time.

However, you must add the following new theme attributes to your existing app
theme, or you will encounter `ThemeEnforcement` errors:

```xml
<style name="Theme.MyApp" parent="Theme.AppCompat OR Theme.MaterialComponents">

  <!-- Original AppCompat attributes. -->
  <item name="colorPrimary">@color/my_app_primary</item>
  <item name="colorPrimaryDark">@color/my_app_primary_dark</item>
  <item name="colorSecondary">@color/my_app_secondary</item>
  <item name="android:colorBackground">@color/my_app_background</item>
  <item name="colorError">@color/my_app_error</item>

  <!-- MaterialComponents attributes (needed if parent="Theme.AppCompat"). -->
  <item name="colorPrimaryVariant">@color/my_app_primary_variant</item>
  <item name="colorSecondaryVariant">@color/my_app_secondary_variant</item>
  <item name="colorSurface">@color/my_app_surface</item>
  <item name="colorOnPrimary">@color/my_app_on_primary</item>
  <item name="colorOnSecondary">@color/my_app_on_secondary</item>
  <item name="colorOnBackground">@color/my_app_on_background</item>
  <item name="colorOnError">@color/my_app_on_error</item>
  <item name="colorOnSurface">@color/my_app_on_surface</item>
  <item name="scrimBackground">@color/mtrl_scrim</item>
  <item name="textAppearanceHeadline1">@style/TextAppearance.MaterialComponents.Headline1</item>
  <item name="textAppearanceHeadline2">@style/TextAppearance.MaterialComponents.Headline2</item>
  <item name="textAppearanceHeadline3">@style/TextAppearance.MaterialComponents.Headline3</item>
  <item name="textAppearanceHeadline4">@style/TextAppearance.MaterialComponents.Headline4</item>
  <item name="textAppearanceHeadline5">@style/TextAppearance.MaterialComponents.Headline5</item>
  <item name="textAppearanceHeadline6">@style/TextAppearance.MaterialComponents.Headline6</item>
  <item name="textAppearanceSubtitle1">@style/TextAppearance.MaterialComponents.Subtitle1</item>
  <item name="textAppearanceSubtitle2">@style/TextAppearance.MaterialComponents.Subtitle2</item>
  <item name="textAppearanceBody1">@style/TextAppearance.MaterialComponents.Body1</item>
  <item name="textAppearanceBody2">@style/TextAppearance.MaterialComponents.Body2</item>
  <item name="textAppearanceCaption">@style/TextAppearance.MaterialComponents.Caption</item>
  <item name="textAppearanceButton">@style/TextAppearance.MaterialComponents.Button</item>
  <item name="textAppearanceOverline">@style/TextAppearance.MaterialComponents.Overline</item>

  <!-- Material3 attributes (needed if parent="Theme.MaterialComponents"). -->
  <item name="colorPrimaryInverse">@color/my_app_primary_inverse</item>
  <item name="colorPrimaryContainer">@color/my_app_primary_container</item>
  <item name="colorOnPrimaryContainer">@color/my_app_on_primary_container</item>
  <item name="colorSecondaryContainer">@color/my_app_secondary_container</item>
  <item name="colorOnSecondaryContainer">@color/my_app_on_secondary_container</item>
  <item name="colorTertiary">@color/my_app_tertiary</item>
  <item name="colorOnTertiary">@color/my_app_on_tertiary</item>
  <item name="colorTertiaryContainer">@color/my_app_tertiary_container</item>
  <item name="colorOnTertiaryContainer">@color/my_app_on_tertiary_container</item>
  <item name="colorSurfaceVariant">@color/my_app_surface_variant</item>
  <item name="colorOnSurfaceVariant">@color/my_app_on_surface_variant</item>
  <item name="colorSurfaceInverse">@color/my_app_inverse_surface</item>
  <item name="colorOnSurfaceInverse">@color/my_app_inverse_on_surface</item>
  <item name="colorOutline">@color/my_app_outline</item>
  <item name="colorErrorContainer">@color/my_app_error_container</item>
  <item name="colorOnErrorContainer">@color/my_app_on_error_container</item>
  <item name="textAppearanceDisplayLarge">@style/TextAppearance.Material3.DisplayLarge</item>
  <item name="textAppearanceDisplayMedium">@style/TextAppearance.Material3.DisplayMedium</item>
  <item name="textAppearanceDisplaySmall">@style/TextAppearance.Material3.DisplaySmall</item>
  <item name="textAppearanceHeadlineLarge">@style/TextAppearance.Material3.HeadlineLarge</item>
  <item name="textAppearanceHeadlineMedium">@style/TextAppearance.Material3.HeadlineMedium</item>
  <item name="textAppearanceHeadlineSmall">@style/TextAppearance.Material3.HeadlineSmall</item>
  <item name="textAppearanceTitleLarge">@style/TextAppearance.Material3.TitleLarge</item>
  <item name="textAppearanceTitleMedium">@style/TextAppearance.Material3.TitleMedium</item>
  <item name="textAppearanceTitleSmall">@style/TextAppearance.Material3.TitleSmall</item>
  <item name="textAppearanceBodyLarge">@style/TextAppearance.Material3.BodyLarge</item>
  <item name="textAppearanceBodyMedium">@style/TextAppearance.Material3.BodyMedium</item>
  <item name="textAppearanceBodySmall">@style/TextAppearance.Material3.BodySmall</item>
  <item name="textAppearanceLabelLarge">@style/TextAppearance.Material3.LabelLarge</item>
  <item name="textAppearanceLabelMedium">@style/TextAppearance.Material3.LabelMedium</item>
  <item name="textAppearanceLabelSmall">@style/TextAppearance.Material3.LabelSmall</item>
  <item name="shapeAppearanceSmallComponent">@style/ShapeAppearance.Material3.SmallComponent</item>
  <item name="shapeAppearanceMediumComponent">@style/ShapeAppearance.Material3.MediumComponent</item>
  <item name="shapeAppearanceLargeComponent">@style/ShapeAppearance.Material3.LargeComponent</item>
</style>
```

## 8. Add Material components

Take a look at our
[documentation](https://material.io/components?platform=android) for the full
list of available Material components. Each component's page has specific
instructions on how to implement it in your app.

Let's use [text fields](components/TextField.md) as an example.

### **Implementing a text field via XML**

The default
[outlined text field](https://material.io/go/design-text-fields#outlined-text-field)
XML is defined as:

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/textfield_label">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>
</com.google.android.material.textfield.TextInputLayout>
```

**Note:** If you are **not** using a theme that inherits from a `Material3`
theme, you will have to specify the text field style as well, via
`style="@style/Widget.Material3.TextInputLayout.OutlinedBox"`

Other text field styles are also provided. For example, if you want a
[filled text field](https://material.io/go/design-text-fields#filled-text-field)
in your layout, you can apply the `Material3` `filled` style to the text field
in XML:

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.FilledBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/textfield_label">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>
</com.google.android.material.textfield.TextInputLayout>
```

## Contributors

Material Components for Android welcomes contributions from the community. Check
out our [contributing guidelines](contributing.md) as well as an overview of the
[directory structure](directorystructure.md) before getting started.

## Useful Links

-   [Theming Guide](theming.md)
-   [Contributing](contributing.md)
-   [Using Snapshot Version](using-snapshot-version.md)
-   [Building From Source](building-from-source.md)
-   [Catalog App](catalog-app.md)
-   [Class documentation](https://developer.android.com/reference/com/google/android/material/classes)
-   [MDC-Android on Stack Overflow](https://www.stackoverflow.com/questions/tagged/material-components+android)
-   [Android Developer’s Guide](https://developer.android.com/training/material/index.html)
-   [Material.io](https://www.material.io)
-   [Material Design Guidelines](https://material.google.com)
