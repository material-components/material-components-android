<!--docs:
title: "Canonical Layouts"
layout: detail
section: adaptive
excerpt: "Guide for building the adaptive canonical layouts"
path: /adaptive/canonicallayouts/
-->

# Canonical Layouts

**Note:** This doc is in progress and will be updated with more information
soon.

The canonical layout demos found in the
[MDC catalog](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/)
are examples of adaptive layouts where components and views change depending on
device configuration, such as screen size, orientation, and/or presence of a
physical fold.

The catalog's
[Adaptive demo](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/)
has the following canonical layout implementations:

*   List View demo
    *   [`AdaptiveListViewDemoActivity`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveListViewDemoActivity.java)
    *   [`AdaptiveListViewDemoFragment`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveListViewDemoFragment.java)
    *   [`AdaptiveListViewDetailDemoFragment`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveListViewDemoFragment.java)
*   Feed demo
    *   [`AdaptiveFeedDemoActivity`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveFeedDemoActivity.java)
    *   [`AdaptiveFeedDemoFragment`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveFeedDemoFragment.java)
*   Single View Hero demo
    *   [`AdaptiveHeroDemoActivity`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveHeroDemoActivity.java)
    *   [`AdaptiveHeroDemoFragment`](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/adaptive/AdaptiveHeroDemoFragment.java)

## Libraries and APIs

To use the Material library, you will need to add a dependency to the Material
Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

The AndroidX
[ConstraintLayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout)
and
[WindowManager](https://developer.android.com/jetpack/androidx/releases/window)
libraries are used to achieve the flexibility of the layouts. For more
information about them, it is suggested that you read the following:

*   [Get started with large screens](https://developer.android.com/guide/topics/ui/responsive-layout-overview)
*   [Build a Responsive UI with ConstraintLayout](https://developer.android.com/training/constraint-layout)
*   [Designing for foldables](https://developer.android.com/training/constraint-layout/foldables)
