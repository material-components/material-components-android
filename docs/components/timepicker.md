<!--docs:
title: "Time Picker"
layout: detail
section: components
excerpt: "Time Pickers are modals that allow the user to choose a time."
iconId: picker
path: /catalog/picker/
-->

## Time Pickers

Time Pickers allow users to select a single time.

## Design

[Material Design: Time Picker](https://material.io/components/time-pickers)

## Customization

A time picker can be instantiated by
[MaterialTimePicker.Builder](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/timepicker/MaterialTimePicker.java).

The builder allows you to do the following:

-   Set the Time Format, 24 Hour clock or 12 Hour clock. Defaults to 12 Hour.
-   Set the starting time. Defaults to 12:00 am
-   Set the input mode to Keyboard or Clock. Defaults to Clock.

Examples of these customizations can be seen in the
[Time Picker Demo](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/timepicker/TimePickerMainDemoFragment.java).

## Demo

You can see the current version of the
[Time Picker Demo](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/timepicker/TimePickerMainDemoFragment.java)
in the Material Android Catalog. The demo lets you choose between the
different formats.

## Code

The
[TimePicker Package](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/timepicker)
contains the code for this component, with the main entry point being
[MaterialTimePicker](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/timepicker/MaterialTimePicker.java).
