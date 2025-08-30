<!--docs:
title: "Loading indicators"
layout: detail
section: components
excerpt: "Loading indicators express an unspecified wait time of a process."
iconId: loading_indicator
path: /catalog/loading-indicators/
-->

# Loading Indicators

[Loading indicators](https://m3.material.io/components/loading-indicator/overview)
show the progress for a short wait time. It has two configurations.

![Loading indicator configurations](assets/loadingindicator/loadingindicator-configurations.gif)

1.  Default (contained)
2.  Uncontained

**Note:** Images use various dynamic color schemes.

## Design & API documentation

*   [Material 3 (M3) spec](https://m3.material.io/components/loading-indicator/overview)

## Anatomy

![Loading indicator anatomy](assets/loadingindicator/anatomy.png)

1.  Active indicator
2.  Container (optional)

More details on anatomy items in the
[component guidelines](https://m3.material.io/components/loading-indicator/guidelines#a6bb9df2-568a-41d4-a4e9-08ac8f844a7d).

## M3 Expressive

### M3 Expressive update

The loading indicator is a new component added to the library in the M3
Expressive update.

The loading indicator is designed to show progress that loads in under five
seconds. It should replace most uses of the indeterminate circular progress
indicator.
[More on M3 Expressive](https://m3.material.io/blog/building-with-m3-expressive)

**Loading indicators:**

*   Can be contained or uncontained
*   Use shape and motion to capture attention
*   Can scale in size

## Key properties

#### Attributes

Element                       | Attribute             | Related method(s)                             | Default value
----------------------------- | --------------------- | --------------------------------------------- | -------------
**Indicator color**           | `app:indicatorColor`  | `setIndicatorColor`</br>`getIndicatorColor`   | `colorPrimary`
**Container color**           | `app:containerColor`  | `setContainerColor`</br>`getContainerColor`   | `transparent`
**Indicator size**            | `app:indicatorSize`   | `setIndicatorSize`</br>`getIndicatorSize`     | 38dp
**Container width**           | `app:containerWidth`  | `setContainerWidth`</br>`getContainerWidth`   | 48dp
**Container height**          | `app:containerHeight` | `setContainerHeight`</br>`getContainerHeight` | 48dp
**Delay (in ms) to show**     | `app:showDelay`       | N/A                                           | 0
**Min delay (in ms) to hide** | `app:minHideDelay`    | N/A                                           | 0

#### Styles

Element             | Style                                         | Theme attribute
------------------- | ---------------------------------------------  |------------
**Default style**   | `Widget.Material3.LoadingIndicator`            | `?attr/loadingIndicatorStyle`
**Contained style** | `Widget.Material3.LoadingIndicator.Contained`  | N/A

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/loadingindicator/res/values/styles.xml)
and
[attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/loadingindicator/res/values/attrs.xml).

## Code implementation

Before you can use Material loading indicators, you need to add a dependency to
the Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

Loading indicators inform users about the indeterminate ongoing processes, such
as loading an app, submitting a form, or saving updates. They communicate an
app’s state and indicate available actions, such as whether users can navigate
away from the current screen. It's recommended as a replacement for
indeterminate circular progress indicators.

**Note:** Use progress indicators, if the processes can transition from
indeterminate to determinate.

### Adding loading indicators

Loading indicators capture attention through motion. It morphs the shape in a
sequence with potential color change, if multiple colors are specified for the
indicator. A fully rounded container is optionally drawn behind the morphing
shape.

Source code:

*   `LoadingIndicator`
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/loadingindicator/LoadingIndicator.java)

A loading indicator can be added to a layout:

```xml
<!-- Loading indicator (uncontained) -->
<com.google.android.material.loadingindicator.LoadingIndicator
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

![Animation of uncontained loading indicator](assets/loadingindicator/loading-indicator.gif)

```xml
<!-- Loading indicator with a container -->
<com.google.android.material.loadingindicator.LoadingIndicator
    style="@style/Widget.Material3.LoadingIndicator.Contained"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

![Animation of contained loading indicator](assets/loadingindicator/loading-indicator-contained.gif)

### Making loading indicators accessible

Loading indicators have limited support for user interactions. Please
consider setting the content descriptor for use with screen readers.

That can be done in XML via the `android:contentDescription` attribute or
programmatically like so:

```kt
loadingIndicator.contentDescription = contentDescription
```

For contained loading indicators, please ensure the indicator color and the
container color have enough contrast (3:1).
