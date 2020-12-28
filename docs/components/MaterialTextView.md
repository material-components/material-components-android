<!--docs:
title: "Material Text View"
layout: detail
section: components
excerpt: "MaterialTextView displays text to the user."
iconId: text_view
path: /catalog/material-text-view/
-->

# Material Text View

## Using text views

A MaterialTextView is a derivative of AppCompatTextView that displays text to
the user. To provide user-editable text, see
[EditText](https://developer.android.com/reference/android/widget/EditText).

## Design & API Documentation

-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/textview/MaterialTextView.java)
-   [Class overview](https://developer.android.com/reference/com/google/android/material/textview/MaterialTextView)

## Usage

Example code of how to include the component in your layout is listed here for
reference.

```xml
<LinearLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:layout_width="match_parent"
  android:layout_height="match_parent">
    <TextView
      android:id="@+id/text_view_id"
      android:layout_height="wrap_content"
      android:layout_width="wrap_content"
      android:text="@string/hello" />
</LinearLayout>
```

Note: If you use our full themes (which we recommend), `TextView` will
auto-inflate to `MaterialTextView`, otherwise, you will need to specify
`<com.google.android.material.textview` in your xml.

### Attributes

`MaterialTextView` supports all of the standard attributes that can be changed
for a
[`AppCompatTextView`](https://developer.android.com/reference/android/support/v7/widget/AppCompatTextView).
Unlike the `AppCompatTextView` which supports specifying the line height only in
a view layout XML, `MaterialTextView` supports the ability to read the line
height from a `TextAppearance` style, which can be applied to the
`MaterialTextView` either using the `style` attribute or using the
`android:textAppearance` attribute.

The following additional attributes can be changed in `TextAppearance` and
applied to a `MaterialTextView`:

Feature     | Relevant attributes
:---------- | :-------------------
Line Height | `android:lineHeight`
