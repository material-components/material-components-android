<!--docs:
title: "Bottom navigation"
layout: detail
section: components
excerpt: "Bottom navigation bars make it easy to explore and switch between top-level views in a single tap."
iconId: bottom_navigation
path: /catalog/bottom-navigation/
-->

# Bottom Navigation

[Bottom navigation](https://material.io/components/bottom-navigation/#) bars
allow movement between primary destinations in an app.

!["Bottom navigation bar with 4 icons"](assets/bottomnav/bottomnav_hero.png)

**Contents**

*   [Design & API documentation](#design-api-documentation)
*   [Using bottom navigation](#using-bottom-navigation)
*   [Bottom navigation bar](#bottom-navigation-bar)
*   [Theming](#theming-a-bottom-navigation-bar)

## Design & API Documentation

*   [Google Material3 Spec](https://material.io/components/navigation-bar/overview)
*   [API Reference](https://developer.android.com/reference/com/google/android/material/bottomnavigation/package-summary)

## Using bottom navigation

Before you can use the Material bottom navigation, you need to add a dependency
to the Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

A typical layout looks like this:

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
  ...
  <com.google.android.material.bottomnavigation.BottomNavigationView
      android:id="@+id/bottom_navigation"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:menu="@menu/bottom_navigation_menu" />

</LinearLayout>
```

The `@menu/bottom_navigation_menu` resource should point to a file named
`bottom_navigation_menu.xml` inside a `menu` resource directory:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
  <item
      android:id="@+id/item_1"
      android:enabled="true"
      android:icon="@drawable/icon_1"
      android:title="@string/text_label_1"/>
  <item
      android:id="@+id/item_2"
      android:enabled="true"
      android:icon="@drawable/icon_2"
      android:title="@string/text_label_2"/>
</menu>
```

**Note:** `BottomNavigationView` does not support more than 5 `menu` items.

In code:

```kt
NavigationBarView.OnItemSelectedListener { item ->
    when(item.itemId) {
        R.id.item_1 -> {
            // Respond to navigation item 1 click
            true
        }
        R.id.item_2 -> {
            // Respond to navigation item 2 click
            true
        }
        else -> false
    }
}
```

There's also a method for detecting when navigation items have been reselected:

```kt
bottomNavigation.setOnItemReselectedListener { item ->
    when(item.itemId) {
        R.id.item_1 -> {
            // Respond to navigation item 1 reselection
        }
        R.id.item_2 -> {
            // Respond to navigation item 2 reselection
        }
    }
}
```

That results in:

![Bottom navigation bar with a white background, one selected purple icon and
another unselected icon.](assets/bottomnav/bottom-nav-default.png)

**Note:** We have deprecated the
`BottomNavigationView#setOnNavigationItemSelectedListener` and
`BottomNavigationView#setOnNavigationItemReselectedListener` methods in favor of
the listeners in `NavigationBarView`. This allows you to share selection
handling code between the `BottomNavigation` and `NavigationRail` view elements.

### Making bottom navigation accessible

You should set an `android:title` for each of your `menu` items so that screen
readers like TalkBack can properly announce what each navigation item
represents:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
  <item
      ...
      android:title="@string/text_label"/>
  ...
</menu>
```

The `labelVisibilityMode` attribute can be used to adjust the behavior of the
text labels for each navigation item. There are four visibility modes:

*   `LABEL_VISIBILITY_AUTO` (default): The label behaves as “labeled” when there
    are 3 items or less, or “selected” when there are 4 items or more
*   `LABEL_VISIBILITY_SELECTED`: The label is only shown on the selected
    navigation item
*   `LABEL_VISIBILITY_LABELED`: The label is shown on all navigation items
*   `LABEL_VISIBILITY_UNLABELED`: The label is hidden for all navigation items

### Adding badges

![Bottom navigation with 3 icons with badges, an icon only badge and two
numbered badges showing 99 and
999+](assets/bottomnav/bottom-navigation-badges.png)

Initialize and show a `BadgeDrawable` associated with `menuItemId`, subsequent
calls to this method will reuse the existing `BadgeDrawable`:

```kt
var badge = bottomNavigation.getOrCreateBadge(menuItemId)
badge.isVisible = true
// An icon only badge will be displayed unless a number or text is set:
badge.number = 99  // or badge.text = "New"
```

As a best practice, if you need to temporarily hide the badge, for instance
until the next notification is received, change the visibility of
`BadgeDrawable`:

```kt
val badgeDrawable = bottomNavigation.getBadge(menuItemId)
    if (badgeDrawable != null) {
        badgeDrawable.isVisible = false
        badgeDrawable.clearNumber()  // or badgeDrawable.clearText()
    }
```

To remove any `BadgeDrawable`s that are no longer needed:

```kt
bottomNavigation.removeBadge(menuItemId)
```

See the [`BadgeDrawable`](BadgeDrawable.md) documentation for more information
about badges.

## Bottom navigation bar

![Example bottom navigation bar with four icons along the bottom: favorites,
music, places, and news. The music icon is
selected](assets/bottomnav/bottom-nav-generic.png)

### Bottom navigation bar example

API and source code:

*   `BottomNavigationView`
    *   [Class description](https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/BottomNavigationView.java)

The following example shows a bottom navigation bar with four icons:

![Bottom navigation bar with four icons](assets/bottomnav/bottom-nav-generic.png)

In `layout.xml`:

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

  <com.google.android.material.bottomnavigation.BottomNavigationView
      android:id="@+id/bottom_navigation"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      app:menu="@menu/bottom_navigation_menu" />

</LinearLayout>
```

In `bottom_navigation_menu.xml` inside a `menu` resource directory:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
  <item
      android:id="@+id/page_1"
      android:enabled="true"
      android:icon="@drawable/ic_star"
      android:title="@string/page_1"/>
  <item
      android:id="@+id/page_2"
      android:enabled="true"
      android:icon="@drawable/ic_star"
      android:title="@string/page_2"/>
  <item
      android:id="@+id/page_3"
      android:enabled="true"
      android:icon="@drawable/ic_star"
      android:title="@string/page_3"/>
  <item
      android:id="@+id/page_4"
      android:enabled="true"
      android:icon="@drawable/ic_star"
      android:title="@string/page_4"/>
</menu>
```

In code:

```kt
bottomNavigation.selectedItemId = R.id.page_2
```

### Anatomy and key properties

The following is an anatomy diagram for the bottom navigation bar:

![Bottom navigation anatomy diagram](assets/bottomnav/bottom-nav-anatomy.png)

*   (1) Container
*   (2) Icon
*   (3) Label text
*   (4) Active indicator
*   (5) Small badge (optional)
*   (6) Large badge (optional)
*   (7) Large badge number

#### Container attributes

**Element**       | **Attribute**         | **Related methods** | **Default value**
----------------- | --------------------- | ------------------- | -----------------
**Color**         | `app:backgroundTint`  | N/A                 | `?attr/colorSurfaceContainer`
**Elevation**     | `app:elevation`       | `setElevation`      | `3dp`
**Compat Shadow** | `compatShadowEnabled` | N/A                 | `false`

#### Navigation item attributes

**Element**               | **Attribute**             | **Related methods**                                   | **Default value**
------------------------- | ------------------------- | ----------------------------------------------------- | -----------------
**Menu resource**         | `app:menu`                | `inflateMenu`<br/>`getMenu`                           | N/A
**Ripple (inactive)**     | `app:itemRippleColor`     | `setItemRippleColor`<br/>`getItemRippleColor`         | Variations of `?attr/colorPrimary` and `?attr/colorOnSurfaceVariant` (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/color/m3_navigation_bar_ripple_color_selector.xml))
**Ripple (active)**       | "                         | "                                                     | Variations of `?attr/colorPrimary` (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/color/m3_navigation_bar_ripple_color_selector.xml))
**Label visibility mode** | `app:labelVisibilityMode` | `setLabelVisibilityMode`<br/>`getLabelVisibilityMode` | `LABEL_VISIBILITY_AUTO`

#### Icon attributes

**Element**          | **Attribute**                         | **Related methods**                                              | **Default value**
-------------------- | ------------------------------------- | ---------------------------------------------------------------- | -----------------
**Icon**             | `android:icon` in the `menu` resource | N/A                                                              | N/A
**Size**             | `app:itemIconSize`                    | `setItemIconSize`<br/>`setItemIconSizeRes`<br/>`getItemIconSize` | `24dp`
**Color (inactive)** | `app:itemIconTint`                    | `setItemIconTintList`<br/>`getItemIconTintList`                  | `?attr/colorOnSurfaceVariant` (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/color/m3_navigation_bar_item_with_indicator_icon_tint.xml))
**Color (active)**   | "                                     | "                                                                | `?attr/colorOnSecondaryContainer` (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/color/m3_navigation_bar_item_with_indicator_icon_tint.xml))

#### Text label attributes

**Element**               | **Attribute**                             | **Related methods**                                                 | **Default value**
------------------------- | ----------------------------------------- | ------------------------------------------------------------------- | -----------------
**Text label**            | `android:title` in the `menu` resource    | N/A                                                                 | N/A
**Color (inactive)**      | `app:itemTextColor`                       | `setItemTextColor`<br/>`getItemTextColor`                           | `?attr/colorOnSurfaceVariant` (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/color/m3_navigation_bar_item_with_indicator_label_tint.xml))
**Color (active)**        | "                                         | "                                                                   | `?attr/colorOnSurface` (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/color/m3_navigation_bar_item_with_indicator_label_tint.xml))
**Typography (inactive)** | `app:itemTextAppearanceInactive`          | `setItemTextAppearanceInactive`<br/>`getItemTextAppearanceInactive` | `?attr/textAppearanceTitleSmall`
**Typography (active)**   | `app:itemTextAppearanceActive`            | `setItemTextAppearanceActive`<br/>`getItemTextAppearanceActive`     | `?attr/textAppearanceTitleSmall`
**Typography (active)**   | `app:itemTextAppearanceActiveBoldEnabled` | `setItemTextAppearanceActiveBoldEnabled`                            | `true`

#### Styles

**Element**       | **Style**                               | **Container color**  | **Icon/Text label color (inactive)** | **Icon/Text label color (active)**
----------------- | --------------------------------------- | -------------------- | ------------------------------------ | ----------------------------------
**Default style** | `Widget.Material3.BottomNavigationView` | `?attr/colorSurface` | `?attr/colorOnSurfaceVariant`        | Icon: `?attr/colorOnSecondaryContainer` <br/> Text: `?attr/colorOnSurface`

Default style theme attribute: `?attr/bottomNavigationStyle`

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/values/styles.xml),
[navigation bar attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigation/res/values/attrs.xml),
and
[bottom navigation attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/res/values/attrs.xml).

## Theming a bottom navigation bar

Bottom navigation supports
[Material Theming](https://material.io/components/bottom-navigation#theming),
which can customize color and typography.

### Bottom navigation theming example

API and source code:

*   `BottomNavigationView`
    *   [Class description](https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/BottomNavigationView.java)

The following example shows a bottom navigation bar with Material Theming.

![Bottom navigation bar with brown icons (favorites, music, places, news) and
pink background](assets/bottomnav/bottom-nav-theming.png)

#### Implementing bottom navigation theming

Use theme attributes and a style in `res/values/styles.xml`, which applies to
all bottom navigation bars and affects other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="colorSurface">@color/shrine_theme_light_surface</item>
    <item name="colorOnSurfaceVariant">@color/shrine_theme_light_onSurfaceVariant</item>
</style>
```

Use a default style theme attribute, styles, and a theme overlay, which apply to
all bottom navigation bars but do not affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="bottomNavigationStyle">@style/Widget.App.BottomNavigationView</item>
</style>

<style name="Widget.App.BottomNavigationView" parent="Widget.Material3.BottomNavigationView">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.BottomNavigationView</item>
</style>

<style name="ThemeOverlay.App.BottomNavigationView" parent="">
    <item name="colorSurface">@color/shrine_theme_light_surface</item>
    <item name="colorOnSurfaceVariant">@color/shrine_theme_light_onSurfaceVariant</item>
</style>
```

Use the style in the layout, which affects only this specific bottom navigation
bar:

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    ...
    style="@style/Widget.App.BottomNavigationView"
/>
```
