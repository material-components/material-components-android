<!--docs:
title: "Radio Buttons"
layout: detail
section: components
excerpt: "Radio Buttons are groupable buttons with two states: selected and unselected."
iconId: radiobutton
path: /catalog/radiobutton/
-->

# Radio Buttons

A `RadioButton` represents a button with two states, selected and unselected.
Unlike checkboxes, changes in the states of one radio button can affect other
buttons in the group. Specifically, selecting a `RadioButton` in a `RadioGroup`
will de-select all other buttons in that group. A radio button is a circle which
fills in with an inset when selected.

## Design & API Documentation

-   [Material Design guidelines: Radiobuttons](https://material.io/go/design-radio-buttons)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/radiobutton/MaterialRadioButton.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/android/widget/RadioButton)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `MaterialRadioButton` widget provides a complete implementation of Material
Design's radio button component. It is auto-inflated when using a non-Bridge
Theme.MaterialComponents.\* theme which sets the MaterialComponentsViewInflater.
Example code of how to include the widget in your layout:

```xml
<RadioGroup
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:checkedButton="@+id/first"
    android:orientation="vertical">
  <RadioButton
      android:id="@+id/first"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:text="@string/first_label"/>
  <RadioButton
      android:id="@+id/second"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:text="@string/second_label"/>
</RadioGroup>
```

### Material Styles

Using a Material Components theme with `MaterialRadioButton` will match the
color of `RadioButton` views to your theme's palette. If you want to override
this behavior, as you might with a custom drawable, set the
`useMaterialThemeColors` parameter to false.

```xml
<RadioButton xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  app:useMaterialThemeColors="false"/>
```

### Styles

Use `radioButtonStyle` for style changes.

```xml
  <item name="radioButtonStyle">@style/Widget.MaterialComponents.CompoundButton.RadioButton</item>
```
