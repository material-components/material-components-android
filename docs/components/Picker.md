<!--docs:
title: "Pickers"
layout: detail
section: components
excerpt: "Pickers are modals that request a user choose a date or time."
iconId: picker
path: /catalog/picker
-->

Warning: The Picker API is not stable, yet. Expect significant changes to class names, theme names, style names, attributes, and customization options.

## Date Pickers

Date Pickers allow users to select a single date or date range.

## Design

[Material Design: Pickers](https://material.io/design/components/pickers.html)

## Demo

Date Pickers are under active development. You can see the current version of the [Material Picker Demo](https://github.com/material-components/material-components-android/blob/master/catalog/java/io/material/catalog/picker/PickerMainDemoFragment.java) in the Material Android Catalog. The demo launches and listens to material pickers with customization options for dialog and fullscreen.

## Code

The [Picker Package](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/picker) contains most of the code for this component. There are entry points for [Date Range Pickers](https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/picker/MaterialDateRangePickerDialogFragment.java) and [Date Pickers](https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/picker/MaterialDatePickerDialogFragment.java).

## Time Pickers

Time Pickers are currently not under development.
