<!--docs:
title: "Pickers"
layout: detail
section: components
excerpt: "Pickers are modals that request a user choose a date or time."
iconId: picker
path: /catalog/picker/
-->

# Date Pickers

Date Pickers allow users to select a single date or date range.

**Design**

[Material Design: Pickers](https://material.io/design/components/pickers.html)

## Customization

The picker can be customized via the
[MaterialDatePicker.Builder](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/datepicker/MaterialDatePicker.java)
and the
[CalendarConstraints.Builder](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/datepicker/CalendarConstraints.java).
These classes allow you to

-   Select the mode: single date or range of dates.
-   Select the theme: dialog, fullscreen, or default (dialog for single date,
    fullscreen for range).
-   Select the bounds: bounds can be restricted to any contiguous set of months.
    Defaults Janaury 1900 to December 2100.
-   Select valid days: valid days can restrict selections to weekdays only.
    Defaults to all days as valid.
-   Set a title.
-   Set the month to which the picker opens (defaults to the current month if
    within the bounds otherwise the earliest month within the bounds).
-   Set a default selection (defaults to no selection).

Examples of these customizations can be seen in the
[Material Picker Demo](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/datepicker/DatePickerMainDemoFragment.java).

## Timezones

*The picker interprets all long values as milliseconds from the UTC Epoch.* If
you have access to Java 8 libraries, it is strongly recommended you use
`LocalDateTime` and `ZonedDateTime`; otherwise, you will need to use `Calendar`.
Please see
[CalendarConstraints.Builder](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/datepicker/CalendarConstraints.java)
for more details, and use the below snippets as a guide.

Java 8:

```java
LocalDateTime local = LocalDateTime.of(year, month, day, 0, 0);
local.atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli();
```

Java 7:

```java
Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
c.set(year, month, day);
c.getTimeInMillis();
```

## Demo

You can see the current version of the
[Material Picker Demo](https://github.com/material-components/material-components-android/tree/master/catalog/java/io/material/catalog/datepicker/DatePickerMainDemoFragment.java)
in the Material Android Catalog. The demo allows you to select several
configuration parameters for your date picker then launch the dialog.

## Code

The
[Picker Package](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/datepicker)
contains the code for this component, with the main entry point being
[MaterialDatePicker](https://github.com/material-components/material-components-android/tree/master/lib/java/com/google/android/material/datepicker/MaterialDatePicker.java).
