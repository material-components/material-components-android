<!--docs:
title: "Predictive Back"
layout: detail
section: foundations
excerpt: "Predictive Back"
iconId: predictive_back
path: /foundations/predictive_back/
-->

# Predictive Back

Predictive Back is a navigation pattern tied to gesture navigation which shows
the user a glimpse of where swiping back will bring them. Before completing a
swipe, the user can decide to continue to the previous view or stay in the
current view.

In Android T (13), the OS introduced support for predictive back-to-home, which
shows the user a preview of the home screen when swiping back to exit an app.

With Android U (14), the OS adds support for in-app predictive back, which apps
can take advantage of to show the user previous destinations when swiping back
within the app itself.

## Design & API documentation

-   Material Design guidelines: Predictive Back (coming soon)
-   [Framework & AndroidX Predictive Back developer guide](https://developer.android.com/guide/navigation/predictive-back-gesture)

## Usage

To opt in to predictive back, apps must:

1. Migrate from the legacy back handling APIs (`Activity#onBackPressed`,
`KeyEvent.KEYCODE_BACK`, etc.) to the more recently introduced "back callback"
APIs (`OnBackAnimationCallback`, `OnBackPressedCallback`, etc.). This involves
flipping the `android:enableOnBackInvokedCallback` manifest flag to `true`, and
registering callbacks to handle back pressed on Android T and above. More
details on this general back migration can be found at the
[Framework & AndroidX Predictive Back developer guide](https://developer.android.com/guide/navigation/predictive-back-gesture).

2. Upgrade to MDC-Android library version **1.10.0-alpha01 (coming soon)** or
above.

Once completing these steps, you will get most of the predictive back animations
within Material Components for free on Android U devices. See the section below
to understand which components support predictive back and to learn about
special considerations for each component.

### Predictive Back Material Components

The following Material Components support predictive back behavior and
animations:

- Search bar
- Bottom sheet
- Side sheet (support coming soon)
- Navigation drawer
- Navigation bar / Bottom navigation view (support coming soon)
- Navigation rail (support coming soon)
