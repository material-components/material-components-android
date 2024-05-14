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

In Android 13 (T / API level 33), the OS introduced support for predictive
back-to-home, which shows the user a preview of the home screen when swiping
back to exit an app.

With Android 14 (U / API level 34), the OS adds support for in-app predictive
back, which apps can take advantage of to show the user previous destinations
when swiping back within the app itself.

## Design and API Documentation

-   [Material Design guidelines: Predictive Back](https://m3.material.io/foundations/interaction/gestures#22462fb2-fbe8-4e0c-b3e7-9278bd18ea0d)
-   [Android design guidelines](https://developer.android.com/design/ui/mobile/guides/patterns/predictive-back)
-   [Framework & AndroidX Predictive Back developer guide](https://developer.android.com/guide/navigation/predictive-back-gesture)
-   [Android 14 Predictive Back developer guide](https://developer.android.com/about/versions/14/features/predictive-back)

## Usage

To opt in to predictive back, apps must:

1. Migrate from the legacy back handling APIs (`Activity#onBackPressed`,
`KeyEvent.KEYCODE_BACK`, etc.) to the more recently introduced "back callback"
APIs (`OnBackAnimationCallback`, `OnBackPressedCallback`, etc.). This involves
flipping the `android:enableOnBackInvokedCallback` manifest flag to `true`, and
registering callbacks to handle back pressed on Android T and above. More
details on this general back migration can be found at the
[Framework & AndroidX Predictive Back developer guide](https://developer.android.com/guide/navigation/predictive-back-gesture).

2. Upgrade to MDC-Android library version **1.10.0** or above.

Once completing these steps, you will get most of the predictive back animations
within Material Components for free on Android U devices. See the section below
to understand which components support predictive back and to learn about
special considerations for each component.

### Predictive Back Material Components

The following Material Components support predictive back behavior and
animations:

- [Search bar](../components/Search.md#predictive-back) (automatically for `SearchView` set up with `SearchBar`)
- [Bottom sheet](../components/BottomSheet.md#predictive-back) (automatically for modal, standard requires integration)
- [Side sheet](../components/SideSheet.md#predictive-back) (automatically for modal, standard and coplanar require integration)
- [Navigation drawer](../components/NavigationDrawer.md#predictive-back) (automatically for `NavigationView` within `DrawerLayout`)

**Note:** The Material Components above only automatically handle back on API
Level 33+, and when the `android:enableOnBackInvokedCallback` manifest flag to
`true`. This is to be consistent with the behavior of other AndroidX and
Framework views, as well as to avoid taking precedence over any pre-existing
back handling behavior that has already been implemented by apps.

Future predictive back support is planned for the following Material Components:

- Navigation bar / Bottom navigation view
- Navigation rail

## Talks

-   [What's New in Android (Google I/O 2023)](https://youtu.be/qXhjN66O7Bk?t=1193)
-   [What's New in Material Design (Google I/O 2023)](https://youtu.be/vnDhq8W98O4?t=156)
-   [Building for the Future of Android (Google I/O 2023)](https://www.youtube.com/watch?v=WMMPXayjP8g&t=333s)

## Blog Posts

-   [Second Beta of Android 14](https://android-developers.googleblog.com/2023/05/android-14-beta-2.html)
-   [Google I/O 2023: What's new in Jetpack](https://android-developers.googleblog.com/2023/05/whats-new-in-jetpack-io-2023.html)
