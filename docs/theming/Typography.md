<!--docs:
title: "Typography Theming"
layout: detail
section: theming
excerpt: "Typography Theming"
iconId: typography
path: /theming/typography/
-->

# Typography Theming

Material Design typography theming can be used to create typographic styles that
reflect your brand or style by defining a set of type scales which will be used
throughout your app. Material components use these type scales to style their
individual text appearance.

## Design & API Documentation

-   [Material Design guidelines:
    Typography](https://material.io/go/design-typography/)
    <!--{: .icon-list-item.icon-list-item--spec }-->

## Usage

Attribute name            | Default style
------------------------- | --------------------
`textAppearanceHeadline1` | Light 96sp
`textAppearanceHeadline2` | Light 60sp
`textAppearanceHeadline3` | Regular 48sp
`textAppearanceHeadline4` | Regular 34sp
`textAppearanceHeadline5` | Regular 24sp
`textAppearanceHeadline6` | Regular 20sp
`textAppearanceSubtitle1` | Regular 16sp
`textAppearanceSubtitle2` | Medium 14sp
`textAppearanceBody1`     | Regular 16sp
`textAppearanceBody2`     | Regular 14sp
`textAppearanceCaption`   | Regular 12sp
`textAppearanceButton`    | Medium all caps 14sp
`textAppearanceOverline`  | Medium all caps 12sp

## Style values

Style values are a combination of the following:

*   Font face name and weight
*   Font size
*   Letter spacing
*   Text transformation (e.g., all caps)

## Customization

Soon all components included in the Material Design Library will reference these
themeable text attributes, enabling you to easily change text appearance across
your whole application. If you display text in `TextView`s or create custom
components, consider referencing one of these text attributes where it makes
sense.

You can change the look of any text style by creating a new style and setting it
in your theme:

```xml
<style name="TextAppearance.MyApp.Headline1" parent="TextAppearance.MaterialComponents.Headline1">
  ...
  <item name="fontFamily">@font/custom_font</item>
  <item name="android:textStyle">normal</item>
  <item name="textAllCaps">false</item>
  <item name="android:textSize">64sp</item>
  <item name="android:letterSpacing">0</item>
  ...
</style>
```

```xml
<style name="MyAppTheme" parent="Theme.MaterialComponents.Light">
  ...
  <item name="textAppearanceHeadline1">@style/TextAppearance.MyApp.Headline1</item>
  ...
</style>
```
