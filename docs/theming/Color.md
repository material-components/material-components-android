<!--docs:
title: "Color Theming"
layout: detail
section: theming
excerpt: "Color Theming"
iconId: color
path: /theming/color/
-->

# Color Theming

The Material 3 color theming system can be used to create a color scheme that
reflects your brand or style.

The Material 3 color theming system uses an organized approach to apply colors
to your UI. Material components use theme colors and their variations to style
backgrounds, text, and more.

## Design & API Documentation

-   [Material Design guidelines:
    Color](https://material.io/go/design-color-theming/)

## Using The Color Theming System

All Material 3 components use a `Widget.Material3` style, and these styles
reference color attributes from the Material 3 theme (`Theme.Material3`). It is
easy to customize those color attributes across your app by simply overriding
them in your theme. We provide three accent color groups (Primary, Secondary,
Tertiary), each with 4-5 color roles that you can customize to represent your
brand color:

<!-- Auto-generated accent color table starts. Do not edit below or remove this comment. -->

Color Role             | Android Attribute         | Light Theme Baseline (Dynamic) Color | Dark Theme Baseline (Dynamic) Color
---------------------- | ------------------------- | ------------------------------------ | -----------------------------------
Primary                | colorPrimary              | #ff6750a4</br>(system_accent1_600)   | #ffd0bcff</br>(system_accent1_200)
On Primary             | colorOnPrimary            | white</br>(system_accent1_0)         | #ff381e72</br>(system_accent1_800)
Primary Container      | colorPrimaryContainer     | #ffeaddff</br>(system_accent1_100)   | #ff4f378b</br>(system_accent1_700)
On Primary Container   | colorOnPrimaryContainer   | #ff21005d</br>(system_accent1_900)   | #ffeaddff</br>(system_accent1_100)
Inverse Primary        | colorPrimaryInverse       | #ffd0bcff</br>(system_accent1_200)   | #ff6750a4</br>(system_accent1_600)
Secondary              | colorSecondary            | #ff625b71</br>(system_accent2_600)   | #ffccc2dc</br>(system_accent2_200)
On Secondary           | colorOnSecondary          | white</br>(system_accent2_0)         | #ff332d41</br>(system_accent2_800)
Secondary Container    | colorSecondaryContainer   | #ffe8def8</br>(system_accent2_100)   | #ff4a4458</br>(system_accent2_700)
On Secondary Container | colorOnSecondaryContainer | #ff1d192b</br>(system_accent2_900)   | #ffe8def8</br>(system_accent2_100)
Tertiary               | colorTertiary             | #ff7d5260</br>(system_accent3_600)   | #ffefb8c8</br>(system_accent3_200)
On Tertiary            | colorOnTertiary           | white</br>(system_accent3_0)         | #ff492532</br>(system_accent3_800)
Tertiary Container     | colorTertiaryContainer    | #ffffd8e4</br>(system_accent3_100)   | #ff633b48</br>(system_accent3_700)
On Tertiary Container  | colorOnTertiaryContainer  | #ff31111d</br>(system_accent3_900)   | #ffffd8e4</br>(system_accent3_100)

<!-- Auto-generated accent color table ends. Do not edit below or remove this comment. -->

By changing these color attributes, you can easily change the styles of all the
Material components that use your theme.

The Material Design color theming system provides additional colors which don't
represent your brand, but define your UI and ensure accessible color
combinations. These additional color attributes are as follows:

<!-- Auto-generated additional color table starts. Do not edit below or remove this comment. -->

Color Role         | Android Attribute       | Light Theme Baseline (Dynamic) Color | Dark Theme Baseline (Dynamic) Color
------------------ | ----------------------- | ------------------------------------ | -----------------------------------
Error              | colorError              | #ffb3261e</br>(Same)                 | #fff2b8b5</br>(Same)
On Error           | colorOnError            | white</br>(Same)                     | #ff601410</br>(Same)
Error Container    | colorErrorContainer     | #fff9dedc</br>(Same)                 | #ff8c1d18</br>(Same)
On Error Container | colorOnErrorContainer   | #ff410e0b</br>(Same)                 | #fff2b8b5</br>(Same)
Outline            | colorOutline            | #ff79747e</br>(system_neutral2_500)  | #ff938f99</br>(system_neutral2_400)
Background         | android:colorBackground | #fffffbfe</br>(system_neutral1_10)   | #ff1c1b1f</br>(system_neutral1_900)
On Background      | colorOnBackground       | #ff1c1b1f</br>(system_neutral1_900)  | #ffe6e1e5</br>(system_neutral1_100)
Surface            | colorSurface            | #fffffbfe</br>(system_neutral1_10)   | #ff1c1b1f</br>(system_neutral1_900)
On Surface         | colorOnSurface          | #ff1c1b1f</br>(system_neutral1_900)  | #ffe6e1e5</br>(system_neutral1_100)
Surface Variant    | colorSurfaceVariant     | #ffe7e0ec</br>(system_neutral2_100)  | #ff49454f</br>(system_neutral2_700)
On Surface Variant | colorOnSurfaceVariant   | #ff49454f</br>(system_neutral2_700)  | #ffcac4d0</br>(system_neutral2_200)
Inverse Surface    | colorSurfaceInverse     | #ff313033</br>(system_neutral1_800)  | #ffe6e1e5</br>(system_neutral1_100)
Inverse On Surface | colorOnSurfaceInverse   | #fff4eff4</br>(system_neutral1_50)   | #ff313033</br>(system_neutral1_800)

<!-- Auto-generated additional color table ends. Do not edit below or remove this comment. -->

## Using Surface Colors

Material 3 uses primary colored elevation overlays to present a visual hierarchy
with different elevations in both light and dark themes. Material 3 themes
enable this by default with setting `?attr/elevationOverlayColor` to
`?attr/colorPrimary`.

Elevation overlays use the following theme attributes:

Attribute Name              |Description                                                                          |Default Value
----------------------------|-------------------------------------------------------------------------------------|-------------
`elevationOverlayEnabled`   |Whether the elevation overlay functionality is enabled.                              |`true`
`elevationOverlayColor`     |The color used for the elevation overlays, applied at an alpha based on elevation.   |`colorPrimary`

If inheriting from the `Theme.Material3` theme or a descendant, you most likely
do not have to set these attributes yourself because Material themes use the
defaults shown above.

The elevation overlays will be applied to surface colors to create tonal
variations. Within the Material 3 color palette, there are five predefined
surface tonal variations (Surface1-5) which are used as the default surface
colors (by applying different elevations) of different Material 3 components.
However, these surface tonal colors are **NOT** implemented as color resources,
but their actual color values are calculated *on the fly* with the
`?attr/elevationOverlayColor`, as mentioned above.

In a practical scenario, you have three ways to include those tonal surface
colors in your app:

##### Material Design Components

The easiest way to use surface colors with tonal variations is with Material
Design Components, which have built-in support for tonal surface
colors/elevation overlays. You can customize surface colors of those components
by changing their elevation.

Here is a list of Material components that support elevation overlays. These
components have `colorSurface` set as the default background color and can be
elevated:

*   [Top App Bar](../components/TopAppBar.md)
*   [Bottom App Bar](../components/BottomAppBar.md)
*   [Bottom Navigation](../components/BottomNavigation.md)
*   [Button](../components/Button.md)
*   [Floating Action Button](../components/FloatingActionButton.md)
*   [Chip](../components/Chip.md)
*   [Tabs](../components/Tabs.md)
*   [Card](../components/Card.md)
*   [Dialog](../components/Dialog.md)
*   [Menu](../components/Menu.md)
*   [Bottom Sheet](../components/BottomSheet.md)
*   [Navigation Drawer](../components/NavigationDrawer.md)
*   [Switch](../components/Switch.md)
*   [Date Picker](../components/DatePicker.md)
*   [Time Picker](../components/TimePicker.md)

##### `SurfaceColors` enums

If using Material Design Components is not an option in your use case, you may
want to consider getting those tonal surface colors on the fly, by using the
convenient enums we provide in the Material Library. For example, if you want to
get the color hex value of Surface1, you can do:

```
int colorSurface1 = SurfaceColors.SURFACE_1.getColor(context);
```

This will return the calculated tonal surface color corresponding to the
Surface1 definition and your `?attr/elevationOverlayColor` setting in your
themes.

##### `MaterialShapeDrawable` or `ElevationOverlayProvider` (*advanced*)

If you have a complicated use case, you can check if
`com.google.android.material.shape.MaterialShapeDrawable` or
`com.google.android.material.elevation.ElevationOverlayProvider` would serve
your needs. These two classes provide a set of APIs to help you calculate and
render blended colors according to different background colors and elevations,
with the same elevation overlay formula used across the Material libraries. Use
them carefully to ensure a consistent look and feel for your app.

## Using dynamic colors

Starting from Android S, the framework provides the ability to support dynamic
colors in your UI based on the user's wallpaper or color choice on the device.

To help in the application of dynamic colors, the Material 3 library provides 3
theme overlays to be used on the base Material 3 themes:

-   `ThemeOverlay.Material3.DynamicColors.Light`
-   `ThemeOverlay.Material3.DynamicColors.Dark`
-   `ThemeOverlay.Material3.DynamicColors.DayNight` (select day/night mode
    automatically.)

To make it easier to implement dynamic color solutions, the Material 3 library
provides a helper class to apply dynamic colors:
`com.google.android.material.color.DynamicColors`. There are several ways to use
this helper class according to different scenarios:

##### Apply dynamic colors to all activities in the app

In your application class’ `onCreate()` method, call:

```
DynamicColors.applyToActivitiesIfAvailable(this);
```

This will register an `ActivityLifeCycleCallbacks` to your application and if
the app is running on Android S+ it will attempt to apply the dynamic color
theme overlay specified by `R.attr.dynamicColorThemeOverlay` in your
app/activity theme in the `onActivityPreCreated()` callback method.

If you are using Material 3 themes, `R.attr.dynamicColorThemeOverlay` will be
`ThemeOverlay.Material3.DynamicColors.Light/Dark` by default.

You can also have finer control over theme overlay deployment by providing a
precondition when calling the method:

```
DynamicColors.applyToActivitiesIfAvailable(this, (activity, themeResId) -> {
  // ...implement your own logic here. Return `true` if dynamic colors should be applied.
});
```

Or provide your own customized dynamic color theme overlays, likely inheriting
from the Material3 theme overlays above, by doing:

```
DynamicColors.applyToActivitiesIfAvailable(this, R.style.ThemeOverlay_MyApp_DynamicColors_DayNight);
```

**Note:** If you are applying your own non-dynamic theme overlays to override
Material colors in certain activities, fragments, layouts, etc., the dynamic
colors will be overwritten by your theme overlays as well because dynamic colors
are applied *before* activities are created. If that’s not the desired behavior
you want, you will need to either stop overriding Material colors in your theme
overlays or customize them with a proper dynamic color definition.

##### Apply dynamic colors to a specific activity

You can also opt to apply dynamic colors to a few specific activities, by
calling the following method in your activities’ `onCreate()` method (or before
you inflate anything from it):

```
DynamicColors.applyIfAvailable(this);
```

If the app is running on Android S+, dynamic colors will be applied to the
activity. You can also apply a custom theme overlay or a precondition as
depicted in the application section above.

##### Apply dynamic colors to a specific fragment/view

Applying dynamic colors to a few of the views in an activity is more complex.
The easiest solution is to create a themed context to create the view. We
provide a helper method for this purpose:

```
context = DynamicColors.wrapContextIfAvailable(context);
```

This method will return a context with the dynamic color theme overlay applied,
if dynamic colors are available on the device.

**Note:** No matter which approach you follow, you will have to have M3 base
themes (for example `Theme.Material3.DayNight.NoActionBar`) applied first to
make dynamic color theme overlays work, because they use all of the same color
theme attributes.

## Custom Colors

Material 3 uses a purple hue for default accent colors if dynamic colors are not
available. If you need different brand colors in your app, you may want to
define custom colors for your theme. Keep in mind that the default Material 3
styles generally use colors in the following combinations:

| Container Color           | Content Color
| ------------------------- | -------------------------------------------
| Surface / Surface Variant | On Surface / On Surface Variant / Primary / Secondary / Error
| Primary                   | On Primary
| Primary Container         | On Primary Container
| Secondary                 | On Secondary
| Secondary Container       | On Secondary Container
| Tertiary                  | On Tertiary
| Tertiary Container        | On Tertiary Container

So if you change one of those colors, you may want to change their related
colors to maintain the visual consistency and the contrast requirement of
Material components.

These color theme attributes can be customized in a theme that inherits from one
of the "baseline" `Theme.Material3.*` themes. Dynamic color theme overlays
(`ThemeOverlay.Material3.DynamicColors.*`) can be applied on top of a customized
"baseline" theme.

**[Important]** Be careful to maintain the same luminance level when creating
custom colors so the contrast requirement won't be broken. For example, since
the default Primary color in light theme has a luminance level of 40, it would
be best to use a luminance level of 40 with your custom Primary color as well,
in order to avoid accidentally breaking the contrast requirement in certain
components.

#### Defining custom colors

When creating app colors, do not use the same name as the color slot:

```xml
<resources>
  <color name="color_primary">...</color>
  <color name="color_surface">...</color>
</resources>
```

Instead use literal names relevant to the RGB value, for example:

```xml
<resources>
  <color name="brand_blue">...</color>
  <color name="brand_grey">...</color>
</resources>
```

#### Theming an Individual Component

If you want to change the color of just one instance of a component without
changing theme-level attributes, create a new component style that extends from
a `Widget.Material3` style.

For example, if you want to change MaterialButton so that it uses
`colorSecondary` for its background tint rather than the default color, define
your own button style that extends from a Material Design style and set the
mapping yourself:

```xml
<style name="Widget.MyApp.Button" parent="Widget.Material3.Button">
  <item name="backgroundTint">?attr/colorSecondary</item>
</style>
```

You would then apply the `Widget.MyApp.Button` style to any buttons you want to
have this alternate style.

#### Theming All Instances of One Component

If you want to change the default styles for **all** instances of a component,
for example 'MaterialButton', modify the `materialButtonStyle` attribute in your
theme.

```xml
<style name="Theme.MyApp" parent="Theme.Material3.Light.NoActionBar">
  ...
  <item name="materialButtonStyle">@style/Widget.MyApp.Button</item>
  ...
</style>
```

This will set the default style of any 'MaterialButtons' in the app to
`Widget.MyApp.Button`. Similar default style attributes exist for most other
components, for example `tabStyle`, `chipStyle`, and `textInputStyle`.

#### Theme Attribute Mapping

All MDC-Android components have been updated to use the theme attributes
described above, when applicable.

To understand how the high-level theme attributes map to specific parts of each
component, please refer directly to the component's documentation.

## Using Color Harmonization

Color harmonization solves the problem of "How do we ensure any particular
Reserved color (eg. those used for semantic or brand) looks good next to a
user's dynamically-generated color?"

To make it easier to implement color harmonization to ensure visual cohesion in
any M3 themes with dynamic colors enabled, MDC-Android provides the following
`MaterialColors` helper method in the `com.google.android.material.color`
package:

##### Harmonize a color with `colorPrimary`

In your application class or activity/fragment/view, call:

```
int harmonizedColor = MaterialColors.harmonizeWithPrimary(context, colorToHarmonize);
```

This method will find the context theme's `colorPrimary`, and shift the hue of
the input color, `colorToHarmonize`, towards the hue of `colorPrimary`. This
will leave the input color recognizable while still meaningfully shifting it
towards `colorPrimary`.

**Note:** If the input color `colorToHarmonize` is the same as `colorPrimary`,
harmonization won't happen and `colorToHarmonize` will be returned.

## Color role mapping utilities

M3 schemes also include roles for much of the semantic meaning and other
conventional uses of color that products are identified with. A single color
scheme currently consists of 4 roles for utility colors. The `ColorRoles` class
is available in the `com.google.android.material.color` package and has getter
methods defined for each utility color role. The luminance level value [0, 100]
will be shifted for each color role based on the theme `LightTheme` or
`DarkTheme`, and the Hue and Chroma values of the color role will stay the same.

#### `ColorRoles` properties

| Name                    | Method                 | Description
| ----------------------- | ---------------------- | -------------------------
| **Accent**              | `getAccent`            | The accent color, used as the main color from the color role.
| **On Accent**           | `getOnAccent`          | Used for content such as icons and text on top of the Accent color.
| **Accent Container**    | `getAccentContainer`   | Used with less emphasis than the accent color.
| **On Accent Container** | `getOnAccentContainer` | Used for content such as icons and text on top of the accent_container color.

The library provides the following two helper methods in the `MaterialColors`
class which return the above-mentioned `ColorRoles` object:

```
ColorRoles colorRoles = MaterialColors.getColorRoles(context, color);
```

or

```
ColorRoles colorRoles = MaterialColors.getColorRoles(color, /* isLightTheme= */ booleanValue);
```
