<!--docs:
title: "Badge"
layout: detail
section: components
excerpt: "Badges can contain dynamic information, such as a number of pending requests."
iconId: badge
path: /catalog/badging/
-->

# `BadgeDrawable`

## Design & API Documentation

*   [Google Material3 Spec](https://material.io/components/badges/overview)
*   [API reference](https://developer.android.com/reference/com/google/android/material/badge/package-summary)

## Using badges

Badge                                         | Badge with number                                    | Badge with a maximum character count
--------------------------------------------- | ---------------------------------------------------- | ------------------------------------
![badge_icon](assets/badge/IconOnlyBadge.png) | ![badge_with_number_99](assets/badge/BadgeNumber.png) | ![badge_with_999+](assets/badge/BadgeNumberLongerThanMaxCharCount.png)

**Note:** This component is still under development and may not support the full
range of customization Material Android components generally support, for
instance, themed attributes.

A `BadgeDrawable` represents dynamic information such as a number of pending
requests in a [`BottomNavigationView`](BottomNavigation.md) or
[`TabLayout`](Tabs.md).

## Usage

API and source code:

*   `BadgeDrawable`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/badge/BadgeDrawable)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/badge/BadgeDrawable.java)
*   `BadgeUtils`
    *   [Class definition](https://developer.android.com/reference/com/google/android/material/badge/BadgeUtils)
    *   [Class source](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/badge/BadgeUtils.java)

Create an instance of `BadgeDrawable` by calling `create(Context)` or
`createFromAttributes(Context, AttributeSet, int, int)}`.

The approach used to add and display a `BadgeDrawable` on top of its anchor view
depends on the API level:

In API 18+ (APIs supported by
[ViewOverlay](https://developer.android.com/reference/android/view/ViewOverlay))

1.  Add `BadgeDrawable` as a
    [ViewOverlay](https://developer.android.com/reference/android/view/ViewOverlay)
    to the desired anchor view.
2.  Update the `BadgeDrawable`'s coordinates (center and bounds) based on its
    anchor view using `#updateBadgeCoordinates(View)`.

Both steps have been encapsulated in a util method:

```java
BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor);
```

In Pre API-18

1.  Set `BadgeDrawable` as the foreground of the anchor view's `FrameLayout`
    ancestor.
2.  Update the `BadgeDrawable`'s coordinates (center and bounds) based on its
    anchor view, relative to its `FrameLayout` ancestor's coordinate space.

Option 1: `BadgeDrawable` will dynamically create and wrap the anchor view in a
`FrameLayout`, then insert the `FrameLayout` into the original anchor view
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

`BadgeDrawable` offers two gravity modes to control how the badge aligns with
its anchor view. By default, (`TOP_END`) badge aligns with the top and end edges
of the anchor (with some offsets). Alternatively, you can use `TOP_START` to
align the badge with the top and start edges of the anchor. Note that
`BOTTOM_START` and `BOTTOM_END` are deprecated and not recommended for use.

### `BadgeDrawable` center offsets

By default, `BadgeDrawable` is aligned with the top and end edges of its anchor
view (with some offsets if `offsetAlignmentMode` is `legacy`). Call `setBadgeGravity(int)` to change it to one of the
other supported modes. To adjust the badge's offsets relative to the anchor's
center, use `setHorizontalOffset(int)` or `setVerticalOffset(int)`

### `BadgeDrawable` Attributes

| Feature                   | Relevant attributes                                                                                                                                      |
|-----------------------    |----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Color                     | `app:backgroundColor` <br> `app:badgeTextColor`                                                                                                          |
| Width                     | `app:badgeWidth` <br> `app:badgeWithTextWidth`                                                                                                           |
| Height                    | `app:badgeHeight` <br> `app:badgeWithTextHeight`                                                                                                         |
| Shape                     | `app:badgeShapeAppearance` <br> `app:badgeShapeAppearanceOverlay` <br> `app:badgeWithTextShapeAppearance` <br> `app:badgeWithTextShapeAppearanceOverlay` |
| Label                     | `app:badgeText` (for text) <br> `app:number` (for numbers)                                                                                               |
| Label Length              | `app:maxCharacterCount` (for all text) <br> `app:maxNumber` (for numbers only)                                                                           |
| Label Text Color          | `app:badgeTextColor`                                                                                                                                     |
| Label Text Appearance     | `app:badgeTextAppearance`                                                                                                                                |
| Badge Gravity             | `app:badgeGravity`                                                                                                                                       |
| Offset Alignment          | `app:offsetAlignmentMode`                                                                                                                                |
| Horizontal Padding        | `app:badgeWidePadding`                                                                                                                                   |
| Vertical Padding          | `app:badgeVerticalPadding`                                                                                                                               |
| Large Font Vertical Offset| `app:largeFontVerticalOffsetAdjustment`                                                                                                                  |
| Auto Adjust               | `app:autoAdjustToWithinGrandparentBounds`                                                                                                                |

**Note:** If both `app:badgeText` and `app:number` are specified, the badge label will be `app:badgeText`.

### Talkback Support

`BadgeDrawable` provides a getter for its content description, which is based on the displayed
number or text (if any). To specify the content description, the developer is provided
with the following methods:
-   `setContentDescriptionForText(CharSequence)`
-   `setContentDescriptionQuantityStringsResource(@PluralsRes int)`
-   `setContentDescriptionExceedsMaxBadgeNumberStringResource(@StringRes int)`
-   `setContentDescriptionNumberless(CharSequence)`
