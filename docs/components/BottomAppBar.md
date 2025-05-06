<!--docs:
title: "Bottom app bars"
layout: detail
section: components
excerpt: "Android bottom app bars."
iconId: bottom_app_bar
path: /catalog/bottom-app-bars/
-->

# Bottom app bars

A [bottom app bar](https://material.io/components/bottom-app-bar/) displays
navigation and key actions at the bottom of mobile screens.

![Purple bottom app bar with floating action button](assets/bottomappbar/bottom-app-bar-hero.png)

## Design & API documentation

*   [Material 3 (M3) spec](https://material.io/components/bottom-app-bar/overview)
*   [API Reference](https://developer.android.com/reference/com/google/android/material/bottomappbar/package-summary)

## Using bottom app bars

Before you can use Material bottom app bars, you need to add a dependency to the
Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/tree/master/docs/getting-started.md)
page.

Bottom app bars provide access to up to four actions, including the
[floating action button](FloatingActionButton.md) (FAB).

### Anatomy

![Bottom app bar anatomy diagram](assets/bottomappbar/bottom-app-bar-anatomy.png)

1.  Container
2.  Floating action button (FAB) (optional)
3.  Action item(s) (optional)
4.  Navigation icon (optional)
5.  Overflow menu (optional)

**Note:** This doc reflects the Bottom App Bar after the changes in 1.7 to
reflect the current M3 style. Use `Widget.Material3.BottomAppBar.Legacy` to
revert back to the previous style.

More details on anatomy items in the [component guidelines](https://m3.material.io/components/bottom-app-bar/guidelines#a1332bc2-701f-4234-93e9-ccc2470434d6).

### Key properties

#### Container attributes

Element       | Attribute                | Related method(s)                          | Default value
------------- | ------------------------ | ------------------------------------------ | -------------
**Color**     | `app:backgroundTint`     | `setBackgroundTint`<br>`getBackgroundTint` | `?attr/colorSurfaceContainer`
**Elevation** | `app:elevation`          | `setElevation`                             | `3dp`
**Height**    | `android:minHeight`      | `setMinimumHeight`<br>`getMinimumHeight`   | `80dp`
**Shadows**   | `app:addElevationShadow` | N/A                                        | `false`

#### Navigation icon attributes

Element   | Attribute                | Related method(s)                          | Default value
--------- | ------------------------ | ------------------------------------------ | -------------
**Icon**  | `app:navigationIcon`     | `setNavigationIcon`<br>`getNavigationIcon` | `null`
**Color** | `app:navigationIconTint` | `setNavigationIconTint`                    | `?attr/colorOnSurfaceVariant` (as `Drawable` tint)

#### FAB attributes

Element                          | Attribute                          | Related method(s)                                                      | Default value
-------------------------------- | ---------------------------------- | ---------------------------------------------------------------------- | -------------
**Alignment mode**               | `app:fabAlignmentMode`             | `setFabAlignmentMode`<br>`getFabAlignmentMode`                         | `end`
**Animation mode**               | `app:fabAnimationMode`             | `setFabAnimationMode`<br>`getFabAnimationMode`                         | `slide`
**Anchor mode**                  | `app:fabAnchorMode`                | `setFabAnchorMode` <br> `getFabAnchorMode`                             | `embed`
**Cradle margin**                | `app:fabCradleMargin`              | `setFabCradleMargin`<br>`getFabCradleMargin`                           | `6dp`
**Cradle rounded corner radius** | `app:fabCradleRoundedCornerRadius` | `setFabCradleRoundedCornerRadius`<br>`getFabCradleRoundedCornerRadius` | `4dp`
**Cradle vertical offset**       | `app:fabCradleVerticalOffset`      | `setCradleVerticalOffset`<br>`getCradleVerticalOffset`                 | `12dp`
**End margin**                   | `app:fabAlignmentModeEndMargin`    | `setFabAlignmentModeEndMargin` <br> `getFabAlignmentModeEndMargin`     | `16dp`
**Embedded elevation**           | `app:removeEmbeddedFabElevation`   | N/A                                                                    | `true`

See the
[FAB documentation](https://github.com/material-components/material-components-android/tree/master/docs/components/FloatingActionButton.md)
for more attributes.

#### Action item(s) attributes

Element            | Attribute               | Related method(s)                                  | Default value
------------------ | ----------------------- | -------------------------------------------------- | -------------
**Menu**           | `app:menu`              | `replaceMenu`<br>`getMenu`                         | `null`
**Icon color**     | N/A                     | N/A                                                | `?attr/colorControlNormal` (as `Drawable` tint)
**Alignment mode** | `app:menuAlignmentMode` | `setMenuAlignmentMode` <br> `getMenuAlignmentMode` | `start`

#### Overflow menu attributes

Element             | Attribute                                                                                          | Related method(s)                      | Default value
------------------- | -------------------------------------------------------------------------------------------------- | -------------------------------------- | -------------
**Icon**            | `android:src` and `app:srcCompat` in `actionOverflowButtonStyle` (in app theme)                    | `setOverflowIcon`<br>`getOverflowIcon` | `@drawable/abc_ic_menu_overflow_material` (before API 23) or `@drawable/ic_menu_moreoverflow_material` (after API 23)
**Theme**           | `app:popupTheme`                                                                                   | `setPopupTheme`<br>`getPopupTheme`     | `@style/ThemeOverlay.Material3.*`
**Item typography** | `textAppearanceSmallPopupMenu` and `textAppearanceLargePopupMenu` in `app:popupTheme` or app theme | N/A                                    | `?attr/textAppearanceTitleMedium`

#### Styles

Element           | Style
----------------- | -------------------------------
**Default style** | `Widget.Material3.BottomAppBar`

Default style theme attribute: `bottomAppBarStyle`

See the full list of
[styles](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomappbar/res/values/styles.xml)
and
[attrs](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomappbar/res/values/attrs.xml).

### Bottom app bar examples

The following example shows a bottom app bar with a navigation icon, 3 action
icons, and an embedded FAB.

<img src="assets/bottomappbar/bottomappbar_basic.png" alt="Purple bottom app bar with grey icons and purple inset floating action button." width="650"/>

In the layout:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    ...
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Note: A RecyclerView can also be used -->
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:paddingBottom="100dp"
        android:clipToPadding="false">

        <!-- Scrollable content -->

    </androidx.core.widget.NestedScrollView>

    <com.google.android.material.bottomappbar.BottomAppBar
        android:id="@+id/bottomAppBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        style="@style/Widget.Material3.BottomAppBar"
        app:navigationIcon="@drawable/ic_drawer_menu_24px"
        app:menu="@menu/bottom_app_bar"
        />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:srcCompat="@drawable/ic_add_24dp"
        app:layout_anchor="@id/bottomAppBar"
        />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

In `menu/bottom_app_bar.xml`:

```xml
<menu
      ...>
    <item
      android:id="@+id/accelerator"
      android:icon="@drawable/ic_accelerator_24px"
      android:title="@string/accelerator"
      android:contentDescription="@string/content_description_accelerator"
      app:showAsAction="ifRoom"/>

    <item
      android:id="@+id/rotation"
      android:icon="@drawable/ic_3d_rotation_24px"
      android:title="@string/rotation"
      android:contentDescription="@string/content_description_rotation"
      app:showAsAction="ifRoom"/>

    <item
      android:id="@+id/dashboard"
      android:icon="@drawable/ic_dashboard_24px"
      android:title="@string/dashboard"
      android:contentDescription="@string/content_description_dashboard"
      app:showAsAction="ifRoom"/>

</menu>
```

  In menu/navigation icon drawables:

```xml
<vector
    ...
    android:tint="?attr/colorControlNormal">
    ...
</vector>
```

In code:

```kt
bottomAppBar.setNavigationOnClickListener {
    // Handle navigation icon press
}

bottomAppBar.setOnMenuItemClickListener { menuItem ->
    when (menuItem.itemId) {
        R.id.accelerator -> {
            // Handle accelerator icon press
            true
        }
        R.id.rotation -> {
            // Handle rotation icon press
            true
        }
        R.id.dashboard -> {
            // Handle dashboard icon press
            true
        }
        else -> false
    }
}
```

## Customizing bottom app bars

<details>
  <summary><h3>Theming bottom app bars</h3></summary>

Bottom app bars support
[Material Theming](https://m3.material.io/components/bottom-app-bar/#specs) which
can customize color, typography and shape.

#### Bottom app bar theming example

API and source code:

*   `CoordinatorLayout`
    *   [Class definition](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout)
*   `BottomAppBar`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/bottomappbar/BottomAppBar)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomappbar/BottomAppBar.java)
*   `FloatingActionButton`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/floatingactionbutton/FloatingActionButton)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/floatingactionbutton/FloatingActionButton.java)
*   `BottomAppBarCutCornersTopEdge`:
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/bottomappbar/BottomAppBarCutCornersTopEdge.java)

