<!--docs:
title: "Color Theming"
layout: detail
section: theming
excerpt: "Color Theming"
iconId: color
path: /theming/color/
-->

# Color Theming

The Material Design color system can be used to create a color scheme that
reflects your brand or style.

The Material Design color system uses an organized approach to applying color to
your UI. In this system, two theme colors are selected to express different
parts of a UI: a primary color and a secondary color. Material components use
these theme colors and their variations to style their individual backgrounds,
text, and more.

## Design & API Documentation

-   [Material Design guidelines:
    Color](https://material.io/go/design-color-theming/)
    <!--{: .icon-list-item.icon-list-item--spec }-->

## Usage

All Material Design components use a `Widget.MaterialComponents` style, and
these styles reference color attributes from the Material Design theme
(`Theme.MaterialComponents`). So, it is easy to re-color attributes across your
app by simply modifying the color attributes in your theme. These attributes
are:

*   `colorPrimary`, `colorPrimaryDark`, `colorPrimaryLight`
*   `colorSecondary`, `colorSecondaryDark`, `colorSecondaryLight`

By changing `colorPrimary` and `colorSecondary` (along with their dark and light
variants), you can easily change the style of all the Material components to
which your theme is applied.

Furthermore, if you want to change the mapping for a component completely, e.g.
you want to change MaterialButton so that it uses `colorPrimary` for its
background tint rather than `colorSecondary`, all you would have to do is define
your own button style that extends from a Material Design style and set the
mapping yourself:

```xml
<style name="Widget.MyApp.MyButton" parent="Widget.MaterialComponents.Button">
  <item name="backgroundTint">?attr/colorPrimary</item>
</style>
```

Then, you can apply this style to individual buttons directly, or apply it to
all of your buttons by setting the `materialButtonStyle` attribute in your
theme:

```xml
<style name="Theme.MyApp" parent="Theme.MaterialComponents.Light">
  ...
  <item name="materialButtonStyle">@style/Widget.MyApp.MyButton</item>
  ...
</style>
```

## Coming Soon

We will be updating our color theming to make component colors much more easily
themeable app-wide.

In a future update, our themes and components will be updated to reference a
**new set of color theming attributes**. Then, all you need to do is update the
hexidecimal color values for these attributes and you can:

*   adjust your component colors
*   switch between dark and light themes
*   ensure accessible color combinations across your app

The new color theme will be based around a primary and secondary color. To
select primary and secondary colors, and generate variations of each, use the
[Material palette generator](https://material.io/go/tools-color), [Material
Plugin for Sketch](https://material.io/go/tools-theme-editor), or 2014 Material
Design palettes.

The full list of new attribute names will be as follows:

Attribute name            | Description
------------------------- | --------------------
`colorPrimary`            | Displayed most frequently across your app.
`colorPrimaryVariant`     | A tonal variation of primary color.
`colorSecondary`          | Accents select parts of your UI.<br/>_If not provided, use primary._
`colorSecondaryVariant`   | A tonal variation of secondary color.
`colorBackground`         | The underlying color of an app’s content.<br/>_Typically the background color of scrollable content._
`colorError`              | The color used to indicate error status.
`colorSurface`            | The color of surfaces such as cards, sheets, menus.
`colorOnPrimary`          | A color that passes accessibility guidelines for text/iconography when drawn on top of primary.
`colorOnSecondary`        | A color that passes accessibility guidelines for text/iconography when drawn on top of secondary.
`colorOnBackground`       | A color that passes accessibility guidelines for text/iconography when drawn on top of background.
`colorOnError`            | A color that passes accessibility guidelines for text/iconography when drawn on top of error.
`colorOnSurface`          | A color that passes accessibility guidelines for text/iconography when drawn on top of surface.

Surface, background, and error colors typically do not represent brand. Surface
colors typically map to components such as cards, sheets, and menus. The
background color is typically found behind scrollable content. Error color is
the indication of errors within components such as text fields.

For `colorOnPrimary`, `colorOnSecondary`, etc., these colors must pass contrast
accessibility guidelines when drawn on top of their corresponding color
attributes. Material components will then use these attributes such that the
color combinations remain accessible in any component state.
