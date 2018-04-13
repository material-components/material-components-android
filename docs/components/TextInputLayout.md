<!--docs:
title: "Text Fields"
layout: detail
section: components
excerpt: "A text field with an animated floating label and other Material Design features."
iconId: text_field
path: /catalog/text-input-layout/
-->

# Text Fields

![Text Fields](assets/text-fields.svg)
<!--{: .article__asset.article__asset--screenshot }-->

`TextInputLayout` provides an implementation for [Material text
fields](https://material.io/guidelines/components/text-fields.html). Used in
conjunction with a
[`TextInputEditText`](https://developer.android.com/reference/android/support/design/widget/TextInputEditText.html),
`TextInputLayout` makes it easy to include Material **text fields** in your
layouts.

## Design & API Documentation

-   [Material Design guidelines: Text
    Fields](https://material.io/guidelines/components/text-fields.html)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/android/support/design/widget/TextInputLayout.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
    <!-- Styles for list items requiring icons instead of standard bullets. -->
-   [Class
    overview](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

To create a material text field, add a `TextInputLayout` to your XML layout and
a `TextInputEditText` as a direct child.

```xml
<android.support.design.widget.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

  <android.support.design.widget.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:hint="@string/hint_text"/>

</android.support.design.widget.TextInputLayout>
```

Note: You can also use an `EditText` for your input text component. However,
using `TextInputEditText` allows `TextInputLayout` greater control over the
visual aspects of the input text - it allows `TextInputLayout` to display hint
in the text field when in "extract mode" (such as landscape mode).

### Material Styles

Using `TextInputLayout` with an updated Material theme
(`Theme.MaterialComponents`) will provide the correct updated Material styles to
your text fields by default. If you need to use an updated Material text field
and your application theme does not inherit from an updated Material theme, you
can apply one of the updated Material styles directly to your widget in XML.

#### Filled Box (Default)

Filled text fields have a solid background color and draw more attention,
especially in layouts where the field is surrounded by other elements. To use a
filled text field, apply the following style to your `TextInputLayout`:

```
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox"
```

To change the background color of a filled text field, you can set the
`boxBackgroundColor` attribute on your `TextInputLayout`.

#### Outline Box

Outline text fields have a stroked border and are less emphasized. To use an
outline text field, apply the following style to your `TextInputLayout`:

```
style="@style/Widget.MaterialComponents.TextInputLayout.OutlineBox"
```

To change the stroke color and width for an outline text field, you can set the
`boxStrokeColor` and `boxStrokeWidth` attributes on your `TextInputLayout`,
respectively.

#### Height Variations

`TextInputLayout` provides two height variations for filled and outline text
fields, **standard** and **dense**. Both box styles default to the standard
height.

In order to reduce the height of a text box, you can use a dense style, which
will reduce the vertical padding within the text box. You can achieve this by
applying the appropriate styles to your `TextInputLayout`, depending on whether
you are using a filled or outline text field:

```
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox.Dense"
```

```
style="@style/Widget.MaterialComponents.TextInputLayout.OutlineBox.Dense"
```

Additionally, to change a text field's internal padding as well as other values
like corner radii, you can apply the following attributes to your
`TextInputLayout`:

##### Padding

* `boxPaddingLeft`
* `boxCollapsedPaddingTop`
* `boxExpandedPaddingTop`
* `boxPaddingRight`
* `boxCollapsedPaddingBottom`
* `boxExpandedPaddingBottom`

##### Corner Radius

* `boxCornerRadiusTopLeft`
* `boxCornerRadiusTopRight`
* `boxCornerRadiusBottomLeft`
* `boxCornerRadiusBottomRight`

## Common features

`TextInputLayout` provides functionality for a number of Material [text field
features](https://material.io/guidelines/components/text-fields.html#text-fields-layout).
These are some commonly used properties you can update to control the look of
your text field:

Text field element                     | Relevant attributes/methods
:------------------------------------- | :--------------------------
Label (also called a “Floating Label”) | [`android:hint`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_android_hint)
                                       | [`app:hintEnabled`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_hintEnabled)
Error message                          | [`app:errorEnabled`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_errorEnabled)
                                       | [`#setError(CharSequence)`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#setError\(java.lang.CharSequence\))
Helper text                            | [`app:helperTextEnabled`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_helperTextEnabled)
                                       | [`app:helperText`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_helperText)
Password redaction                     | [`app:passwordToggleEnabled`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_passwordToggleEnabled)
                                       | [`app:passwordToggleDrawable`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_passwordToggleDrawable)
Character counter                      | [`app:counterEnabled`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_counterEnabled)
                                       | [`app:counterMaxLength`](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#attr_TextInputLayout_counterMaxLength)

## Notes about setting the hint

If a hint is specified on the child `EditText` in XML, then the
`TextInputLayout` will use the `EditText`'s hint as its floating label. To
specify or change the hint programmatically, make sure to call the `setHint()`
method on `TextInputLayout`, instead of on the `EditText`.

## Related concepts

*   [TextView](https://developer.android.com/reference/android/widget/TextView.html)
*   [Specifying the Input Type (Android Developers
    Guide)](https://developer.android.com/training/keyboard-input/style.html)
*   [Copy and Paste (Android Developers
    Guide)](https://developer.android.com/guide/topics/text/copy-paste.html)
