<!--docs:
title: "Dark Theme"
layout: detail
section: theming
excerpt: "Dark Theme"
iconId: dark
path: /theming/dark/
-->

# Dark theme

The [Material dark theme system][dark-theme-mdc-spec] can be used to create a
beautiful and functional dark theme for your app. A dark theme generally
consists of dark background colors and light foreground colors for elements such
as text and iconography.

Benefits of a dark theme include: improved battery power conservation for
devices with OLED screens; reduced eye strain; and better visibility in
low-light environments.

Starting with [Android Q][dark-theme-dac-docs], users are now able to switch
their device to a dark theme via a new system setting, which applies to both the
Android system UI and apps running on the device.

## Design and API Documentation

-   [Material Design guidelines: Dark Theme][dark-theme-mdc-spec]
-   [Android Q Dark Theme Documentation][dark-theme-dac-docs]
-   [AppCompat DayNight Documentation][daynight-appcompat-docs]

## Setup

Before you can use the Material dark theme functionality, you need to add a
dependency to the Material Components for Android library. For more information,
go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

In order to support a dark theme for Android Q and above, make sure you are
depending on the latest version of the
[Material Android library][maven-repo-mdc], and update your app theme to inherit
from `Theme.Material3.DayNight` (or one of its descendants). For example:

**res/values/themes.xml**

```xml
<style name="Theme.MyApp" parent="Theme.Material3.DayNight">
    <!-- ... -->
</style>
```

Alternatively, if you want to define separate `Light` and `Dark` themes for your
app, you can inherit from `Theme.Material3.Light` in the `res/values` directory,
and `Theme.Material3.Dark` in the `res/values-night` directory:

**res/values/themes.xml**

```xml
<style name="Theme.MyApp" parent="Theme.Material3.Light">
    <!-- ... -->
</style>
```

**res/values-night/themes.xml**

```xml
<style name="Theme.MyApp" parent="Theme.Material3.Dark">
    <!-- ... -->
</style>
```

The `Theme.Material3.Dark` theme is a static dark theme, whereas
`Theme.Material3.DayNight` is a more dynamic theme which will help facilitate
easy switching between your app's `Light` and `Dark` theme. If using a
`DayNight` theme, you can define one app theme that references color resources,
which can be overridden in the `values-night` directory if needed.

## Catalog

To see how Material components adapt in a dark theme, build and run the
[Catalog app](../catalog-app.md) and enable a dark theme in one of the following
ways:

*   Any API Level: Settings gear menu icon on Catalog home and demo screens
*   Android Q: `Settings > Display > Dark Theme` (or Dark Theme tile in Notification Tray)
*   Android P: `Settings > System > Developer options > Night mode`

## Color palette

At the core of any dark theme is a color palette that uses dark background
colors and light foreground colors. The Material `Dark` themes make use of the
[Material Color System](Color.md), in order to provide default dark theme values
for neutral palette colors such as `android:colorBackground` and `colorSurface`.

The baseline Material `Dark` theme background and surface colors are dark grey
instead of black, which increases visibility for shadows and also reduces eye
strain for light text.

The Material `Dark` themes also provide adjusted defaults for the baseline
branded palette, including `colorPrimary`, `colorSecondary`, `colorTertiary`,
and more. See the [Material Dark Theme spec][dark-theme-mdc-spec-ui-application]
for guidance on how you can adjust your brand colors for a dark theme.

## Elevation overlays

