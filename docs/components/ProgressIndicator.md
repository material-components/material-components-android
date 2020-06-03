# Progress Indicator

com.google.android.material provides an implementation of linear and circular
progress indicator, compatible back to API 15 (Ice Cream Sandwich MR1). The
easiest way to make use of these indicators is through `ProgressIndicator`.

## ProgressIndicator

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
    style="@style/Widget.MaterialComponents.ProgressIndicator.Circular.Indeterminate"
    android:id="@+id/Progress"/>

  <!-- ... -->
</FrameLayout>
```

### Java

```java
// An example to create circular indeterminate progress indicator.
import static com.google.android.material.progressindicator.R.style.
    Widget_MaterialComponents_ProgressIndicator_Circular_Indeterminate;

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
    Widget_MaterialComponents_ProgressIndicator_Circular_Indeterminate);
// ... configures the components.
layout.addView(progress, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
```

Same GrowMode will have different visual effects to the linear and circular
progress indicators.

<table>
  <tr><td>GrowMode</td><td>Linear</td><td>Circular</td></tr>
  <tr>
    <td>GROW_MODE_INCOMING</td>
    <td>Indicator expending vertically from top</td>
    <td>Indicator expending inward from outter side</td></tr>
  <tr>
    <td>GROW_MODE_OUTCOMING</td>
    <td>Indicator expending vertically from bottom</td>
    <td>Indicator expending outward from inner side</td></tr>
  <tr>
    <td>GROW_MODE_BIDIRECTIONAL</td>
    <td colspan=2>Indicator expending in both ways from the central line</td></tr>
  <tr>
    <td>GROW_MODE_NONE</td>
    <td colspan=2>Show or hide immediately</td></tr>
</table>

### Preset styles

There are a number of preset styles available that match the types of indicators
shown in the spec. To use them, simply set your `style` attribute in XML to the
desired name.

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
  <tr>
    <td>Widget.MaterialComponents.ProgressIndicator.<br>&emsp;Circular.Determinate</td>
    <td>48x48 dp circular determinate with default max (100)</td>
  </tr>
  <tr>
    <td>Widget.MaterialComponents.ProgressIndicator.<br>&emsp;Circular.Indeterminate</td>
    <td>48x48 dp circular indeterminate</td>
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

Use `indicatorWidth` to customize the size of linear types. It can adjust the
height of the progress bar (as the width of the indicator/track). To add inset
for linear types, please use padding of the layout.

Note: For both linear and circular types, indicator and track always have the
same width.

The size of circular types can be set with a combination of `indicatorWidth`,
`circularRadius`, and `circularInset`. `indicatorWidth` defines the width of the
ring (circular spinner). `circularRadius` defines the radius of the central line
of the ring. `circularInset` gives extra space between the outer boundary to the
bounds of the component.

Note: If half of the `indicatorWidth` is greater than the `circularRadius`,
`IllegalArgumentException` will be thrown during initialization.

### Caveats

Subclassing `ProgressIndicator` is not recommended. If additional features are
needed, please file an issue througth Github with `feature request` tag. Pull
requests directly to `ProgressIndicator` are also welcome.
