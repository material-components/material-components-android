<!--docs:
title: "Back Layer"
layout: detail
section: components
excerpt: "Back layers are views that sit below a main content layer with some content in the back layer being obscured by the content layer."
path: /catalog/back-layer-layout/
-->

# Back Layer

Back layers are very similar to [app bars](AppBarLayout.md). The main
differences are:

-   back layers sit below the a main view (called the *content layer* in this
    document)
-   a part of the back layer is always visible as opposed to app bars which can
    slide off-screen when you scroll.
    -   The always-visible part of the back layer can be anchored to any edge of
        the screen (left, top, right, bottom, start or end). The app bar is
        always anchored to the top.
-   The content layer is elevated above the back layer and casts a shadow on the
    back layer.

The back layer has two possible states: collapsed and expanded. In the expanded
state the content layer slides away to reveal the entire contents of the back
layer. In the collapsed state only part of the back layer content is displayed.
This part of the content acts as a header and is always visible.

The back layer is similar to a modal dialog. When the back layer is expanded,
the content layer is not fully visible and does not handle interactions, instead
clicks on the content layer collapse the back layer and do not interact with the
content layer.

## Design & API Documentation

-   [BackLayerLayout class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/src/android/support/design/backlayer/BackLayerLayout.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [BackLayerSiblingBehavior class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/src/android/support/design/backlayer/BackLayerSiblingBehavior.java)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

To use a back layer you need two views: one that is the back layer and the other
which will act as the content layer. Both views must be direct children of the
same `CoordinatorLayout`.

Note: To use `BackLayerLayout` your activity **MUST NOT** handle configuration
changes itself (this means, you can't use `android:configChanges` in
`AndroidManifest.xml`).

The back layer is a view of type `BackLayerLayout`, which is a subclass of
(`LinearLayout`)[https://developer.android.com/reference/android/widget/LinearLayout.html].

The back layer must:

-   Set both `layout_width` and `layout_height` to `match_parent`.
-   Anchor to an edge of the screen by setting `android:gravity` and
    `android:layout_gravity`. The value for those two properties must be the
    same and can be any of `left`, `top`, `right`, `bottom`, `start` or `end`.
-   Set `android:orientation`to `vertical` or `horizontal` matching the gravity
    set above: `vertical` if `gravity` is either `top` or `bottom`, `horizontal`
    otherwise.
-   Choose a way to set the minimum (collapsed) size:
    -   Either set `android:minimumHeight` and `android:minimumWidth`
        appropriately, or
    -   put all of the content that should be shown at all times inside a
        `android.support.design.backlayer.CollapsedBackLayerContents` and let
        the `BackLayerLayout` calculate its own minimum size. There must be
        exactly one of such `CollapsedBackLayerContents`subviews.
-   NOT use a `ViewGroup.OnHierarchyChangedListener`. The back layer uses
    this listener for internal housekeeping.

The content layer must:

-   Set `app:layout_behavior="@string/design_backlayer_sibling_behavior"`.
-   Set a value for elevation, higher than that of the back layer.
-   Set both `layout_width` and `layout_height` to `match_parent`.
-   Set a string as `app:behavior_expandedContentDescription`. This string will
    be used as a content description for the content layer, so vision-impaired
    users are informed that clicking the content layer collapses the back layer.
    If not provided, a default string will be used for this purpose.
-   be below the `BackLayerLayout` in the xml layout file.
-   NOT use a `ViewGroup.OnHierarchyChangedListener` on the content layer as the
    BackLayerSiblingBehavior uses this for internal housekeeping.

### Exposing or collapsing the back layer

`BackLayerLayout` does not provide a default binding to expand the back layer,
so you must do it manually. For example, you can add a button with a
`View.OnClickListener` that expands/collapses the back layer. For this purpose
`BackLayerLayout` provides `setExpanded(boolean)` and `isExpanded()`.

When the back layer is expanded, any click on the content layer automatically
collapses the back layer. This is by design, since part of the content layer
will not be visible.

The back layer expansion or collapse is animated. You can add your own
animations to those events using a `BackLayerCallback`.

When the activity is restarted, the back layer saves and restores its own state
(whether it is expanded or not). The back layer does not animate restoring to an
expanded state nor calls the `onBeforeExpand` / `onAfterExpand` methods in this
case.

Since the back layer does not call `onBeforeExpand` and `onAfterExpand` when
restoring a expanded back layer after an activity restart, you may need to save
and restore the state these callbacks change elsewhere (your activity/fragment
onSaveInstanceState/onRestoreInstanceState) or implement a non-animated version
in `BackLayerCallback.onRestoringExpandedBackLayer()`.

### Multiple back layer contents

You can have multiple pieces of content inside the back layer. For example, a
back layer can show navigation or search based on different click listeners.

The way to achieve this is to set the visibility of the secondary content to
gone. You can switch the visibility of the content at any time in response to
click events and the content layer will slide into position to accommodate to
the new size.

### A typical layout file

```xml
<android.support.design.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/coordinator_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <android.support.design.backlayer.BackLayerLayout
      android:id="@+id/backlayer"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:layout_gravity="top"
      android:background="@android:color/white"
      android:gravity="top"
      android:orientation="vertical">
    <android.support.design.backlayer.CollapsedBackLayerContents
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
      <!-- This part of the back layer is always visible. -->
      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:gravity="center_vertical"
          android:orientation="horizontal">
        <!-- This is the button that opens the back layer in navigation mode -->
        <ImageView
            android:id="@+id/menu_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_menu_grey600_36dp"/>
        <!-- This is some title text to display in the back layer -->
        <TextView
            style="@style/backlayer_header_style"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/backlayer_title"/>
        <!-- This is the button that opens the back layer in search mode -->
        <ImageView
            android:id="@+id/backlayer_search_button"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@android:drawable/ic_menu_search"/>
      </LinearLayout>
    </android.support.design.backlayer.CollapsedBackLayerContents>
    <!-- This is the part of the back layer that is obscured by the content
         layer -->
    <NavigationView
        android:id="@+id/backlayer_navigation"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
    <!-- Notice that the SearchView's visibility is set to gone because it's the
         secondary content. When the search button is clicked it can switch the
         visibility accordingly. -->
    <SearchView
        android:id="@+id/backlayer_search"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"/>
  </android.support.design.backlayer.BackLayerLayout>
  <!-- The content layer is below the BackLayerLayout in the xml layout, but the
       content layer uses a higher elevation. It can be any view you like as
       long as it sets the BackLayerSiblingBehavior as its app:layout_behavior.
  -->
  <YourContentView
      android:id="@+id/backlayer_content"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@color/content_background"
      android:elevation="12dp"
      app:layout_behavior="@string/design_backlayer_sibling_behavior"
      app:behavior_expandedContentDescription=
                   "@string/cat_backlayer_expanded_content_description"/>
</android.support.design.widget.CoordinatorLayout>
```
