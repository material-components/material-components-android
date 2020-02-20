<!--docs:
title: "Menus"
layout: detail
section: components
excerpt: "Menus display a list of choices on temporary surfaces."
iconId: menu
path: /catalog/menu/
-->

# Menu

## Design & API Documentation

*   [Material Design guidelines: Menus](https://material.io/design/components/menus.html)
    <!--{: .icon-list-item.icon-list-item--spec }-->

## Material Styles

The MaterialComponents theme provides styling for overflow, contextual and popup
menus. Just use any `Theme.MaterialComponents.*` theme or add the widget styles
to your theme:

```xml
<item name="popupMenuStyle">@style/Widget.MaterialComponents.PopupMenu</item>
<item name="android:contextPopupMenuStyle">@style/Widget.MaterialComponents.PopupMenu.ContextMenu</item>
<item name="actionOverflowMenuStyle">@style/Widget.MaterialComponents.PopupMenu.Overflow</item>
```

For information on the Exposed Dropdown Menu, see its
[specific section below](#exposed-dropdown-menus).

## Overflow Menus

The following will provide the Material style through the
`actionOverflowMenuStyle` theme attribute.

**res/menu/custom_menu.xml**

```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/search"
          android:icon="@drawable/ic_search"
          android:title="@string/search_label"
          android:showAsAction="ifRoom"/>
    <item android:id="@+id/help"
          android:icon="@drawable/ic_help"
          android:title="@string/help_label" />
</menu>
```

**MainActivity.java**

```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.custom_menu, menu);
    return true;
}
```

## Popup Menus

Inflating menus via `PopupMenu` will also have have the right styling through
the `popupMenuStyle` theme attribute.

```java
public boolean showMenu(View anchor) {
    PopupMenu popup = new PopupMenu(this, anchor);
    popup.getMenuInflater().inflate(R.menu.custom_menu, popup.getMenu());
    popup.show();
}
```

## Exposed Dropdown Menus

The Exposed Dropdown Menu is implemented via the use of the `TextInputLayout`.
For detailed information on how
[Material text fields](https://material.io/design/components/text-fields.html)
work, see the [TextInputLayout documentation](TextField.md).

### Usage

In order to create an Exposed Dropdown Menu, you will need to use a
[TextInputLayout](https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout)
with a `Widget.MaterialComponents.TextInputLayout.*.ExposedDropdownMenu` style. Additionally, the `TextInputLayout` should have an [AutoCompleteTextView](https://developer.android.com/reference/android/widget/AutoCompleteTextView)
as its direct child.


**res/layout/dropdown_menu.xml**

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox.ExposedDropdownMenu"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/hint_text">

  <AutoCompleteTextView
      android:id="@+id/filled_exposed_dropdown"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>

</com.google.android.material.textfield.TextInputLayout>
```

You will also need an item layout resource to populate the dropdown popup. The example
below provides a layout that follows the Material Design guidelines.

**res/layout/dropdown_menu_popup_item.xml**

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    android:ellipsize="end"
    android:maxLines="1"
    android:textAppearance="?attr/textAppearanceSubtitle1"/>
```

Finally, you will need to set the `AutoCompleteTextView`'s adapter.

**MainActivity.java**

```java
String[] COUNTRIES = new String[] {"Item 1", "Item 2", "Item 3", "Item 4"};

ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            getContext(),
            R.layout.dropdown_menu_popup_item,
            COUNTRIES);

AutoCompleteTextView editTextFilledExposedDropdown =
    view.findViewById(R.id.filled_exposed_dropdown);
editTextFilledExposedDropdown.setAdapter(adapter);
```

The example above will provide an editable filled Exposed Dropdown Menu.

Note: In order to have a non editable variation of the menu, you should disable
user input in the `AutoCompleteTextView`. That can be achieved by setting
`android:editable="false"` on the `AutoCompleteTextView`.

### Variations

#### Filled

As seen above, apply this style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox.ExposedDropdownMenu"`
```

#### Outlined

Apply this style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.ExposedDropdownMenu"`
```

#### Dense Filled

Apply this style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.FilledBox.Dense.ExposedDropdownMenu"`
```

#### Dense Outlined

Apply this style to your `TextInputLayout`:

```xml
style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox.Dense.ExposedDropdownMenu"`
```
