<!--docs:
title: "Bottom Navigation"
layout: detail
section: components
excerpt: "Bottom navigation bars make it easy to explore and switch between top-level views in a single tap."
iconId: bottom_navigation
path: /catalog/bottom-navigation-view/
-->

# Bottom Navigation

![Bottom Navigation](assets/bottom-navigation.svg)
<!--{: .article__asset.article__asset--screenshot }-->

`BottomNavigationView` creates **bottom navigation** bars, making it easy to
explore and switch between top-level content views with a single tap.

This pattern can be used when you have between 3 and 5 top-level destinations to
navigate to.

## Design & API Documentation

-   [Material Design guidelines: Bottom
    Navigation](https://material.io/go/design-bottom-navigation)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomnavigation/BottomNavigationView.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
    <!-- Styles for list items requiring icons instead of standard bullets. -->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

1. Create a [menu
resource](https://developer.android.com/guide/topics/resources/menu-resource.html)
with up to 5 navigation targets (`BottomNavigationView` does not support more than
5 items).
2. Lay out your `BottomNavigationView` below your content.
3. Set the `app:menu` attribute on your `BottomNavigationView` to your menu
resource.
4. Listen for selection events using `setOnNavigationItemSelectedListener(...)`.

A typical layout file would look something like this:

```xml
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <!-- Main content -->

  <com.google.android.material.bottomnavigation.BottomNavigationView
      android:id="@+id/bottom_navigation"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:layout_gravity="bottom"
      android:background="@color/colorPrimary"
      app:itemIconTint="@color/white"
      app:itemTextColor="@color/white"
      app:menu="@menu/bottom_navigation_menu" />

</FrameLayout>
```

### Handling States

The `app:itemIconTint` and `app:itemTextColor` take a
[ColorStateList](https://developer.android.com/reference/android/content/res/ColorStateList.html)
instead of a simple color. This means that you can write a `selector` for these
colors that responds to the items' state changes.

For example, you could have a `bottom_navigation_colors.xml` `ColorStateList`
that contains:

```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
  <item
      android:state_checked="true"
      android:color="@color/colorPrimary" />
  <item
      android:state_checked="false"
      android:color="@color/grey" />
 </selector>
```

And you would use it like this on your `BottomNavigationView`:

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottom_navigation"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/white"
    app:itemIconTint="@drawable/bottom_navigation_colors"
    app:itemTextColor="@drawable/bottom_navigation_colors"
    app:menu="@menu/bottom_navigation_menu" />
```

#### Material Styles

Using `BottomNavigationView` with an updated Material theme
(`Theme.MaterialComponents`) will provide the correct updated Material styles to
your `BottomNavigationView`s by default. If you need to use an updated
`BottomNavigationView` and your application theme does not inherit from an
updated Material theme, or if you'd like to use a variant style, you can apply
one of the updated Material styles directly to your widget in XML:

##### Updated Material Style (Default)

The default Material `BottomNavigationView` style consists of updated colors,
text sizing, and behavior. The default BottomNavigationView has white background
and icons and text colored with `colorPrimary`.

```
style="@style/Widget.MaterialComponents.BottomNavigationView"
```

##### Colored Material Style

This style inherits from the default style but sets the colors to different
mappings. Use the colored style to get a bottom navigation bar with a
`colorPrimary` background and shades of white for the icon and text colors.

```
style="@style/Widget.MaterialComponents.BottomNavigationView.Colored"
```

##### Legacy Style

You can set this style on your `BottomNavigationView` if you'd like a bottom
navigation bar with the old behavior. However, we recommend you use the updated
Material style where possible.

```
style="@style/Widget.Design.BottomNavigationView"
```

#### Theme Attribute Mapping

##### Updated Material Style (Default)

```
style="@style/Widget.MaterialComponents.BottomNavigationView"
```

Component Attribute                | Default Theme Attribute Value
---------------------------------- | -------------------------------
`itemTextAppearanceActive`         | `textAppearanceCaption`
`itemTextAppearanceInactive`       | `textAppearanceCaption`
`android:background`               | `colorSurface`
`itemIconTint` (checked, enabled)  | `colorPrimary`
`itemIconTint` (enabled)           | `colorOnSurface` at 54% opacity
`itemIconTint` (disabled)          | `colorOnSurface` at 21% opacity
`itemTextColor` (checked, enabled) | `colorPrimary`
`itemTextColor` (enabled)          | `colorOnSurface` at 54% opacity
`itemTextColor` (disabled)         | `colorOnSurface` at 21% opacity

##### Colored Material Style

```
style="@style/Widget.MaterialComponents.BottomNavigationView.Colored"
```

Component Attribute                | Default Theme Attribute Value
---------------------------------- | -------------------------------
`itemTextAppearanceActive`         | `textAppearanceCaption`
`itemTextAppearanceInactive`       | `textAppearanceCaption`
`android:background`               | `colorPrimary`
`itemIconTint` (checked, enabled)  | `colorOnPrimary`
`itemIconTint` (enabled)           | `colorOnPrimary` at 54% opacity
`itemIconTint` (disabled)          | `colorOnPrimary` at 21% opacity
`itemTextColor` (checked, enabled) | `colorOnPrimary`
`itemTextColor` (enabled)          | `colorOnPrimary` at 54% opacity
`itemTextColor` (disabled)         | `colorOnPrimary` at 21% opacity

##### Legacy Style

```
style="@style/Widget.Design.BottomNavigationView"
```

The legacy Material style of `BottomNavigationView` does not make use of our new
theming attributes.

## Related Concepts

There are other navigation patterns you should be aware of:

-   [Hierarchical navigation](https://developer.android.com/training/implementing-navigation/index.html).
    *See also [Navigation with Back and
    Up](https://developer.android.com/design/patterns/navigation.html).*
-   Swipeable tabs using [TabLayout](TabLayout.md) and
    [ViewPager](https://developer.android.com/reference/android/support/v4/view/ViewPager.html).
-   Using [NavigationView](NavigationView.md) to display a longer list of
    navigation targets, usually in a navigation drawer.
