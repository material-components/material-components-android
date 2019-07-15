<!--docs:
title: "Switches"
layout: detail
section: components
excerpt: "Switches are toggleable buttons with two states: on and off (selected and unselected)."
iconId: switch
path: /catalog/switch/
-->

# Switches

A `Switch` represents a button with two states, on and off. Switches are most
often used on mobile devices to enable and disable options in an options menu. A
switch consists of a track and thumb; the thumb moves along the track to
indicate its current state.

## Design & API Documentation

-   [Material Design guidelines: Switches](https://material.io/go/design-switches)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/switchmaterial/SwitchMaterial.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/android/support/v7/widget/SwitchCompat)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `SwitchMaterial` widget provides a complete implementation of Material
Design's switch component. It extends from the support library's `SwitchCompat`
widget, but not from the framework Switch widget. As such, it does not
auto-inflate, unlike other selection controls, and must be explicitly specified
in layouts.

```xml
<com.google.android.material.switchmaterial.SwitchMaterial
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:checked="true"
  android:text="@string/switch_text"/>
```

### Material Styles

Using a Material Components theme with `SwitchMaterial` will match the color of
`SwitchMaterial` views to your theme's palette. If you want to override this
behavior, as you might with custom drawables, set the `useMaterialThemeColors`
parameter to false.

```xml
<com.google.android.material.switchmaterial.SwitchMaterial
  xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  app:useMaterialThemeColors="false"
  android:text="@string/switch_text"/>
```

### Styles

Use `switchStyle` for style changes.

```xml
  <item name="switchStyle">@style/Widget.MaterialComponents.CompoundButton.Switch</item>
```
