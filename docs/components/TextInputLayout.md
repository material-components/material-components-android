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
fields](https://material.io/go/design-text-fields). Used in conjunction with a
[`TextInputEditText`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputEditText),
`TextInputLayout` makes it easy to include Material **text fields** in your
layouts.

## Design & API Documentation

-   [Material Design guidelines: Text
    Fields](https://material.io/go/design-text-fields)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class
    definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/textfield/TextInputLayout.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
    <!-- Styles for list items requiring icons instead of standard bullets. -->
-   [Class
    overview](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

To create a material text field, add a `TextInputLayout` to your XML layout and
a `TextInputEditText` as a direct child.

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:hint="@string/hint_text"/>

</com.google.android.material.textfield.TextInputLayout>
```

Note: A `TextInputEditText` should be used instead of an `EditText` as your
input text component. An `EditText` might work, but `TextInputEditText` provides
accessibility support for the text field and allows `TextInputLayout` greater
control over the visual aspects of the input text.

### Material Styles

To use an updated `TextInputLayout`, apply one of the updated Material styles
directly to your widget in XML.

#### Filled Box

Filled text fields have a solid background color and draw more attention,
especially in layouts where the field is surrounded by other elements. To use a
filled text field, apply the following style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox"
```

To change the background color of a filled text field, you can set the
`boxBackgroundColor` attribute on your `TextInputLayout`.

#### Outline Box

Outline text fields have a stroked border and are less emphasized. To use an
outline text field, apply the following style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
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
applying the appropriate styles to your `TextInputLayout` and
`TextInputEditText`, depending on whether you are using a filled or outline text
field:

##### Dense Filled Box

Apply this style to your `TextInputLayout`: `xml
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox.Dense"`

##### Dense Outline Box

Apply this style to your `TextInputLayout`: `xml
style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.Dense"`

To change a text box's internal padding and overall dimensions, you can adjust
the `android:padding` attributes on the `TextInputEditText`.

##### Corner Radius

* `boxCornerRadiusTopLeft`
* `boxCornerRadiusTopRight`
* `boxCornerRadiusBottomLeft`
* `boxCornerRadiusBottomRight`

## Common features

`TextInputLayout` provides functionality for a number of Material [text field
features](https://material.io/go/design-text-fields#text-fields-layout).
These are some commonly used properties you can update to control the look of
your text field:

Text field element                     | Relevant attributes/methods
:------------------------------------- | :--------------------------
Label (also called a “Floating Label”) | [`android:hint`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_android_hint)<br/>[`app:hintEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_hintEnabled)
Error message                          | [`app:errorEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_errorEnabled)<br/>[`#setError(CharSequence)`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#setError(java.lang.CharSequence))
Helper text                            | [`app:helperTextEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_helperTextEnabled)<br/>[`app:helperText`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_helperText)
Password redaction                     | [`app:passwordToggleEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_passwordToggleEnabled)<br/>[`app:passwordToggleDrawable`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_passwordToggleDrawable)
Character counter                      | [`app:counterEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_counterEnabled)<br/>[`app:counterMaxLength`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_counterMaxLength)

## Notes about setting the hint

The hint should be set on `TextInputLayout`, rather than the `TextInputEditText`
or `EditText`. If a hint is specified on the child `EditText` in XML, the
`TextInputLayout` might still work correctly; `TextInputLayout` will use the
`EditText`'s hint as its floating label. However, future calls to modify the
hint will not update `TextInputLayout`'s hint. To avoid unintended behavior,
call `setHint()` and `getHint()` on `TextInputLayout`, instead of on `EditText`.

## Notes about using `TextInputLayout` programmatically

If you construct the `EditText` child of a `TextInputLayout` programmatically,
you should use `TextInputLayout's` context to create the view. This will allow
`TextInputLayout` to pass along the appropriate styling to the
`TextInputEditText` or `EditText`.

```java
TextInputEditText editText = new TextInputEditText(textInputLayout.getContext());
```

## Related concepts

*   [TextView](https://developer.android.com/reference/android/widget/TextView.html)
*   [Specifying the Input Type (Android Developers
    Guide)](https://developer.android.com/training/keyboard-input/style.html)
*   [Copy and Paste (Android Developers
    Guide)](https://developer.android.com/guide/topics/text/copy-paste.html)
