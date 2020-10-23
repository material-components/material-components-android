# Progress Indicator

<!-- TODO(b/169262619) Update API and style for sub-type progress indicator classes. -->

Note: Due to an internal change of API, ProgressIndicator will be deprecated in
the near future.

com.google.android.material provides an implementation of linear and circular
progress indicator, compatible back to API 15 (Ice Cream Sandwich MR1). The
easiest way to make use of these indicators is through `ProgressIndicator` and
`CircularProgressIndicator`.

## ProgressIndicator

Note: `ProgressIndicator` currently only supports linear type. Circular type is
implemented in `CircularProgressIndicator`. In the near future,
`ProgressIndicator` will be completely deprecated and removed. Instead, the
linear type will be implemented in `LinearProgressIndicator`.

`ProgressIndicator` is API-compatible with Android's `ProgressBar` class and can
therefore be used as a drop-in replacement. It can be included in XML layouts,
or constructed programmatically:

### XML

```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  android:layout_width="match_parent"
  android:layout_height="match_parent">
  <!-- ... -->

  <com.google.android.material.progressindicator.ProgressIndicator
    style="@style/Widget.MaterialComponents.ProgressIndicator.Linear.Indeterminate"
    android:id="@+id/Progress"/>

  <!-- ... -->
</FrameLayout>
```

### Java

```java
// An example to create linear indeterminate progress indicator.
import static com.google.android.material.progressindicator.R.style.
    Widget_MaterialComponents_ProgressIndicator_Linear_Indeterminate;

import com.google.android.material.progressindicator.ProgressIndicator;

ProgressIndicator progressIndicator =
    new ProgressIndicator(getContext(), null, 0,
        Widget_MaterialComponents_ProgressIndicator_Circular_Indeterminate);
```

### Showing and hiding with animations

The progress indicator can be animated in and out by calling `#show()` or
`#hide()` actively on `ProgressIndicator`:

```java
// ...
ProgressIndicator progress = (ProgressIndicator) findViewById(R.id.Progress);
progress.show();
// ... wait until finished
progress.hide();
```

The component can also be animated in when it's attached to the current window:

```java
// ...
LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
ProgressIndicator progress = new ProgressIndicator(activity, null, 0,
    Widget_MaterialComponents_ProgressIndicator_Linear_Indeterminate);
// ... configures the components.
layout.addView(progress, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
```

By setting `growMode`, the linear progress indicator can have different
animation effects while showing or hiding.

<table>
  <tr><td>GrowMode</td><td>show()</td><td>hide()</td></tr>
  <tr>
    <td>GROW_MODE_INCOMING</td>
    <td>Expending vertically from the top</td>
    <td>Collapsing vertically to the top</td></tr>
  <tr>
    <td>GROW_MODE_OUTCOMING</td>
    <td>Expending vertically from the bottom</td>
    <td>Collapsing vertically to the bottom</td></tr>
  <tr>
    <td>GROW_MODE_BIDIRECTIONAL</td>
    <td>Expending from the horizontal central line</td>
    <td>Collapsing to the horizontal central line</td></tr>
  <tr>
    <td>GROW_MODE_NONE</td>
    <td>Appear immediately</td>
    <td>Disappear immediately</td></tr>
</table>

### Preset styles

There are 2 preset styles available that match the types of indicators shown in
the spec. To use them, simply set your `style` attribute in XML to the desired
name.

<table>
  <tr><td>Preset style name</td><td>Style</td></tr>
  <tr>
    <td>Widget.MaterialComponents.ProgressIndicator.<br>&emsp;Linear.Determinate</td>
    <td>4 dp high linear determinate with default max (100)</td>
  </tr>
  <tr>
    <td>Widget.MaterialComponents.ProgressIndicator.<br>&emsp;Linear.Indeterminate</td>
    <td>4 dp high linear indeterminate</td>
  </tr>
</table>

### Custom styling

`ProgressIndicator` can be styled further by customizing colors, size, grow
mode, etc. These are done through XML attributes, or their corresponding setter
methods.

#### Colors

