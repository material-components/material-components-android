<!--docs:
title: "Modal Bottom Sheets"
layout: detail
section: components
excerpt: "Modal bottom sheets act like a dialog at the bottom of the screen."
iconId: bottom_sheet
path: /catalog/bottom-sheet-dialog-fragment/
-->

# Modal Bottom Sheets

## Using modal bottom sheets

`BottomSheetDialogFragment` is a thin layer on top of the regular support
library Fragment that renders your fragment as a **modal bottom sheet**,
fundamentally acting as a dialog.

Modal bottom sheets render a shadow on the content below them to indicate
that they are modal, essentially a dialog. If the content outside of the dialog
is tapped then the bottom sheet is dismissed. Modal bottom sheets can be dragged
vertically and dismissed by completely sliding them down.

Note: To implement non-modal **Persistent bottom sheets** use
[BottomSheetBehavior](BottomSheetBehavior.md) in conjunction with a
[CoordinatorLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout).

## Design & API Documentation

-   [Material Design guidelines: Modal Bottom Sheets](https://material.io/go/design-sheets-bottom#bottom-sheets-modal-bottom-sheets)
-   [Class definition](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/bottomsheet/BottomSheetDialogFragment.java)
-   [Class overview](https://developer.android.com/reference/com/google/android/material/bottomsheet/BottomSheetDialogFragment)

## Usage

1.  Subclass `BottomSheetDialogFragment`
2.  Override
    [`onCreateView`](https://developer.android.com/reference/android/app/Fragment.html#onCreateView(android.view.LayoutInflater,%20android.view.ViewGroup,%20android.os.Bundle)).
3.  Use one of the two versions of
    [`show`](https://developer.android.com/reference/android/support/v4/app/DialogFragment.html#show(androidx.fragment.app.FragmentManager,%20java.lang.String))
    to display the dialog. *Notice `BottomSheetDialogFragment `is a subclass of
    AppCompatFragment, which means you need to use
    `Activity.getSupportFragmentManager()`.*

Note: Don't call `setOnCancelListener` or `setOnDismissListener` on a
`BottomSheetDialogFragment`, instead you can override `onCancel(DialogInterface)`
or `onDismiss(DialogInterface)` if necessary.

## Theming

There are two options for theming the `BottomSheetDialog` that is displayed by
this fragment. Either using the `Theme.MaterialComponents.BottomSheetDialog`,
`Theme.MaterialComponents.Light.BottomSheetDialog`, and
`Theme.MaterialComponents.DayNight.BottomSheetDialog` variants of the themes, or
by using `ThemeOverlay.MaterialComponents.BottomSheetDialog`. The benefit of
using the `ThemeOverlay` version is that any changes to your main theme, such as
updated colors will be reflected in the BottomSheet. If you use the `Theme`
versions you have more control over exactly what attributes are included in
each, but it also means you'll have to duplicate any changes that you've made in
your main theme into these as well.

## Fullscreen mode

On API 21 and above the BottomSheet will be rendered fullscreen (edge to edge)
if the navigationBar is transparent, and `enableEdgeToEdge` is true. It can
automatically add insets if any of `paddingBottomSystemWindowInsets`,
`paddingLeftSystemWindowInsets`, or `paddingRightSystemWindowInsets` are set to
true in the style, either by updating the style passed to the constructor, or by
updating the default style specified by the `bottomSheetDialogTheme` attribute
in your theme.

`BottomSheetDialog` will also add padding to the top when the BottomSheet slides
under the status bar to prevent content from being drawn underneath it.

## Related Concepts

`BottomSheetDialogFragment`s are a more modern version of
[Dialogs](https://developer.android.com/guide/topics/ui/dialogs.html). They have
a nicer-looking entrance animation and since they are pinned to the bottom they
may feel easier to use on larger devices.

`BottomSheetDialogFragment`s look very similar to the effects of
[BottomSheetBehavior](BottomSheetBehavior.md) but the latter is not modal and
requires a
[CoordinatorLayout](https://developer.android.com/reference/androidx/coordinatorlayout/widget/CoordinatorLayout).
