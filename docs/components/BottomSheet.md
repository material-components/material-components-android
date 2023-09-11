<!--docs:
title: "Bottom Sheets"
layout: detail
section: components
excerpt: "Bottom sheets slide up from the bottom of the screen to reveal more content."
iconId: bottom_sheet
path: /catalog/bottom-sheet-behavior/
-->

# Bottom Sheets

[Bottom sheets](https://material.io/components/sheets-bottom) are surfaces
containing supplementary content that are anchored to the bottom of the screen.

![Example bottom sheet: modal bottom sheet](assets/bottomsheet/bottomsheet_hero.png)

**Contents**

*   [Design & API Documentation](#design-api-documentation)
*   [Using bottom sheets](#using-bottom-sheets)
*   [Standard bottom sheet](#standard-bottom-sheet)
*   [Modal bottom sheet](#modal-bottom-sheet)
*   [Anatomy and key properties](#anatomy-and-key-properties)
*   [Predictive Back](#predictive-back)
*   [Theming](#theming-bottom-sheets)

## Design & API Documentation

*   [Google Material3 Spec](https://material.io/components/bottom-sheets/overview)
*   [API Reference](https://developer.android.com/reference/com/google/android/material/bottomsheet/package-summary)

## Using bottom sheets

Before you can use Material bottom sheets, you need to add a dependency to the
Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

Standard bottom sheet basic usage:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
  ...>

  <FrameLayout
    ...
    android:id="@+id/standard_bottom_sheet"
    app:layout_behavior="com.google.android.material.bottomsheet.BottomSheetBehavior">

    <!-- Bottom sheet contents. -->

  </FrameLayout>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

Modal bottom sheet basic usage:

```kt
class ModalBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_bottom_sheet_content, container, false)

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}

class MainActivity : AppCompatActivity() {
    ...
    val modalBottomSheet = ModalBottomSheet()
    modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
    ...
}
```

More information on each individual section, below.

### Setting behavior

There are several attributes that can be used to adjust the behavior of both
standard and modal bottom sheets.

Behavior attributes can be applied to standard bottom sheets in xml by setting
them on a child `View` set to `app:layout_behavior`, or programmatically:

```kt
val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
// Use this to programmatically apply behavior attributes
```

Behavior attributes can be applied to modal bottom sheets using app-level theme
attributes and styles:

```xml
<style name="ModalBottomSheet" parent="Widget.Material3.BottomSheet.Modal">
  <!-- Apply attributes here -->
</style>

<style name="ModalBottomSheetDialog" parent="ThemeOverlay.Material3.BottomSheetDialog">
  <item name="bottomSheetStyle">@style/ModalBottomSheet</item>
</style>

<style name="AppTheme" parent="Theme.Material3.*">
  <item name="bottomSheetDialogTheme">@style/ModalBottomSheetDialog</item>
</style>
```

Or programmatically:

```kt
val modalBottomSheetBehavior = (modalBottomSheet.dialog as BottomSheetDialog).behavior
// Use this to programmatically apply behavior attributes
```

More information about these attributes and their default values is available in
the [behavior attributes](#behavior-attributes) section.

### Retaining behavior on configuration change

In order to save and restore specific behaviors of the bottom sheet on
configuration change, the following flags can be set (or combined with bitwise
OR operations):

*   `SAVE_PEEK_HEIGHT`: `app:behavior_peekHeight` is preserved.
*   `SAVE_HIDEABLE`: `app:behavior_hideable` is preserved.
*   `SAVE_SKIP_COLLAPSED`: `app:behavior_skipCollapsed` is preserved.
*   `SAVE_FIT_TO_CONTENTS`: `app:behavior_fitToContents` is preserved.
*   `SAVE_ALL`: All aforementioned attributes are preserved.
*   `SAVE_NONE`: No attribute is preserved. This is the default value.

Behaviors can also be set in code:

```kt
bottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
```

Or in xml using the `app:behavior_saveFlags` attribute.

### Setting state

Standard and modal bottom sheets have the following states:

*   `STATE_COLLAPSED`: The bottom sheet is visible but only showing its peek
    height. This state is usually the 'resting position' of a bottom sheet, and
    should have enough height to indicate there is extra content for the user to
    interact with.
*   `STATE_EXPANDED`: The bottom sheet is visible at its maximum height and it
    is neither dragging nor settling (see below).
*   `STATE_HALF_EXPANDED`: The bottom sheet is half-expanded (only applicable if
    `behavior_fitToContents` has been set to false), and is neither dragging nor
    settling (see below).
*   `STATE_HIDDEN`: The bottom sheet is no longer visible and can only be
    re-shown programmatically.
*   `STATE_DRAGGING`: The user is actively dragging the bottom sheet up or down.
*   `STATE_SETTLING`: The bottom sheet is settling to a specific height after a
    drag/swipe gesture. This will be the peek height, expanded height, or 0, in
    case the user action caused the bottom sheet to hide.

You can set a state on the bottom sheet:

```kt
bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
```

**Note:** `STATE_SETTLING` and `STATE_DRAGGING` should not be set programmatically.

### Listening to state and slide changes

A `BottomSheetCallback` can be added to a `BottomSheetBehavior`:

```kt
val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        // Do something for new state.
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        // Do something for slide offset.
    }
}

// To add the callback:
bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)

// To remove the callback:
bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
```

### Handling insets and fullscreen

`BottomSheetBehavior` can automatically handle insets (such as for
[edge to edge](https://developer.android.com/training/gestures/edge-to-edge)) by
specifying any of these to true on the view:

*   `app:paddingBottomSystemWindowInsets`
*   `app:paddingLeftSystemWindowInsets`
*   `app:paddingRightSystemWindowInsets`
*   `app:paddingTopSystemWindowInsets`

On API 21 and above the modal bottom sheet will be rendered fullscreen (edge to
edge) if the navigation bar is transparent and `app:enableEdgeToEdge` is true.
To enable edge-to-edge by default for modal bottom sheets, you can override
`?attr/bottomSheetDialogTheme` like the below example:

```xml
<style name="AppTheme" parent="Theme.Material3.*">
  ...
  <item name="bottomSheetDialogTheme">@style/ThemeOverlay.App.BottomSheetDialog</item>
</style>

<style name="ThemeOverlay.App.BottomSheetDialog" parent="ThemeOverlay.Material3.BottomSheetDialog">
    <item name="android:navigationBarColor" tools:ignore="NewApi">@android:color/transparent</item>
</style>
```

Insets can be added automatically if any of the padding attributes above are set
to true in the style, either by updating the style passed to the constructor, or
by updating the default style specified by the `?attr/bottomSheetDialogTheme`
attribute in your theme.

`BottomSheetDialog` will also add padding to the top when the bottom sheet
slides under the status bar, to prevent content from being drawn underneath it.

### Making bottom sheets accessible

The contents within a bottom sheet should follow their own accessibility
guidelines, such as setting content descriptions for images.

To support dragging bottom sheets with accessibility services such as TalkBack,
Voice Access, Switch Access, etc., we provide a convenient widget
`BottomSheetDragHandleView` which will automatically receive and handle
accessibility commands to expand and collapse the attached bottom sheet when
the accessibility mode is enabled. To use `BottomSheetDragHandleView`, you can
add it to the top of your bottom sheet content. It will show a customizable
visual indicator for all users. See the example in the below section for how to
add a drag handle to your bottom sheet.

**Note:** `BottomSheetDragHandleView` has a default min width and height of 48dp
to conform to the minimum touch target requirement. So you will need to preserve
at least 48dp at the top to place a drag handle.

## Standard bottom sheet

Standard bottom sheets co-exist with the screen’s main UI region and allow for
simultaneously viewing and interacting with both regions. They are commonly used
to keep a feature or secondary content visible on screen when content in the
main UI region is frequently scrolled or panned.

[`BottomSheetBehavior`](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior)
is applied to a child of
[CoordinatorLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout)
to make that child a **persistent bottom sheet**, which is a view that comes up
from the bottom of the screen, elevated over the main content. It can be dragged
vertically to expose more or less content.

API and source code:

*   `BottomSheetBehavior`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/BottomSheetBehavior.java)

### Standard bottom sheet example

The following example shows a standard bottom sheet in its collapsed and
expanded states:

Collapsed                                                                                 | Expanded
----------------------------------------------------------------------------------------- | --------
![Standard collapsed bottom sheet example.](assets/bottomsheet/bottomsheet_standard1.png) | ![Standard expanded bottom sheet example.](assets/bottomsheet/bottomsheet_standard2.png)

`BottomSheetBehavior` works in tandem with `CoordinatorLayout` to let you
display content on a bottom sheet, perform enter/exit animations, respond to
dragging/swiping gestures, etc.

Apply the `BottomSheetBehavior` to a direct child `View` of `CoordinatorLayout`:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
  ...>

  <FrameLayout
    android:id="@+id/standard_bottom_sheet"
    style="@style/Widget.Material3.BottomSheet"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_behavior="com.google.android.material.bottomsheet.BottomSheetBehavior">

    <!-- Drag handle for accessibility -->
    <com.google.android.material.bottomsheet.BottomSheetDragHandleView
    android:id="@+id/drag_handle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>

    <!-- Bottom sheet contents. -->
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
    android:id="@+id/bottomsheet_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/action"
    .../>

    <com.google.android.material.switchmaterial.SwitchMaterial
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/switch_label"/>

  </FrameLayout>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

In this example, the bottom sheet is the `FrameLayout`.

You can use the `BottomSheetBehavior` to set attributes like so:

```kt
val standardBottomSheet = findViewById<FrameLayout>(R.id.standard_bottom_sheet)
val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
// Use this to programmatically apply behavior attributes; eg.
// standardBottomSheetBehavior.setState(STATE_EXPANDED);
```

More information about using the behavior to set attributes is in the
[setting behavior](#setting-behavior) section.

## Modal bottom sheet

Modal bottom sheets present a set of choices while blocking interaction with the
rest of the screen. They are an alternative to inline menus and simple dialogs
on mobile devices, providing additional room for content, iconography, and
actions.

[`BottomSheetDialogFragment`](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialogFragment)
is a thin layer on top of the regular support library Fragment that renders your
fragment as a **modal bottom sheet**, fundamentally acting as a dialog.

Modal bottom sheets render a shadow on the content below them, to indicate that
they are modal. If the content outside of the dialog is tapped, the bottom sheet
is dismissed. Modal bottom sheets can be dragged vertically and dismissed by
sliding them down completely.

API and source code:

*   `BottomSheetDialogFragment`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialogFragment)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/BottomSheetDialogFragment.java)

### Modal bottom sheet example

The following example shows a modal bottom sheet in its collapsed and expanded
states:

Collapsed                                                                           | Expanded
----------------------------------------------------------------------------------- | --------
![Modal collapsed bottom sheet example.](assets/bottomsheet/bottomsheet_modal1.png) | ![Modal expanded bottom sheet example.](assets/bottomsheet/bottomsheet_modal2.png)

First, subclass `BottomSheetDialogFragment` and overwrite `onCreateView` to
provide a layout for the contents of the sheet (in this example, it's
`modal_bottom_sheet_content.xml`):

```kt
class ModalBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.modal_bottom_sheet_content, container, false)

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
```

Then, inside an `AppCompatActivity`, to show the bottom sheet:

```kt
val modalBottomSheet = ModalBottomSheet()
modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
```

`BottomSheetDialogFragment` is a subclass of `AppCompatFragment`, which means
you need to use `Activity.getSupportFragmentManager()`.

**Note:** Don't call `setOnCancelListener` or `setOnDismissListener` on a
`BottomSheetDialogFragment`. You can override
`onCancel(DialogInterface)` or `onDismiss(DialogInterface)` if necessary.

## Anatomy and key properties

Bottom sheets have a sheet, a drag handle, and, if modal, a scrim.

![Bottom sheet anatomy](assets/bottomsheet/bottomsheet_anatomy.png)

1.  Sheet
2.  Drag Handle
3.  Scrim (in modal bottom sheets)

Content can also be added below the drag handle. (see [Using bottom sheets](#using-bottom-sheets))

### Sheet attributes

Element        | Attribute             | Related method(s)                 | Default value
-------------- | --------------------- | --------------------------------- | -------------
**Color**      | `app:backgroundTint`  | N/A                               | `?attr/colorSurfaceContainerLow`
**Shape**      | `app:shapeAppearance` | N/A                               | `?attr/shapeAppearanceCornerExtraLarge`
**Elevation**  | `android:elevation`   | N/A                               | `1dp`
**Max width**  | `android:maxWidth`    | `setMaxWidth`<br/>`getMaxWidth`   | `640dp`
**Max height** | `android:maxHeight`   | `setMaxHeight`<br/>`getMaxHeight` | N/A

### Behavior attributes

More info about these attributes and how to use them in the
[setting behavior](#setting-behavior) section.

Behavior                                    | Related method(s)                                                         | Default value
------------------------------------------- | ------------------------------------------------------------------------- | -------------
`app:behavior_peekHeight`                   | `setPeekHeight`<br/>`getPeekHeight`                                       | `auto`
`app:behavior_hideable`                     | `setHideable`<br/>`isHideable`                                            | `false` for standard<br/>`true` for modal
`app:behavior_skipCollapsed`                | `setSkipCollapsed`<br/>`getSkipCollapsed`                                 | `false`
`app:behavior_fitToContents`                | `setFitToContents`<br/>`isFitToContents`                                  | `true`
`app:behavior_draggable`                    | `setDraggable`<br/>`isDraggable`                                          | `true`
`app:behavior_halfExpandedRatio`            | `setHalfExpandedRatio`<br/>`getHalfExpandedRatio`                         | `0.5`
`app:behavior_expandedOffset`               | `setExpandedOffset`<br/>`getExpandedOffset`                               | `0dp`
`app:behavior_significantVelocityThreshold` | `setSignificantVelocityThreshold` <br/> `getSignificantVelocityThreshold` | `500 pixels/s`

To save behavior on configuration change:

Attribute                | Related method(s)                 | Default value
------------------------ | --------------------------------- | -------------
`app:behavior_saveFlags` | `setSaveFlags`<br/>`getSaveFlags` | `SAVE_NONE`

### Styles

**Element**               | **Default value**
------------------------- | -------------------------------------------
**Default style (modal)** | `@style/Widget.Material3.BottomSheet.Modal`

Default style theme attribute:`?attr/bottomSheetStyle`

Note: The `?attr/bottomSheetStyle` default style theme attribute is for modal
bottom sheets only. There is no default style theme attribute for standard
bottom sheets, because `BottomSheetBehavior`s don't have a designated associated
`View`.

### Theme overlays

**Element**               | **Theme overlay**
------------------------- | ------------------------------------------
**Default theme overlay** | `ThemeOverlay.Material3.BottomSheetDialog`

Default theme overlay attribute: `?attr/bottomSheetDialogTheme`

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/res/values/styles.xml),
[attrs](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/res/values/attrs.xml),
and
[themes and theme overlays](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/res/values/themes.xml).

## Predictive Back

### Modal Bottom Sheets

The modal `BottomSheetDialogFragment` and `BottomSheetDialog` components
automatically support [Predictive Back](../foundations/PredictiveBack.md). No
further integration is required on the app side other than the general
Predictive Back prerequisites and migration steps mentioned
[here](../foundations/PredictiveBack.md#usage).

Visit the
[Predictive Back design guidelines](https://m3.material.io/components/bottom-sheets/guidelines#3d7735e2-73ea-4f3e-bd42-e70161fc1085)
to see how the component behaves when a user swipes back.

### Standard (Non-Modal) Bottom Sheets

To set up Predictive Back for standard (non-modal) bottom sheets using
`BottomSheetBehavior`, create an AndroidX back callback that forwards
`BackEventCompat` objects to your `BottomSheetBehavior`:

```kt
val bottomSheetBackCallback = object : OnBackPressedCallback(/* enabled= */false) {
  override fun handleOnBackStarted(backEvent: BackEventCompat) {
    bottomSheetBehavior.startBackProgress(backEvent)
  }

  override fun handleOnBackProgressed(backEvent: BackEventCompat) {
    bottomSheetBehavior.updateBackProgress(backEvent)
  }

  override fun handleOnBackPressed() {
    bottomSheetBehavior.handleBackInvoked()
  }

  override fun handleOnBackCancelled() {
    bottomSheetBehavior.cancelBackProgress()
  }
}
```

And then add and enable the back callback as follows:

```kt
getOnBackPressedDispatcher().addCallback(this, bottomSheetBackCallback)

bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
  override fun onStateChanged(bottomSheet: View, newState: Int) {
    when (newState) {
      STATE_EXPANDED, STATE_HALF_EXPANDED -> bottomSheetBackCallback.setEnabled(true)
      STATE_COLLAPSED, STATE_HIDDEN -> bottomSheetBackCallback.setEnabled(false)
      else -> {
        // Do nothing, only change callback enabled for "stable" states.
      }
    }
  }

  override fun onSlide(bottomSheet: View, slideOffset: Float) {}
})
```

## Theming bottom sheets

Bottom sheets support
[Material Theming](https://material.io/components/sheets-bottom#theming), which
can customize color and shape.

### Bottom sheet theming example

API and source code:

*   `BottomSheetBehavior`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetBehavior)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/BottomSheetBehavior.java)
*   `BottomSheetDialogFragment`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialogFragment)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/BottomSheetDialogFragment.java)

The following example shows a bottom sheet with Material Theming, in its
collapsed and expanded states.

![Bottom sheet with pink background color. Collapsed on the left and expanded on
the right.](assets/bottomsheet/bottomsheet_theming.png)

#### Implementing bottom sheet theming

Setting the theme attribute `bottomSheetDialogTheme` to your custom
`ThemeOverlay` will affect all bottom sheets.

In `res/values/themes.xml`:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
  ...
  <item name="bottomSheetDialogTheme">@style/ThemeOverlay.App.BottomSheetDialog</item>
</style>

<style name="ThemeOverlay.App.BottomSheetDialog" parent="ThemeOverlay.Material3.BottomSheetDialog">
    <item name="bottomSheetStyle">@style/ModalBottomSheetDialog</item>
</style>
```

In `res/values/styles.xml`:

```xml
<style name="ModalBottomSheetDialog" parent="Widget.Material3.BottomSheet.Modal">
    <item name="backgroundTint">@color/shrine_pink_light</item>
    <item name="shapeAppearance">@style/ShapeAppearance.App.LargeComponent</item>
</style>

<style name="ShapeAppearance.App.LargeComponent" parent="ShapeAppearance.Material3.LargeComponent">
    <item name="cornerFamily">cut</item>
    <item name="cornerSize">24dp</item>
</style>
```

**Note:** The benefit of using a custom `ThemeOverlay` is that any changes to
your main theme, such as updated colors, will be reflected in the bottom sheet,
as long as they're not overridden in your custom theme overlay. If you use a
custom `Theme` instead, by extending from one of the
`Theme.Material3.*.BottomSheetDialog` variants, you will have more control over
exactly what attributes are included in each, but it also means you'll have to
duplicate any changes that you've made in your main theme into your custom
theme.
