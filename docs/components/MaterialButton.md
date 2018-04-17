<!--docs:
title: "Material Button"
layout: detail
section: components
excerpt: "A customizable button component with updated visual styles."
iconId: materialbutton
path: /catalog/material-button/
-->

# Material Button

`Material Button` is a customizable button component with updated visual styles.
This button component has several built-in styles to support different levels of
emphasis, as typically any UI will contain a few different buttons to indicate
different actions. These levels of emphasis include:

-   raised button: A rectangular material button that lifts and displays ink
    reactions on press
-   unelevated button: A button made of ink that displays ink reactions on press
    but does not lift

## Design & API Documentation

-   [Material Design guidelines:
    Buttons](https://material.io/guidelines/components/buttons.html)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/MaterialButton.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/button/MaterialButton.html)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `MaterialButton` component provides a complete implementation of Material
Design's button component. Example code of how to include the component in your
layout:

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/button_label_enabled"/>

<com.google.android.material.button.MaterialButton
    android:id="@+id/disabled_material_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:enabled="false"
    android:text="@string/button_label_disabled"/>

<com.google.android.material.button.MaterialButton
    android:id="@+id/material_unelevated_button"
    style="@style/Widget.MaterialComponents.Button.UnelevatedButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/unelevated_button_label_enabled"/>
```

For raised buttons, your theme's `colorAccent` provides the default background
color of the component, and the text color is white by default. For unelevated
buttons, your theme's `colorAccent` provides the default text color of the
component, and the background color is transparent by default.

## Attributes

The following attributes can be changed for Material Button:

-   `icon`: Add an icon to the start of the component.
-   `iconPadding`, `iconTint`, `iconTintMode`: Set the corresponding icon
    properties.
-   `additionalPaddingForIconLeft`, `additionalPaddingForIconRight`: Padding to
    add to the left/right side of the button when an icon is present.
-   `buttonBackgroundTint`: Change the background color.
-   `rippleColor`: Change the ripple/press color. Ripple opacity will be
    determined by the Android framework when available. Otherwise, this color
    will be overlaid on the button at a 50% opacity when button is pressed.
-   `strokeColor`: Add a solid stroke with the specified color.
-   `strokeWidth`: Set the width of the solid stroke.
-   `cornerRadius`: Set the radius of all four corners of the button.

The following shows an example of setting `icon` and `iconPadding` attributes on
a button:

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_icon_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/icon_button_label_enabled"
    app:icon="@drawable/icon_24px"
    app:iconPadding="8dp"/>
```

## Related Concepts

If your app requires actions to be persistent and readily available, you can use
[FloatingActionButton](FloatingActionButton.md) instead.
