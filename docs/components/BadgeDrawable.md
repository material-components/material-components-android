<!--docs:
title: "Badge"
layout: detail
section: components
excerpt: "Badges can contain dynamic information, such as a number of pending requests."
iconId: badge
path: /catalog/badging/
-->

# `BadgeDrawable`

## Using badges

Badge                                   | Badge with number                              | Badge with a maximum character count
--------------------------------------- | ---------------------------------------------- | ------------------------------------
![badge_icon](assets/IconOnlyBadge.png) | ![badge_with_number_8](assets/BadgeNumber.png) | ![badge_with_999+](assets/BadgeNumberLongerThanMaxCharCount.png)

NOTE: This component is still under development and may not support the full
range of customization Material Android components generally support (e.g.
themed attributes).

A `BadgeDrawable` represents dynamic information such as a number of pending
requests in a [`BottomNavigationView`](BottomNavigation.md) or
[`TabLayout`](Tabs.md).

## Design & API Documentation

-   [Material Design guidelines: Chips](https://material.io/design/components/bottom-navigation.html#behavior)
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/badge/BadgeDrawable.java)
-   [Class overview](https://developer.android.com/reference/com/google/android/material/badge/BadgeDrawable)

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
    anchor view using `#updateBadgeCoordinates(View)`.

Both of the above steps have been encapsulated in a util method:

```java
BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor);
```

In Pre API-18

1.  Set `BadgeDrawable` as the foreground of the anchor view's `FrameLayout`
    ancestor.
1.  Update the `BadgeDrawable`'s coordinates (center and bounds) based on its
    anchor view (relative to its `FrameLayout` ancestor's coordinate space),

Option 1: `BadgeDrawable` will dynamically create and wrap the anchor view in a
`FrameLayout`, then insert the `FrameLayout` into the anchor view original
position in the view hierarchy. Same syntax as API 18+

```java
BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor);
```

Option 2: If you do not want `BadgeDrawable` to modify your view hierarchy, you
can specify a `FrameLayout` to display the badge instead.

```java
* BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, anchorFrameLayoutParent);
```

### `BadgeDrawable` Gravity Modes

`BadgeDrawable` provides 4 pre-packaged gravity modes that control how the badge
aligns with respect to its anchor view. By default (`TOP_END`), badge aligns to
the top and end edges of the anchor (with some offsets). The other options are
`TOP_START`, `BOTTOM_START` and `BOTTOM_END`.

### `BadgeDrawable` center offsets

By default, `BadgeDrawable` is aligned to the top and end edges of its anchor
view (with some offsets). Call `setBadgeGravity(int)` to change it to one of the
other supported modes. To adjust the badge's offsets w.r.t. the anchor's center,
use `setHoriziontalOffset(int)` or `setVerticalOffset(int)`

### `BadgeDrawable` Attributes

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
