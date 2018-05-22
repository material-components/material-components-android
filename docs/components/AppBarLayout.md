<!--docs:
title: "Top App Bars"
layout: detail
section: components
excerpt: "A flexible toolbar designed to provide a typical Material Design experience."
iconId: toolbar
path: /catalog/app-bar-layout/
-->

# Top App Bars

![Top App Bars](assets/app-bars.svg)
<!--{: .article__asset.article__asset--screenshot }-->

`AppBarLayout` is a ViewGroup, most commonly used to wrap a `Toolbar`, that provides
many of the Material Design features and interactions for **Top App Bars**, namely
responsiveness to scrolling.

## Design & API Documentation

*   [Material Design guidelines: Top App
    Bar](https://material.io/go/design-app-bar-top)
    <!--{: .icon-list-item.icon-list-item--spec }-->
*   [Material Design guidelines: Scrolling
    techniques](https://material.io/go/design-app-bar-top#behavior)
    <!--{: .icon-list-item.icon-list-item--spec }-->
*   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/appbar/AppBarLayout.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
    <!-- Styles for list items requiring icons instead of standard bullets. -->
*   [Class
    overview](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

As a container for Toolbars, and other views, it works with
[CoordinatorLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout)
in order to respond to scrolling techniques. `AppBarLayout` depends heavily on
being used as a direct child of the CoordinatorLayout and reacts to a sibling
that supports scrolling (e.g.
[NestedScrollView](https://developer.android.com/reference/android/support/v4/widget/NestedScrollView.html),
[RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html)).

Flags are added to each child of the `AppBarLayout` to control how they will
respond to scrolling. These are interpreted by the `AppBarLayout.LayoutParams`.

**Available flags are:**

*   [enterAlways](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout.LayoutParams#SCROLL_FLAG_ENTER_ALWAYS)
*   [enterAlwaysCollapsed](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout.LayoutParams#SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED)
*   [exitUntilCollapsed](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout.LayoutParams#SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
*   [scroll](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout.LayoutParams#SCROLL_FLAG_SCROLL)
*   [snap](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout.LayoutParams#SCROLL_FLAG_SNAP)
*   [snapMargins](https://developer.android.com/reference/com/google/android/material/appbar/AppBarLayout.LayoutParams#SCROLL_FLAG_SNAP_MARGINS)

Views using the scroll flag should be declared and visually positioned before
other views in the `AppBarLayout`. This ensures that they are able to exit at the
top of the screen, leaving behind fixed, or pinned, elements.

### Lift On Scroll

Top App Bars can also be fixed in place and positioned at the same elevation as
content. Upon scroll, they can increase elevation and let content scroll behind
them. This design pattern is called "Lift On Scroll" and can be implemented by
setting `app:liftOnScroll="true"` on your `AppBarLayout`.

Note: the `liftOnScroll` attribute requires that you apply the
`@string/appbar_scrolling_view_behavior` `layout_behavior` to your scrolling view (e.g.,
`NestedScrollView`, `RecyclerView`, etc.).

## Related Concepts

The Top App Bar is a way of referencing a specific type of *Toolbar*. It's not a
separate Android class. This UI element is often used to provide branding for
the app as well as a place to handle common actions like navigation, search, and
menus. These are accessible via text or buttons in the Toolbar. A Toolbar that
provides some of these features is often referred to as the Top App Bar. They
are programmatically identical and use the Toolbar class.

The Top App Bar was previously termed *action bar*, and there are methods that
utilize this name (e.g. [getSupportActionBar](https://developer.android.com/reference/android/support/v7/app/AppCompatActivity.html#getSupportActionBar())).
Other than use of the action bar APIs, references to this prominent Toolbar
element should be *Top App Bar*.

A CollapsingToolbarLayout is often used as a wrapper around the Toolbar to
provide additional UI features in relation to scrolling.

*   [Top App Bar](https://material.io/go/design-app-bar-top)
*   [CollapsingToolbarLayout](CollapsingToolbarLayout.md)
