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

Note: `MaterialButton` is visually different from `Button` and
`AppCompatButton`. One of the main differences is that `AppCompatButton` has a
`4dp` inset on the left and right sides, whereas `MaterialButton` does not. To
add an inset to match `AppCompatButton`, set `android:insetLeft` and
`android:insetRight` on the button to `4dp`, or change the spacing on the
button's parent layout.

When replacing buttons in your app with `MaterialButton`, you should inspect
these changes for sizing and spacing differences.

## Design & API Documentation

-   [Material Design guidelines: Buttons](https://material.io/go/design-buttons)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/MaterialButton.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/button/MaterialButton)
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

## Styles

We provide several styles for the `MaterialButton` component.

### Filled, elevated button (default)

The default style represents an elevated button with a colored background. This
should be used for important, final actions that complete a flow, like 'Save' or
'Confirm'. If no style attribute is specified for a `MaterialButton`, this is
the style that will be used.

```
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_button"
    style="@style/Widget.MaterialComponents.Button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/button_label_enabled"/>
```

### Filled, unelevated button

The `UnelevatedButton` style represents an unelevated button with a colored
background.

```
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_unelevated_button"
    style="@style/Widget.MaterialComponents.Button.UnelevatedButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/unelevated_button_label_enabled"/>
```

### Text button

The `TextButton` style has a transparent background with colored text. Text
buttons are used for low-priority actions, especially when presenting multiple
options.

```
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_text_button"
    style="@style/Widget.MaterialComponents.Button.TextButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/text_button_label_enabled"/>
```

### Icon button

Every style for Material Button has an additional `.Icon` style. This style is
meant to be used when the `icon` attribute is set for the button. The icon
button style has smaller start and end paddings to achieve visual balance in the
button when an icon is present.

The following shows a filled, elevated button with an icon:

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_icon_button"
    style="@style/Widget.MaterialComponents.Button.Icon"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/icon_button_label_enabled"
    app:icon="@drawable/icon_24px"/>
```

The following shows a text button with an icon:

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_icon_button"
    style="@style/Widget.MaterialComponents.Button.TextButton.Icon"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/icon_button_label_enabled"
    app:icon="@drawable/icon_24px"/>
```

## Attributes

The following attributes can be changed for Material Button:

Description                                                  | Relevant attributes
------------------------------------------------------------ | -------------------
Button padding                                               | `android:padding`<br/>`android:paddingLeft`<br/>`android:paddingRight`<br/>`android:paddingStart`<br/>`android:paddingEnd`<br/>`android:paddingTop`<br/>`android:paddingBottom`
Button inset                                                 | `android:insetLeft`<br/>`android:insetRight`<br/>`android:insetTop`<br/>`android:insetBottom`
Background color                                             | `app:backgroundTint`<br/>`app:backgroundTintMode`
Icon drawable                                                | `app:icon`
Padding between icon and button text                         | `app:iconPadding`
Icon color                                                   | `app:iconTint`<br/>`app:iconTintMode`
Stroke                                                       | `app:strokeColor`<br/>`app:strokeWidth`
The radius of all four corners of the<br/>button             | `app:cornerRadius`
Ripple                                                       | `app:rippleColor`

The following shows an example of setting `icon` and `iconPadding` attributes on
a button:

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/material_icon_button"
    style="@style/Widget.MaterialComponents.Button.Icon"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/icon_button_label_enabled"
    app:icon="@drawable/icon_24px"
    app:iconPadding="8dp"/>
```

## Related Concepts

If your app requires actions to be persistent and readily available, you can use
[FloatingActionButton](FloatingActionButton.md) instead.
