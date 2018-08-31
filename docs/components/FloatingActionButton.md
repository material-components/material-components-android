<!--docs:
title: "Floating Action Buttons"
layout: detail
section: components
excerpt: "A floating button for the primary action in an application."
iconId: button
path: /catalog/floating-action-button/
-->

# Floating Action Buttons

A `FloatingActionButton` displays the primary action in an application. It is
a round icon button that's elevated above other page content. **Floating action
buttons** come in a default and mini size.

Floating action buttons provide quick access to important or common actions
within an app. They have a variety of uses, including:

-   Performing a common action, such as starting a new email in a mail app.
-   Displaying additional related actions.
-   Update or transforming into other UI elements on the screen.

Floating action buttons adjust their position and visibility in response to
other UI elements on the screen.

## Design & API Documentation

-   [Material Design guidelines: Floating Action
    Buttons](https://material.io/go/design-fab)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/floatingactionbutton/FloatingActionButton.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `FloatingActionButton` widget provides a complete implementation of Material
Design's floating action button component. Here's how to include the widget in
your layout:

```xml
<android.support.design.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <!-- Main content -->

  <com.google.android.material.floatingactionbutton.FloatingActionButton
      android:id="@+id/floating_action_button"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="bottom|right"
      android:layout_margin="16dp"
      app:srcCompat="@drawable/ic_plus_24"/>

</android.support.design.widget.CoordinatorLayout>
```

Note: If the `FloatingActionButton` is a child of a `CoordinatorLayout`, you get
certain behaviors for free. It will automatically shift so that any displayed
[Snackbars](Snackbar.md) do not cover it, and will automatially hide when
covered by an [AppBarLayout](AppBarLayout.md) or
[BottomSheetBehavior](BottomSheetBehavior.md).

### Material Styles

Using `FloatingActionButton` with an updated Material theme
(`Theme.MaterialComponents`) will provide the correct updated Material styles to
your floating action buttons by default. If you need to use an updated Material
floating action button and your application theme does not inherit from an
updated Material theme, you can apply one of the updated Material styles
directly to your widget in XML.

#### Updated Material Style

The updated Material `FloatingActionButton` style consists of updated elevation,
ripple, and motion changes.

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/floating_action_button"
    style="@style/Widget.MaterialComponents.FloatingActionButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|right"
    android:layout_margin="16dp"
    app:srcCompat="@drawable/ic_plus_24"/>
```

#### Legacy Material Style

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/floating_action_button"
    style="@style/Widget.Design.FloatingActionButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|right"
    android:layout_margin="16dp"
    app:srcCompat="@drawable/ic_plus_24"/>
```

### Attributes

Feature    | Relevant attributes
:--------- | :-------------------------------
Icon       | `app:srcCompat`<br> `app:tint`<br> `app:maxImageSize`
Size       | `app:fabSize`<br> `app:fabCustomSize`
Background | `app:backgroundTint`
Ripple     | `app:rippleColor`
Border     | `app:borderWidth`
Elevation  | `app:elevation`<br> `app:hoveredFocusedTranslationZ`<br> `app:pressedTranslationZ`
Motion     | `app:showMotionSpec`<br> `app:hideMotionSpec`

### Handling Clicks

`FloatingActionButton` handles clicks in the same way as all views:

```java
FloatingActionButton floatingActionButton =
    (FloatingActionButton) findViewById(R.id.floating_action_button);

floatingActionButton.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View view) {
        // Handle the click.
    }
});
```

### Visibility

Use the `show` and `hide` methods to animate the visibility of a
`FloatingActionButton`. The show animation grows the widget and fades it in,
while the hide animation shrinks the widget and fades it out.

### Sizing

The `FloatingActionButton` can be sized either by using the discrete sizing
modes or a totally custom size.

##### Sizing Modes

The supported sizing modes are as follows:

* `normal` - the normal sized button, 56dp.
* `mini` - the mini sized button, 40dp.
* `auto` - the button size will change based on the window size. For small
sized windows (largest screen dimension < 470dp) this will select a mini sized
button, and for larger sized windows it will select a normal sized button.

By default, the sizing mode will be `auto`, but this can be adjusted via the
`app:fabSize` attribute or the `FloatingActionButton#setSize` method.

##### Custom Size

To set a custom size for your `FloatingActionButton`, you can use the
`app:fabCustomSize` attribute or the `FloatingActionButton#setCustomSize`
method.

If you've set a custom size and would like to clear it, you can call the
`FloatingActionButton#clearCustomSize` method. If called, custom sizing will
not be used and the size will be calculated based on the value set using
`FloatingActionButton#setSize` or the `app:fabSize` attribute.

## Related Concepts

-   [CoordinatorLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout)
