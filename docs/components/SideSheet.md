<!--docs:
title: "Side Sheets"
layout: detail
section: components
excerpt: "Side sheets slide in from the side of the screen to reveal more content."
iconId: side_sheet
path: /catalog/side-sheet-behavior/
-->

# Side Sheets

[Side sheets](https://material.io/components/sheets-side) are surfaces
containing supplementary content that are anchored to the side of the screen.

See [Bottom Sheet documentation](BottomSheet.md) for documentation about
[bottom sheets](https://m3.material.io/components/bottom-sheets/overview).

**Contents**

*   [Using side sheets](#using-side-sheets)
*   [Standard side sheet](#standard-side-sheet)
*   [Anatomy and key properties](#anatomy-and-key-properties)
*   [Theming](#theming-side-sheets)

## Using side sheets

Before you can use Material side sheets, you need to add a dependency to the
Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

Standard side sheet basic usage:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
  ...>

  <FrameLayout
    ...
    android:id="@+id/standard_side_sheet"
    app:layout_behavior="com.google.android.material.sidesheet.SideSheetBehavior">

    <!-- Side sheet content. -->

  </FrameLayout>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Setting behavior

There are several attributes that can be used to adjust the behavior of
standard side sheets.

Behavior attributes can be applied to standard side sheets in xml by setting
them on a child `View` set to `app:layout_behavior`, or programmatically:

```kt
val standardSideSheetBehavior = SideSheetBehavior.from(standardSideSheet)
// Use this to programmatically apply behavior attributes
```

More information about these attributes and their default values is available in
the [behavior attributes](#behavior-attributes) section.

### Setting state

Standard side sheets have the following states:

*   `STATE_EXPANDED`: The side sheet is visible at its maximum height and it
    is neither dragging nor settling (see below).
*   `STATE_HIDDEN`: The side sheet is no longer visible and can only be
    re-shown programmatically.
*   `STATE_DRAGGING`: The user is actively dragging the side sheet.
*   `STATE_SETTLING`: The side sheet is settling to a specific height after a
    drag/swipe gesture. This will be the peek height, expanded height, or 0, in
    case the user action caused the side sheet to hide.

You can set a state on the side sheet:

```kt
sideSheetBehavior.state = Sheet.STATE_HIDDEN
```

**Note:** `STATE_SETTLING` and `STATE_DRAGGING` should not be set programmatically.

## Standard side sheet

Standard side sheets co-exist with the screen’s main UI region and allow for
simultaneously viewing and interacting with both regions. They are commonly used
to keep a feature or secondary content visible on screen when content in the
main UI region is frequently scrolled or panned.

`SideSheetBehavior` is applied to a child of
[CoordinatorLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout)
to make that child a **standard side sheet**, which is a view that comes up
from the side of the screen, elevated over the main content. It can be dragged
vertically to expose more or less content.

API and source code:

*   `SideSheetBehavior`
  *   [Class definition](https://developer.android.com/reference/com/google/android/material/sidesheet/SideSheetBehavior)
  *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/sidesheet/SideSheetBehavior.java)

### Standard side sheet example

The following example shows a standard side sheet in its collapsed and
expanded states:

`SideSheetBehavior` works in tandem with `CoordinatorLayout` to let you
display content in a side sheet, perform enter/exit animations, respond to
dragging/swiping gestures, and more.

Apply the `SideSheetBehavior` to a direct child `View` of `CoordinatorLayout`:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
  ...>

  <LinearLayout
    android:id="@+id/standard_side_sheet"
    style="@style/Widget.Material3.SideSheet"
    android:layout_width="256dp"
    android:layout_height="match_parent"
    android:orientation="vertical"
    app:layout_behavior="com.google.android.material.sidesheet.SideSheetBehavior">

    <!-- Side sheet contents. -->
    <TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/title"
    .../>

    <TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/supporting_text"
    .../>

    <Button
    android:id="@+id/sidesheet_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/action"
    .../>

  </LinearLayout>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

In this example, the side sheet is the `LinearLayout`.

## Anatomy and key properties

Side sheets have a sheet and content.

### Sheet attributes

Element        | Attribute             | Related method(s)                 | Default value
-------------- | --------------------- | --------------------------------- | -------------
**Color**      | `app:backgroundTint`  | N/A                               | `?attr/colorSurface`
**Shape**      | `app:shapeAppearance` | N/A                               | `?attr/shapeAppearanceLargeComponent`
**Elevation**  | `android:elevation`   | N/A                               | 0dp
**Max width**  | `android:maxWidth`    | `setMaxWidth`<br/>`getMaxWidth`   | N/A
**Max height** | `android:maxHeight`   | `setMaxHeight`<br/>`getMaxHeight` | N/A

### Behavior attributes

More info about these attributes and how to use them in the
[setting behavior](#setting-behavior) section.

Behavior                                    | Related method(s)                                                         | Default value
------------------------------------------- | ------------------------------------------------------------------------- | -------------
`app:behavior_draggable`                    | `setDraggable`<br/>`isDraggable`                                          | `true`

### Styles

**Element**               | **Value**
------------------------- | -------------------------------------------
Standard side sheet style | `@style/Widget.Material3.SideSheet`

Note: There is no default style theme attribute for standard
side sheets, because `SideSheetBehavior`s don't have a designated associated
`View`.

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/sidesheet/res/values/styles.xml),
and
[attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/sidesheet/res/values/attrs.xml).

## Theming side sheets

Side sheets support
[Material Theming](https://material.io/components/sheets-side#theming), which
can customize color and shape.

### Side sheet theming example

API and source code:

*   `SideSheetBehavior`
  *   [Class definition](https://developer.android.com/reference/com/google/android/material/sidesheet/SideSheetBehavior)
  *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/sidesheet/SideSheetBehavior.java)
