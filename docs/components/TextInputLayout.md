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

`TextInputLayout` also provides an implementation for the
[Exposed Dropdown Menu](https://material.io/design/components/menus.html#exposed-dropdown-menu)
when used in conjuction with an
[AutoCompleteTextView](https://developer.android.com/reference/android/widget/AutoCompleteTextView)
and a `Widget.MaterialComponents.TextInputLayout.*.ExposedDropdownMenu` style.
For information on the Exposed Dropdown Menu usage, see the [Menu documentation](Menu.md#exposed-dropdown-menus).

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
    android:layout_height="wrap_content"
    android:hint="@string/hint_text">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>

</com.google.android.material.textfield.TextInputLayout>
```

Note: A `TextInputEditText` should be used instead of an `EditText` as your
input text component. An `EditText` might work, but `TextInputEditText` provides
accessibility support for the text field and allows `TextInputLayout` greater
control over the visual aspects of the input text.

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

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox"
```

To change the background color of a filled text field, you can set the
`boxBackgroundColor` attribute on your `TextInputLayout`.

Note: When using a filled text field with an `EditText` child that is not a
`TextInputEditText`, make sure to set the `EditText`'s `android:background` to
`@null`. This allows `TextInputLayout` to set a filled background on the
`EditText`.

#### Outlined Box

Outlined text fields have a stroked border and are less emphasized. To use an
outlined text field, apply the following style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
```

To change the stroke color and width for an outline text field, you can set the
`boxStrokeColor` and `boxStrokeWidth` attributes on your `TextInputLayout`,
respectively.

Note: When using an outlined text field with an `EditText` child that is not a
`TextInputEditText`, make sure to set the `EditText`'s `android:background` to
`@null`. This allows `TextInputLayout` to set an outline background on the
`EditText`.

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

* `boxCornerRadiusTopStart`
* `boxCornerRadiusTopEnd`
* `boxCornerRadiusBottomStart`
* `boxCornerRadiusBottomEnd`

## Common features

`TextInputLayout` provides functionality for a number of Material [text field
features](https://material.io/go/design-text-fields#text-fields-layout).
These are some commonly used properties you can update to control the look of
your text field:

Text field element                                                | Relevant attributes/methods
:---------------------------------------------------------------- | :--------------------------
Label (also called a “Floating Label”)                            | [`android:hint`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_android_hint)<br/>[`app:hintEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_hintEnabled)
Error message                                                     | [`app:errorEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_errorEnabled)<br/>[`#setError(CharSequence)`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#setError\(java.lang.CharSequence\))
Error icon                                                        | [`app:errorIconDrawable`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_errorIconDrawable)<br/>[`#setErrorIconDrawable(Drawable)`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#setErrorIconDrawable\(android.graphics.drawable.Drawable\))
Helper text                                                       | [`app:helperTextEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_helperTextEnabled)<br/>[`app:helperText`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_helperText)
Placeholder text                                                  | [`app:placeholderText`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_placeholderText)
Character counter                                                 | [`app:counterEnabled`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_counterEnabled)<br/>[`app:counterMaxLength`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_counterMaxLength)
Prefix text                                                       | [`app:prefixText`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_prefixText)
Suffix text                                                       | [`app:suffixText`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_suffixText)
End icon mode (password redaction, text clearing and custom mode) | [`app:endIconMode`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_endIconMode)

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
## End Icon Modes

The `TextInputLayout` provides certain pre-packaged `EndIconMode`s that come
with specific behaviors. However, their appearance and behaviors can be
customized via the end icon API and its attributes. The `TextInputLayout` also
provides support for a custom end icon, with custom appearance and behaviors.

Note: You should opt to use the `EndIconMode` API instead of setting an
end/right compound drawable on the `EditText`.

### Pre-packaged `EndIconMode`s
#### Password redaction
If set, a button is displayed to toggle between the password being displayed as
plain-text or disguised when the `EditText` is set to display a password.

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/hint_text"
    app:endIconMode="password_toggle">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      android:inputType="textPassword"/>

</com.google.android.material.textfield.TextInputLayout>
```

#### Text clearing
If set, a button is displayed when text is present and clicking it clears the
`EditText` field.

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/hint_text"
    app:endIconMode="clear_text">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>

</com.google.android.material.textfield.TextInputLayout>
```

### Custom `EndIconMode`

It is possible to set a custom drawable or button as the `EditText`'s end icon
via `app:endIconMode="custom"`. You should specify a drawable and content
description for the icon, and, optionally, specify custom behaviors.

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/custom_end_icon"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/hint_text"
    app:endIconMode="custom"
    app:endIconDrawable="@drawable/custom_icon"
    app:endIconContentDescription="@string/custom_content_desc">

  <com.google.android.material.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>

</com.google.android.material.textfield.TextInputLayout>
```

Optionally, in code (more info on the section below):

```java
TextInputLayout textInputCustomEndIcon = view.findViewById(R.id.custom_end_icon);

// If the icon should work as button, set an OnClickListener to it
textInputCustomEndIcon
    .setEndIconOnClickListener(/* custom OnClickListener */);

// If any specific changes should be done when the EditText is attached (and
// thus when the end icon is added to it), set an OnEditTextAttachedListener
textInputCustomEndIcon
    .addOnEditTextAttachedListener(/* custom OnEditTextAttachedListener */);

// If any specific changes should be done if/when the endIconMode gets changed,
// set an OnEndIconChangedListener
textInputCustomEndIcon
    .addOnEndIconChangedListener(/* custom OnEndIconChangedListener */);
```

### Customizing the end icon
The following elements can be customized.

End icon element                       | Relevant attributes/methods
:------------------------------------- | :--------------------------
Drawable                               | [`app:endIconDrawable`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_endIconDrawable)
Content description                    | [`app:endIconContentDescription`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_endIconContentDescription)
Tint                                   | [`app:endIconTint`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_endIconTint)
Tint mode                              | [`app:endIconTintMode`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#attr_TextInputLayout_endIconTintMode)
Functionality                          | [`#setEndIconOnClickListener(OnClickListener)`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#setEndIconOnClickListener(View.OnClickListener))
Behavior that depends on the EditText* | [`#addOnEditTextAttachedListener(OnEditTextAttachedListener)`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#addOnEditTextAttachedListener(OnEditTextAttachedListener))
Behavior if EndIconMode changes**      | [`#addOnEndIconChangedListener(OnEndIconChangedListener)`](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout#addOnEndIconChangedListener(OnEndIconChangedListener))

\* Example: The clear text end icon's visibility behavior depends on whether the
`EditText` has input present. Therefore, an `OnEditTextAttachedListener` is set
so things like `editText.getText()` can be called.

** Example: If the password toggle end icon is set and a different `EndIconMode`
gets set, the `TextInputLayout` has to make sure that the `EditText`'s `TransformationMethod`
is still `PasswordTransformationMethod`. Because of that, an `OnEndIconChangedListener`
is used.

## Related concepts

*   [TextView](https://developer.android.com/reference/android/widget/TextView.html)
*   [Specifying the Input Type (Android Developers
    Guide)](https://developer.android.com/training/keyboard-input/style.html)
*   [Copy and Paste (Android Developers
    Guide)](https://developer.android.com/guide/topics/text/copy-paste.html)
