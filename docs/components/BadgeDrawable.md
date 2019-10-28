<!--docs:
title: "Badge"
layout: detail
section: components
excerpt: "Badges can contain dynamic information, such as a number of pending requests."
iconId: badge
path: /catalog/badging/
-->

# BadgeDrawable



Badge                                   | Badge with number                              | Badge with a maximum character count
--------------------------------------- | ---------------------------------------------- | ------------------------------------
![badge_icon](assets/IconOnlyBadge.png) | ![badge_with_number_8](assets/BadgeNumber.png) | ![badge_with_999+](assets/BadgeNumberLongerThanMaxCharCount.png)

NOTE: This component is still under development and may not support the full
range of customization Material Android components generally support (e.g.
themed attributes).

A `BadgeDrawable` represents dynamic information such as a number of pending
requests in a [`BottomNavigationView`](BottomNavigationView.md) or
[`TabLayout`](TabLayout.md).

## Design & API Documentation

-   [Material Design guidelines: Chips](https://material.io/design/components/bottom-navigation.html#behavior)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/badge/BadgeDrawable.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/com/google/android/material/badge/BadgeDrawable)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

Create an instance of `BadgeDrawable` by calling `create(Context)` or
`createFromAttributes(Context, AttributeSet, int, int)}`.

How to add and display a `BadgeDrawable` on top of its anchor view depends on
the API level:

In API 18+ (APIs supported by
[ViewOverlay](https://developer.android.com/reference/android/view/ViewOverlay))

1.  Add `BadgeDrawable` as a
    [ViewOverlay](https://developer.android.com/reference/android/view/ViewOverlay)
    to the desired anchor view.
1.  Update the `BadgeDrawable`'s coordinates (center and bounds) based on its
    anchor view.

Both of the above steps have been encapsulated in a util method:

```java
  BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, null);
```

In Pre API-18

1.  Set `BadgeDrawable` as the foreground of the anchor view's FrameLayout
    ancestor.
1.  Update the `BadgeDrawable`'s coordinates (center and bounds) based on its
    anchor view (relative to its FrameLayout ancestor's coordinate space),

Both of the above steps have been encapsulated in a util method:

```java
  BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, anchorFrameLayoutParent);
```

### `BadgeDrawable` Gravity Modes

BadgeDrawable provides 4 pre-packaged gravity modes that control how the badge
aligns with respect to its anchor view. By default (`TOP_END`), badge aligns to
the top and end edges of the anchor (with some offsets). The other options are
`TOP_START`, `BOTTOM_START` and `BOTTOM_END`.

### BadgeDrawable Attributes

Feature       | Relevant attributes
------------- | -----------------------------------------------
Color         | `app:backgroundColor` <br> `app:badgeTextColor`
Label         | `app:number`
Label Length  | `app:maxCharacterCount`
Badge Gravity | `app:badgeGravity`

### Talkback Support

`BadgeDrawable` provides a getter for its content description, which is based on
the number (if any) being displayed. Users should specify content description:
`setContentDescriptionNumberless(CharSequence)`
`setContentDescriptionQuantityStringsResource(@StringRes)`
