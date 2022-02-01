<!--docs:
title: "Material selection controls: Switches"
layout: detail
section: components
excerpt: "Selection controls allow the user to select options."
iconId: switch
path: /catalog/switches/
-->

# Selection controls: switches

[Selection controls](https://material.io/components/selection-controls#usage)
allow the user to select options.

Switches toggle the state of a single setting on or off. They are the preferred
way to adjust settings on mobile devices.

![White "Settings" menu with purple header and switches to turn on options, such
as "Wi-fi" and "Bluetooth"](assets/switch/switch_hero.png)

**Contents**

*   [Using switches](#using-switches)
*   [Switch](#switch)
*   [Theming switches](#theming-switches)

## Using switches

Before you can use Material switches, you need to add a dependency to the
Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

**Note:** The `SwitchMaterial` widget provides a complete implementation of
Material Design's switch component. It extends from the support library's
`SwitchCompat` widget, but not from the framework `Switch` widget. As such, it
does not auto-inflate, unlike other selection controls, and must be explicitly
specified in layouts.

Use switches to:

*   Toggle a single item on or off, on mobile and tablet
*   Immediately activate or deactivate something

### Making switches accessible

Switches support content labeling for accessibility and are readable by most
screen readers, such as TalkBack. Text rendered in switches is automatically
provided to accessibility services. Additional content labels are usually
unnecessary.

## Switch

A `Switch` represents a button with two states, on and off. Switches are most
often used on mobile devices to enable and disable options in an options menu. A
switch consists of a track and thumb; the thumb moves along the track to
indicate its current state.

### Switches example

API and source code:

*   `SwitchMaterial`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/switchmaterial/SwitchMaterial)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/switchmaterial/SwitchMaterial.java)

The following example shows a list of five switches.

![Example of 5 switches, the first one is toggled and the last one is disabled.](assets/switch/switch_example.png)

In the layout:

```xml
<com.google.android.material.switchmaterial.SwitchMaterial
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:checked="true"
    android:text="@string/label_1"/>
<com.google.android.material.switchmaterial.SwitchMaterial
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:text="@string/label_2"/>
<com.google.android.material.switchmaterial.SwitchMaterial
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:text="@string/label_3"/>
<com.google.android.material.switchmaterial.SwitchMaterial
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:text="@string/label_4"/>
<com.google.android.material.switchmaterial.SwitchMaterial
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:enabled="false"
    android:text="@string/label_5"/>
```

In code:

```kt
// To check a switch
switchMaterial.isChecked = true

// To listen for a switch's checked/unchecked state changes
switchMaterial.setOnCheckedChangeListener { buttonView, isChecked
    // Responds to switch being checked/unchecked
}
```

## Anatomy and key properties

The following is an anatomy diagram that shows a switch thumb and a switch
track:

![Switch anatomy diagram](assets/switch/switch_anatomy.png)

1.  Thumb
2.  Track

### Switch attributes

Element                    | Attribute                                  | Related method(s)                                          | Default value
-------------------------- | ------------------------------------------ | ---------------------------------------------------------- | -------------
**To use material colors** | `app:useMaterialThemeColors`               | `setUseMaterialThemeColors`<br/>`isUseMaterialThemeColors` | `true` (ignored if specific tint attrs are set)
**Min size**               | `android:minWidth`<br/>`android:minHeight` | `(set/get)MinWidth`<br/>`(set/get)MinHeight`               | `?attr/minTouchTargetSize`

The color of the switch defaults to using `?attr/colorPrimary`,
`?attr/colorPrimaryContainer`, `?attr/colorOnSurface`, and `?attr/colorOutline`
defined in your app theme. If you want to override this behavior, as you might
with a custom drawable that should not be tinted, set
`app:useMaterialThemeColors` to `false`:

```xml
<com.google.android.material.switchmaterial.SwitchMaterial
        ...
    app:useMaterialThemeColors="false"
    />
```

### Thumb attributes

Element       | Attribute       | Related method(s)                         | Default value
------------- | --------------- | ----------------------------------------- | -------------
**Thumb**     | `android:thumb` | `setThumbDrawable`<br/>`getThumbDrawable` | inherits from `SwitchCompat`
**Color**     | `app:thumbTint` | `setThumbTintList`<br/>`getThumbTintList` | `?attr/colorOnSurface` (unchecked)<br/>`?attr/colorPrimary` (checked)
**Elevation** | N/A             | N/A                                       | `4dp`

### Track attributes

Element   | Attribute       | Related method(s)                         | Default value
--------- | --------------- | ----------------------------------------- | -------------
**Track** | `app:track`     | `setTrackDrawable`<br/>`getTrackDrawable` | inherits from `SwitchCompat`
**Color** | `app:trackTint` | `setTrackTintList`<br/>`getTrackTintList` | `?attr/colorOutline` (unchecked)<br/>`?attr/colorPrimaryContainer` (checked)

### Text label attributes

Element        | Attribute                | Related method(s)                  | Default value
-------------- | ------------------------ | ---------------------------------- | -------------
**Text label** | `android:text`           | `setText`<br/>`getText`            | `null`
**Color**      | `android:textColor`      | `setTextColor`<br/>`getTextColors` | `?android:attr/textColorPrimaryDisableOnly`
**Typography** | `android:textAppearance` | `setTextAppearance`                | `?attr/textAppearanceBodyMedium`

### Switch states

Switches can be on or off. Switches have enabled, hover, focused, and pressed
states.

Display the outer radial reaction only on form factors that use touch, where
interaction may obstruct the element completely.

For desktop, the radial reaction isn't needed.

![Switch states in an array. Columns are enabled, disabled, hover, focused,
pressed. Rows are on or off](assets/switch/switch_states.png)

### Styles

Element           | Style
----------------- | ----------------------------------------
**Default style** | `Widget.Material3.CompoundButton.Switch`

Default style theme attribute: `?attr/switchStyle`

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/switchmaterial/res/values/styles.xml)
and
[attrs](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/switchmaterial/res/values/attrs.xml).

## Theming switches

Switches support
[Material Theming](https://material.io/components/selection-controls#theming),
which can customize color and typography.

### Switch theming example

API and source code:

*   `SwitchMaterial`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/switchmaterial/SwitchMaterial)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/switchmaterial/SwitchMaterial.java)

The following example shows a list of switches with Material Theming.

!["5 switches with brown text: first switch is on and has pink thumb and track"](assets/switch/switch_theming.png)

#### Implementing switch theming

Use theme attributes in `res/values/styles.xml`, which applies to all switches
and affects other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="colorPrimaryContainer">@color/pink_100</item>
    <item name="colorPrimary">@color/pink_200</item>
</style>

```

Use default style theme attributes, styles and theme overlays, which apply to
all switches but do not affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="switchStyle">@style/Widget.App.Switch</item>
</style>

<style name="Widget.App.Switch" parent="Widget.Material3.CompoundButton.Switch">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.Switch</item>
</style>

<style name="ThemeOverlay.App.Switch" parent="">
    <item name="colorPrimaryContainer">@color/pink_100</item>
    <item name="colorPrimary">@color/pink_200</item>
</style>
```

Use the styles in the layout, which affects only this switch:

```xml
<com.google.android.material.switchmaterial.SwitchMaterial
        ...
    style="@style/Widget.App.Switch"
    />
```
