<!--docs:
title: "Navigation Rail"
layout: detail
section: components
excerpt: "Navigation rails provide access to primary destinations in your app on tablet and desktop screens.
iconId: navigation_rail
path: /catalog/navigation-rail/
-->

# Navigation Rail

[Navigation rail](https://material.io/components/navigation-rail/#) provides
access to primary destinations in your app on tablet and desktop screens.

![The navigation rail container is 80 dp wide by default.](assets/navigationrail/navigation-rail-default.png)

**Contents**

*   [Using navigation rail](#using-navigation-rail)
*   [Navigation rail example](#navigation-rail-example)
*   [Theming](#theming-a-navigation-rail)

## Using navigation rail

Before you can use the Material Navigation Rail, you need to add a dependency to
the Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

A typical layout will look similar to this:

```xml
<com.google.android.material.navigationrail.NavigationRailView
    android:id="@+id/navigation_rail"
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    app:menu="@menu/navigation_rail_menu" />
```

**Note:** The width of a `NavigationRailView` will be 80dp wide by default.The
width of the rail can be changed by setting the `android:layout_width`attribute
to a specific DP value.

In `navigation_rail_menu.xml` inside a `menu` resource directory:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
  <item
      android:id="@+id/alarms"
      android:enabled="true"
      android:icon="@drawable/icon_alarms"
      android:title="@string/alarms_destination_label"/>
  <item
      android:id="@+id/schedule"
      android:enabled="true"
      android:icon="@drawable/icon_clock"
      android:title="@string/schedule_destination_label"/>
  <item
      android:id="@+id/timer"
      android:enabled="true"
      android:icon="@drawable/icon_sand_clock"
      android:title="@string/timer_destination_label"/>
  <item
      android:id="@+id/stopwatch"
      android:enabled="true"
      android:icon="@drawable/icon_stop_watch"
      android:title="@string/stopwatch_destination_label"/>
</menu>
```

**Note:** `NavigationRailView` displays three to no more than seven app
destinations, and can include a header view. Each destination is represented by
an icon and a text label.

In code:

```kt
// Listeners are defined on the super class NavigationBarView
// to support both NavigationRail and BottomNavigation with the
// same listeners
NavigationBarView.OnNavigationItemSelectedListener { item ->
    when(item.itemId) {
        R.id.alarms -> {
            // Respond to alarm navigation item click
            true
        }
        R.id.schedule -> {
            // Respond to schedule navigation item click
            true
        }
        else -> false
    }
}
```

There's also a method for detecting if navigation items have been reselected:

```kt
navigationRail.setOnNavigationItemReselectedListener { item ->
    when(item.itemId) {
        R.id.item1 -> {
            // Respond to navigation item 1 reselection
        }
        R.id.item2 -> {
            // Respond to navigation item 2 reselection
        }
    }
}
```

Which results in:

![The navigation rail container is 72 dp wide by default.](assets/navigationrail/navigation-rail-demo.png)

By default, Navigation rail adds top and bottom padding according to top and
bottom window insets—helping the header layout and menu items dodge system
spaces. This is controlled by the `android:fitsSystemWindowInsets` attribute,
which is set to true by default. To remove this behavior, set
`android:fitsSystemWindowInsets` to `false` or opt in or out of the top and
bottom insets independently by using `app:paddingTopSystemWindowInsets` and
`app:paddingBottomSystemWindowInsets`.

### Making navigation rail accessible

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

### Adding a header view

The rail provides a convenient container for anchoring a header view, such as a
`FloatingActionButton` or a logo, to the top of the rail, using the
`app:headerLayout` attribute.

![Navigation rail with badges](assets/navigationrail/navigation-rail-fab.png)

```xml
<com.google.android.material.navigationrail.NavigationRailView
    android:id="@+id/navigation_rail"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:headerLayout="@layout/navigation_rail_fab"
    app:menu="@menu/navigation_rail_menu" />
```

The header view can also be added or removed at runtime using the following
methods:

**Method**                               | **Description**
---------------------------------------- | ---------------
`void addHeaderView(@NonNull View view)` | The specified header view will be attached to the NavigationRailView, so that it will appear at the top. If the view already has a header view attached to it, it will be removed first.
`void removeHeaderView()`                | Detaches the current header view if any, from the Navigation Rail.

The following methods can be used to manipulate the header view at runtime.

**Method**                       | **Description**
-------------------------------- | ---------------
`@Nullable view getHeaderView()` | Returns an instance of the header view associated with the Navigation Rail, null if none was currently attached.

### Adding badges

Rail icons can include badges on the upper right corner of the icon. Badges
convey dynamic information about the associated destination, such as counts or
status.

![Navigation rail with badges](assets/navigationrail/navigation-rail-badges.png)

Initialize and show a `BadgeDrawable` associated with `menuItemId`. Subsequent
calls to this method will reuse the existing `BadgeDrawable`:

```kt
var badge = navigationRail.getOrCreateBadge(menuItemId)
badge.isVisible = true
// An icon only badge will be displayed unless a number is set:
badge.number = 99
```

As best practice, if you need to temporarily hide the badge, for example until
the next notification is received, change the visibility of `BadgeDrawable`:

```kt
val badgeDrawable = navigationRail.getBadge(menuItemId)
    if (badgeDrawable != null) {
        badgeDrawable.isVisible = false
        badgeDrawable.clearNumber()
    }
```

To remove any `BadgeDrawable`s that are no longer needed:

```kt
navigationRail.removeBadge(menuItemId)
```

See the [`BadgeDrawable`](BadgeDrawable.md) documentation for more information.

## Navigation rail example

API and source code:

*   `NavigationRailView`
    *   [Class description](https://developer.android.com/reference/com/google/android/material/navigationrail/NavigationRailView)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigationrail/NavigationRailView.java)

The following example shows a navigation rail with four icons:

*   Alarms
*   Schedule
*   Timers
*   Stopwatch

In `navigation_rail_menu.xml` inside a `menu` resource directory:

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
  <item
      android:id="@+id/alarms"
      android:enabled="true"
      android:icon="@drawable/icon_alarm"
      android:title="@string/alarms_destination_label"/>
  <item
      android:id="@+id/schedule"
      android:enabled="true"
      android:icon="@drawable/icon_clock"
      android:title="@string/schedule_destination_label"/>
  <item
      android:id="@+id/timers"
      android:enabled="true"
      android:icon="@drawable/icon_sand_clock"
      android:title="@string/timers_destination_label"/>
  <item
      android:id="@+id/stopwatch"
      android:enabled="true"
      android:icon="@drawable/icon_stop_watch"
      android:title="@string/stopwatch_destination_label"/>
</menu>
```

In code:

```kt
navigationRail.selectedItemId = R.id.images
```

### Anatomy and key properties

The following is an anatomy diagram for the navigation rail:

![Navigation rail anatomy diagram](assets/navigationrail/navigation-rail-anatomy.png)

1.  Container
2.  Header - menu icon (optional)
3.  Header - Floating action button (optional)
4.  Icon - active
5.  Active indicator
6.  Label text - active (optional)
7.  Icon - inactive
8.  Label text - inactive (optional)
9.  Large badge (optional)
10. Large badge label (optional)
11. Badge (optional)

#### Container attributes

**Element**                             | **Attribute**                         | **Related methods**                               | **Default value**
--------------------------------------- | ------------------------------------- | ------------------------------------------------- | -----------------
**Color**                               | `app:backgroundTint`                  | N/A                                               | `?attr/colorSurface`
**Elevation**                           | `app:elevation`                       | `setElevation`                                    | `0dp`
**Fits system windows**                 | `android:fitsSystemWindows`           | `getFitsSystemWindows`<br/>`setFitsSystemWindows` | `true`
**Padding top system window insets**    | `app:paddingTopSystemWindowInsets`    | N/A                                               | `null`
**Padding bottom system window insets** | `app:paddingBottomSystemWindowInsets` | N/A                                               | `null`

#### Header attributes

**Element**     | **Attribute**      | **Related methods**                                        | **Default value**
--------------- | ------------------ | ---------------------------------------------------------- | -----------------
**Header view** | `app:headerLayout` | `addHeaderView`<br/>`removeHeaderView`<br/>`getHeaderView` | N/A

See the
[FAB documentation](https://github.com/material-components/material-components-android/tree/master/docs/components/FloatingActionButton.md)
for more attributes.

#### Navigation item attributes

**Element**               | **Attribute**             | **Related methods**                                   | **Default value**
------------------------- | ------------------------- | ----------------------------------------------------- | -----------------
**Menu resource**         | `app:menu`                | `inflateMenu`<br/>`getMenu`                           | N/A
**Ripple (inactive)**     | `app:itemRippleColor`     | `setItemRippleColor`<br/>`getItemRippleColor`         | `?attr/colorPrimary` at 12% (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigation/res/color/mtrl_navigation_bar_ripple_color.xml))
**Ripple (active)**       | `app:itemRippleColor`     | `setItemRippleColor`<br/>`getItemRippleColor`         | `?attr/colorPrimary` at 12% (see all [states](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigation/res/color/mtrl_navigation_bar_ripple_color.xml))
**Label visibility mode** | `app:labelVisibilityMode` | `setLabelVisibilityMode`<br/>`getLabelVisibilityMode` | `LABEL_VISIBILITY_AUTO`
**Item minimum height**   | `app:itemMinHeight`       | `setItemMinimumHeight`<br/>`getItemMinimumHeight`     | `NO_ITEM_MINIMUM_HEIGHT`

#### Active indicator attributes

**Element**           | **Attribute**          | **Related methods**                                                                   | **Default value**
--------------------- | ---------------------- | ------------------------------------------------------------------------------------- | -----------------
**Color**             | `android:color`        | `setItemActiveIndicatorColor`<br/>`getItemActiveIndicatorColor`                       | `?attr/colorSecondaryContainer`
**Width**             | `android:width`        | `setItemActiveIndicatorWidth`<br/>`getItemActiveIndicatorWidth`                       | `56dp`
**Height**            | `android:height`       | `setItemActiveIndicatorHeight`<br/>`setItemActiveIndicatorHeight`                     | `32dp`
**Shape**             | `app:shapeAppearance`  | `setItemActiveIndicatorShapeAppearance`<br/>`getItemActiveIndicatorShapeAppearance`   | `50% rounded`
**Margin horizontal** | `app:marginHorizontal` | `setItemActiveIndicatorMarginHorizontal`<br/>`getItemActiveIndicatorMarginHorizontal` | `4dp`

#### Icon attributes

**Element**          | **Attribute**                         | **Related methods**                                              | **Default value**
-------------------- | ------------------------------------- | ---------------------------------------------------------------- | -----------------
**Icon**             | `android:icon` in the `menu` resource | N/A                                                              | N/A
**Size**             | `app:itemIconSize`                    | `setItemIconSize`<br/>`setItemIconSizeRes`<br/>`getItemIconSize` | `24dp`
**Color (inactive)** | `app:itemIconTint`                    | `setItemIconTintList`<br/>`getItemIconTintList`                  | `?attr/colorOnSurfaceVariant`
**Color (active)**   | `app:itemIconTint`                    | `setItemIconTintList`<br/>`getItemIconTintList`                  | `?attr/colorOnSecondaryContainer`

#### Text label attributes

**Element**               | **Attribute**                          | **Related methods**                                                 | **Default value**
------------------------- | -------------------------------------- | ------------------------------------------------------------------- | -----------------
**Text label**            | `android:title` in the `menu` resource | N/A                                                                 | N/A
**Color (inactive)**      | `app:itemTextColor`                    | `setItemTextColor`<br/>`getItemTextColor`                           | `?attr/colorOnSurfaceVariant`
**Color (active)**        | `app:itemTextColor`                    | `setItemTextColor`<br/>`getItemTextColor`                           | `?attr/colorOnSurface`
**Typography (inactive)** | `app:itemTextAppearanceInactive`       | `setItemTextAppearanceInactive`<br/>`getItemTextAppearanceInactive` | `?attr/textAppearanceTitleSmall`
**Typography (active)**   | `app:itemTextAppearanceActive`         | `setItemTextAppearanceActive`<br/>`getItemTextAppearanceActive`     | `?attr/textAppearanceTitleSmall`

#### Styles

**Element**       | **Style**                             | **Container color**  | **Icon/Text label color (inactive)** | **Icon/Text label color (active)**
----------------- | ------------------------------------- | -------------------- | ------------------------------------ | ----------------------------------
**Default style** | `Widget.Material3.NavigationRailView` | `?attr/colorSurface` | `?attr/colorOnSurfaceVariant`        | `?attr/colorOnSurface`<br/>`?attr/colorOnSecondaryContainer`

Default style theme attribute: `?attr/navigationRailStyle`

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigationrail/res/values/styles.xml),
[navigation bar attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigation/res/values/attrs.xml),
and
[navigation rail attributes](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigationrail/res/values/attrs.xml).

## Theming a navigation rail

Navigation rail supports
[Material Theming](https://material.io/components/navigation-rail#theming),
which can customize color and typography.

### Navigation rail theming example

API and source code:

*   `NavigationRailView`
    *   [Class description](https://developer.android.com/reference/com/google/android/material/navigationrail/NavigationRailView)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/navigationrail/NavigationRailView.java)

The following example shows a navigation rail with Material Theming.

![Navigation rail theming example](assets/navigationrail/navigation-rail-theming.png)

#### Implementing navigation rail theming

Use theme attributes and a style in `res/values/styles.xml` which apply to all
navigation rails and affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="colorPrimary">@color/shrine_theme_light_primary</item>
    <item name="colorSecondaryContainer">@color/shrine_theme_light_secondaryContainer</item>
    <item name="colorOnSecondaryContainer">@color/shrine_theme_light_onSecondaryContainer</item>
    <item name="colorTertiaryContainer">@color/shrine_theme_light_tertiaryContainer</item>
    <item name="colorOnTertiaryContainer">@color/shrine_theme_light_onTertiaryContainer</item>
    <item name="colorError">@color/shrine_theme_light_error</item>
    <item name="colorErrorContainer">@color/shrine_theme_light_errorContainer</item>
    <item name="colorOnError">@color/shrine_theme_light_onError</item>
    <item name="colorOnErrorContainer">@color/shrine_theme_light_onErrorContainer</item>
    <item name="colorSurface">@color/shrine_theme_light_surface</item>
    <item name="colorOnSurface">@color/shrine_theme_light_onSurface</item>
    <item name="colorOnSurfaceVariant">@color/shrine_theme_light_onSurfaceVariant</item>
</style>
```

Use a default style theme attribute, styles, and a theme overlay, which apply to
all navigation rails but do not affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="navigationRailStyle">@style/Widget.App.NavigationRailView</item>
</style>

<style name="Widget.App.NavigationRailView" parent="Widget.Material3.NavigationRailView">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.NavigationRailView</item>
</style>

<style name="ThemeOverlay.App.NavigationRailView" parent="">
    <item name="colorPrimary">@color/shrine_theme_light_primary</item>
    <item name="colorSecondaryContainer">@color/shrine_theme_light_secondaryContainer</item>
    <item name="colorOnSecondaryContainer">@color/shrine_theme_light_onSecondaryContainer</item>
    <item name="colorTertiaryContainer">@color/shrine_theme_light_tertiaryContainer</item>
    <item name="colorOnTertiaryContainer">@color/shrine_theme_light_onTertiaryContainer</item>
    <item name="colorError">@color/shrine_theme_light_error</item>
    <item name="colorErrorContainer">@color/shrine_theme_light_errorContainer</item>
    <item name="colorOnError">@color/shrine_theme_light_onError</item>
    <item name="colorOnErrorContainer">@color/shrine_theme_light_onErrorContainer</item>
    <item name="colorSurface">@color/shrine_theme_light_surface</item>
    <item name="colorOnSurface">@color/shrine_theme_light_onSurface</item>
    <item name="colorOnSurfaceVariant">@color/shrine_theme_light_onSurfaceVariant</item>
</style>
```

Or use the style in the layout, which affects only this specific navigation rail
bar:

```xml
<com.google.android.material.navigationrail.NavigationRailView
    ...
    style="@style/Widget.App.NavigationRailView"
/>
```
