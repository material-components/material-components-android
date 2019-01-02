<!--docs:
title: "Alert Dialogs"
layout: detail
section: components
excerpt: "Alert Dialogs are modal windows that must be interacted."
iconId: dialog
path: /catalog/dialog/
-->

# Alert Dialogs

An `AlertDialog` is a window that interrupts the current user flow. It is used to flag important choices like discarding drafts or changing permission settings.
Material maintains usage of the framework `AlertDialog`, but provides a new builder, `MaterialAlertDialogBuilder`, which configures the instantiated `AlertDialog` with Material specs and theming.
By default, a Material `AlertDialog` darkens the background with a scrim and appears in the center of the viewport. It is a fixed size based upon screen orientation and size.

## Design & API Documentation

-   [Material Design guidelines: Dialogs](https://material.io/design/components/dialogs.html)
    <!--{: .icon-list-item.icon-list-item--spec }-->
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/dialog/MaterialAlertDialogBuilder.java)
    <!--{: .icon-list-item.icon-list-item--link }-->
-   [Class overview](https://developer.android.com/reference/android/app/AlertDialog)
    <!--{: .icon-list-item.icon-list-item--link }--> <!--{: .icon-list }-->

## Usage

The `MaterialAlertDialogBuilder` allows creation of a Material `AlertDialog`. `MaterialAlertDialogBuilder` extends `AlertDialog.Builder` and passes through all builder methods changing the return type to a 'MaterialAlertDialogBuilder'.

```java
        new MaterialAlertDialogBuilder(context)
            .setTitle("Title")
            .setMessage("Message")
            .setPositiveButton("Ok", null)
            .show();
```

### Material Theming

`MaterialAlertDialogBuilder` requires that your application use a Material Components theme (e.g., `Theme.MaterialComponents.Light`). Using a Material Components theme with `MaterialAlertDialogBuilder` will result in an `AlertDialog` that matches your appplication's color, typography, and shape theming.

### Styles

To change the appearance of every `AlertDialog` built with a `MaterialAlertDialogBuilder`, use the `materialAlertDialogTheme` attribute.

```xml
  <item name="materialAlertDialogTheme">@style/ThemeOverlay.MaterialComponents.MaterialAlertDialog</item>
```

To change an individual `AlertDialog`, pass-in a `themeResId` to the constructor of a `MaterialAlertDialogBuilder` instanced.

```java
new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
```

The `materialAlertDialogTheme` attribute supports additional choices:

```xml
<item name="materialAlertDialogTitlePanelStyle">@style/MaterialAlertDialog.MaterialComponents.Title.Panel</item>
<item name="materialAlertDialogTitleIconStyle">@style/MaterialAlertDialog.MaterialComponents.Title.Icon</item>
<item name="materialAlertDialogTitleTextStyle">@style/MaterialAlertDialog.MaterialComponents.Title.Text</item>item>
```

`AlertDialog` objects created by a `MaterialAlertDialogBuilder` will also respond to these additional attributes set in `alertDialogStyle` that help position the window.

```xml
<attr name="backgroundInsetStart" format="dimension"/>
<attr name="backgroundInsetTop" format="dimension"/>
<attr name="backgroundInsetEnd" format="dimension"/>
<attr name="backgroundInsetBottom" format="dimension"/>
```

### Template Styles

For the common case of a centered title, use `ThemeOverlay.MaterialComponents.MaterialAlertDialog.Centered`.

```java
new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Centered)
  .setTitle("Title")
  .setMessage("Message")
  .setPositiveButton("Accept", /* listener = */ null));
```