For single color mode styles (i.e., all determinate types, and some
indeterminate types), the color of indicator (or stroke) can be set in attribute
`indicatorColor` in XML or `setIndicatorColors(int[])` programmatically; the
color of track (the parts of track other than stroke) can be set in attribute
`trackColor` in XML or `setTrackColor(int)` programmatically.

Note: Multiple colors can only take effect in indeterminate mode. If multiple
indicator colors are set in determinate mode, only the first color will be used.

Note: In XML, only one of the attributes `indicatorColor` and `indicatorColors`
can be set. If both are defined, `IllegalArgumentException` will be thrown while
initialization. If neither is defined, the primary color of the current theme
will be used as the indicator color.

#### Size

Use `indicatorSize` to customize the size of the linear type. It can adjust the
height of the progress bar. To add inset for linear types, please use padding of
the layout.

Note: The indicator and track always have the same width.

The size of circular types can be set with a combination of `indicatorSize`,
`circularRadius`, and `circularInset`. `indicatorSize` defines the width of the
ring (circular spinner). `circularRadius` defines the radius of the central line
of the ring. `circularInset` gives extra space between the outer boundary to the
bounds of the component.

Note: If half of the `indicatorSize` is greater than the `circularRadius`,
`IllegalArgumentException` will be thrown during initialization.

### Caveats

Subclassing `ProgressIndicator` is not recommended. If additional features are
needed, please file an issue througth Github with `feature request` tag. Pull
requests directly to `ProgressIndicator` are also welcome.

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
import com.google.android.material.progressindicator.ProgressIndicator;

CircularProgressIndicator indicator = new CircularProgressIndicator(getContext());
```

### Default style

The default style `Widget.MaterialComponents.CircularProgressIndicator` will
configure the indicator to draw a circular (or partial) ring with the track
width of 4dp. The radius is 18dp measured from the axial line (or the circular
between the inner and outer edges). There is also 4dp inset added on all sides.
The indicator will spin or grow clockwise regardless the layout direction. No
animation will be used while showing or hiding.

### Custom styling

`CircularProgressIndicator` can be configured with custom values to have
different appearance.

#### Colors

Similar as `ProgressIndicator`, the indicator color and track color can be
configured by setting `indicatorColor` and `trackColor`. The difference is that,
for multiple indicator colors, one should set `indicatorColor` with a color
array instead of using `indicatorColors`. To set it programmatically, one can
use `setIndicatorColor(int[])`.

Note: `getIndicatorColor()` will always return in the form of an int array
regardless the number of indicator colors used.

#### Size and layout

`CircularProgressIndicator` can configure its size by adjusting `indicatorSize`,
`indicatorRadius`, and `indicatorInset`. `indicatorSize` sets the width of the
track/indicator. `indicatorRadius` sets the size of the ring by the radius
measured from the axial line. `indicatorInset` gives an extra space between the
outer edge to the drawable's bounds.

Note: If half of the `indicatorSize` is greater than the `indicatorRadius`,
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

By setting the `showBehaviorCircular` and `hideBehaviorCircular`, the indicator
can have different animation effects as follows:

<table>
  <tr><td colspan=2>showBehaviorCircular<td></tr>
  <tr><td>none</td><td>Appear immediately</td></tr>
  <tr><td>inward</td><td>Expanding from the outer edge</td></tr>
  <tr><td>outward</td><td>Expanding from the inner edge</td></tr>
  <tr><td colspan=2>hideBehaviorCircular<td></tr>
  <tr><td>none</td><td>Disappear immediately</td></tr>
  <tr><td>inward</td><td>Collapsing to the inner edge</td></tr>
  <tr><td>outward</td><td>Collapsing to the outer edge</td></tr>
</table>

### Caveats

`CircularProgressIndicator` is final. For more customized appearances, please
consider to implement custom `DrawingDelegate` and
`IndetermianteAnimatorDelegate` and inherit from the `BaseProgressIndicator`.

## LinearProgressIndicator

<!--TODO(b/169262416) Finishes after implementation is done. -->
