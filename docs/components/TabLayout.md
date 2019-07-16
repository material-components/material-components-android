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

-   scrolling behavior,
-   (swipe) gestures,
-   tab selection,
-   animations,
-   and alignment.

The Android Developers site provides
[detailed documentation](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout)
on implementing `TabLayout`.

## Design & API Documentation

-   [Material Design guidelines: Tabs](https://material.io/go/design-tabs)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/tabs/TabLayout.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout)
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

Use
[setupWithViewPager(ViewPager)](https://developer.android.com/reference/com/google/android/material/tabs/TabLayout#setupWithViewPager\(ViewPager\))
to link a `TabLayout` with a ViewPager. The individual tabs in the `TabLayout`
will be automatically populated with the page titles from the PagerAdapter.

```java
ViewPager pager;
TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

tabs.setupWithViewPager(pager);
```

Alternatively, you can add a `TabLayout` to a ViewPager in XML:

```xml
<androidx.viewpager.widget.ViewPager
    android:layout_width="match_parent"
    android:layout_height="match_parent">

  <com.google.android.material.tabs.TabLayout
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:layout_gravity="top" />

</androidx.viewpager.widget.ViewPager>
```

### Badges

![TabLayout with badges](assets/tablayout-badges.png)

`TabLayout` supports displaying icon and number badges.

```java
// Creates and initializes an instance of BadgeDrawable associated with a tab.
// Subsequent calls to this method will reuse the existing BadgeDrawable.
// This method does not guarantee that the badge is visible.
BadgeDrawable badge = tablayout.getTab(0).getOrCreateBadge();
badge.setVisible(true);
// Optionally show a number.
badge.setNumber(99);
```

NOTE: Don't forget to remove any BadgeDrawables that are no longer needed.

```java
tablayout.getTab(0).removeBadge();
```

Best Practice: If you only need to temporarily hide the badge(e.g. until the
next notification is received), the recommended/lightweight alternative is to
change the visibility of the BadgeDrawable instead.

Please see [`BadgeDrawable`](BadgeDrawable.md) for details on how to update the
badge content being displayed.

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
based on `?attr/colorPrimary`.

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
---------- | -------------------
Size       | `app:tabMinWidth`<br/>`app:tabMaxWidth`
Scroll     | `app:tabMode`
Centered   | `app:tabGravity`
Background | `app:tabBackground`
Icon       | `app:tabIconTint`<br/>`app:tabIconTintMode`
Label      | `app:tabInlineLabel`<br/>`app:tabTextAppearance`<br/>`app:tabTextColor`<br/>`app:tabSelectedTextColor`
Indicator  | `app:tabIndicatorColor`<br/>`app:tabIndicatorHeight`<br/>`app:tabIndicator`<br/>`app:tabIndicatorGravity`<br/>`app:tabIndicatorFullWidth`
Position   | `app:tabContentStart`<br/>`app:tabPaddingStart`<br/>`app:tabPaddingTop`<br/>`app:tabPaddingEnd`<br/>`app:tabPaddingBottom`<br/>`app:tabPadding`
Ripple     | `app:tabRippleColor`

### Theme Attribute Mapping

#### Updated Material Style (Default)

```
style="@style/Widget.MaterialComponents.TabLayout"
```

Component Attribute       | Default Theme Attribute Value
------------------------- | -------------------------------
`tabTextAppearance`       | `textAppearanceButton`
`android:background`      | `colorSurface`
`tabTextColor` (selected) | `colorPrimary`
`tabTextColor`            | `colorOnSurface` at 60% opacity
`tabIconTint` (selected)  | `colorPrimary`
`tabIconTint`             | `colorOnSurface` at 60% opacity
`tabRippleColor`          | `colorPrimary`
`tabIndicatorColor`       | `colorPrimary`

#### Colored Material Style

```
style="@style/Widget.MaterialComponents.TabLayout.Colored"
```

Component Attribute       | Default Theme Attribute Value
------------------------- | -------------------------------
`tabTextAppearance`       | `textAppearanceButton`
`android:background`      | `colorPrimary`
`tabTextColor` (selected) | `colorOnPrimary`
`tabTextColor`            | `colorOnPrimary` at 60% opacity
`tabIconTint` (selected)  | `colorOnPrimary`
`tabIconTint`             | `colorOnPrimary` at 60% opacity
`tabRippleColor`          | `colorOnPrimary`
`tabIndicatorColor`       | `colorOnPrimary`

#### Legacy Material Style

```
style="@style/Widget.Design.TabLayout"
```

The legacy Material style of `TabLayout` does not make use of our new theming
attributes.
