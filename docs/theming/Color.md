<!--docs:
title: "Color Theming"
layout: detail
section: theming
excerpt: "Color Theming"
iconId: color
path: /theming/color/
-->

# Color theming

The Material 3 color theming system can be used to create a color scheme that
reflects your brand or style.

The Material 3 color theming system uses an organized approach to apply colors
to your UI. Material components use theme colors and their variations to style
backgrounds, text, and more.

## Design and API Documentation

-   [Material Design guidelines: Color](https://m3.material.io/styles/color/overview/)

## Using The Color Theming System

All Material 3 components use a `Widget.Material3` style, and these styles
reference color attributes from the Material 3 theme (`Theme.Material3`). It is
easy to customize those color attributes across your app by simply overriding
them in your theme. We provide three accent color groups (Primary, Secondary,
Tertiary), each with 4-5 color roles that you can customize to represent your
brand color:

<!-- Auto-generated accent color table starts. Do not edit below or remove this comment. -->

Color role                 | Android attribute            | Light baseline | Light dynamic 31-33 | Light dynamic 34+                   | Dark baseline | Dark dynamic 31-33 | Dark dynamic 34+
-------------------------- | ---------------------------- | -------------- | ------------------- | ----------------------------------- | ------------- | ------------------ | ----------------
Primary                    | colorPrimary                 | primary40      | system_accent1_600  | system_primary_light                | primary80     | system_accent1_200 | system_primary_dark
On Primary                 | colorOnPrimary               | white          | system_accent1_0    | system_on_primary_light             | primary20     | system_accent1_800 | system_on_primary_dark
Primary Container          | colorPrimaryContainer        | primary90      | system_accent1_100  | system_primary_container_light      | primary30     | system_accent1_700 | system_primary_container_dark
On Primary Container       | colorOnPrimaryContainer      | primary10      | system_accent1_900  | system_on_primary_container_light   | primary90     | system_accent1_100 | system_on_primary_container_dark
Inverse Primary            | colorPrimaryInverse          | primary80      | system_accent1_200  | system_primary_dark                 | primary40     | system_accent1_600 | system_primary_light
Primary Fixed              | colorPrimaryFixed            | primary90      | system_accent1_100  | system_primary_fixed                | primary90     | system_accent1_100 | system_primary_fixed
Primary Fixed Dim          | colorPrimaryFixedDim         | primary80      | system_accent1_200  | system_primary_fixed_dim            | primary80     | system_accent1_200 | system_primary_fixed_dim
On Primary Fixed           | colorOnPrimaryFixed          | primary10      | system_accent1_900  | system_on_primary_fixed             | primary10     | system_accent1_900 | system_on_primary_fixed
On Primary Fixed Variant   | colorOnPrimaryFixedVariant   | primary30      | system_accent1_700  | system_on_primary_fixed_variant     | primary30     | system_accent1_700 | system_on_primary_fixed_variant
Secondary                  | colorSecondary               | secondary40    | system_accent2_600  | system_secondary_light              | secondary80   | system_accent2_200 | system_secondary_dark
On Secondary               | colorOnSecondary             | white          | system_accent2_0    | system_on_secondary_light           | secondary20   | system_accent2_800 | system_on_secondary_dark
Secondary Container        | colorSecondaryContainer      | secondary90    | system_accent2_100  | system_secondary_container_light    | secondary30   | system_accent2_700 | system_secondary_container_dark
On Secondary Container     | colorOnSecondaryContainer    | secondary10    | system_accent2_900  | system_on_secondary_container_light | secondary90   | system_accent2_100 | system_on_secondary_container_dark
Secondary Fixed            | colorSecondaryFixed          | secondary90    | system_accent2_100  | system_secondary_fixed              | secondary90   | system_accent2_100 | system_secondary_fixed
Secondary Fixed Dim        | colorSecondaryFixedDim       | secondary80    | system_accent2_200  | system_secondary_fixed_dim          | secondary80   | system_accent2_200 | system_secondary_fixed_dim
On Secondary Fixed         | colorOnSecondaryFixed        | secondary10    | system_accent2_900  | system_on_secondary_fixed           | secondary10   | system_accent2_900 | system_on_secondary_fixed
On Secondary Fixed Variant | colorOnSecondaryFixedVariant | secondary30    | system_accent2_700  | system_on_secondary_fixed_variant   | secondary30   | system_accent2_700 | system_on_secondary_fixed_variant
Tertiary                   | colorTertiary                | tertiary40     | system_accent3_600  | system_tertiary_light               | tertiary80    | system_accent3_200 | system_tertiary_dark
On Tertiary                | colorOnTertiary              | white          | system_accent3_0    | system_on_tertiary_light            | tertiary20    | system_accent3_800 | system_on_tertiary_dark
Tertiary Container         | colorTertiaryContainer       | tertiary90     | system_accent3_100  | system_tertiary_container_light     | tertiary30    | system_accent3_700 | system_tertiary_container_dark
On Tertiary Container      | colorOnTertiaryContainer     | tertiary10     | system_accent3_900  | system_on_tertiary_container_light  | tertiary90    | system_accent3_100 | system_on_tertiary_container_dark
Tertiary Fixed             | colorTertiaryFixed           | tertiary90     | system_accent3_100  | system_tertiary_fixed               | tertiary90    | system_accent3_100 | system_tertiary_fixed
Tertiary Fixed Dim         | colorTertiaryFixedDim        | tertiary80     | system_accent3_200  | system_tertiary_fixed_dim           | tertiary80    | system_accent3_200 | system_tertiary_fixed_dim
On Tertiary Fixed          | colorOnTertiaryFixed         | tertiary10     | system_accent3_900  | system_on_tertiary_fixed            | tertiary10    | system_accent3_900 | system_on_tertiary_fixed
On Tertiary Fixed Variant  | colorOnTertiaryFixedVariant  | tertiary30     | system_accent3_700  | system_on_tertiary_fixed_variant    | tertiary30    | system_accent3_700 | system_on_tertiary_fixed_variant

<!-- Auto-generated accent color table ends. Do not edit below or remove this comment. -->

By changing these color attributes, you can easily change the styles of all the
Material components that use your theme.

The Material Design color theming system provides additional colors which don't
represent your brand, but define your UI and ensure accessible color
combinations. These additional color attributes are as follows:

<!-- Auto-generated additional color table starts. Do not edit below or remove this comment. -->

Color role                | Android attribute            | Light baseline    | Light dynamic 31-33                      | Light dynamic 34+                      | Dark baseline     | Dark dynamic 31-33                       | Dark dynamic 34+
------------------------- | ---------------------------- | ----------------- | ---------------------------------------- | -------------------------------------- | ----------------- | ---------------------------------------- | ----------------
Error                     | colorError                   | error40           | error40                                  | system_error_light                     | error80           | error80                                  | system_error_dark
On Error                  | colorOnError                 | white             | white                                    | system_on_error_light                  | error20           | error20                                  | system_on_error_dark
Error Container           | colorErrorContainer          | error90           | error90                                  | system_error_container_light           | error30           | error30                                  | system_error_container_dark
On Error Container        | colorOnErrorContainer        | error10           | error10                                  | system_on_error_container_light        | error90           | error90                                  | system_on_error_container_dark
Outline                   | colorOutline                 | neutral_variant50 | system_neutral2_500                      | system_outline_light                   | neutral_variant60 | system_neutral2_400                      | system_outline_dark
Outline Variant           | colorOutlineVariant          | neutral_variant80 | system_neutral2_200                      | system_outline_variant_light           | neutral_variant30 | system_neutral2_700                      | system_outline_variant_dark
Background                | android:colorBackground      | neutral98         | m3_ref_palette_dynamic_neutral_variant98 | system_background_light                | neutral6          | m3_ref_palette_dynamic_neutral_variant6  | system_background_dark
On Background             | colorOnBackground            | neutral10         | system_neutral1_900                      | system_on_background_light             | neutral90         | system_neutral1_100                      | system_on_background_dark
Surface                   | colorSurface                 | neutral98         | m3_ref_palette_dynamic_neutral_variant98 | system_surface_light                   | neutral6          | m3_ref_palette_dynamic_neutral_variant6  | system_surface_dark
On Surface                | colorOnSurface               | neutral10         | system_neutral1_900                      | system_on_surface_light                | neutral90         | system_neutral1_100                      | system_on_surface_dark
Surface Variant           | colorSurfaceVariant          | neutral_variant90 | system_neutral2_100                      | system_surface_variant_light           | neutral_variant30 | system_neutral2_700                      | system_surface_variant_dark
On Surface Variant        | colorOnSurfaceVariant        | neutral_variant30 | system_neutral2_700                      | system_on_surface_variant_light        | neutral_variant80 | system_neutral2_200                      | system_on_surface_variant_dark
Inverse Surface           | colorSurfaceInverse          | neutral20         | system_neutral1_800                      | system_surface_dark                    | neutral90         | system_neutral1_100                      | system_surface_light
Inverse On Surface        | colorOnSurfaceInverse        | neutral95         | system_neutral1_50                       | system_on_surface_dark                 | neutral20         | system_neutral1_800                      | system_on_surface_light
Surface Bright            | colorSurfaceBright           | neutral98         | m3_ref_palette_dynamic_neutral_variant98 | system_surface_bright_light            | neutral24         | m3_ref_palette_dynamic_neutral_variant24 | system_surface_bright_dark
Surface Dim               | colorSurfaceDim              | neutral87         | m3_ref_palette_dynamic_neutral_variant87 | system_surface_dim_light               | neutral6          | m3_ref_palette_dynamic_neutral_variant6  | system_surface_dim_dark
Surface Container         | colorSurfaceContainer        | neutral94         | m3_ref_palette_dynamic_neutral_variant94 | system_surface_container_light         | neutral12         | m3_ref_palette_dynamic_neutral_variant12 | system_surface_container_dark
Surface Container Low     | colorSurfaceContainerLow     | neutral96         | m3_ref_palette_dynamic_neutral_variant96 | system_surface_container_low_light     | neutral10         | system_neutral2_900                      | system_surface_container_low_dark
Surface Container Lowest  | colorSurfaceContainerLowest  | white             | system_neutral2_0                        | system_surface_container_lowest_light  | neutral4          | m3_ref_palette_dynamic_neutral_variant4  | system_surface_container_lowest_dark
Surface Container High    | colorSurfaceContainerHigh    | neutral92         | m3_ref_palette_dynamic_neutral_variant92 | system_surface_container_high_light    | neutral17         | m3_ref_palette_dynamic_neutral_variant17 | system_surface_container_high_dark
Surface Container Highest | colorSurfaceContainerHighest | neutral90         | system_neutral2_100                      | system_surface_container_highest_light | neutral22         | m3_ref_palette_dynamic_neutral_variant22 | system_surface_container_highest_dark

<!-- Auto-generated additional color table ends. Do not edit below or remove this comment. -->

## Using Surface Colors

As of version 1.11.0-alpha02 and above, Material3 components will use the
following tonal surface color roles by default (instead of elevation overlays
which involved blending the Surface and Primary colors):

-   `colorSurfaceContainer`
-   `colorSurfaceContainerLow`
-   `colorSurfaceContainerHigh`
-   `colorSurfaceContainerLowest`
-   `colorSurfaceContainerHighest`
-   `colorSurfaceDim`
-   `colorSurfaceBright`

**Note:** When resolving these tonal surface colors above, please use
`MaterialColors.getColor()` to avoid resolving unexpected colors.

These color roles are used as the default container/background color in the
components below. Please check the
[Surface colors guidelines](https://m3.material.io/styles/color/the-color-system/color-roles#0abbf8b7-61e1-49ee-9f97-4967beb1e4fe)
for more details.

*   [Bottom App Bar](../components/BottomAppBar.md)
*   [Bottom Navigation](../components/BottomNavigation.md)
*   [Bottom Sheet](../components/BottomSheet.md)
*   [Button](../components/Button.md)
*   [Card](../components/Card.md)
*   [Chip](../components/Chip.md)
*   [Date Picker](../components/DatePicker.md)
*   [Dialog](../components/Dialog.md)
*   [Floating Action Button](../components/FloatingActionButton.md)
*   [Menu](../components/Menu.md)
*   [Navigation Drawer](../components/NavigationDrawer.md)
*   [Search](../components/Search.md)
*   [Switch](../components/Switch.md)
*   [Tabs](../components/Tabs.md)
*   [Text Field](../components/TextField.md)
*   [Time Picker](../components/TimePicker.md)
*   [Top App Bar](../components/TopAppBar.md)

### Using Surface with Elevation Overlay

**Note:** Surface with elevation overlay has been replaced with tonal surface
colors in Material's components. If you use the Material styles or default
styles in the Material theme, the container color will no longer respond to the
elevation overlay color. The elevation overlay utility has not been removed from
the implementation. If the surface with elevation overlay is needed for
migration purposes, please set the corresponding container color to
`?attr/colorSurface`. The maintenance to the elevation overlay has been
discontinued.

Surface with elevation overlay uses primary colored overlays to present a visual
hierarchy with different elevations in both light and dark themes. Material 3
themes enable this by default with setting `?attr/elevationOverlayColor` to
`?attr/colorPrimary`.

Elevation overlays use the following theme attributes:

Attribute Name            | Description                                                                        | Default Value
------------------------- | ---------------------------------------------------------------------------------- | -------------
`elevationOverlayEnabled` | Whether the elevation overlay functionality is enabled.                            | `true`
`elevationOverlayColor`   | The color used for the elevation overlays, applied at an alpha based on elevation. | `colorPrimary`

If inheriting from the `Theme.Material3` theme or a descendant, you most likely
do not have to set these attributes yourself because Material themes use the
defaults shown above.

The elevation overlays will be applied to surface colors to create tonal
variations. Within the Material 3 color palette, there are five predefined
surface tonal variations (Surface1-5) which are available to be applied to
different Material 3 components. However, these surface tonal colors are **NOT**
implemented as color resources, but their actual color values are calculated *on
the fly* with the `?attr/elevationOverlayColor`, as mentioned above.

In a practical scenario, you have three ways to include those tonal surface
colors in your app:

##### Material Design Components

The easiest way to use surface colors with tonal variations is with Material
Design Components, which have built-in support for surface with elevation
overlays. You can customize surface colors of those components by changing their
elevation.

Here is a list of Material components that support elevation overlays when
`?attr/colorSurface` is set as the container/background color.

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

```java
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

```java
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

```java
DynamicColors.applyToActivitiesIfAvailable(this, (activity, themeResId) -> {
  // ...implement your own logic here. Return `true` if dynamic colors should be applied.
});
```

Or provide your own customized dynamic color theme overlays, likely inheriting
from the Material3 theme overlays above, by doing:

```java
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

```java
DynamicColors.applyToActivityIfAvailable(this);
```

If the app is running on Android S+, dynamic colors will be applied to the
activity. You can also apply a custom theme overlay or a precondition as
depicted in the application section above.

##### Apply dynamic colors to all activities in the app using `DynamicColorsOptions`

You also have the option to apply dynamic colors to all activities in the app by
passing in a `DynamicColorsOptions` object. When constructing
`DynamicColorsOptions`, you may optionally specify a customized theme overlay,
likely inheriting from the `Material3` theme overlays above and/or a
precondition, to have finer control over theme overlay deployment. You may also
optionally specify an `OnAppliedCallback` function, which will be called after
dynamic colors have been applied:

```java
DynamicColorsOptions dynamicColorsOptions =
    new DynamicColorsOptions.Builder()
        .setThemeOverlay(themeOverlay)
        .setPrecondition(precondition)
        .setOnAppliedCallback(onAppliedCallback)
        .build()
DynamicColors.applyToActivitiesIfAvailable(application, dynamicColorsOptions);
```

##### Apply dynamic colors to a specific activity using `DynamicColorsOptions`

You can also apply dynamic colors to a specific activity in the app by passing
in the specific activity and a `DynamicColorsOptions` object:

```java
DynamicColorsOptions dynamicColorsOptions =
    new DynamicColorsOptions.Builder()
        .setThemeOverlay(themeOverlay)
        .setPrecondition(precondition)
        .setOnAppliedCallback(onAppliedCallback)
        .build()
DynamicColors.applyToActivityIfAvailable(activity, dynamicColorsOptions);
```

##### Apply dynamic colors to a specific fragment/view

Applying dynamic colors to a few of the views in an activity is more complex.
The easiest solution is to create a themed context to create the view. We
provide a helper method for this purpose:

```java
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

Container Color           | Content Color
------------------------- | -------------
Surface / Surface Variant | On Surface / On Surface Variant / Primary / Secondary / Error
Primary                   | On Primary
Primary Container         | On Primary Container
Secondary                 | On Secondary
Secondary Container       | On Secondary Container
Tertiary                  | On Tertiary
Tertiary Container        | On Tertiary Container

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

##### Harmonize a color with `colorPrimary`

To make it easier to implement color harmonization to ensure visual cohesion in
any M3 themes with dynamic colors enabled, MDC-Android provides the following
`MaterialColors` helper method in the `com.google.android.material.color`
package:

In your application class or activity/fragment/view, call:

```java
int harmonizedColor = MaterialColors.harmonizeWithPrimary(context, colorToHarmonize);
```

This method will find the context theme's `colorPrimary`, and shift the hue of
the input color, `colorToHarmonize`, towards the hue of `colorPrimary`. This
will leave the input color recognizable while still meaningfully shifting it
towards `colorPrimary`.

**Note:** If the input color `colorToHarmonize` is the same as `colorPrimary`,
harmonization won't happen and `colorToHarmonize` will be returned.

##### Color Resources Harmonization

We've provided the `HarmonizedColors` and `HarmonizedColorsOptions` classes in
the `com.google.android.material.color` package for color resources
harmonization. `HarmonizedColorsOptions.Builder` is a Builder class and to
construct a `HarmonizedColorsOptions`. You can optionally pass in an array of
resource ids for the color resources you'd like to harmonize, a
`HarmonizedColorAttributes` object and/or the color attribute to harmonize with:

```java
HarmonizedColorsOptions options =
    new HarmonizedColorsOptions.Builder()
        .setColorResourceIds(colorResources)
        .setColorAttributes(HarmonizedColorAttributes.create(attributes))
        .setColorAttributeToHarmonizeWith(colorAttributeResId)
        .build();
```

In the `HarmonizedColorsOptions` class, we also provided a convenience method
`createMaterialDefaults()`, with Error colors being harmonized by default.

```java
HarmonizedColorsOptions options = HarmonizedColorsOptions.createMaterialDefaults();
HarmonizedColors.applyToContextIfAvailable(context, options);
```

If you need to harmonize color resources at runtime to a context and use the
harmonized color resources in xml, call:

```java
HarmonizedColors.applyToContextIfAvailable(context, harmonizedColorsOptions);
```

To return a new `Context` with color resources being harmonized, call:

```java
HarmonizedColors.wrapContextIfAvailable(context, harmonizedColorsOptions);
```

##### `HarmonizedColorAttributes`

Static Factory Methods                                                   | Description
------------------------------------------------------------------------ | -----------
**HarmonizedColorAttributes.create(int[] attributes)**                   | Provides an int array of attributes for harmonization
**HarmonizedColorAttributes.create(int[] attributes, int themeOverlay)** | Provides a themeOverlay, along with the int array of attributes from the theme overlay for harmonization.
**HarmonizedColorAttributes.createMaterialDefaults()**                   | Provides a default implementation of `HarmonizedColorAttributes`, with Error colors being harmonized.

If the first static factory method is used, the color resource's id and value of
the attribute will be resolved at runtime and the color resources will be
harmonized.

**Note:** The way we harmonize color attributes is by looking up the color
resource the attribute points to, and harmonizing the color resource directly.
If you are looking to harmonize only color resources, in most cases when
constructing `HarmonizedColorsOptions`, the
`setColorResourceIds(colorResources)` method should be enough.

If you're concerned about accidentally overwriting color resources, the second
static factory method should be used. In this method, instead of the color
resource that the color attribute is pointing to in the main theme/context being
harmonized directly, the color resources pointed by the color attributes after
the theme overlay is applied will be harmonized. In the theme overlay, the color
resources pointed by the color attributes are dummy values, to avoid color
resources that the color attributs are pointing to in the main theme/context be
overridden.

Here is an example of how we harmonize Error colors with theme overlay, to avoid
accidentally overriding the resources from the main theme/context. We have an
array of color attributes defined as:

```java
private static final int[] HARMONIZED_MATERIAL_ATTRIBUTES =
      new int[] {
        R.attr.colorError,
        R.attr.colorOnError,
        R.attr.colorErrorContainer,
        R.attr.colorOnErrorContainer
      };
```

And a theme overlay defined as:

```xml
<style name="ThemeOverlay.Material3.HarmonizedColors" parent="">
    <item name="colorError">@color/material_harmonized_color_error</item>
    <item name="colorOnError">@color/material_harmonized_color_on_error</item>
    <item name="colorErrorContainer">@color/material_harmonized_color_error_container</item>
    <item name="colorOnErrorContainer">@color/material_harmonized_color_on_error_container</item>
</style>
```

With this theme overlay, instead of directly overwriting the resources that
`colorError`, `colorOnError`, `colorErrorContainer`, and `colorOnErrorContainer`
point to in the main theme/context, we would:

1.  look up the resource values in the `Context` themed by the theme overlay
2.  retrieve the harmonized resources with Primary
3.  override `@color/material_harmonized_color_error`,
    `@color/material_harmonized_color_on_error`, etc. with the harmonized colors

That way the Error roles in the theme overlay would point to harmonized
resources.

If you would like to harmonize additional color attributes along with
harmonizing Error roles by default, the `HarmonizedColorAttributes` would look
like:

```java
HarmonizedColorAttributes.create(
    ArrayUtils.addAll(createMaterialDefaults().getAttributes(), myAppAttributes),
    R.style.ThemeOverlay_MyApp_HarmonizedColors);
```

**Note:** For your custom theme overlay
`R.style.ThemeOverlay_MyApp_HarmonizedColors`, we recommend you to extend from
our theme overlay at `R.style.ThemeOverlay_Material3_HarmonizedColors`.

You can also use color resources harmonization separate from dynamic colors if
needed, but the general use case for color resources harmonization is after
dynamic colors have been applied, to ensure visual cohesion for reserved colors
(e.g. semantic colors) in a M3 theme with dynamic colors enabled. A Material
suggested default when applying dynamic colors, is to harmonize M3 Error colors
in the callback when constructing `DynamicColorsOptions`:

```java
DynamicColorsOptions dynamicColorsOptions =
    new DynamicColorsOptions.Builder(activity)
        ...
        .setOnAppliedCallback(
            activity ->
                HarmonizedColors.applyToContextIfAvailable(
                    activity,
                    HarmonizedColorsOptions.createMaterialDefaults()))
        .build()
DynamicColors.applyToActivityIfAvailable(activity, dynamicColorsOptions);
```

For color ressources harmonization in a fragment/view, you would use the context
generated from applying dynamic colors when constructing
`HarmonizedColorsOptions` and call
`wrapContextIfAvailable(harmonizedColorsOptions)` to apply resources
harmonization:

```java
Context newContext = DynamicColors.wrapContextIfAvailable(getContext());

HarmonizedColorsOptions options =
    new HarmonizedColorsOptions.Builder()
        .setColorResources(colorResources)
        .build();
Context harmonizedContext = HarmonizedColors.wrapContextIfAvailable(newContext, options);
// Usage example with the new harmonizedContext.
MaterialColors.getColor(harmonizedContext, R.attr.customColor, -1);
```

**Note:** This is only supported for API 30 and above.

## Color role mapping utilities

M3 schemes also include roles for much of the semantic meaning and other
conventional uses of color that products are identified with. A single color
scheme currently consists of 4 roles for utility colors. The `ColorRoles` class
is available in the `com.google.android.material.color` package and has getter
methods defined for each utility color role. The luminance level value [0, 100]
will be shifted for each color role based on the theme `LightTheme` or
`DarkTheme`, and the Hue and Chroma values of the color role will stay the same.

#### `ColorRoles` properties

Name                    | Method                 | Description
----------------------- | ---------------------- | -----------
**Accent**              | `getAccent`            | The accent color, used as the main color from the color role.
**On Accent**           | `getOnAccent`          | Used for content such as icons and text on top of the Accent color.
**Accent Container**    | `getAccentContainer`   | Used with less emphasis than the accent color.
**On Accent Container** | `getOnAccentContainer` | Used for content such as icons and text on top of the accent_container color.

The library provides the following two helper methods in the `MaterialColors`
class which return the above-mentioned `ColorRoles` object:

```java
ColorRoles colorRoles = MaterialColors.getColorRoles(context, color);
```

or

```java
ColorRoles colorRoles = MaterialColors.getColorRoles(color, /* isLightTheme= */ booleanValue);
```

## Content-based Dynamic Colors

Content-based color describes the color system’s capability to generate and
apply a color scheme based on in-app content. In-app content colors can be
derived from a range of sources, such as album artwork, a brand logo, or a video
tile.

##### *Use Content-based Dynamic Colors*

A single source color is extracted from a bitmap and then used to derive five
key colors. Specific tones are mapped into specific color roles that are then
mapped to Material components.

During this process, chroma fidelity enables Material colors to flex to
consistently achieve desired chroma, whether high or low. It maintains color
schemes’ integrity, so existing products will not break. A content scheme then
produces the range of tones needed for both light and dark theme applications.

We have provided the following two APIs in the `DynamicColorsOptions` class.

API Method                     | Description
------------------------------ | -----------
#setContentBasedSource(Bitmap) | Provides a Bitmap from which a single source color is extracted as input
#setContentBasedSource(int)    | Provides a single source color as input

An example usage for applying content-based dynamic colors to a specific
activity can be seen below. Since we are overriding color resources in xml at
runtime, make sure the method is invoked before you inflate the view to take
effect.

```java
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.DynamicColors;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    // Invoke before the view is inflated in your activity.
    DynamicColors.applyToActivityIfAvailable(
        this,
        new DynamicColorsOptions.Builder()
            .setContentBasedSource(bitmap)
            .build()
    );

    setContentView(R.layout.xyz);
  }
```

An example usage for applying content-based dynamic colors to a specific
fragment/view:

```java
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.DynamicColors;

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

    Context context = DynamicColors.wrapContextIfAvailable(
            requireContext(),
            new DynamicColorsOptions.Builder()
                .setContentBasedSource(sourceColor)
                .build());

    return layoutInflater.cloneInContext(context).inflate(R.layout.xyz, viewGroup, false);
  }
```

This method will return a context with a content-based dynamic colors theme
overlay applied, if Dynamic Colors are available on the device.

**Important:** Please note that this feature is only available for S+.

## Contrast Control

Tone quantifies the lightness or darkness of colors. It's one foundational
dimension of the Material color system and schemes. The difference in tone
between two colors creates visual contrast. A greater difference creates higher
contrast. Color contrast control allows users to adjust their UI contrast levels
in the system so they can comfortably see and use digital experiences.

##### *Use Contrast Control - Dynamic*

You will get contrast control for free if you already use dynamic colors.
Material color roles enforce contrast through tone while maintaining hierarchy
and visual relationships. The color value of a Material color role (e.g.
primaryContainer) can systematically adjust in tone to achieve sufficient
contrast.

**Important:** Please note that this feature is only available for U+.

##### *Use Contrast Control - non-Dynamic*

If you are not using dynamic colors and would like to use contrast control for
your branded or custom themes, we have created the following API in the
`ColorContrast` class that you can call manually.

*Apply contrast to all activities in the app*

In your application class’ `onCreate()` method, call:

```java
ColorContrast.applyToActivitiesIfAvailable(
        this,
        new ColorContrastOptions.Builder()
            .setMediumContrastThemeOverlay(mediumContrastThemeOverlayResId)
            .setHighContrastThemeOverlay(highContrastThemeOverlayResId)
            .build();
);
```

Note that if you want contrast support for both light and dark theme, then for
`mediumContrastThemeOverlayResId` and `highContrastThemeOverlayResId`, you
should pass in a DayNight theme, which will help facilitate easy switching
between your app’s Light and Dark theme.

##### *Use Contrast Control - Custom Colors*

If you have custom colors in your app that would like to obey contrast changes
from the system, whether or not you are using dynamic colors, they should be
included in the abovementioned theme overlays for medium and high contrast
support. To make your custom colors obey contrast for all activities in the app,
please refer to the API from the section above.