The following example shows a bottom app bar with Material Theming.

<img src="assets/bottomappbar/bottomappbar_theming.png" alt="Pink bottom app bar with pink diamond inset FAB and brown icons." width="600"/>

#### Implementing bottom app bar theming

Use theme attributes in `res/values/styles.xml`, which applies the theme to all
bottom app bars and FABs and affects other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="colorSurfaceContainer">@color/shrine_pink_100</item>
    <item name="colorOnSurface">@color/shrine_pink_900</item>
    <item name="colorPrimaryContainer">@color/shrine_pink_50</item>
    <item name="colorOnPrimaryContainer">@color/shrine_pink_900</item>
    <item name="textAppearanceTitleMedium">@style/TextAppearance.App.Medium</item>
    <item name="shapeAppearanceCornerLarge">@style/ShapeAppearance.App.Corner.Large</item>
</style>

<style name="TextAppearance.App.Medium" parent="TextAppearance.Material3.TitleMedium">
    <item name="fontFamily">@font/rubik</item>
    <item name="android:fontFamily">@font/rubik</item>
</style>

<style name="ShapeAppearance.App.Corner.Large" parent="ShapeAppearance.Material3.Corner.Large">
    <item name="cornerFamily">cut</item>
    <item name="cornerSize">50%</item>
