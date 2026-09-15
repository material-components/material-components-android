<!--docs:
title: "Icon buttons"
layout: detail
section: components
excerpt: "A customizable button component with updated visual styles."
iconId: materialbutton
path: /catalog/buttons/
-->

# Icon buttons

[Icon buttons](https://m3.material.io/components/icon-buttons/overview) help
people take minor actions with one tap. There are two variants of icon buttons.

![2 types of icon buttons](assets/buttons/iconbutton-types.png)

1.  Default icon button
2.  Toggle icon button

**Note:** Images use various dynamic color schemes.

## Design & API documentation

*   [Material 3 (M3) spec](https://m3.material.io/components/icon-buttons/overview)
*   [API reference](https://developer.android.com/reference/com/google/android/material/button/package-summary)

## Anatomy

![Anatomy of an icon button ](assets/buttons/iconbuttons-anatomy.png)

1.  Icon
2.  Container

More details on anatomy items in the
[component guidelines](https://m3.material.io/components/icon-buttons/guidelines#1f6f6121-e403-4d82-aa6a-5ab276f4bc4c).

## M3 Expressive

### M3 Expressive update

Before you can use `Material3Expressive` component styles, follow the
[`Material3Expressive` themes setup instructions](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md#material3expressive-themes).

<img src="assets/buttons/iconbuttons-expressive.png" alt="Icon buttons can vary in size, shape, and width." height="400"/>

1.  Five sizes
2.  Two shapes
3.  Three widths

Icon buttons now have a wider variety of shapes and sizes, changing shape when
selected. When placed in button groups, icon buttons interact with each other
when pressed.
[More on M3 Expressive](https://m3.material.io/blog/building-with-m3-expressive)

**Types and naming:**

*   Default and toggle (selection)
*   Color styles are now configurations. (filled, tonal, outlined, standard)

**Shapes:**

*   Round and square options
*   Shape morphs when pressed
*   Shape morphs when selected

**Sizes:**

*   Extra small
*   Small (default)
*   Medium
*   Large
*   Extra large

**Widths:**

*   Narrow
*   Default
*   Wide

### M3 Expressive styles

#### Icon button shapes

<details><summary><h5>Round</h5></summary>

|Default <div style="width:250px"></div>| Checked <div style="width:250px"></div>|Unchecked <div style="width:250px"></div>|
| ------ | ---- | ---- |
|![Default expressive round filled icon only button in light theme](assets/buttons/iconbutton-round-default-light-theme.png)|![Checked expressive round filled icon only button in light theme](assets/buttons/iconbutton-round-checked-light-theme.png)|![Unchecked expressive round filled icon only button in light theme](assets/buttons/iconbutton-round-unchecked-light-theme.png)|

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"/>
```

</details>

<details><summary><h5>Square</h5></summary>

|Default <div style="width:250px"></div>| Checked <div style="width:250px"></div>|Unchecked <div style="width:250px"></div>|
| ------ | ---- | ---- |
|![Default expressive square filled icon only button in light theme](assets/buttons/iconbutton-square-default-light-theme.png)|![Checked expressive square filled icon only button in light theme](assets/buttons/iconbutton-square-checked-light-theme.png)|![Unchecked expressive square filled icon only button in light theme](assets/buttons/iconbutton-square-unchecked-light-theme.png)|

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.{Small}.Square"/>
```

</details>

#### Icon button in different sizes

There are five icon button size variants: Extra small, small, medium, large, and
extra large.

<details><summary><h5>Extra small</h5></summary>

**Note:** Images below show the icon only buttons in different sizes relatively.
The actual sizes in px on mobile devices depends on the screen pixel density.

<img src="assets/buttons/iconbutton-extra-small-light-theme.png" alt="Extra small filled icon only button example in light theme" height="80">

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.Xsmall"/>
```

</details>

<details><summary><h5>Small</h5></summary>

<img src="assets/buttons/iconbutton-small-light-theme.png" alt="Small filled icon only button example in light theme" height="120">

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.Small"/>
```

</details>

<details><summary><h5>Medium</h5></summary>

<img src="assets/buttons/iconbutton-medium-light-theme.png" alt="Medium filled icon only button example in light theme" height="160">

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.Medium"/>
```

</details>

<details><summary><h5>Large</h5></summary>

<img src="assets/buttons/iconbutton-large-light-theme.png" alt="Large filled icon only button example in light theme" height="200">

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.Large"/>
```

</details>

<details><summary><h5>Extra large</h5></summary>

<img src="assets/buttons/iconbutton-extra-large-light-theme.png" alt="Extra large filled icon only button example in light theme" height="240">

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.Xlarge"/>
```

</details>

#### Icon button in different width and height ratio

Each icon button has three width options: narrow, uniform (default), and wide.

<details><summary><h5>Narrow</h5></summary>

![Narrow filled icon only button example in light theme](assets/buttons/iconbutton-narrow-light-theme.png)

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.{Small}.Narrow"/>
```

</details>

<details><summary><h5>Default</h5></summary>

![Default filled icon only button example in light theme](assets/buttons/iconbutton-default-light-theme.png)

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"/>
```

</details>

<details><summary><h5>Wide</h5></summary>

![Wide filled icon only button example in light theme](assets/buttons/iconbutton-wide-light-theme.png)

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:icon="@drawable/icon"
    app:materialSizeOverlay="@style/SizeOverlay.Material3Expressive.Button.IconButton.{Small}.Wide"/>
```

</details>

## Key properties

### Styles and theme attributes

Element                      | Style                                             | Theme Attribute
---------------------------- | ------------------------------------------------- | ---------------
**Default style**            | `Widget.Material3.Button.IconButton`              | `?attr/materialIconButtonStyle`
**Filled Icon Button**       | `Widget.Material3.Button.IconButton.Filled`       | `?attr/materialIconButtonFilledStyle`
**Filled Tonal Icon Button** | `Widget.Material3.Button.IconButton.Filled.Tonal` | `?attr/materialIconButtonFilledTonalStyle`
**Outlined Icon Button**     | `Widget.Material3.Button.IconButton.Outlined`     | `?attr/materialIconButtonOutlinedStyle`

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/res/values/styles.xml)
and
[attrs](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/res/values/attrs.xml).

## Variants of icon buttons

There are four icon button color styles, in order of emphasis:

![Diagram of default and toggle icon buttons in 4 color styles.](assets/buttons/iconbuttons-color.png)

1.  Filled
2.  Tonal
3.  Outlined
4.  Standard

For the highest emphasis, use the filled style. For the lowest emphasis, use
standard.

### Default icon button

*   Default icon buttons can open other elements, such as a menu or search.
*   Default icon buttons should use filled icons.

Single icons can be used for additional, supplementary actions. They're best for
areas of a compact layout, such as a toolbar.

The default dimensions allow for a touch target of `48dp`. If using an icon
bigger than the default size, the padding dimensions should be adjusted to
preserve the circular shape. `android:inset*` dimensions can also be adjusted if
less empty space is desired around the icon.

Always include an `android:contentDescription` so that icon only buttons are
readable for screen readers.

![Default icon button.](assets/buttons/iconbuttons-default.png)
Standard, filled unselected, filled selected, filled tonal, and outlined icon
buttons

**Note:** The examples below show how to create an icon button using `Button`
which will be inflated to `MaterialButton` when using a Material theme. There is
a known performance issue where `MaterialButton` takes longer to initialize when
compared to `ImageButton` or `AppCompatImageButton`, in large part because
`MaterialButton` extends from `AppCompatButton` which supports more than just
icon buttons. Consider using those pure icon button alternatives if the extra
latency causes a noticeable issue for your app.

#### Adding icon button

<details><summary><h5>Filled</h5></summary>

|Default <div style="width:250px"></div>|Checked <div style="width:250px"></div>|Unchecked <div style="width:250px"></div>|
| ------ | ---- | ---- |
|![Default expressive filled icon only button in light theme](assets/buttons/iconbutton-default-light-theme.png)|![Checked expressive filled icon only button in light theme](assets/buttons/iconbutton-checked-light-theme.png) |![Unchecked filled icon button in light theme](assets/buttons/iconbutton-unchecked-light-theme.png)|
|![Default expressive filled icon only button in dark theme](assets/buttons/iconbutton-default-dark-theme.png)|![Checked expressive filled icon only button in dark theme](assets/buttons/iconbutton-checked-dark-theme.png)|![filled icon only unchecked button_dark](assets/buttons/iconbutton-unchecked-dark-theme.png)|

By default, the standard icon only button is uncheckable. To make it checkable,
enable the `android:checkable` attribute in style or layout.

```xml
<Button
    style="?attr/materialIconButtonFilledStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/icon_description"
    android:checkable="true"
    app:icon="@drawable/icon"/>
```

</details>

<details><summary><h5>Standard</h5></summary>

|Default <div style="width:250px"></div>|Checked <div style="width:250px"></div>|Unchecked <div style="width:250px"></div>|
| ------ | ---- | ---- |
|![Default expressive standard icon only button in light theme](assets/buttons/iconbutton-standard-default-light-theme.png)|![Checked expressive standard icon only button in light theme](assets/buttons/iconbutton-standard-checked-light-theme.png)|![standard icon only unchecked button_light](assets/buttons/iconbutton-standard-unchecked-light-theme.png)|
|![Default expressive standard icon only button in dark theme](assets/buttons/iconbutton-standard-default-dark-theme.png)|![Checked expressive standard icon only button in dark theme](assets/buttons/iconbutton-standard-checked-dark-theme.png)|![standard icon only unchecked button_dark](assets/buttons/iconbutton-standard-unchecked-dark-theme.png)|

By default, the standard icon only button is uncheckable. To make it checkable,
enable the `android:checkable` attribute in style or layout.

```xml
<Button
    style="?attr/materialIconButtonStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/icon_description"
    android:checkable="true"
    app:icon="@drawable/icon"/>
```

</details>

<details><summary><h5>Filled tonal</h5></summary>

|Default <div style="width:250px"></div>|Checked <div style="width:250px"></div> |Unchecked <div style="width:250px"></div>|
| ------ | ---- | ---- |
|![Default expressive filled tonal icon only button in light theme](assets/buttons/iconbutton-filledtonal-default-light-theme.png)|![Checked expressive filled tonal icon only button in light theme](assets/buttons/iconbutton-filledtonal-checked-light-theme.png) |![filled tonal icon only unchecked button_light](assets/buttons/iconbutton-filledtonal-unchecked-light-theme.png)|
|![Default expressive filled tonal icon only button in dark theme](assets/buttons/iconbutton-filledtonal-default-dark-theme.png)|![Checked expressive filled tonal icon only button in dark theme](assets/buttons/iconbutton-filledtonal-checked-dark-theme.png)|![filled tonal icon only unchecked button_dark](assets/buttons/iconbutton-filledtonal-unchecked-dark-theme.png)|

By default, the standard icon only button is uncheckable. To make it checkable,
enable the `android:checkable` attribute in style or layout.

```xml
<Button
    style="?attr/materialIconButtonFilledTonalStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/icon_description"
    android:checkable="true"
    app:icon="@drawable/icon"/>
```

</details>

<details><summary><h5>Outlined</h5></summary>

|Default <div style="width:250px"></div>| Checked <div style="width:250px"></div>|Unchecked <div style="width:250px"></div>|
| ------ | ---- | ---- |
|![Default expressive outlined icon only button in light theme](assets/buttons/iconbutton-outlined-default-light-theme.png)|![Checked expressive outlined icon only button in light theme](assets/buttons/iconbutton-outlined-checked-light-theme.png)|![outlined icon only unchecked button_light](assets/buttons/iconbutton-outlined-unchecked-light-theme.png)|
|![Default expressive outlined icon only button in dark theme](assets/buttons/iconbutton-outlined-default-dark-theme.png)|![Checked expressive outlined icon only button in dark theme](assets/buttons/iconbutton-outlined-checked-dark-theme.png)|![outlined icon only unchecked button_dark](assets/buttons/iconbutton-outlined-unchecked-dark-theme.png)|

By default, the standard icon only button is uncheckable. To make it checkable,
enable the `android:checkable` attribute in style or layout.

```xml
<Button
    style="?attr/materialIconButtonOutlinedStyle"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/icon_description"
    android:checkable="true"
    app:icon="@drawable/icon"/>
```

</details>

### Toggle icon button

*   Toggle icon buttons can represent binary actions that can be toggled on and
    off, such as favorite or bookmark.

*   Each icon button has as an optional toggle behavior, which lets people
    select and unselect the button. Toggle buttons remain highlighted when
    selected, and are styled differently than the default, non-toggle buttons.

*   Toggle buttons should use an outlined icon when unselected, and a filled
    version of the icon when selected.

#### Adding toggle icon button

In toggle buttons, use the outlined style of an icon for the unselected state,
and the filled style for the selected state.

The following example shows a toggle icon button.

Create file `res/drawable/toggle_icon_button_selector.xml` to include both
outlined and filled icons for the toggle icon button:

```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
<item android:state_checked="true" android:drawable="@drawable/star_filled" />
<item android:drawable="@drawable/star_outline" /> </selector>

<com.google.android.material.button.MaterialButton
        android:id="@+id/toggleIconButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        style="@style/Widget.Material3.Button.IconButton.Standard"
        app:icon="@drawable/toggle_icon_button_selector"
        app:iconTint="?attr/colorAccent"
        android:contentDescription="Toggle icon button"
        android:checkable="true"/>
```

## Code implementation

### Making buttons accessible

Buttons support content labeling for accessibility and are readable by most
screen readers, such as TalkBack. Text rendered in buttons is automatically
provided to accessibility services. Additional content labels are usually
unnecessary.

For more information on content labels, go to the
[Android accessibility help guide](https://support.google.com/accessibility/android/answer/7158690).

## Customizing icon buttons

### Theming buttons

Buttons support the customization of color, typography, and shape.

#### Button theming example

API and source code:

*   `MaterialButton`
    *   [Class description](https://developer.android.com/reference/com/google/android/material/button/MaterialButton)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/button/MaterialButton.java)

The following example shows text, outlined and filled button types with Material
theming.

!["Button theming with three buttons - text, outlined and filled - with pink
color theming and cut corners."](assets/buttons/button-theming.png)

##### Implementing button theming

Use theme attributes and styles in `res/values/styles.xml` to add the theme to
all buttons. This affects other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="colorPrimary">@color/shrine_pink_100</item>
    <item name="colorOnPrimary">@color/shrine_pink_900</item>
    <item name="textAppearanceLabelLarge">@style/TextAppearance.App.Button</item>
    <item name="shapeCornerFamily">cut</item>
</style>

<style name="TextAppearance.App.Button" parent="TextAppearance.Material3.LabelLarge">
    <item name="fontFamily">@font/rubik</item>
    <item name="android:fontFamily">@font/rubik</item>
</style>
```

Use default style theme attributes, styles and theme overlays. This adds the
theme to all buttons but does not affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="borderlessButtonStyle">@style/Widget.App.Button.TextButton</item>
    <item name="materialButtonOutlinedStyle">@style/Widget.App.Button.OutlinedButton</item>
    <item name="materialButtonStyle">@style/Widget.App.Button</item>
</style>

<style name="Widget.App.Button.TextButton" parent="Widget.Material3.Button.TextButton">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.Button.TextButton</item>
    <item name="android:textAppearance">@style/TextAppearance.App.Button</item>
    <item name="shapeAppearance">@style/ShapeAppearance.App.Button</item>
</style>

<style name="Widget.App.Button.OutlinedButton" parent="Widget.Material3.Button.OutlinedButton">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.Button.TextButton</item>
    <item name="android:textAppearance">@style/TextAppearance.App.Button</item>
    <item name="shapeAppearance">@style/ShapeAppearance.App.Button</item>
</style>

<style name="Widget.App.Button" parent="Widget.Material3.Button">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.Button</item>
    <item name="android:textAppearance">@style/TextAppearance.App.Button</item>
    <item name="shapeAppearance">@style/ShapeAppearance.App.Button</item>
</style>

<style name="ThemeOverlay.App.Button.TextButton" parent="ThemeOverlay.Material3.Button.TextButton">
    <item name="colorOnContainer">@color/shrine_pink_900</item>
</style>

<style name="ThemeOverlay.App.Button" parent="ThemeOverlay.Material3.Button">
    <item name="colorContainer">@color/shrine_pink_100</item>
    <item name="colorOnContainer">@color/shrine_pink_900</item>
</style>

<style name="ShapeAppearance.App.Button" parent="">
    <item name="cornerFamily">cut</item>
    <item name="cornerSize">4dp</item>
</style>
```

Use one of the styles in the layout. That will affect only this button:

```xml

<Button style="@style/Widget.App.Button".../>
```

### Optical centering

Optical centering means to offset the `MaterialButton`’s contents (icon and/or
label) when the shape is asymmetric. Before optical centering, we only provided
centering with horizontally asymmetrical shapes.

To turn on optical centering for a given button, use
`setOpticalCenterEnabled(true)`. Optical centering is disabled by default. When
enabled, the shift amount of the icon and/or text is calculated as a value with
the fixed ratio to the difference between left corner size in dp and right
corner size in dp. The shift amount is applied to the padding start and padding
end.