**Note:** Surface with elevation overlays has been replaced in Material
components with the
[tonal surface color system](./Color.md#using-surface-colors).

In addition to the color palette adjustments mentioned above, communicating the
hierarchy of a UI via elevation requires some dark theme-specific
considerations.

Shadows are less effective in an app using a dark theme, because they will have
less contrast with the dark background colors and will appear to be less
visible. In order to compensate for this, Material surfaces become lighter and
more colorful at higher elevations, when they are closer to the implied light
source.

This is accomplished via elevation overlays, which are semi-transparent
(`colorPrimary`) overlays that are conceptually placed on top of the surface
color. The semi-transparent alpha percentage is calculated using an equation
based on elevation, which results in higher alpha percentages at higher
elevations, and therefore lighter surfaces.

**Note:** we avoid overdraw with the elevation overlays by calculating a
composite blend of the surface color with the overlay color and using that as
the surface's background, instead of drawing another layer on the canvas.

### Affected components

The following is a list of Material components that support elevation overlays,
because they use `colorSurface` for their background and can be elevated:

*   [Top App Bar](../components/TopAppBar.md)
*   [Bottom App Bar](../components/BottomAppBar.md)
*   [Bottom Navigation](../components/BottomNavigation.md)
*   [Navigation Rail](../components/NavigationRail.md)
*   [Navigation Drawer](../components/NavigationDrawer.md)
*   [Bottom Sheet](../components/BottomSheet.md)
*   [Dialog](../components/Dialog.md)
*   [Date Picker](../components/DatePicker.md)
*   [Time Picker](../components/TimePicker.md)
*   [Menu](../components/Menu.md)
*   [Tabs](../components/Tabs.md)
*   [Card](../components/Card.md)
*   [FAB](../components/FloatingActionButton.md)
*   [Button](../components/Button.md)
*   [Chip](../components/Chip.md)
*   [Switch](../components/Switch.md)

### Theme attributes

In order to facilitate some orchestration around the elevation overlays, we have
the following theme attributes:

Attribute Name              |Description                                                                          |Default Value
----------------------------|-------------------------------------------------------------------------------------|-------------
`elevationOverlayEnabled`   |Whether the elevation overlay functionality is enabled.                              |`true` in `Light` and `Dark` themes
`elevationOverlayColor`     |The color used for the elevation overlays, applied at an alpha based on elevation.   |`colorPrimary`

**Note:** If inheriting from a `Theme.Material3.*` theme, you most likely do not
have to set these attributes yourself because the Material themes already set up
the defaults, above.

### Custom views and non-Material Components

If you would like to apply elevation overlays to your custom views or any
non-Material views that are elevated surfaces, you can use the
`MaterialShapeDrawable` or `ElevationOverlayProvider` APIs.

#### MaterialShapeDrawable

The key to supporting elevation overlays in a custom view is creating a
`MaterialShapeDrawable` with the overlay support enabled via
`MaterialShapeDrawable#createWithElevationOverlay`, and setting it as the
background of your view.

Next, override the `View#setElevation` method and forward the elevation passed
in to your `MaterialShapeDrawable` background's `setElevation` method.

`MaterialShapeDrawable` is the preferred approach for custom views because it
will keep track of the elevation value for you, and factor that into the overlay
any time elevation changes. You don't have to worry about incorrectly
compounding the overlays multiple times.

#### ElevationOverlayProvider

If you have a case where the elevation value is more static and you would like
to get the corresponding elevation overlay color (perhaps to color an existing
view), then you can use `ElevationOverlayProvider`.

If elevation overlays are enabled at the theme level, the
`ElevationOverlayProvider#compositeOverlayWithThemeSurfaceColorIfNeeded` method
will return `colorSurface` with the overlay color blended in at an alpha level
based on the elevation passed in. Otherwise, it will simply return
`colorSurface`, so that you can use the result of this method without needing
any additional orchestration logic.

If you need to blend the overlays with an arbitrary color or an adjusted surface
color, or you need to get access to lower level values such as the overlay alpha
percentages, take a look at the other `ElevationOverlayProvider` methods
including `compositeOverlayIfNeeded`, `compositeOverlay`, and
`calculateOverlayAlpha`.

#### Absolute Elevation

When calculating the elevation overlay alpha percentage, Material components
factor in the absolute elevation of their parent view. This is because the
distance from the light source is the driving factor behind elevation overlays.

If you need to factor in absolute elevation in a custom view that supports
overlays, you can use the `MaterialShapeUtils#setParentAbsoluteElevation`
methods when using a `MaterialShapeDrawable` background. For example:

```java
@Override
protected void onAttachedToWindow() {
  super.onAttachedToWindow();

  MaterialShapeUtils.setParentAbsoluteElevation(this);
}
```

Alternatively, you could use the `ElevationOverlayProvider` composite methods
that take in a `View` parameter or the `getParentAbsoluteElevation` method.

**Note:** This means that you should consider accessibility contrast ratios for
text and iconography, when deeply nesting elevated Material components and views
that support elevation overlays.

[dark-theme-mdc-spec]: https://material.io/design/color/dark-theme.html
[dark-theme-mdc-spec-ui-application]: https://material.io/design/color/dark-theme.html#ui-application
[dark-theme-mdc-spec-custom-application]: https://material.io/design/color/dark-theme.html#custom-application
[dark-theme-dac-docs]: https://developer.android.com/preview/features/darktheme
[daynight-appcompat-docs]: https://medium.com/androiddevelopers/appcompat-v23-2-daynight-d10f90c83e94
[maven-repo-mdc]: https://maven.google.com/web/index.html#com.google.android.material:material
