<!--docs:
title: "Material Button Toggle Group"
layout: detail
section: components
excerpt: "A customizable button component with updated visual styles."
iconId: materialbuttontogglegroup
path: /catalog/material-button-toggle-group/
-->

# Material Button Toggle Group

`MaterialButtonToggleGroup`, sometimes referred to as `Toggle Button`, `Toggle
Button Group`, or `Segmented Selector`, is a `ViewGroup` which groups together
several checkable `MaterialButton` child views. The `MaterialButton`s in this
group will be shown on a single line.

`MaterialButtonToggleGroup` adjusts the shape and margins of its child buttons
to give the appearance of a cohesive, common container that all its child
buttons belong to. This is useful for selection controls and forms where related
options should be grouped together. `MaterialButtonToggleGroup` handles the
following appearance changes on child `MaterialButton`s:

-   Removes corner radii and corner shape for all but the leftmost and rightmost
    corners. This means all buttons in the middle of the layout will be
    rectangular in shape.
-   Buttons added to this group are automatically made to be `checkable`. The
    default `MaterialButton` TextButton and OutlinedButton styles have checked
    states specified in their ColorStateLists to support this behavior.
-   If two adjacent button children have a `strokeWidth` greater than 0,
    `MaterialButtonToggleGroup` will set negative margins such that the adjacent
    strokes overlap each other, in order to avoid double-width strokes on
    adjacent buttons.
-   Buttons in the checked state will be drawn on top of buttons in the
    unchecked state, so that the checked stroke color can be seen even with the
    aforementioned stroke overlapping.

`MaterialButtonToggleGroup` has several selection modes, which can be set via
the `app:singleSelection` attribute:

-   `app:singleSelection=false` (default): Multiple buttons within the same
    group can be checked.
-   `app:singleSelection=true`: At most one button within the same group can be
    checked at any time. This functionality is similar to that of a
    `RadioGroup`.

You can specify which button should be checked by default when the component is
initialized by setting the `app:checkedButton` attribute on a
`MaterialButtonToggleGroup` to the ID of the intended child button.

## Design & API Documentation

-   [Material Design guidelines: Toggle Buttons](https://material.io/go/design-buttons#toggle-button)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/MaterialButtonToggleGroup.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/com/google/android/material/button/MaterialButtonToggleGroup)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

`MaterialButtonToggleGroup` currently only supports child views of type
`MaterialButton`. Buttons can be added to this group via XML, as follows:

```xml
<com.google.android.material.button.MaterialButtonToggleGroup
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/toggle_button_group"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content">

    <com.google.android.material.button.MaterialButton
        style="?attr/materialButtonOutlinedStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/button_label_private"/>
    <com.google.android.material.button.MaterialButton
        style="?attr/materialButtonOutlinedStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/button_label_team"/>
    <com.google.android.material.button.MaterialButton
        style="?attr/materialButtonOutlinedStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/button_label_everyone"/>
    <com.google.android.material.button.MaterialButton
        style="?attr/materialButtonOutlinedStyle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/button_label_custom"/>

</com.google.android.material.button.MaterialButtonToggleGroup>
```

You can also add buttons to this view group programmatically via the
`addView(View)` methods.

`MaterialButtonToggleGroup` provides the `OnButtonCheckedListener` to listen to
changes on the checked state of child buttons. Listeners can be added via
`addOnButtonCheckedListener` and removed via `removeOnButtonCheckedListener`.
The `OnButtonCheckedListener` interface has one callback method,
`onButtonChecked(MaterialButtonToggleGroup group, @IdRes int checkedId, boolean
isChecked)`. This callback has a reference to the `MaterialButtonToggleGroup`,
the ID of the child button whose check state changed, and a boolean indicating
whether that child button is currently checked.

## Styling

We aim to be as non-prescriptive as possible when styling child buttons in a
`MaterialButtonToggleGroup`. Other than the appearance changes described in the
first section, this layout keeps the styling of child views as-is. Styling must
applied to each child button individually.

We recommend using the `?attr/materialButtonOutlinedStyle` attribute for all
child buttons. `?attr/materialButtonOutlinedStyle` will most closely match the
Material Design guidelines for this component, and supports the checked state
for child buttons.

## Related Concepts

For more information about styling child `MaterialButton`s, check out the
[MaterialButton](MaterialButton.md) documentation.
