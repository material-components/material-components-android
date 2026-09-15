<!--docs:
title: "Typography Theming"
layout: detail
section: theming
excerpt: "Typography Theming"
iconId: typography
path: /theming/typography/
-->

# Typography theming

Material Design typography theming can be used to create typographic styles that
reflect your brand or style by defining a set of type scales which will be used
throughout your app. You can use type scales to customize the appearance of text
in Material components.

## Design and API Documentation

-   [Material Design guidelines: Typography](https://m3.material.io/styles/typography/overview/)

## Usage

### Baseline scale

Attribute name                 | Default style
------------------------------ | -------------
`textAppearanceDisplayLarge`   | Regular 57sp
`textAppearanceDisplayMedium`  | Regular 45sp
`textAppearanceDisplaySmall`   | Regular 36sp
`textAppearanceHeadlineLarge`  | Regular 32sp
`textAppearanceHeadlineMedium` | Regular 28sp
`textAppearanceHeadlineSmall`  | Regular 24sp
`textAppearanceTitleLarge`     | Regular 22sp
`textAppearanceTitleMedium`    | Medium 16sp
`textAppearanceTitleSmall`     | Medium 14sp
`textAppearanceBodyLarge`      | Regular 16sp
`textAppearanceBodyMedium`     | Regular 14sp
`textAppearanceBodySmall`      | Regular 12sp
`textAppearanceLabelLarge`     | Medium 14sp
`textAppearanceLabelMedium`    | Medium 12sp
`textAppearanceLabelSmall`     | Medium 11sp

### Emphasized scale

Emphasized styles are used to create hierarchy and are recommended for showing
selection, actions, headlines, or other editorial treatments.

Attribute name                           | Default style
---------------------------------------- | -------------
`textAppearanceDisplayLargeEmphasized`   | Medium 57sp
`textAppearanceDisplayMediumEmphasized`  | Medium 45sp
`textAppearanceDisplaySmallEmphasized`   | Medium 36sp
`textAppearanceHeadlineLargeEmphasized`  | Medium 32sp
`textAppearanceHeadlineMediumEmphasized` | Medium 28sp
`textAppearanceHeadlineSmallEmphasized`  | Medium 24sp
`textAppearanceTitleLargeEmphasized`     | Medium 22sp
`textAppearanceTitleMediumEmphasized`    | Bold 16sp
`textAppearanceTitleSmallEmphasized`     | Bold 14sp
`textAppearanceBodyLargeEmphasized`      | Medium 16sp
`textAppearanceBodyMediumEmphasized`     | Medium 14sp
`textAppearanceBodySmallEmphasized`      | Medium 12sp
`textAppearanceLabelLargeEmphasized`     | Bold 14sp
`textAppearanceLabelMediumEmphasized`    | Bold 12sp
`textAppearanceLabelSmallEmphasized`     | Bold 11sp

## Style values

Style values are a combination of the following:

*   Font face name and weight
*   Font size
*   Letter spacing
*   Text transformation (e.g., all caps)

## Customization

The components included in the Material Design Library reference these themeable
text attributes, enabling you to easily change text appearance across your whole
application. If you display text in `TextView`s or create custom components,
consider referencing one of these text attributes where it makes sense.

You can change the look of any text style by creating a new style and setting it
in your theme:

```xml
<style name="TextAppearance.MyApp.DisplaySmall" parent="TextAppearance.Material3.DisplaySmall">
  ...
  <item name="fontFamily">@font/custom_font</item>
  <item name="android:textStyle">normal</item>
  <item name="android:textAllCaps">false</item>
  <item name="android:textSize">64sp</item>
  <item name="android:letterSpacing">0</item>
  ...
</style>
```

```xml
<style name="Theme.MyApp" parent="Theme.Material3.DayNight.NoActionBar">
  ...
  <item name="textAppearanceDisplaySmall">@style/TextAppearance.MyApp.DisplaySmall</item>
  ...
</style>
```

## Downloadable fonts

Android O and Android Support Library 26 add support for [Downloadable
Fonts](https://developer.android.com/guide/topics/ui/look-and-feel/downloadable-fonts.html).
This allows you to easily use the entire Google Fonts Open Source collection
without bundling a font with your apk. Find more information at
https://developers.google.com/fonts/docs/android.

**Note:** If you want to use a Downloadable Font before Android O, make sure you
are using `AppCompatTextView` or that you are loading the font yourself with
`ResourcesCompat.getFont()`.
