<!--docs:
title: "Tab Layout"
layout: detail
section: components
excerpt: "A horizontal layout to display tabs."
iconId: tabs
path: /catalog/tab-layout/
-->

# Tab Layout

`TabLayout` provides a horizontal layout to display tabs. The layout handles
interactions for a group of tabs including:

- scrolling behavior,
- (swipe) gestures,
- tab selection,
- animations,
- and alignment.

The Android Developers site provides [detailed documentation](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout.html)
on implementing `TabLayout`.

## Design & API Documentation

-   [Material Design guidelines:
    Tabs](https://material.io/go/design-tabs)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/tabs/TabLayout.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout.html)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

To use a `TabLayout` with a static number of tabs, define each tab as a
`TabItem` directly in the layout.

```xml
<com.google.android.material.tabs.TabLayout
    android:id="@+id/tabs"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

  <com.google.android.material.tabs.TabItem
      android:icon="@drawable/ic_icon_a_24"
      android:text="@string/tab_a_label"/>
  <com.google.android.material.tabs.TabItem
      android:icon="@drawable/ic_icon_b_24"
      android:text="@string/tab_b_label"/>
  <com.google.android.material.tabs.TabItem
      android:icon="@drawable/ic_icon_c_24"
      android:text="@string/tab_c_label"/>

</com.google.android.material.tabs.TabLayout>
```

A tab layout should be used above the content associated with the respective
tabs and lets the user quickly change between content views. These content views
are often held in a
[ViewPager](https://developer.android.com/reference/android/support/v4/view/ViewPager.html).

Use [setupWithViewPager(ViewPager)](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout.html#setupWithViewPager(android.support.v4.view.ViewPager))
to link a `TabLayout` with a ViewPager. The
individual tabs in the `TabLayout` will be automatically populated with the page
titles from the PagerAdapter.

```java
ViewPager pager;
TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

tabs.setupWithViewPager(pager);
```

Alternatively, you can add a `TabLayout` to a ViewPager in XML:

```xml
<android.support.v4.view.ViewPager
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <com.google.android.material.tabs.TabLayout
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:layout_gravity="top" />

</android.support.v4.view.ViewPager>
```

### Material Styles

Using `TabLayout` with an updated Material theme (`Theme.MaterialComponents`)
will provide the correct updated Material styles to your tabs by default. If you
need to use updated Material tabs and your application theme does not inherit
from an updated Material theme, you can apply one of the updated Material styles
directly to your widget in XML.

#### Updated Material Style (Default)

The updated Material `TabLayout` style consists of updated icon and label tints,
ripple color, and ripple shape.

```xml
<com.google.android.material.tabs.TabLayout
    style="@style/Widget.MaterialComponents.TabLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

#### Colored Material Style

The colored Material `TabLayout` style consists of updated background color
based on `?attr/colorAccent`.

```xml
<com.google.android.material.tabs.TabLayout
    style="@style/Widget.MaterialComponents.TabLayout.Colored"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

#### Legacy Material Style

```xml
<com.google.android.material.tabs.TabLayout
    style="@style/Widget.Design.TabLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

### Attributes

Feature    | Relevant attributes
:--------- | :--------------------------
Size       | `app:tabMinWidth`
           | `app:tabMaxWidth`
Scroll     | `app:tabMode`
Centered   | `app:tabGravity`
Background | `app:tabBackground`
Icon       | `app:tabIconTint`
           | `app:tabIconTintMode`
Label      | `app:tabInlineLabel`
           | `app:tabTextAppearance`
           | `app:tabTextColor`
           | `app:tabSelectedTextColor`
Indicator  | `app:tabIndicatorColor`
           | `app:tabIndicatorHeight`
           | `app:tabIndicator`
           | `app:tabIndicatorGravity`
           | `app:tabIndicatorFullWidth`
Position   | `app:tabContentStart`
           | `app:tabPaddingStart`
           | `app:tabPaddingTop`
           | `app:tabPaddingEnd`
           | `app:tabPaddingBottom`
           | `app:tabPadding`
Ripple     | `app:tabRippleColor`
           | `app:tabUnboundedRipple`