</style>
```

Use default style theme attributes, styles and theme overlays, which applies the
theme to all bottom app bars and FABs but does not affect other components:

```xml
<style name="Theme.App" parent="Theme.Material3.*">
    ...
    <item name="bottomAppBarStyle">@style/Widget.App.BottomAppBar</item>
    <item name="floatingActionButtonStyle">@style/Widget.App.FloatingActionButton</item>
</style>

<style name="Widget.App.BottomAppBar" parent="Widget.Material3.BottomAppBar">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.BottomAppBar</item>
</style>

<style name="Widget.App.FloatingActionButton" parent="Widget.Material3.FloatingActionButton.Primary">
    <item name="materialThemeOverlay">@style/ThemeOverlay.App.FloatingActionButton</item>
</style>

<style name="ThemeOverlay.App.BottomAppBar" parent="ThemeOverlay.Material3.BottomAppBar">
    <item name="colorSurfaceContainer">@color/shrine_pink_100</item>
    <item name="colorOnSurface">@color/shrine_pink_900</item>
    <item name="textAppearanceTitleMedium">@style/TextAppearance.App.TitleMedium</item>
</style>

<style name="ThemeOverlay.App.FloatingActionButton" parent="ThemeOverlay.Material3.FloatingActionButton.Primary">
    <item name="colorPrimaryContainer">@color/shrine_pink_50</item>
    <item name="colorOnPrimaryContainer">@color/shrine_pink_900</item>
    <item name="shapeAppearanceCornerLarge">@style/ShapeAppearance.App.Corner.Large</item>
</style>
```

Use the styles in the layout, which affects only this bottom app bar and FAB:

```xml
<com.google.android.material.bottomappbar.BottomAppBar
    ...
    style="@style/Widget.App.BottomAppBar"
    />

<com.google.android.material.floatingactionbutton.FloatingActionButton
    ...
    style="@style/Widget.App.FloatingActionButton"
    />
```
</details>

<details>
<summary><h3>Making bottom app bars accessible</h3></summary>

Android's bottom app bar component APIs provide support for the navigation icon,
action items, overflow menu and more to tell the user what each action performs.
While optional, their use is strongly encouraged.

#### Content descriptions

When using navigation icons, action items and other elements of bottom app bars,
you should set a content description for them so that screen readers like
TalkBack are able to announce their purpose or action.

For an overall content description of the bottom app bar, set an
`android:contentDescription` or use the `setContentDescription` method on the
`BottomAppBar`.

For the navigation icon, use the `app:navigationContentDescription` attribute or
`setNavigationContentDescription` method.

For action items and items within the overflow menu, set the content description
in the menu:

```xml
<menu ...>
    ...
    <item
          ...
          android:contentDescription="@string/content_description_one" />
    <item
          ...
          android:contentDescription="@string/content_description_two" />
</menu>
```

#### Talkback

Bottom app bar can optionally hide on scroll with the `app:hideOnScroll`
attribute. When this attribute is set to true, scrolling will hide the bottom
app bar and prevent it from being seen by any screen readers which may be
confusing for users. To prevent this, the hide behavior is automatically
disabled when Talkback is enabled. Although discouraged for accessibility, you
can optionally force the hide behavior by calling
`bottomAppBar.disableHideOnTouchExploration(false)`.

Depending on your layout, disabling the hide behavior may potentially cause
content to be obscured behind the bar. Make sure to add the appropriate bottom
padding of the height of the bottom app bar to the content. See below for an
example:

```
val am = context.getSystemService(AccessibilityManager::class.java)
if (am != null && am.isTouchExplorationEnabled) {
    bar.setHideOnScroll(false)
    bar.post {
        content.setPadding(
            content.paddingLeft,
            content.paddingTop,
            content.paddingRight,
            content.paddingBottom + bar.measuredHeight
        )
    }
}
```

</details>

<details>
  <summary><h3>Applying scrolling behavior to the bottom app bar</h3></summary>

The following example shows the bottom app bar hiding when scrolling the
scrollable content down, and appearing when scrolling up.

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    ...>

    ...

    <com.google.android.material.bottomappbar.BottomAppBar
        ...
        app:hideOnScroll="true"
        />

    ...

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```
</details>
