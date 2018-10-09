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

Note: `MaterialCardView` is an _in-progress_ implementation, and will continue
to receive new features and updates. The majority of these updates will be
style-based with additional options for the layout of content inside of a card
(for example: dividers, images, actions, and text treatments). Updates will also
include functionality for card behavior in groups of cards.

## Design & API Documentation

-   [Material Design guidelines:
    Cards](https://material.io/go/design-cards)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/card/MaterialCardView.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/card/MaterialCardView)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

Example code of how to include the component in your layout is listed here
for reference. Note that the margins around the card (its "gutters") need to
be listed in the layout of the card and cannot be included in a `style` tag.

```xml
<com.google.android.material.card.MaterialCardView
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
</com.google.android.material.card.MaterialCardView>
```

### Material Styles

Using `MaterialCardView` with an updated Material theme
(`Theme.MaterialComponents`) will provide the correct updated Material styles to
your cards by default. If you need to use an updated Material card and your
application theme does not inherit from an updated Material theme, you can apply
the `Widget.MaterialComponents.CardView` style directly to your widget in XML.

#### Updated Material Style

The updated `MaterialCardView` style consists of updated elevation and
background color.

```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.MaterialComponents.CardView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="@dimen/mtrl_card_spacing"
    android:layout_marginTop="@dimen/mtrl_card_spacing"
    android:layout_marginRight="@dimen/mtrl_card_spacing"
    android:minHeight="200dp">

  <!-- Card contents. -->

</com.google.android.material.card.MaterialCardView>
```

#### Legacy Material Style

```xml
<android.support.v7.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginLeft="@dimen/mtrl_card_spacing"
    android:layout_marginTop="@dimen/mtrl_card_spacing"
    android:layout_marginRight="@dimen/mtrl_card_spacing"
    android:minHeight="200dp">

  <!-- Card contents. -->

</android.support.v7.widget.CardView>
```

### Attributes

`MaterialCardView` supports all of the standard attributes that can be changed
for a
[`CardView`](https://developer.android.com/reference/android/support/v7/widget/CardView.html).
The following additional attributes can be changed for a `MaterialCardView`:

Feature | Relevant attributes
:------ | :------------------
Border  | `app:strokeColor`
        | `app:strokeWidth`

### Theme Attribute Mapping

#### Updated Material Style

```
style="@style/Widget.MaterialComponents.CardView"
```

Component Attribute   | Default Theme Attribute Value
--------------------- | -----------------------------
`cardBackgroundColor` | `colorSurface`
`strokeColor`         | None

#### Legacy Material Style

```
style="@style/CardView"
```

The legacy Material style of `MaterialCardView` does not make use of our new
color theming attributes.
