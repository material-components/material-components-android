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

The following will provide the Material style through the `actionOverflowMenuStyle`
theme attribute.

## Overflow Menus

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
