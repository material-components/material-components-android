# Progress Indicator

Note: Due to an internal change of API, `ProgressIndicator` has been deprecated.
It will be removed in the near future after the internal migration process.

com.google.android.material provides an implementation of linear and circular
progress indicator, compatible back to API 15 (Ice Cream Sandwich MR1). The
easiest way to make use of these indicators is through `LinearProgressIndicator`
and `CircularProgressIndicator`.

## LinearProgressIndicator

`LinearProgressIndicator` is a separate implementation of linear type. It can be
added to the layout via xml or programmatically.

### XML

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent">
  <!-- ... -->

  <com.google.android.material.progressindicator.LinearProgressIndicator
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:id="@+id/Progress"/>

  <!-- ... -->
</FrameLayout>
```

The default material style `Widget.MaterialComponents.LinearProgressIndicator`
will be used as a determinate progress indicator. For the indeterminate mode,
please add `android:indeterminate="true"` as the attribute.

### Java

```java
// An example to create a linear indeterminate progress indicator.
import com.google.android.material.progressindicator.LinearProgressIndicator;

LinearProgressIndicator indicator = new LinearProgressIndicator(getContext());
```

### Default style

The default style `Widget.MaterialComponents.LinearProgressIndicator` will
configure the indicator to draw a linear strip with the track thickness of 4dp.
The indicator will swipe or grow from `start` to `end` regarding the layout
direction. No animation will be used while showing or hiding.

### Custom styling

`LinearProgressIndicator` can be configured with custom values to have different
appearance.

#### Colors

The indicator color and track color can be configured by setting
`indicatorColor` and `trackColor`. For multiple indicator color use, a color
array can be assigned to `indicatorColor`, too. To set it programmatically, one
can use `setIndicatorColor(int[])`.

Note: `getIndicatorColor()` will always return in the form of an int array
regardless the number of indicator colors used.

#### Size and layout

`LinearProgressIndicator` can configure its height by adjusting `trackThickness`.
`trackThickness` sets the width of the track/indicator; in the linear type case,
it's the height of the strip.

Note: The width of strip depends on the view and its layout.

#### Showing and hiding with animations

The progress indicator can be animated in and out by calling `#show()` or
`#hide()` actively on `LinearProgressIndicator`:

```java
// ...
LinearProgressIndicator progress =
    (LinearProgressIndicator) findViewById(R.id.Progress);
progress.show();
// ... wait until finished
progress.hide();
```

The component can also be animated in when it's attached to the current window:

```java
// ...
LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
LinearProgressIndicator progress =
    new LinearProgressIndicator(getContext());
// ... configures the components.
layout.addView(progress, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
```

By setting the `showAnimationBehavior` and `hideAnimationBehavior`, the indicator can
have different animation effects as follows:

<table>
  <tr><td colspan=2>showAnimationBehavior</td></tr>
  <tr><td>none</td><td>Appear immediately</td></tr>
  <tr><td>upward</td><td>Expanding from the bottom edge</td></tr>
  <tr><td>downward</td><td>Expanding from the top edge</td></tr>
  <tr><td colspan=2>hideAnimationBehavior</td></tr>
  <tr><td>none</td><td>Disappear immediately</td></tr>
  <tr><td>upward</td><td>Collapsing to the top edge</td></tr>
  <tr><td>downward</td><td>Collapsing to the bottom edge</td></tr>
</table>

## CircularProgressIndicator

`CircularProgressIndicator` is a separate implementation of circular type. It
can be added to the layout via xml or programmatically.

### XML

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent">
  <!-- ... -->

  <com.google.android.material.progressindicator.CircularProgressIndicator
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:id="@+id/Progress"/>

  <!-- ... -->
</FrameLayout>
```

The default material style `Widget.MaterialComponents.CircularProgressIndicator`
will be used as a determinate progress indicator. For the indeterminate mode,
please add `android:indeterminate="true"` as the attribute.

### Java

```java
// An example to create a circular indeterminate progress indicator.
import com.google.android.material.progressindicator.CircularProgressIndicator;

CircularProgressIndicator indicator = new CircularProgressIndicator(getContext());
```

### Default style

The default style `Widget.MaterialComponents.CircularProgressIndicator` will
configure the indicator to draw a circular (or partial) ring with the track
thickness of 4dp. The indicator size is 40dp measured as the outer edge. There
is also 4dp inset added on all sides. The indicator will spin or grow clockwise
regardless the layout direction. No animation will be used while showing or
hiding.

### Custom styling

`CircularProgressIndicator` can be configured with custom values to have
different appearance.

#### Colors

Same as `LinearProgressIndicator`.

#### Size and layout

`CircularProgressIndicator` can configure its size by adjusting
`trackThickness`, `indicatorSize`, and `indicatorInset`. `trackThickness` sets
the thickness of the track/indicator. `indicatorSize` sets the size of the ring
by the diameter of the outer edge. `indicatorInset` gives an extra space between
the outer edge to the drawable's bounds.

Note: If the `trackThickness` is greater than the half of the `indicatorSize`,
`IllegalArgumentException` will be thrown during initialization.

#### Showing and hiding with animations

The progress indicator can be animated in and out by calling `#show()` or
`#hide()` actively on `CircularProgressIndicator`:

```java
// ...
CircularProgressIndicator progress =
    (CircularProgressIndicator) findViewById(R.id.Progress);
progress.show();
// ... wait until finished
progress.hide();
```

The component can also be animated in when it's attached to the current window:

```java
// ...
LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
CircularProgressIndicator progress =
    new CircularProgressIndicator(getContext());
// ... configures the components.
layout.addView(progress, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
```

By setting the `showAnimationBehavior` and `hideAnimationBehavior`, the indicator
can have different animation effects as follows:

<table>
  <tr><td colspan=2>showAnimationBehavior</td></tr>
  <tr><td>none</td><td>Appear immediately</td></tr>
  <tr><td>inward</td><td>Expanding from the outer edge</td></tr>
  <tr><td>outward</td><td>Expanding from the inner edge</td></tr>
  <tr><td colspan=2>hideAnimationBehavior</td></tr>
  <tr><td>none</td><td>Disappear immediately</td></tr>
  <tr><td>inward</td><td>Collapsing to the inner edge</td></tr>
  <tr><td>outward</td><td>Collapsing to the outer edge</td></tr>
</table>

## Caveats

`CircularProgressIndicator` and `LinearProgressIndicator` are final. For more
customized appearances, please consider to implement custom `DrawingDelegate`
and `IndetermianteAnimatorDelegate` and inherit from the `BaseProgressIndicator`.
