<!--docs:
title: "Material Card"
layout: detail
section: components
excerpt: "Cards display content and actions on a single subject."
iconId: card
path: /catalog/material-card-view/
-->

# Material Card

A card is a sheet of material that may contain a photo, text, and a link about a
single subject. They may display content containing elements of varying size,
such as photos with captions of variable length.

`MaterialCardView` is a customizable component based on
[`CardView`](https://developer.android.com/reference/android/support/v7/widget/CardView.html)
from the Android Support Library. `MaterialCardView` provides all of the
features of `CardView`, but adds attributes for customizing the stroke and uses
an updated Material style by default.

## Design & API Documentation

-   [Material Design guidelines:
    Cards](https://material.io/guidelines/components/cards.html)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/android/support/design/card/MaterialCardView.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Superclass
    overview](https://developer.android.com/reference/android/support/v7/widget/CardView.html)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `MaterialCardView` component provides an _in-progress_ implementation of
Material Design's card component. It will continue to receive new features and
updates.

The majority of these updates will be style-based with additional
options for the layout of content inside of a card (for example: dividers,
images, actions, and text treatments). Updates will also include functionality
for card behavior in groups of cards.

Example code of how to include the component in your layout is listed here
for reference. Note that the margins around the card (its "gutters") need to
be listed in the layout of the card and cannot be included in a `style` tag.

```xml
<android.support.design.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="@dimen/mtrl_card_spacing"
    android:layout_marginTop="@dimen/mtrl_card_spacing"
    android:layout_marginRight="@dimen/mtrl_card_spacing"
    android:minHeight="200dp">
  <TextView
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:text="@string/demo_card_text"/>
</android.support.design.card.MaterialCardView>
```

See the summary for [`CardView`](https://developer.android.com/reference/android/support/v7/widget/CardView.html)
for standard attributes that can be changed for a `CardView`.

The following additional attributes can be changed for a Material card:

-   `strokeColor`: Color of the stroke path for a card. A stroke will not be
    drawn unless a `strokeColor` is provided, regardless of the `strokeWidth`
    attribute value.
-   `strokeWidth`: Size of the stroke path for a card. The default is 0.
