<!--docs:
title: "Bottom App Bar"
layout: detail
section: components
excerpt: "A flexible toolbar designed to provide a typical Material Design experience."
iconId: bottom_app_bar
path: /catalog/bottom-app-bar/
-->

# Bottom App Bars

One of the defining features of Material Design is the design of the
`BottomAppBar`. Based on the changing needs and behaviors of users, the
`BottomAppBar` is an evolution from standard Material guidance. It puts more
focus on features, increases engagement, and visually anchors the UI.

## Design & API Documentation

-   [Material Design guidelines: Bottom App
    Bar](https://material.io/go/design-app-bar-bottom)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com.google.android.material.bottomappbar/BottomAppBar.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/bottomappbar/BottomAppBar)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

Here's an example of how to include the widget in your layout:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <!-- Other components and views -->

  <com.google.android.material.bottomappbar.BottomAppBar
      android:id="@+id/bar"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:layout_gravity="bottom"
      app:navigationIcon="@drawable/ic_menu_24"/>

  <com.google.android.material.floatingactionbutton.FloatingActionButton
      android:id="@+id/fab"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      app:layout_anchor="@id/bar"/>

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

The `FloatingActionButton` can be anchored to the `BottomAppBar` by setting
`app:layout_anchor` or by calling
`CoordinatorLayout.LayoutParams#setAnchorId(int)`.

### Material Styles

Using `BottomAppBar` with an updated Material theme (`Theme.MaterialComponents`)
will provide the correct updated Material styles by default. If your application
theme does not inherit from an updated Material theme, you can apply the
`BottomAppBar` Material style directly to your widget in XML.

```xml
style="@style/Widget.MaterialComponents.BottomAppBar"
```

### Bottom App Bar Attributes

Feature                  | Relevant attributes
:----------------------- | :---------------------------------
Background Tint          | `app:backgroundTint`
FAB Alignment Mode       | `app:fabAlignmentMode`
FAB Cradle Margin        | `app:fabCradleMargin`
FAB Cradle Corner Radius | `app:fabCradleRoundedCornerRadius`
FAB Vertical Offset      | `app:fabCradleVerticalOffset`
Hide on scroll           | `app:hideOnScroll`

#### Background Tint

The `BottomAppBar` internally handles its own background. This allows it to
automatically cradle the `FloatingActionButton` when it is attached, but it also
means that you shouldn't call `setBackground()` or use the `android:background`
attribute in xml. Instead, the `app:backgroundTint` attribute will allow you to
set a tint.

#### `FloatingActionButton` Alignment Modes

The `FloatingActionButton` can be aligned either to the center
(`FAB_ALIGNMENT_MODE_CENTER`) or to the end (`FAB_ALIGNMENT_MODE_END`) by
calling `setFabAlignmentMode(int)`. The default animation will automatically be
run. This can be coordinated with a `Fragment` transition to allow for a smooth
animation from a primary screen to a secondary screen.

#### `FloatingActionButton` Attributes

The placement of the `FloatingActionButton` can be controlled by
`fabAlignmentMode`, `fabCradleMargin`, `fabCradleRoundedCornerRadius`, and
`fabCradleVerticalOffset`. The starting alignment mode (`fabAlignmentMode`) can
be set to either `center` or `end`. Changing the `fabCradleMargin` will increase
or decrease the distance between the `FloatingActionButton` and the
`BottomAppBar`. The `fabCradleRoundedCornerRadius` specifies the roundness of
the corner around the cutout. The `fabCradleVerticalOffset` specifies the
vertical offset between the `FloatingActionButton` and the `BottomAppBar`. If
`fabCradleVerticalOffset` is 0, the center of the `FloatingActionButton` will be
aligned with the top of the `BottomAppBar`.

#### Hide on scroll

The `BottomAppBar` can be set to hide on scroll with the `hideOnScroll`
attribute. To enable this behavior, you should ensure that the scrolling content
is in a `NestedScrollView`. There's no need to wrap the `BottomAppBar` in an
`AppBarLayout` or use any of the scroll flags associated with `AppBarLayout`
such as `app:layout_scrollFlags`.

### Handling Menu Options

There are two ways to handle menu options. The first way is to directly call
`setOnMenuItemClickListener(OnMenuItemClickListener)` and to handle the menu
options in the callback:

```java
BottomAppBar bar = (BottomAppBar) findViewById(R.id.bar);

bar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Handle actions based on the menu item
        return true;
    }
});
```

The other way is to call `setSupportActionBar()` on the `BottomAppBar`. This
will set up the menu callbacks in a similar way to Toolbar which hooks into
`Activity#onCreateOptionsMenu()` and `Activity#onOptionsItemSelected()`. This
makes it easier to transition from a `Toolbar` which was set as the action bar
to a `BottomAppBar`. This will also allow you to handle the navigation item
click by checking if the menu item id is `android.R.id.home`.

```java
BottomAppBar bar = (BottomAppBar) findViewById(R.id.bar);
setSupportActionBar(bar);
```

### Handling Navigation Item Click

If you use `setSupportActionBar()` to set up the `BottomAppBar` you can handle
the navigation menu click by checking if the menu item id is
`android.R.id.home`. The other option is to call
`setNavigationOnClickListener(OnClickListener)`:

```java
BottomAppBar bar = (BottomAppBar) findViewById(R.id.bar);
bar.setNavigationOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        // Handle the navigation click by showing a BottomDrawer etc.
    }
});
```
