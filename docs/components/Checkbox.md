<!--docs:
title: "Checkboxes"
layout: detail
section: components
excerpt: "Checkboxes are independent buttons with two states: selected and unselected."
iconId: checkbox
path: /catalog/checkbox/
-->

# Checkboxes

A `CheckBox` represents a button with two states, selected and unselected.
Unlike radio buttons, changes in the states of one checkbox do not usually
affect other checkboxes. A checkbox is a rounded square button with a check to
denote its current state.

## Design & API Documentation

-   [Material Design guidelines: Checkboxes](https://material.io/go/design-checkboxes)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/checkbox/MaterialCheckBox.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/android/widget/CheckBox)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `MaterialCheckBox` widget provides a complete implementation of Material
Design's checkbox component. It is auto-inflated when using a non-Bridge
Theme.MaterialComponents.\* theme which sets the MaterialComponentsViewInflater.
Example code of how to include the widget in your layout:

```xml
<CheckBox
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"/>
```

### Material Styles

Using a Material Components theme with `MaterialCheckBox` will match the color
of `CheckBox` views to your theme's palette. If you want to override this
behavior, as you might with a custom drawable, set the `useMaterialThemeColors`
parameter to false.

```xml
<CheckBox xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  app:useMaterialThemeColors="false"/>
```

### Styles

Use `checkboxStyle` for style changes.

```xml
  <item name="checkboxStyle">@style/Widget.MaterialComponents.CompoundButton.CheckBox</item>
```
