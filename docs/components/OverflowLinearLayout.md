# Overflow Linear Layout

The `OverflowLinearLayout` is usually used with the [FloatingToolbar](https://github.com/material-components/material-components-android/tree/master//docs/components/FloatingToolbar.md)
and the [DockedToolbar](https://github.com/material-components/material-components-android/tree/master//docs/components/DockedToolbar.md).
It allows for its children to be automatically hidden/shown depending on its
parent's max size. The hidden children are put in an overflow menu, and an
overflow button is added as the last child of its parent layout.

Note: if you'd like to hide/show children independently from this layout's
decisions, you'll need to add/remove the desired view(s), instead of changing
their visibility, as the `OverflowLinearLayout` will determine the final
visibility value of its children.

## Using Overflow Linear Layout

A common usage looks like:

```xml
<ParentLayout
 ...>
  <com.google.android.material.overflow.OverflowLinearLayout>
    <ParentLayoutItem
      ...
      app:layout_overflowText="@string/item_1_text"
      app:layout_overflowIcon="@drawable/item_1_icon" />
    <ParentLayoutItem
      ...
      app:layout_overflowText="@string/item_2_text"
      app:layout_overflowIcon="@drawable/item_2_icon" />
    ...
  </com.google.android.material.overflow.OverflowLinearLayout>
</ParentLayout>
```

When using `OverflowLinearLayout`, you should set `app:layout_overflowText` on
on each child, as that will show as the text of the menu item that corresponds
to the hidden child. Optionally, you can also set `app:layout_overflowIcon`.

See
[FloatingToolbar](https://github.com/material-components/material-components-android/tree/master//docs/components/FloatingToolbar.md)
and [DockedToolbar](https://github.com/material-components/material-components-android/tree/master//docs/components/DockedToolbar.md)
for example usages with those components.

API and source code:

* `OverflowLinearLayout`
  * [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/overflow/OverflowLinearLayout.java)

### Making Overflow Linear Layout accessible

As mentioned above, you should set `app:layout_overflowText` on each direct
child of `OverflowLinearLayout` that may be overflowed, so that the overflow
menu items have text that can be read by screen readers.

## Attributes and styles

### `OverflowLinearLayout` attributes

 **Element**              | **Attribute**            | **Related methods**                                                                     | **Default value**
--------------------------|--------------------------|-----------------------------------------------------------------------------------------|-------------------------------------------
 **Overflow button icon** | `app:overflowButtonIcon` | `setOverflowButtonIcon`<br/>`setOverflowButtonIconResource`<br/>`getOverflowButtonIcon` | `@drawable/abc_ic_menu_overflow_material`

### `OverflowLinearLayout_Layout` attributes

Attributes for the children of `OverflowLinearLayout`:

 **Element**                        | **Attribute**                   | **Related methods** | **Default value**
------------------------------------|---------------------------------|---------------------|-------------------
 **Overflow menu's item text**      | `app:layout_overflowText`       | N/A                 | `null`
 **Overflow menu's item icon**      | `app:layout_overflowIcon`       | N/A                 | `null`

### `OverflowLinearLayout` Styles

 **Element** | **Style**
-------------|-------------------------------------------------------
 **Style**   | `Widget.Material3.OverflowLinearLayout`

Style theme attribute: `?attr/overflowLinearLayoutStyle`

Button overflow theme attribute: `?attr/overflowLinearLayoutOverflowButtonStyle`
(defaults to `?attr/materialIconButtonStyle`)

Popup overflow menu theme attribute: `?attr/overflowLinearLayoutPopupMenuStyle`
(defaults to `?attr/popupMenuStyle`)

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/overflow/res/values/styles.xml) and
[overflow linear layout attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/overflow/res/values/attrs.xml)

## Theming an Overflow Linear Layout

Overflow Linear Layout supports
[Material Theming](https://m3.material.io/foundations/customization),
which can customize color, shape and typography.

### Implementing overflow linear layout theming

Use theme attributes and a style in `res/values/styles.xml` which apply to all
overflow linear layouts and affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="colorPrimary">@color/shrine_pink_100</item>
    ...
</style>
```

Use a default style theme attribute, styles, and a theme overlay, which apply to
all overflow linear layouts but do not affect other components:

```xml

<style name="Theme.App" parent="Theme.Material3.*">
  ...
  <item name="overflowLinearLayoutStyle">@style/Widget.App.OverflowLinearLayout</item>
  <item name="floatingToolbarStyle">@style/Widget.App.FloatingToolbar</item>
</style>

<style name="Widget.App.OverflowLinearLayout" parent="Widget.Material3.OverflowLinearLayout">
  <item name="materialThemeOverlay">@style/ThemeOverlay.App.OverflowLinearLayout</item>
</style>

<style name="ThemeOverlay.App.OverflowLinearLayout" parent="ThemeOverlay.Material3.OverflowLinearLayout">
  <item name="colorPrimary">@color/shrine_theme_light_primary</item>
  <item name="overflowLinearLayoutOverflowButtonStyle">@style/Widget.App.OverflowButton</item>
  <item name="overflowLinearLayoutPopupMenuStyle">@style/Widget.App.PopupMenuStyle</item>
  ...
</style>

<style name="Widget.App.OverflowButton" parent="Widget.Material3.Button.IconButton">
  ...
</style>

<style name="Widget.App.PopupMenuStyle" parent="Widget.Material3.PopupMenuStyle">
...
</style>
```

Or use the style in the layout, which affects only this specific overflow
linear layout:

```xml

<com.google.android.material.floatingtoolbar.FloatingToolbarLayout
  ...
  style="@style.Widget.App.FloatingToolbarLayout">
  <com.google.android.material.overflow.OverflowLinearLayout
    ...
    style="@style/Widget.App.OverflowLinearLayout"/>
</com.google.android.material.floatingtoolbar.FloatingToolbarLayout>
```
