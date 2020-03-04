<!--docs:
title: "Motion"
layout: detail
section: theming
excerpt: "Transition between UI elements to help users understand and navigate an app."
latest_update: "February 11, 2020"
path: /theming/motion/
-->

# Motion

Material motion is a set of transition patterns that help users understand and navigate
an app.

Before you can use the motion library, you need to add a dependency on the
Material Components for Android library. For more information, go to the
[Getting started](https://github.com/material-components/material-components-android/blob/master/docs/getting-started.md)
page.

_**Note:** Motion is only available on API level 21 (Android 5.0 Lollipop) and
up. Additionally, the transitions included in the motion library are built using the
Android Framework Transition library (`android.transition`), **not** the
AndroidX Transition library (`androidx.transition`). Make sure any `Transition`
being used in conjunction with the transitions provided extends
`android.transition.*` and not `androidx.transition.*`._

Material Components for Android provides support for all four motion patterns
defined in the Material spec.

1.  [Container transform](#container-transform)
2.  [Shared axis](#shared-axis)
3.  [Fade through](#fade-through)
4.  [Fade](#fade)

<br><br>

## Container transform

The **container transform** pattern is designed for transitions between UI elements that include a container. This pattern creates a visible connection between two UI elements.

`MaterialContainerTransform` is a
[shared element transition](https://developer.android.com/training/transitions/start-activity#start-with-element).
Unlike traditional Android shared elements, it is not designed around a singular
piece of shared content, such as an image, to be moved between two scenes.
Instead, the shared element here refers to the bounding container of a start
`View` or `ViewGroup` (e.g. the entire row layout of an item in a list)
transforming its size and shape into that of an end `View` or `ViewGroup` (e.g.
the root `ViewGroup` of a full screen Fragment). These start and end container
Views are the “shared element” of a container transform. While these containers
are being transformed, their contents are swapped to create the transition.

!["Container transform gallery - normal speed and slow motion"](assets/motion/container_transform_lineup.gif)
_Examples of the container transform:_

1.  _A card into a details page_
2.  _A list item into a details page_
3.  _A FAB into a details page_
4.  _A search bar into expanded search_

### Using the container transform pattern

A container transform can be configured to transition between a number of
Android structures including Fragments, Activities and Views.

### Container transform examples

#### Transition between Fragments

In Fragment A and Fragment B's layouts, identify the start and end Views (as described in the [container transform overview](#material-container-transform)) which will be shared. Add a matching `transitionName` to each of these Views.

```xml
<!--fragment_a.xml-->
<View
  android:id=”@+id/start_view”
  android:transitionName=”shared_element_container”  />
```
```xml
<!--fragment_b.xml-->
<View
  android:id=”@+id/end_view”
  android:transitionName=”shared_element_container” />
```

_**Note:** There cannot be more than a 1:1 mapping of `transitionNames` between
the start and end layouts. If you have multiple Views in your start layout that
could be mapped to an end View in your end layout (e.g. each `RecyclerView` item
to a details screen), read about shared element mapping at
[Continuous Shared Element Transitions: RecyclerView to ViewPager](https://android-developers.googleblog.com/2018/02/continuous-shared-element-transitions.html)._

Set Fragment B's `sharedElementEnterTransition` to a new `MaterialContainerTransform`. This can be done either before adding/replacing Fragment B into your Fragment container or in Fragment B's `onCreate` method.

```kt
// FragmentA.kt
val fragmentB =  FragmentB()
fragmentB.sharedElementEnterTransition = MaterialContainerTransform(requireContext())


/*** OR ***/


// FragmentB.kt
override fun onCreate(savedInstanceState:  Bundle?)  {
  super.onCreate(savedInstanceState)
  sharedElementEnterTransition = MaterialContainerTransform(requireContext())
}
```

Add or replace Fragment B, adding the shared element from your start scene to your Fragment transaction.

```kt
childFragmentManager
  .beginTransaction()
  // Map the start View in FragmentA and the transitionName of the end View in FragmentB
  .addSharedElement(view,  “shared_element_container”)
  .replace(R.id.fragment_container, fragmentB, FragmentB.TAG)
  .addToBackStack(FragmentB.TAG)
  .commit()
```

If using the Navigation Architecture Component, use the following.

```kt
// Map the start View in FragmentA and the transitionName of the end View in FragmentB
val extras =  FragmentNavigatorExtras(view to “shared_element_container”)
findNavController().navigate(R.id.action_fragmentA_to_fragmentB, null, null, extras)
```

Completing these steps should give you a working enter and return container transform when navigating from Fragment A to Fragment B and popping from Fragment B to Fragment A.

_**Note:** Fragments are able to define enter and return shared element transitions. When only an enter shared element transition is set, it will be re-used when the Fragment is popped (returns). `MaterialContainerTransform` internally configures the transition’s properties based on whether or not it’s entering or returning. If you need to customize either the enter or return style of the transition, see [Customizing the container transform](#customization)._

When running this new transition, you might notice that Fragment A (everything besides the shared element) disappears as soon as the container transform starts. This is because FragmentA has been removed from its container. To “hold” FragmentA in place as the container transform plays, set the provided `Hold` transition as FragmentA’s exit transition.

```kt
// FragmentA.kt
fun onCreate(savedInstanceState:  Bundle?) {
  super.onCreate(savedInstanceState)

  // Fragment A’s exitTransition can be set any time before Fragment A is
  // replaced withFragment B. Ensure Hold's duration is set to the same
  // duration as your MaterialContainerTransform.
  exitTransition =  Hold()
}
```

#### Transition between Activities

In Activity A’s layout, identify the start View to be used as the “shared element” as described in the [container transform overview](#material-container-transform). Give the start view a `transitionName`.

```xml
<!--activity_a.xml-->
<View
  android:id=”@+id/start_view”
  android:transitionName=”shared_element_container” />
```

Configure Activity A for an exit shared element transition as follows:

```kt
override  fun onCreate(bundle: Bundle) {

  // Enable Activity Transitions. Optionally enable Activity transitions in your
  // theme with <item name=”android:windowActivityTransitions”>true</item>.
  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

  // Attach a callback used to capture the shared elements from this Activity to be used
  // by the container transform transition
  setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

  // Keep system bars (status bar, navigation bar) persistent throughout the transition.
  window.sharedElementsUseOverlay = false
  super.onCreate(bundle);
  setContentView(R.layout.activity_a)
  ...
}
```

In Activity B, configure the Activity for transitions in a similar fashion.

```kt
override  fun onCreate(bundle: Bundle) {

  // Enable Activity Transitions. Optionally enable Activity transitions in your
  // theme with <item name=”android:windowActivityTransitions”>true</item>.
  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

  // Set the transition name, which matches Activity A’s start view transition name, on
  // the root view.
  findViewById(android.R.id.content).transitionName = "shared_element_container"

  // Attach a callback used to receive the shared elements from Activity A to be
  // used by the container transform transition.
  setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

  // Set this Activity’s enter and return transition to a MaterialContainerTransform
  window.sharedElementEnterTransition = MaterialContainerTransform(this).apply {
    addTarget(android.R.id.content)
    duration = 300L
  }
  window.sharedElementReturnTransition = MaterialContainerTransform(this).apply {
    addTarget(android.R.id.content)
    duration = 300L
  }

  super.onCreate(bundle);
  setContentView(R.layout.activity_b);
  ...
}
```

_**Note:** We are using `android.R.id.content` (the window’s root) as the shared element “container” in Activity B. This will cause the start view being transformed from Activity A to transition into the full screen of Activity B. If you have views in Activity A and Activity B that you do not want included as part of the transform, you can alternatively set the transition name on a `View`/`ViewGroup` in Activity B’s layout or include/exclude `View`s with helper methods on the Transition class (`Transition#addTarget`, `Transition#excludeChildren`, etc)._

From Activity A, start the container transform by constructing an Intent with the following options.

```kt
val intent = Intent(this,  ActivityB::class.java);

val options =  ActivityOptions.makeSceneTransitionAnimation(
  this,
  startView,
  "shared_element_container"  // The transition name to be matched in Activity B.
);
startActivity(intent, options.toBundle());
```

#### Transition between Views

In the Activity or Fragment where you are transitioning between two views, trigger a `MaterialContainerTransform` by manually setting the transition’s start and end `View`s.

```kt
val transform =  MaterialContainerTransform(this).apply {
  // Manually tell the container transform which Views to transform between.
  startView = fab
  endView = bottomToolbar

  // Optionally add a curved path to the transform
  pathMotion =  MaterialArcMotion()

  // Since View to View transforms often are not transforming into full screens,
  // remove the transition's scrim.
  scrimColor =  Color.TRANSPARENT
}

// Begin the transition by changing properties on the start and end views or
// removing/adding them from the hierarchy.
TransitionManager.beginDelayedTransition(container, transform)
fab.visibility =  View.GONE
bottomToolbar.visibility =  View.VISIBLE
```

This will perform a container transform from the start view transitioning to the end view. To return, set up the same transform, switching the start and end Views and undoing any property changes (setting the FAB back to `View.VISIBLE` and the `bottomToolbar` back to `View.GONE`) done by the first transform.

### Customization

While the out-of-the-box container transform should work in most cases, you can manually set the following properties on `MaterialContainerTransform` to customize the look and feel of the animation:

#### Container transform attributes<

<!--  Todo: Update this table with links to source where listing defaults is too lengthy (thresholds) -->

&nbsp;         | Attribute                | Related method(s)                 | Default value
-------------- | ------------------------ | --------------------------------- | -------------
**Shape** | `transitionShapeAppearance`           | `getStartShapeAppearanceModel`<br/>`setStartShapeAppearanceModel`<br/>`getEndShapeAppearanceModel`<br/>`setEndShapeAppearanceModel`          | `null`

#### Container transform properties

&nbsp;         | Related method(s)                 | Default value
-------------- |  --------------------------------- | -------------
**Duration** | `getDuration`<br/>`setDuration`           | `300`
**Interpolation** | `getInterpolation`<br/>`setInterpolation`           | `R.interpolator.fast_out_slow_in`
**Path Motion** | `getPathMotion`<br/>`setPathMotion`           | `null` (Linear)
**Z Order** | `getDrawInViewId`<br/>`setDrawInViewId`           | `android.R.id.content`
**Container Background Color** | `getContainerColor`<br/>`setContainerColor`           | `Color.TRANSPARENT`
**Scrim Color** | `getScrimColor`<br/>`setScrimColor`           | `R.attr.scrimBackground`
**Direction** | `getTransitionDirection`<br/>`setTransitionDirection`           | `TransitionDirection.TRANSITION_DIRECTION_AUTO`
**Fade Mode** | `getFadeMode`<br/>`setFadeMode`           | `FadeMode.FADE_MODE_IN`
**Fit Mode** | `getFitMode`<br/>`setFitMode`           | `FitMode.FIT_MODE_AUTO`
**Fade Thresholds** | `getFadeProgressThresholds`<br/>`setFadeProgressThresholds`           |
**Scale Thresholds** | `getScaleProgressThresholds`<br/>`setScaleProgressThresholds`           |
**Scale Mask Thresholds** | `getScaleMaskProgressThresholds`<br/>`setScaleMaskProgressThresholds`           |
**Shape Mask Thresholds** | `getShapeMaskProgressThresholds`<br/>`setShapeMaskProgressThresholds`           |
**Debug Drawing** | `isDrawDebugEnabled()`<br/>`setDrawDebugEnabled()`           | `false`

_**Note:** All of these properties have defaults. In most cases, each property
has a different default value depending on whether or not the transition is
entering or returning._

_When you manually set any of the above properties, the value set will be used when the transition is both entering and returning (including when an enter transition is being re-used due to no return being set). If you need to manually set properties which differ depending on whether or not the transition is entering or returning, create two `MaterialContainerTransforms` and set both the `sharedElementEnterTransition` and `sharedElementReturnTransition`._

<br><br>

## Shared axis

The **shared axis** pattern is used for transitions between UI elements that
have a spatial or navigational relationship. This pattern uses a shared
transformation on the x, y, or z axis to reinforce the relationship between
elements.

!["Shared axis gallery - normal speed and slow motion"](assets/motion/shared_axis_lineup.gif)
_Examples of the shared axis pattern:_

1.  _An onboarding flow transitions along the x-axis_
2.  _A stepper transitions along the y-axis_
3.  _A parent-child navigation transitions along the z-axis_

### Using the shared axis pattern


`MaterialSharedAxis` is a `TransitionSet` composed of smaller, “atomic”,
transitions. By default, these atomic transitions extend `Visibility`, a
`Transition` which triggers when the target View's visibility is changed or when
the View is added or removed. This means `MaterialSharedAxis` requires a View to
be changing in visibility or to be added or removed to trigger its animation.

`MaterialSharedAxis` has the concept of moving in the forward or backward
direction. Below are the directions in which a `MaterialSharedAxis` will move
for both the forward and backward directions along each axis.

#### Shared axis direction

Axis  | Forward           | Backward
----- | ----------------- | ------------------
**X** | Left on x-axis    | Right on x-axis
**Y** | Up on y-axis      | Down on y-axis
**Z** | Forward on z-axis | Backward on z-axis

A shared axis transition can be configured to transition between a number of
Android structures including Fragments, Activities and Views.

### Shared axis examples

#### Transition between Fragments

In the following example, we’re creating a shared axis Z transition between
FragmentA and FragmentB. Moving from FragmentA to FragmentB should be a
“forward” movement and returning from FragmentB to FragmentA should be a
“backward” movement.

In Fragment A, configure an enter and exit transition.

```kt
// FragmentA.kt

override fun onCreate(savedInstanceState:  Bundle?) {
  super.onCreate(savedInstanceState)

  val backward =  MaterialSharedAxis.create(requireContext(),  MaterialSharedAxis.Z,  forward = false)
  enterTransition = backward

  val forward =  MaterialSharedAxis.create(requireContext(),  MaterialSharedAxis.Z,  forward = true)
  exitTransition = forward
}
```

In Fragment B, again configure an enter and exit transition.

```kt
// FragmentB.kt

override fun onCreate(savedInstanceState:  Bundle?) {
  super.onCreate(savedInstanceState)

  val forward =  MaterialSharedAxis.create(requireContext(),  MaterialSharedAxis.Z,  forward = true)
  enterTransition = forward

  val backward =  MaterialSharedAxis.create(requireContext(),  MaterialSharedAxis.Z,  forward = false)
  exitTransition = backward
}
```

It’s important to note here how these two fragments move together. When Fragment
A is exiting, Fragment B will be entering. This is why, in Fragment A, the exit
transition is `forward` and in Fragment B the enter transition is also
`forward`. This will ensure that both Fragments are moving in the same direction
when their transitions are playing. The opposite is true in the backwards
direction. When Fragment B is exiting, Fragment A will be entering. For this
reason, Fragment B is configured to exit in the backward direction and Fragment
A is configured to enter in the backward direction.

When you're ready to move from Fragment A to B, replace Fragment A with Fragment
B.

```kt
supportFragmentManager
  .beginTransaction()
  .replace(R.id.fragment_container, FragmentB())
  .commit();
```

The above should give you a working shared axis transition between Fragment A
and Fragment B. Changing the axis to `MaterialSharedAxis.X` or
`MaterialSharedAxis.Y` will create the same, coordinated interaction in their
respective axis. Alternatively, try replacing `MaterialSharedAxis` with a
`MaterialFadeThrough` for a transition between destinations or layouts that are
_not_ spatially related.

#### Transition between Activities

Enable Activity transitions by either setting
`android:windowActivityTransitions` to true in your theme or enabling them on an
Activity by Activity basis by setting the `Window.FEATURE_ACTIVITY_TRANSITIONS`
flag.

```xml
<style  name="MyTheme" parent="Theme.MaterialComponents.DayNight">
  ...
  <item  name="android:windowActivityTransitions">true</item>
</style>
```

Or in your Activities:

```kt
override fun onCreate(savedInstanceState:  Bundle?) {
  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
  ...
}
```

To get started, configure a new shared axis transition in Activity A and set it
as the Activity's `exitTransition`.

```kt
// ActivityA.kt

override fun onCreate(savedInstanceState:  Bundle?) {

  val exit  =  MaterialSharedAxis.create(this, MaterialSharedAxis.X, forward = true).apply {

    // Only run the transition on the contents of this activity, excluding
    // system bars or app bars if provided by the app’s theme.
    addTarget(R.id.a_container)
  }
  window.exitTransition =  exit

  // TODO: Add a reenter transition in the backwards direction to animate
  // ActivityB out and ActivityA back in in the opposite direction.

  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_a)
}
```

You can optionally add or exclude targets to have the transition affect or
ignore Views. Use the combination you need to have the transition applied where
desired. For example:

```kt
val exit  =  MaterialSharedAxis.create(this, MaterialSharedAxis.X, forward = true).apply {

  // Only run the transition on the root ViewGroup of this activity. This will exclude
  // other views except what is specified by this method.
  addTarget(R.id.a_container)

  // OR

  // Run the transition on everything except the system status and navigation bars. All
  // other Views, besides those explicitly excluded, will be affected by the transition.
  excludeTarget(android.R.id.statusBarBackground, true)
  excludeTarget(android.R.id.navigationBarBackground, true)
}
```

Next, configure a new `MaterialSharedAxis` enter transition in Activity B.

```kt
// ActivityB.kt

override fun onCreate(savedInstanceState:  Bundle?) {

  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

  val enter =  MaterialSharedAxis.create(this, MaterialSharedAxis.X, forward = true).apply {
    addTarget(R.id.b_container)
  }
  window.enterTransition = enter
  // TODO: Configure a return transition in the backwards direction.

  // Allow Activity A’s exit transition to play at the same time as this Activity’s
  // enter transition instead of playing them sequentially.
  window.allowEnterTransitionOverlap =  true

  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_b)
  ...
}
```

When you're ready to navigate from Activity A to Activity B, start Activity B
like your normally would.

```kt
startActivity(Intent(this,  ActivityB::class.java));
```

#### Transition between Views

In your Activity or Fragment’s layout, identify the two views which will be
“swapped”. The outgoing View should be added to the layout and visible. The
incoming View's visibility should either be set to `View.GONE` or the View
should not yet be added to the layout. When you’re ready to replace the outgoing
view with the incoming View, do so with a shared axis transition as follows.

```kt
// Set up a new MaterialSharedAxis in the specified ais and direction.
val sharedAxis =  MaterialSharedAxis.create(requireContext(),  MaterialSharedAxis.Y,  forward = true)

// Begin watching for changes in the View hierarchy.
TransitionManager.beginDelayedTransition(container, sharedAxis)

// Make any changes to the hierarchy to be animated by the shared axis transition.
outgoingView.visibility =  View.GONE
incomingView.visibility =  View.VISIBLE
```

This will transition between your outgoing and incoming Views with a shared axis
transition. To reverse the animation, set up a new shared axis in the opposite
direction and set your outgoing View back to `View.VISIBLE` and your incoming
View back to `View.GONE`.

### Customization

`MaterialSharedAxis` is an extension of `MaterialTransitionSet`. A
`MaterialTransitionSet` is composed of a primary and secondary Transition. For
any `MaterialTransitionSet`, the secondary transition can either be modified or
replaced using `MaterialTransitionSet.getSecondaryTransition` and
`MaterialTransitionSet.setSecondaryTransition`.

#### Shared axis composition

| &nbsp;                 | Primary transition         | Secondary transition |
| ---------------------- | -------------------------- | -------------------- |
| **MaterialSharedAxis** |  **X** -`SlideDistance`<br> **Y** -`SlideDistance`<br> **Z** -`Scale` | `FadeThrough`        |



This allows the tweaking of shared axis to create “variants” as mentioned in the
Material Motion spec. <!-- Todo: Add Link to spec article -->

#### Shared axis fade variant

The following is a `MaterialSharedAxis` Z transition between Activities which fades
Activity B in and over Activity A while leaving Activity A’s alpha unchanged can
be accomplished by removing the secondary `FadeThrough` transition from Activity
A's exit transition.

```kt
// ActivityA.kt

override fun onCreate(savedInstanceState:  Bundle?) {
  val exit  =  MaterialSharedAxis.create(this,  MaterialSharedAxis.Z,  forward = true).apply {
    // Remove the exit transitions secondary transition completely so this Activity
    // only scales instead of scaling and fading out. Alternatively, this could be
    // set to a modified FadeThrough transition or any other custom transition.
    secondaryTransition =  null

    addTarget(R.id.main_container)
  }

  window.exitTransition =  exit

  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_main)
  ...
}
```


<br><br>

## Fade Through

The **fade through** pattern is used for transitions between UI elements that do
not have a strong relationship to each other.

!["Fade through gallery - normal speed and slow motion"](assets/motion/fade_through_lineup.gif)
_Examples of the fade through pattern:_

1.  _Tapping destinations in a bottom navigation bar_
2.  _Tapping a refresh icon_
3.  _Tapping an account switcher_

### Using the fade through pattern

`MaterialFadeThrough` is a `TransitionSet` composed of smaller, “atomic”,
transitions. By default, these atomic transitions extend `Visibility`, a
`Transition` which triggers when the target View's visibility is changed or when
the View is added or removed. This means `MaterialFadeThrough` requires a View
to be changing in visibility or to be added or removed to trigger its animation.

A fade through can be configured to transition between a number of Android
structures including Fragments, Activities and Views.

### Fade through examples

#### Transition between Fragments

In Fragment A, configure an exit `MaterialFadeThrough` transition and in
Fragment B configure an enter `MaterialFadeThrough` transition. Both of these
will be used (and reused) when navigating from Fragment A to Fragment B and from
Fragment B to Fragment A.

```kt
// FragmentA.kt

override fun onCreate(savedInstanceState:  Bundle?) {
  super.onCreate(savedInstanceState)

  exitTransition =  MaterialFadeThrough.create(requireContext())
}
```

```kt
// FragmentB.kt

override fun onCreate(savedInstanceState:  Bundle?) {
  super.onCreate(savedInstanceState)

  enterTransition =  MaterialFadeThrough.create(requireContext())
}
```

_**Note:** Since `MaterialFadeThrough`'s primary and secondary transitions both
extend `Visibility`, `MaterialFadeThrough` appropriately animates its targets
depending on whether they are appearing or disappearing._

When you're ready to navigate between Fragment A and Fragment B, use a standard
Fragment transaction or use the
[Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started).

```kt
supportFragmentManager
  .beginTransaction()
  .replace(R.id.fragment_container, FragmentB())
  .commit();
```

#### Transition between Activities

Enable Activity transitions by either setting
`android:windowActivityTransitions` to true in your theme or enabling them on an
Activity-by-Activity basis by setting the `Window.FEATURE_ACTIVITY_TRANSITIONS`
flag.

```xml
<style  name="MyTheme" parent="Theme.MaterialComponents.DayNight">
  ...
  <item  name="android:windowActivityTransitions">true</item>
</style>
```

Or in your Activities:

```kt
override fun onCreate(savedInstanceState:  Bundle?) {
  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
  ...
}
```

To get started, configure a new `MaterialFadeThrough` in Activity A and set it
as the Activity's exitTransition.

```kt
// ActivityA.kt

override fun onCreate(savedInstanceState:  Bundle?) {

  val exit  =  MaterialFadeThrough.create(this).apply {

    // Only run the transition on the contents of this activity, excluding
    // system bars or app bars if provided by the app’s theme.
    addTarget(R.id.a_container)
  }
  window.exitTransition =  exit

  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_a)
}
```

You can optionally add or exclude targets to have the transition affect or
ignore Views. Use the combination you need to have the transition applied where
you’d like. For example:

```kt
val exit  =  MaterialFadeThrough.create(this).apply {

  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

  // Only run the transition on the root ViewGroup of this activity. This will exclude
  // other Views except what is specified by this method.
  addTarget(R.id.a_container)

  // OR

  // Run the transition on everything except the system status and navigation bars. All
  // other Views, besides those explicitly excluded, will be affected by the transition.
  excludeTarget(android.R.id.statusBarBackground, true)
  excludeTarget(android.R.id.navigationBarBackground, true)
}
```

Next, configure a new `MaterialFadeThrough` enter transition in Activity B.

```kt
// ActivityB.kt

override fun onCreate(savedInstanceState:  Bundle?) {

  window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

  val enter =  MaterialFadeThrough.create(this).apply {
    addTarget(R.id.b_container)
  }
  window.enterTransition = enter

  // Allow Activity A’s exit transition to play at the same time as this Activity’s
  // enter transition instead of playing them sequentially.
  window.allowEnterTransitionOverlap =  true

  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_b)
  ...
}
```

When you're ready to navigate from Activity A to Activity B, start Activity B as
your normally would.

```kt
startActivity(Intent(this,  ActivityB::class.java));
```

#### Transition between Views

In your Activity or Fragment’s layout, identify the two Views which will be
“swapped”. The outgoing View should be added to the layout and visible. The
incoming View should either be set to `View.GONE` or not yet added to the
layout. When you’re ready to replace the outgoing View with the incoming View,
do so with a fade through transition as follows.

```kt
val fadeThrough =  MaterialFadeThrough.create(requireContext())

// Begin watching for changes in the View hierarchy.
TransitionManager.beginDelayedTransition(container, fadeThrough)

// Make any changes to the hierarchy to be animated by the fade through transition.
outgoingView.visibility =  View.GONE
incomingView.visibility =  View.VISIBLE
```

This will transition between your outgoing and incoming Views with a fade
through transition. To reverse the animation, follow the same steps, setting
your outgoing View back to `View.VISIBLE` and your incoming View back to
`View.GONE`.

### Customization

`MaterialFadeThrough` is an extension of `MaterialTransitionSet`. A
`MaterialTransitionSet` is composed of a primary and secondary `Transition`. For
any `MaterialTransitionSet`, the secondary transition can either be modified or
replaced using `MaterialTransitionSet.getSecondaryTransition` and
`MaterialTransitionSet.setSecondaryTransition`.

#### Fade through composition

&nbsp;                  | Primary transition | Secondary transition
----------------------- | ------------------ | --------------------
**MaterialFadeThrough** | `FadeThrough`      | `Scale`


This allows the tweaking of fade through to create “variants” as mentioned in
the Material Motion design spec. <!-- Todo: Add Link to spec article -->

#### Fade through slide variant

The below will create a fade through between Fragments which fades
Fragment A out (without a scale) and fades Fragment B in with a _slide_ instead
of a scale.

```kt
// FragmentA.kt

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  exitTransition = MaterialFadeThrough.create(requireContext()).apply {
    // Remove the exit fade through's secondary scale so this Fragment simply fades out.
    secondaryTransition = null
  }
}
```

```kt
// FragmentA.kt

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)

  enterTransition = MaterialFadeThrough.create(requireContext()).apply {
    // Replace the enter fade through's secondary transition to use a SlideDistance transition.
    secondaryTransition = SlideDistance(requireContext(), Gravity.BOTTOM)
  }
}
```

<br><br>

## Fade

The **fade** pattern is used for UI elements that enter or exit within the
bounds of the screen, such as a dialog that fades in the center of the screen.

!["Fade gallery - normal speed and slow motion"](assets/motion/fade_lineup.gif)
_Examples of the fade pattern:_

1.  _A dialog_
2.  _A menu_
3.  _A snackbar_
4.  _A FAB_

### Using the fade pattern

`MaterialFade` is a `TransitionSet` composed of smaller, “atomic”, transitions.
By default, these atomic transitions extend `Visibility`, a `Transition` which
triggers when the target View's visibility is changed or when the View is added
or removed. This means `MaterialFade` requires a View to be changing in
visibility or to be added or removed to trigger its animation.

### Fade examples

#### Transition a View

In your Activity or Fragment, toggle the visibility of your target View, in this
case a Floating Action Button, using a `MaterialFade` to animate the change.

```kt
// FragmentA.kt

showButton.setOnClickListener {
   val materialFade =  MaterialFade.create(this)
   TransitionManager.beginDelayedTransition(container, materialFade)
   fab.visibility = View.VISIBLE
}
```

_**Note:** `MaterialFade` optionally takes an `entering` parameter in its
`create` constructor. This controls the duration used by `MaterialFade`'s
primary `Fade` transition - using a longer duration when true and a shorter
duration when false. By default, `MaterialFade` is configured to "enter". If
your target View is disappearing, construct a `MaterialFade` and set the
entering parameter to `false`._

```kt
// FragmentA.kt

hideButton.setOnClickListener {
   val materialFade =  MaterialFade.create(this, false)
   TransitionManager.beginDelayedTransition(container, materialFade)
   fab.visibility = View.GONE
}
```


### Customization

`MaterialFade` is an extension of `MaterialTransitionSet`. A
`MaterialTransitionSet` is composed of a primary and secondary `Transition`. For
any `MaterialTransitionSet`, the secondary transition can either be modified or
replaced using `MaterialTransitionSet.getSecondaryTransition` and
`MaterialTransitionSet.setSecondaryTransition`.

#### Fade composition

&nbsp;           | Primary transition | Secondary transition
---------------- | ------------------ | --------------------
**MaterialFade** | `Fade`             | `Scale`


This allows the tweaking of fade to create “variants” as mentioned in the
Material Motion design spec. <!-- Todo: Add Link to spec article -->

<!-- Todo: Add snippet of variant -->
