<!--docs:
title: "Extended Floating Action Button"
layout: detail
section: components
excerpt: "A customizable button component with updated visual styles."
iconId: button
path: /catalog/extended-floating-action-button/
-->

# Extended Floating Action Button

An `ExtendedFloatingActionButton` displays the primary action in an application.
The Extended FAB is wider than the regular `FloatingActionButton`, and it
includes a text label.

Extended floating action buttons provide quick access to important or common
actions within an app. They have a variety of uses, including:

-   Performing a common action, such as starting a new email in a mail app.
-   Displaying additional related actions.
-   Update or transforming into other UI elements on the screen.

Extended floating action buttons adjust their position and visibility in
response to other UI elements on the screen.

## Design & API Documentation

-   [Material Design guidelines: Floating Action Buttons](https://material.io/go/design-extended-fab)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/floatingactionbutton/ExtendedFloatingActionButton.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/com/google/android/material/floatingactionbutton/ExtendedFloatingActionButton)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `ExtendedFloatingActionButton` widget provides a complete implementation of
Material Design's extended FAB component. The example below shows a usage of the
extended FAB within a CoordinatorLayout, but the CoordinatorLayout is not
necessary for this component. There are more generic examples of usage later on.
Here's how to include the widget in your layout:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <!-- Main content -->

  <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_margin="8dp"
      android:contentDescription="@string/extended_fab_content_desc"
      android:text="@string/extended_fab_label"
      app:icon="@drawable/ic_plus_24px"
      app:layout_anchor="@id/app_bar"
      app:layout_anchorGravity="bottom|right|end"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

Note: `ExtendedFloatingActionButton` is a child class of `MaterialButton`,
rather than `FloatingActionButton`. This means that several attributes which are
applicable to `FloatingActionButton` have different naming in
`ExtendedFloatingActionButton`. For example, `FloatingActionButton` uses
`app:srcCompat` to set the icon drawable, whereas `ExtendedFloatingActionButton`
uses `app:icon`. To compare the attribute sets for these two components, please
see the [FloatingActionButton](FloatingActionButton.md) page, and the
"Attributes" table on this page.

### Material Styles

Using `ExtendedFloatingActionButton` with a Material theme
(`Theme.MaterialComponents`) will provide the correct Material styling to your
extended FABs by default.

#### Default Extended Floating Action Button Style

The default style represents an extended floating action button with a colored
background, text, and an icon.

```xml
  <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:contentDescription="@string/extended_fab_content_desc"
      android:text="@string/extended_fab_label"
      app:icon="@drawable/ic_plus_24px"/>
```

Extended FABs with no style directly applied to them, but with a Material theme
applied, are styled with the
`Widget.MaterialComponents.ExtendedFloatingActionButton.Icon` style. The `Icon`
suffix indicates that the paddings for this button have been adjusted to give a
more even spacing when an icon is present.

#### Text-only Extended Floating Action Button Style

```xml
  <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
      style="@style/Widget.MaterialComponents.ExtendedFloatingActionButton"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:contentDescription="@string/extended_fab_content_desc"
      android:text="@string/extended_fab_label"/>
```

The `Widget.MaterialComponents.ExtendedFloatingActionButton`, with no `Icon`
suffix, indicates that the paddings for this extended FAB are more suited for a
text-only button. This style should only be used when your extended FAB does not
need to display an icon, and must be manually applied to your extended FAB.

## Attributes

The following attributes can be changed for Extended Floating Action Button:

Description                   | Relevant attributes
----------------------------- | -------------------
Button padding                | `android:padding`<br/>`android:paddingLeft`<br/>`android:paddingRight`<br/>`android:paddingStart`<br/>`android:paddingEnd`<br/>`android:paddingTop`<br/>`android:paddingBottom`
Button inset                  | `android:insetLeft`<br/>`android:insetRight`<br/>`android:insetTop`<br/>`android:insetBottom`
Background color              | `app:backgroundTint`<br/>`app:backgroundTintMode`
Icon drawable                 | `app:icon`<br/>`app:iconSize`
Padding between icon and text | `app:iconPadding`
Icon color                    | `app:iconTint`<br/>`app:iconTintMode`
Stroke                        | `app:strokeColor`<br/>`app:strokeWidth`
Ripple                        | `app:rippleColor`
Shape                         | `app:shapeAppearance`<br/>`app:shapeAppearanceOverlay`

### Theme Attribute Mapping

```
style="@style/Widget.MaterialComponents.ExtendedFloatingActionButton" and
style="@style/Widget.MaterialComponents.ExtendedFloatingActionButton.Icon"
```

Component Attribute      | Default Theme Attribute Value
------------------------ | -------------------------------------------
`android:textAppearance` | `textAppearanceButton`
`backgroundTint`         | `colorSecondary`
`iconTint`               | `colorOnSecondary`
`rippleColor`            | `colorOnSecondary` at 32% opacity (pressed)
`android:textColor`      | `colorOnSecondary`

### Visibility

Use the `show` and `hide` methods to animate the visibility of a
`ExtendedFloatingActionButton`. The show animation grows the widget and fades it
in, while the hide animation shrinks the widget and fades it out.

### Extending and Shrinking

In addition, `ExtendedFloatingActionButton` has the methods `extend` and
`shrink` to animate showing and hiding the extended FAB's text. The `extend`
animation extends the FAB to show the text and the icon. The `shrink` animation
shrinks the FAB to show just the icon.

## Related Concepts

-   [FloatingActionButton](FloatingActionButton.md)
-   [Button](Button.md)
