/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.slider;

import com.google.android.material.R;

import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.RangeInfoCompat.RANGE_TYPE_FLOAT;
import static androidx.core.math.MathUtils.clamp;
import static com.google.android.material.slider.LabelFormatter.LABEL_FLOATING;
import static com.google.android.material.slider.LabelFormatter.LABEL_GONE;
import static com.google.android.material.slider.LabelFormatter.LABEL_WITHIN_BOUNDS;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Float.compare;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.RangeInfoCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.SeekBar;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.customview.widget.ExploreByTouchHelper;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewOverlayImpl;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.tooltip.TooltipDrawable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The slider can function either as a continuous slider, or as a discrete slider. The mode of
 * operation is controlled by the value of the step size. If the step size is set to 0, the slider
 * operates as a continuous slider where the slider's thumb can be moved to any position along the
 * horizontal line. If the step size is set to a number greater than 0, the slider operates as a
 * discrete slider where the slider's thumb will snap to the closest valid value. See {@link
 * #setStepSize(float)}.
 *
 * <p>The {@link LabelFormatter} interface defines a formatter to be used to render text within the
 * value indicator label on interaction.
 *
 * <p>{@link BasicLabelFormatter} is a simple implementation of the {@link LabelFormatter} that
 * displays the selected value using letters to indicate magnitude (e.g.: 1.5K, 3M, 12B, etc..).
 *
 * <p>With the default style {@link
 * com.google.android.material.R.style#Widget_MaterialComponents_Slider}, colorPrimary and
 * colorOnPrimary are used to customize the color of the slider when enabled, and colorOnSurface is
 * used when disabled. The following attributes are used to customize the slider's appearance
 * further:
 *
 * <ul>
 *   <li>{@code haloColor}: the color of the halo around the thumb.
 *   <li>{@code haloRadius}: The radius of the halo around the thumb.
 *   <li>{@code labelBehavior}: The behavior of the label which can be {@code LABEL_FLOATING},
 *       {@code LABEL_WITHIN_BOUNDS}, or {@code LABEL_GONE}. See {@link LabelBehavior} for more
 *       information.
 *   <li>{@code labelStyle}: the style to apply to the value indicator {@link TooltipDrawable}.
 *   <li>{@code thumbColor}: the color of the slider's thumb.
 *   <li>{@code thumbStrokeColor}: the color of the thumb's stroke.
 *   <li>{@code thumbStrokeWidth}: the width of the thumb's stroke.
 *   <li>{@code thumbElevation}: the elevation of the slider's thumb.
 *   <li>{@code thumbRadius}: The radius of the slider's thumb.
 *   <li>{@code tickColorActive}: the color of the slider's tick marks for the active part of the
 *       track. Only used when the slider is in discrete mode.
 *   <li>{@code tickColorInactive}: the color of the slider's tick marks for the inactive part of
 *       the track. Only used when the slider is in discrete mode.
 *   <li>{@code tickColor}: the color of the slider's tick marks. Only used when the slider is in
 *       discrete mode. This is a short hand for setting both the {@code tickColorActive} and {@code
 *       tickColorInactive} to the same thing. This takes precedence over {@code tickColorActive}
 *       and {@code tickColorInactive}.
 *   <li>{@code tickVisible}: Whether to show the tick marks. Only used when the slider is in
 *       discrete mode.
 *   <li>{@code trackColorActive}: The color of the active part of the track.
 *   <li>{@code trackColorInactive}: The color of the inactive part of the track.
 *   <li>{@code trackColor}: The color of the whole track. This is a short hand for setting both the
 *       {@code trackColorActive} and {@code trackColorInactive} to the same thing. This takes
 *       precedence over {@code trackColorActive} and {@code trackColorInactive}.
 *   <li>{@code trackHeight}: The height of the track.
 * </ul>
 *
 * <p>The following XML attributes are used to set the slider's various parameters of operation:
 *
 * <ul>
 *   <li>{@code android:valueFrom}: <b>Required.</b> The slider's minimum value. This attribute must
 *       be less than {@code valueTo} or an {@link IllegalStateException} will be thrown when the
 *       view is laid out.
 *   <li>{@code android:valueTo}: <b>Required.</b> The slider's maximum value. This attribute must
 *       be greater than {@code valueFrom} or an {@link IllegalStateException} will be thrown when
 *       the view is laid out.
 *   <li>{@code android:value}: <b>Optional.</b> The initial value of the slider. If not specified,
 *       the slider's minimum value {@code android:valueFrom} is used.
 *   <li>{@code android:stepSize}: <b>Optional.</b> This value dictates whether the slider operates
 *       in continuous mode, or in discrete mode. If missing or equal to 0, the slider operates in
 *       continuous mode. If greater than 0 and evenly divides the range described by {@code
 *       valueFrom} and {@code valueTo}, the slider operates in discrete mode. If negative an {@link
 *       IllegalArgumentException} is thrown, or if greater than 0 but not a factor of the range
 *       described by {@code valueFrom} and {@code valueTo}, an {@link IllegalStateException} will
 *       be thrown when the view is laid out.
 * </ul>
 *
 * @attr ref com.google.android.material.R.styleable#Slider_android_enabled
 * @attr ref com.google.android.material.R.styleable#Slider_android_stepSize
 * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
 * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
 * @attr ref com.google.android.material.R.styleable#Slider_haloColor
 * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
 * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
 * @attr ref com.google.android.material.R.styleable#Slider_labelStyle
 * @attr ref com.google.android.material.R.styleable#Slider_thumbColor
 * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
 * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
 * @attr ref com.google.android.material.R.styleable#Slider_tickColor
 * @attr ref com.google.android.material.R.styleable#Slider_tickColorActive
 * @attr ref com.google.android.material.R.styleable#Slider_tickColorInactive
 * @attr ref com.google.android.material.R.styleable#Slider_tickVisible
 * @attr ref com.google.android.material.R.styleable#Slider_trackColor
 * @attr ref com.google.android.material.R.styleable#Slider_trackColorActive
 * @attr ref com.google.android.material.R.styleable#Slider_trackColorInactive
 * @attr ref com.google.android.material.R.styleable#Slider_trackHeight
 */
abstract class BaseSlider<
        S extends BaseSlider<S, L, T>,
        L extends BaseOnChangeListener<S>,
        T extends BaseOnSliderTouchListener<S>>
    extends View {

  private static final String TAG = BaseSlider.class.getSimpleName();
  private static final String EXCEPTION_ILLEGAL_VALUE =
      "Slider value(%s) must be greater or equal to valueFrom(%s), and lower or equal to"
          + " valueTo(%s)";
  private static final String EXCEPTION_ILLEGAL_DISCRETE_VALUE =
      "Value(%s) must be equal to valueFrom(%s) plus a multiple of stepSize(%s) when using"
          + " stepSize(%s)";
  private static final String EXCEPTION_ILLEGAL_VALUE_FROM =
      "valueFrom(%s) must be smaller than valueTo(%s)";
  private static final String EXCEPTION_ILLEGAL_VALUE_TO =
      "valueTo(%s) must be greater than valueFrom(%s)";
  private static final String EXCEPTION_ILLEGAL_STEP_SIZE =
      "The stepSize(%s) must be 0, or a factor of the valueFrom(%s)-valueTo(%s) range";
  private static final String WARNING_FLOATING_POINT_ERRROR =
      "Floating point value used for %s(%s). Using floats can have rounding errors which may"
          + " result in incorrect values. Instead, consider using integers with a custom"
          + " LabelFormatter to display the  value correctly.";

  private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
  private static final int HALO_ALPHA = 63;
  private static final double THRESHOLD = .0001;

  static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Slider;
  static final int UNIT_VALUE = 1;
  static final int UNIT_PX = 0;

  private static final long LABEL_ANIMATION_ENTER_DURATION = 83L;
  private static final long LABEL_ANIMATION_EXIT_DURATION = 117L;

  @NonNull private final Paint inactiveTrackPaint;
  @NonNull private final Paint activeTrackPaint;
  @NonNull private final Paint thumbPaint;
  @NonNull private final Paint haloPaint;
  @NonNull private final Paint inactiveTicksPaint;
  @NonNull private final Paint activeTicksPaint;
  @NonNull private final AccessibilityHelper accessibilityHelper;
  private final AccessibilityManager accessibilityManager;
  private AccessibilityEventSender accessibilityEventSender;

  private interface TooltipDrawableFactory {
    TooltipDrawable createTooltipDrawable();
  }

  @NonNull private final TooltipDrawableFactory labelMaker;
  @NonNull private final List<TooltipDrawable> labels = new ArrayList<>();
  @NonNull private final List<L> changeListeners = new ArrayList<>();
  @NonNull private final List<T> touchListeners = new ArrayList<>();

  // Whether the labels are showing or in the process of animating in.
  private boolean labelsAreAnimatedIn = false;
  private ValueAnimator labelsInAnimator;
  private ValueAnimator labelsOutAnimator;

  private final int scaledTouchSlop;

  private int minTrackSidePadding;
  private int defaultThumbRadius;

  private int widgetHeight;
  private int labelBehavior;
  private int trackHeight;
  private int trackSidePadding;
  private int trackTop;
  private int thumbRadius;
  private int haloRadius;
  private int labelPadding;
  private float touchDownX;
  private MotionEvent lastEvent;
  private LabelFormatter formatter;
  private boolean thumbIsPressed = false;
  private float valueFrom;
  private float valueTo;
  // Holds the values set to this slider. We keep this array sorted in order to check if the value
  // has been changed when a new value is set and to find the minimum and maximum values.
  private ArrayList<Float> values = new ArrayList<>();
  // The index of the currently touched thumb.
  private int activeThumbIdx = -1;
  // The index of the currently focused thumb.
  private int focusedThumbIdx = -1;
  private float stepSize = 0.0f;
  private float[] ticksCoordinates;
  private boolean tickVisible = true;
  private int trackWidth;
  private boolean forceDrawCompatHalo;
  private boolean isLongPress = false;
  private boolean dirtyConfig;

  @NonNull private ColorStateList haloColor;
  @NonNull private ColorStateList tickColorActive;
  @NonNull private ColorStateList tickColorInactive;
  @NonNull private ColorStateList trackColorActive;
  @NonNull private ColorStateList trackColorInactive;

  @NonNull private final MaterialShapeDrawable thumbDrawable = new MaterialShapeDrawable();

  private float touchPosition;
  @SeparationUnit private int separationUnit = UNIT_PX;

  /**
   * Determines the behavior of the label which can be any of the following.
   *
   * <ul>
   *   <li>{@code LABEL_FLOATING}: The label will only be visible on interaction. It will float
   *       above the slider and may cover views above this one. This is the default and recommended
   *       behavior.
   *   <li>{@code LABEL_WITHIN_BOUNDS}: The label will only be visible on interaction. The label
   *       will always be drawn within the bounds of this view. This means extra space will be
   *       visible above the slider when the label is not visible.
   *   <li>{@code LABEL_GONE}: The label will never be drawn.
   * </ul>
   */
  @IntDef({LABEL_FLOATING, LABEL_WITHIN_BOUNDS, LABEL_GONE})
  @Retention(RetentionPolicy.SOURCE)
  @interface LabelBehavior {}

  @IntDef({UNIT_PX, UNIT_VALUE})
  @Retention(RetentionPolicy.SOURCE)
  @interface SeparationUnit {}

  public BaseSlider(@NonNull Context context) {
    this(context, null);
  }

  public BaseSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.sliderStyle);
  }

  public BaseSlider(
      @NonNull Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    inactiveTrackPaint = new Paint();
    inactiveTrackPaint.setStyle(Style.STROKE);
    inactiveTrackPaint.setStrokeCap(Cap.ROUND);

    activeTrackPaint = new Paint();
    activeTrackPaint.setStyle(Style.STROKE);
    activeTrackPaint.setStrokeCap(Cap.ROUND);

    thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    thumbPaint.setStyle(Style.FILL);
    thumbPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

    haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    haloPaint.setStyle(Style.FILL);

    inactiveTicksPaint = new Paint();
    inactiveTicksPaint.setStyle(Style.STROKE);
    inactiveTicksPaint.setStrokeCap(Cap.ROUND);

    activeTicksPaint = new Paint();
    activeTicksPaint.setStyle(Style.STROKE);
    activeTicksPaint.setStrokeCap(Cap.ROUND);

    loadResources(context.getResources());

    // Because there's currently no way to copy the TooltipDrawable we use this to make more if more
    // thumbs are added.
    labelMaker =
        new TooltipDrawableFactory() {
          @Override
          public TooltipDrawable createTooltipDrawable() {
            final TypedArray a =
                ThemeEnforcement.obtainStyledAttributes(
                    getContext(), attrs, R.styleable.Slider, defStyleAttr, DEF_STYLE_RES);
            TooltipDrawable d = parseLabelDrawable(getContext(), a);
            a.recycle();
            return d;
          }
        };

    processAttributes(context, attrs, defStyleAttr);

    setFocusable(true);
    setClickable(true);

    // Set up the thumb drawable to always show the compat shadow.
    thumbDrawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);

    scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    accessibilityHelper = new AccessibilityHelper(this);
    ViewCompat.setAccessibilityDelegate(this, accessibilityHelper);

    accessibilityManager =
        (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
  }

  private void loadResources(@NonNull Resources resources) {
    widgetHeight = resources.getDimensionPixelSize(R.dimen.mtrl_slider_widget_height);

    minTrackSidePadding = resources.getDimensionPixelOffset(R.dimen.mtrl_slider_track_side_padding);
    trackSidePadding = minTrackSidePadding;

    defaultThumbRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_thumb_radius);

    trackTop = resources.getDimensionPixelOffset(R.dimen.mtrl_slider_track_top);

    labelPadding = resources.getDimensionPixelSize(R.dimen.mtrl_slider_label_padding);
  }

  private void processAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Slider, defStyleAttr, DEF_STYLE_RES);
    valueFrom = a.getFloat(R.styleable.Slider_android_valueFrom, 0.0f);
    valueTo = a.getFloat(R.styleable.Slider_android_valueTo, 1.0f);
    setValues(valueFrom);
    stepSize = a.getFloat(R.styleable.Slider_android_stepSize, 0.0f);

    boolean hasTrackColor = a.hasValue(R.styleable.Slider_trackColor);

    int trackColorInactiveRes =
        hasTrackColor ? R.styleable.Slider_trackColor : R.styleable.Slider_trackColorInactive;
    int trackColorActiveRes =
        hasTrackColor ? R.styleable.Slider_trackColor : R.styleable.Slider_trackColorActive;

    ColorStateList trackColorInactive =
        MaterialResources.getColorStateList(context, a, trackColorInactiveRes);
    setTrackInactiveTintList(
        trackColorInactive != null
            ? trackColorInactive
            : AppCompatResources.getColorStateList(
                context, R.color.material_slider_inactive_track_color));
    ColorStateList trackColorActive =
        MaterialResources.getColorStateList(context, a, trackColorActiveRes);
    setTrackActiveTintList(
        trackColorActive != null
            ? trackColorActive
            : AppCompatResources.getColorStateList(
                context, R.color.material_slider_active_track_color));
    ColorStateList thumbColor =
        MaterialResources.getColorStateList(context, a, R.styleable.Slider_thumbColor);
    thumbDrawable.setFillColor(thumbColor);

    if (a.hasValue(R.styleable.Slider_thumbStrokeColor)) {
      setThumbStrokeColor(
          MaterialResources.getColorStateList(context, a, R.styleable.Slider_thumbStrokeColor));
    }
    setThumbStrokeWidth(a.getDimension(R.styleable.Slider_thumbStrokeWidth, 0));

    ColorStateList haloColor =
        MaterialResources.getColorStateList(context, a, R.styleable.Slider_haloColor);
    setHaloTintList(
        haloColor != null
            ? haloColor
            : AppCompatResources.getColorStateList(context, R.color.material_slider_halo_color));

    tickVisible = a.getBoolean(R.styleable.Slider_tickVisible, true);
    boolean hasTickColor = a.hasValue(R.styleable.Slider_tickColor);
    int tickColorInactiveRes =
        hasTickColor ? R.styleable.Slider_tickColor : R.styleable.Slider_tickColorInactive;
    int tickColorActiveRes =
        hasTickColor ? R.styleable.Slider_tickColor : R.styleable.Slider_tickColorActive;
    ColorStateList tickColorInactive =
        MaterialResources.getColorStateList(context, a, tickColorInactiveRes);
    setTickInactiveTintList(
        tickColorInactive != null
            ? tickColorInactive
            : AppCompatResources.getColorStateList(
                context, R.color.material_slider_inactive_tick_marks_color));
    ColorStateList tickColorActive =
        MaterialResources.getColorStateList(context, a, tickColorActiveRes);
    setTickActiveTintList(
        tickColorActive != null
            ? tickColorActive
            : AppCompatResources.getColorStateList(
                context, R.color.material_slider_active_tick_marks_color));

    setThumbRadius(a.getDimensionPixelSize(R.styleable.Slider_thumbRadius, 0));
    setHaloRadius(a.getDimensionPixelSize(R.styleable.Slider_haloRadius, 0));

    setThumbElevation(a.getDimension(R.styleable.Slider_thumbElevation, 0));

    setTrackHeight(a.getDimensionPixelSize(R.styleable.Slider_trackHeight, 0));

    labelBehavior = a.getInt(R.styleable.Slider_labelBehavior, LABEL_FLOATING);

    if (!a.getBoolean(R.styleable.Slider_android_enabled, true)) {
      setEnabled(false);
    }

    a.recycle();
  }

  @NonNull
  private static TooltipDrawable parseLabelDrawable(
      @NonNull Context context, @NonNull TypedArray a) {
    return TooltipDrawable.createFromAttributes(
        context,
        null,
        0,
        a.getResourceId(R.styleable.Slider_labelStyle, R.style.Widget_MaterialComponents_Tooltip));
  }

  private void maybeIncreaseTrackSidePadding() {
    int increasedSidePadding = max(thumbRadius - defaultThumbRadius, 0);
    trackSidePadding = minTrackSidePadding + increasedSidePadding;
    if (ViewCompat.isLaidOut(this)) {
      updateTrackWidth(getWidth());
    }
  }

  private void validateValueFrom() {
    if (valueFrom >= valueTo) {
      throw new IllegalStateException(
          String.format(
              EXCEPTION_ILLEGAL_VALUE_FROM, Float.toString(valueFrom), Float.toString(valueTo)));
    }
  }

  private void validateValueTo() {
    if (valueTo <= valueFrom) {
      throw new IllegalStateException(
          String.format(
              EXCEPTION_ILLEGAL_VALUE_TO, Float.toString(valueTo), Float.toString(valueFrom)));
    }
  }

  private boolean valueLandsOnTick(float value) {
    // Check that the value is a multiple of stepSize given the offset of valueFrom
    // We're using BigDecimal here to avoid floating point rounding errors.
    double potentialTickValue =
        new BigDecimal(Float.toString(value))
            .subtract(new BigDecimal(Float.toString(valueFrom)))
            .divide(new BigDecimal(Float.toString(stepSize)), MathContext.DECIMAL64)
            .doubleValue();

    // If the potentialTickValue is a whole number, it means the value lands on a tick.
    return Math.abs(Math.round(potentialTickValue) - potentialTickValue) < THRESHOLD;
  }

  private void validateStepSize() {
    if (stepSize > 0.0f && !valueLandsOnTick(valueTo)) {
      throw new IllegalStateException(
          String.format(
              EXCEPTION_ILLEGAL_STEP_SIZE,
              Float.toString(stepSize),
              Float.toString(valueFrom),
              Float.toString(valueTo)));
    }
  }

  private void validateValues() {
    for (Float value : values) {
      if (value < valueFrom || value > valueTo) {
        throw new IllegalStateException(
            String.format(
                EXCEPTION_ILLEGAL_VALUE,
                Float.toString(value),
                Float.toString(valueFrom),
                Float.toString(valueTo)));
      }
      if (stepSize > 0.0f && !valueLandsOnTick(value)) {
        throw new IllegalStateException(
            String.format(
                EXCEPTION_ILLEGAL_DISCRETE_VALUE,
                Float.toString(value),
                Float.toString(valueFrom),
                Float.toString(stepSize),
                Float.toString(stepSize)));
      }
    }
  }

  private void warnAboutFloatingPointError() {
    if (stepSize == 0) {
      // Only warn if slider uses a step value.
      return;
    }

    if ((int) stepSize != stepSize) {
      Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERRROR, "stepSize", stepSize));
    }

    if ((int) valueFrom != valueFrom) {
      Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERRROR, "valueFrom", valueFrom));
    }

    if ((int) valueTo != valueTo) {
      Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERRROR, "valueTo", valueTo));
    }
  }

  private void validateConfigurationIfDirty() {
    if (dirtyConfig) {
      validateValueFrom();
      validateValueTo();
      validateStepSize();
      validateValues();
      warnAboutFloatingPointError();
      dirtyConfig = false;
    }
  }

  /**
   * Returns the slider's {@code valueFrom} value.
   *
   * @see #setValueFrom(float)
   * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
   */
  public float getValueFrom() {
    return valueFrom;
  }

  /**
   * Sets the slider's {@code valueFrom} value.
   *
   * <p>The {@code valueFrom} value must be strictly lower than the {@code valueTo} value. If that
   * is not the case, an {@link IllegalStateException} will be thrown when the view is laid out.
   *
   * @param valueFrom The minimum value for the slider's range of values
   * @see #getValueFrom()
   * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
   */
  public void setValueFrom(float valueFrom) {
    this.valueFrom = valueFrom;
    dirtyConfig = true;
    postInvalidate();
  }

  /**
   * Returns the slider's {@code valueTo} value.
   *
   * @see #setValueTo(float)
   * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
   */
  public float getValueTo() {
    return valueTo;
  }

  /**
   * Sets the slider's {@code valueTo} value.
   *
   * <p>The {@code valueTo} value must be strictly greater than the {@code valueFrom} value. If that
   * is not the case, an {@link IllegalStateException} will be thrown when the view is laid out.
   *
   * @param valueTo The maximum value for the slider's range of values
   * @see #getValueTo()
   * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
   */
  public void setValueTo(float valueTo) {
    this.valueTo = valueTo;
    dirtyConfig = true;
    postInvalidate();
  }

  @NonNull
  List<Float> getValues() {
    return new ArrayList<>(values);
  }

  /**
   * Sets multiple values for the slider. Each value will represent a different thumb.
   *
   * <p>Each value must be greater or equal to {@code valueFrom}, and lesser or equal to {@code
   * valueTo}. If that is not the case, an {@link IllegalStateException} will be thrown when the
   * view is laid out.
   *
   * <p>If the slider is in discrete mode (i.e. the tick increment value is greater than 0), the
   * values must be set to a value falls on a tick (i.e.: {@code value == valueFrom + x * stepSize},
   * where {@code x} is an integer equal to or greater than 0). If that is not the case, an {@link
   * IllegalStateException} will be thrown when the view is laid out.
   *
   * @param values An array of values to set.
   * @see #getValues()
   */
  void setValues(@NonNull Float... values) {
    ArrayList<Float> list = new ArrayList<>();
    Collections.addAll(list, values);
    setValuesInternal(list);
  }

  /**
   * Sets multiple values for the slider. Each value will represent a different thumb.
   *
   * <p>Each value must be greater or equal to {@code valueFrom}, and lesser or equal to {@code
   * valueTo}. If that is not the case, an {@link IllegalStateException} will be thrown when the
   * view is laid out.
   *
   * <p>If the slider is in discrete mode (i.e. the tick increment value is greater than 0), the
   * values must be set to a value falls on a tick (i.e.: {@code value == valueFrom + x * stepSize},
   * where {@code x} is an integer equal to or greater than 0). If that is not the case, an {@link
   * IllegalStateException} will be thrown when the view is laid out.
   *
   * @param values An array of values to set.
   * @throws IllegalArgumentException If {@code values} is empty.
   */
  void setValues(@NonNull List<Float> values) {
    setValuesInternal(new ArrayList<>(values));
  }

  /**
   * This method assumes the list passed in is a copy. It is split out so we can call it from {@link
   * #setValues(Float...)} and {@link #setValues(List)}
   */
  private void setValuesInternal(@NonNull ArrayList<Float> values) {
    if (values.isEmpty()) {
      throw new IllegalArgumentException("At least one value must be set");
    }

    Collections.sort(values);

    if (this.values.size() == values.size()) {
      if (this.values.equals(values)) {
        return;
      }
    }

    this.values = values;
    dirtyConfig = true;
    // Only update the focused thumb index. The active thumb index will be updated on touch.
    focusedThumbIdx = 0;
    updateHaloHotspot();
    createLabelPool();
    dispatchOnChangedProgramatically();
    postInvalidate();
  }

  private void createLabelPool() {
    // If there are too many labels, remove the extra ones from the end.
    if (labels.size() > values.size()) {
      List<TooltipDrawable> tooltipDrawables = labels.subList(values.size(), labels.size());
      for (TooltipDrawable label : tooltipDrawables) {
        if (ViewCompat.isAttachedToWindow(this)) {
          detachLabelFromContentView(label);
        }
      }
      tooltipDrawables.clear();
    }

    // If there's not enough labels, add more.
    while (labels.size() < values.size()) {
      TooltipDrawable tooltipDrawable = labelMaker.createTooltipDrawable();
      labels.add(tooltipDrawable);
      if (ViewCompat.isAttachedToWindow(this)) {
        attachLabelToContentView(tooltipDrawable);
      }
    }

    // Add a stroke if there is more than one label for when they overlap.
    int strokeWidth = labels.size() == 1 ? 0 : 1;
    for (TooltipDrawable label : labels) {
      label.setStrokeWidth(strokeWidth);
    }
  }

  /**
   * Returns the step size used to mark the ticks.
   *
   * <p>A step size of 0 means that the slider is operating in continuous mode. A step size greater
   * than 0 means that the slider is operating in discrete mode.
   *
   * @see #setStepSize(float)
   * @attr ref com.google.android.material.R.styleable#Slider_android_stepSize
   */
  public float getStepSize() {
    return stepSize;
  }

  /**
   * Sets the step size to use to mark the ticks.
   *
   * <p>Setting this value to 0 will make the slider operate in continuous mode. Setting this value
   * to a number greater than 0 will make the slider operate in discrete mode.
   *
   * <p>The step size must evenly divide the range described by the {@code valueFrom} and {@code
   * valueTo}, it must be a factor of the range. If the step size is not a factor of the range, an
   * {@link IllegalStateException} will be thrown when this view is laid out.
   *
   * <p>Setting this value to a negative value will result in an {@link IllegalArgumentException}.
   *
   * @param stepSize The interval value at which ticks must be drawn. Set to 0 to operate the slider
   *     in continuous mode and not have any ticks.
   * @throws IllegalArgumentException If the step size is less than 0
   * @see #getStepSize()
   * @attr ref com.google.android.material.R.styleable#Slider_android_stepSize
   */
  public void setStepSize(float stepSize) {
    if (stepSize < 0.0f) {
      throw new IllegalArgumentException(
          String.format(
              EXCEPTION_ILLEGAL_STEP_SIZE,
              Float.toString(stepSize),
              Float.toString(valueFrom),
              Float.toString(valueTo)));
    }
    if (this.stepSize != stepSize) {
      this.stepSize = stepSize;
      dirtyConfig = true;
      postInvalidate();
    }
  }

  /** Returns the index of the currently focused thumb */
  public int getFocusedThumbIndex() {
    return focusedThumbIdx;
  }

  /** Sets the index of the currently focused thumb */
  public void setFocusedThumbIndex(int index) {
    if (index < 0 || index >= values.size()) {
      throw new IllegalArgumentException("index out of range");
    }
    focusedThumbIdx = index;
    accessibilityHelper.requestKeyboardFocusForVirtualView(focusedThumbIdx);
    postInvalidate();
  }

  protected void setActiveThumbIndex(int index) {
    activeThumbIdx = index;
  }

  /** Returns the index of the currently active thumb, or -1 if no thumb is active */
  public int getActiveThumbIndex() {
    return activeThumbIdx;
  }

  /**
   * Registers a callback to be invoked when the slider changes.
   * On the RangeSlider implementation, the listener is invoked once for each value.
   *
   * @param listener The callback to run when the slider changes
   */
  public void addOnChangeListener(@Nullable L listener) {
    changeListeners.add(listener);
  }

  /**
   * Removes a callback for value changes from this slider.
   *
   * @param listener The callback that'll stop receive slider changes
   */
  public void removeOnChangeListener(@NonNull L listener) {
    changeListeners.remove(listener);
  }

  /** Removes all instances of attached to this slider */
  public void clearOnChangeListeners() {
    changeListeners.clear();
  }

  /**
   * Registers a callback to be invoked when the slider touch event is being started or stopped
   *
   * @param listener The callback to run when the slider starts or stops being touched
   */
  public void addOnSliderTouchListener(@NonNull T listener) {
    touchListeners.add(listener);
  }

  /**
   * Removes a callback to be invoked when the slider touch event is being started or stopped
   *
   * @param listener The callback that'll stop be notified when the slider is being touched
   */
  public void removeOnSliderTouchListener(@NonNull T listener) {
    touchListeners.remove(listener);
  }

  /** Removes all instances of touch listeners attached to this slider */
  public void clearOnSliderTouchListeners() {
    touchListeners.clear();
  }

  /**
   * Returns {@code true} if the slider has a {@link LabelFormatter} attached, {@code false}
   * otherwise.
   */
  public boolean hasLabelFormatter() {
    return formatter != null;
  }

  /**
   * Registers a {@link LabelFormatter} to be used to format the value displayed in the bubble shown
   * when the slider operates in discrete mode.
   *
   * @param formatter The {@link LabelFormatter} to use to format the bubble's text
   */
  public void setLabelFormatter(@Nullable LabelFormatter formatter) {
    this.formatter = formatter;
  }

  /**
   * Returns the elevation of the thumb.
   *
   * @see #setThumbElevation(float)
   * @see #setThumbElevationResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
   */
  public float getThumbElevation() {
    return thumbDrawable.getElevation();
  }

  /**
   * Sets the elevation of the thumb.
   *
   * @see #getThumbElevation()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
   */
  public void setThumbElevation(float elevation) {
    thumbDrawable.setElevation(elevation);
  }

  /**
   * Sets the elevation of the thumb from a dimension resource.
   *
   * @see #getThumbElevation()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
   */
  public void setThumbElevationResource(@DimenRes int elevation) {
    setThumbElevation(getResources().getDimension(elevation));
  }

  /**
   * Returns the radius of the thumb.
   *
   * @see #setThumbRadius(int)
   * @see #setThumbRadiusResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
   */
  @Dimension
  public int getThumbRadius() {
    return thumbRadius;
  }

  /**
   * Sets the radius of the thumb in pixels.
   *
   * @see #getThumbRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
   */
  public void setThumbRadius(@IntRange(from = 0) @Dimension int radius) {
    if (radius == thumbRadius) {
      return;
    }

    thumbRadius = radius;
    maybeIncreaseTrackSidePadding();

    thumbDrawable.setShapeAppearanceModel(
        ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, thumbRadius).build());
    thumbDrawable.setBounds(0, 0, thumbRadius * 2, thumbRadius * 2);

    postInvalidate();
  }

  /**
   * Sets the radius of the thumb from a dimension resource.
   *
   * @see #getThumbRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
   */
  public void setThumbRadiusResource(@DimenRes int radius) {
    setThumbRadius(getResources().getDimensionPixelSize(radius));
  }

  /**
   * Sets the stroke color for the thumbs. Both thumbStroke color and thumbStroke width must be set
   * for a stroke to be drawn.
   *
   * @param thumbStrokeColor Color to use for the stroke in the thumbs.
   * @attr ref com.google.android.material.R.styleable#Slider_thumbStrokeColor
   * @see #setThumbStrokeColorResource(int)
   * @see #getThumbStrokeColor()
   */
  public void setThumbStrokeColor(@Nullable ColorStateList thumbStrokeColor) {
    thumbDrawable.setStrokeColor(thumbStrokeColor);
    postInvalidate();
  }

  /**
   * Sets the stroke color resource for the thumbs. Both thumbStroke color and thumbStroke width
   * must be set for a stroke to be drawn.
   *
   * @param thumbStrokeColorResourceId Color resource to use for the stroke.
   * @attr ref com.google.android.material.R.styleable#Slider_thumbStrokeColor
   * @see #setThumbStrokeColor(ColorStateList)
   * @see #getThumbStrokeColor()
   */
  public void setThumbStrokeColorResource(@ColorRes int thumbStrokeColorResourceId) {
    if (thumbStrokeColorResourceId != 0) {
      setThumbStrokeColor(
          AppCompatResources.getColorStateList(getContext(), thumbStrokeColorResourceId));
    }
  }

  /**
   * Gets the stroke color for the thumb.
   *
   * @return The color used for the stroke in the thumb.
   * @attr ref com.google.android.material.R.styleable#Slider_thumbStrokeColor
   * @see #setThumbStrokeColor(ColorStateList)
   * @see #setThumbStrokeColorResource(int)
   */
  public ColorStateList getThumbStrokeColor() {
    return thumbDrawable.getStrokeColor();
  }

  /**
   * Sets the stroke width for the thumb. Both thumbStroke color and thumbStroke width must be set
   * for a stroke to be drawn.
   *
   * @param thumbStrokeWidth Stroke width for the thumb
   * @attr ref com.google.android.material.R.styleable#Slider_thumbStrokeWidth
   * @see #setThumbStrokeWidthResource(int)
   * @see #getThumbStrokeWidth()
   */
  public void setThumbStrokeWidth(float thumbStrokeWidth) {
    thumbDrawable.setStrokeWidth(thumbStrokeWidth);
    postInvalidate();
  }

  /**
   * Sets the stroke width dimension resource for the thumb.Both thumbStroke color and thumbStroke
   * width must be set for a stroke to be drawn.
   *
   * @param thumbStrokeWidthResourceId Stroke width dimension resource for the thumb
   * @attr ref com.google.android.material.R.styleable#Slider_thumbStrokeWidth
   * @see #setThumbStrokeWidth(float)
   * @see #getThumbStrokeWidth()
   */
  public void setThumbStrokeWidthResource(@DimenRes int thumbStrokeWidthResourceId) {
    if (thumbStrokeWidthResourceId != 0) {
      setThumbStrokeWidth(getResources().getDimension(thumbStrokeWidthResourceId));
    }
  }

  /**
   * Gets the stroke width for the thumb
   *
   * @return Stroke width for the thumb.
   * @attr ref com.google.android.material.R.styleable#Slider_thumbStrokeWidth
   * @see #setThumbStrokeWidth(float)
   * @see #setThumbStrokeWidthResource(int)
   */
  public float getThumbStrokeWidth() {
    return thumbDrawable.getStrokeWidth();
  }

  /**
   * Returns the radius of the halo.
   *
   * @see #setHaloRadius(int)
   * @see #setHaloRadiusResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
   */
  @Dimension()
  public int getHaloRadius() {
    return haloRadius;
  }

  /**
   * Sets the radius of the halo in pixels.
   *
   * @see #getHaloRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
   */
  public void setHaloRadius(@IntRange(from = 0) @Dimension int radius) {
    if (radius == haloRadius) {
      return;
    }

    haloRadius = radius;
    Drawable background = getBackground();
    if (!shouldDrawCompatHalo() && background instanceof RippleDrawable) {
      DrawableUtils.setRippleDrawableRadius((RippleDrawable) background, haloRadius);
      return;
    }

    postInvalidate();
  }

  /**
   * Sets the radius of the halo from a dimension resource.
   *
   * @see #getHaloRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
   */
  public void setHaloRadiusResource(@DimenRes int radius) {
    setHaloRadius(getResources().getDimensionPixelSize(radius));
  }

  /**
   * Returns the {@link LabelBehavior} used.
   *
   * @see #setLabelBehavior(int)
   * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
   */
  @LabelBehavior
  public int getLabelBehavior() {
    return labelBehavior;
  }

  /**
   * Determines the {@link LabelBehavior} used.
   *
   * @see LabelBehavior
   * @see #getLabelBehavior()
   * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
   */
  public void setLabelBehavior(@LabelBehavior int labelBehavior) {
    if (this.labelBehavior != labelBehavior) {
      this.labelBehavior = labelBehavior;
      requestLayout();
    }
  }

  /** Returns the side padding of the track. */
  @Dimension()
  public int getTrackSidePadding() {
    return trackSidePadding;
  }

  /** Returns the width of the track in pixels. */
  @Dimension()
  public int getTrackWidth() {
    return trackWidth;
  }

  /**
   * Returns the height of the track in pixels.
   *
   * @see #setTrackHeight(int)
   * @attr ref com.google.android.material.R.styleable#Slider_trackHeight
   */
  @Dimension()
  public int getTrackHeight() {
    return trackHeight;
  }

  /**
   * Set the height of the track in pixels.
   *
   * @see #getTrackHeight()
   * @attr ref com.google.android.material.R.styleable#Slider_trackHeight
   */
  public void setTrackHeight(@IntRange(from = 0) @Dimension int trackHeight) {
    if (this.trackHeight != trackHeight) {
      this.trackHeight = trackHeight;
      invalidateTrack();
      postInvalidate();
    }
  }

  /**
   * Returns the color of the halo.
   *
   * @see #setHaloTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Slider_haloColor
   */
  @NonNull
  public ColorStateList getHaloTintList() {
    return haloColor;
  }

  /**
   * Sets the color of the halo.
   *
   * @see #getHaloTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_haloColor
   */
  public void setHaloTintList(@NonNull ColorStateList haloColor) {
    if (haloColor.equals(this.haloColor)) {
      return;
    }

    this.haloColor = haloColor;
    Drawable background = getBackground();
    if (!shouldDrawCompatHalo() && background instanceof RippleDrawable) {
      ((RippleDrawable) background).setColor(haloColor);
      return;
    }

    haloPaint.setColor(getColorForState(haloColor));
    haloPaint.setAlpha(HALO_ALPHA);
    invalidate();
  }

  /**
   * Returns the color of the thumb.
   *
   * @see #setThumbTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbColor
   */
  @NonNull
  public ColorStateList getThumbTintList() {
    return thumbDrawable.getFillColor();
  }

  /**
   * Sets the color of the thumb.
   *
   * @see #getThumbTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbColor
   */
  public void setThumbTintList(@NonNull ColorStateList thumbColor) {
    if (thumbColor.equals(thumbDrawable.getFillColor())) {
      return;
    }

    thumbDrawable.setFillColor(thumbColor);
    invalidate();
  }

  /**
   * Returns the color of the tick if the active and inactive parts aren't different.
   *
   * @throws IllegalStateException If {@code tickColorActive} and {@code tickColorInactive} have
   *     been set to different values.
   * @see #setTickTintList(ColorStateList)
   * @see #setTickInactiveTintList(ColorStateList)
   * @see #setTickActiveTintList(ColorStateList)
   * @see #getTickInactiveTintList()
   * @see #getTickActiveTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_tickColor
   */
  @NonNull
  public ColorStateList getTickTintList() {
    if (!tickColorInactive.equals(tickColorActive)) {
      throw new IllegalStateException(
          "The inactive and active ticks are different colors. Use the getTickColorInactive() and"
              + " getTickColorActive() methods instead.");
    }
    return tickColorActive;
  }

  /**
   * Sets the color of the tick marks.
   *
   * @see #setTickInactiveTintList(ColorStateList)
   * @see #setTickActiveTintList(ColorStateList)
   * @see #getTickTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_tickColor
   */
  public void setTickTintList(@NonNull ColorStateList tickColor) {
    setTickInactiveTintList(tickColor);
    setTickActiveTintList(tickColor);
  }

  /**
   * Returns the color of the ticks on the active portion of the track.
   *
   * @see #setTickActiveTintList(ColorStateList)
   * @see #setTickTintList(ColorStateList)
   * @see #getTickTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_tickColorActive
   */
  @NonNull
  public ColorStateList getTickActiveTintList() {
    return tickColorActive;
  }

  /**
   * Sets the color of the ticks on the active portion of the track.
   *
   * @see #getTickActiveTintList()
   * @see #setTickTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Slider_tickColorActive
   */
  public void setTickActiveTintList(@NonNull ColorStateList tickColor) {
    if (tickColor.equals(tickColorActive)) {
      return;
    }
    tickColorActive = tickColor;
    activeTicksPaint.setColor(getColorForState(tickColorActive));
    invalidate();
  }

  /**
   * Returns the color of the ticks on the inactive portion of the track.
   *
   * @see #setTickInactiveTintList(ColorStateList)
   * @see #setTickTintList(ColorStateList)
   * @see #getTickTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_tickColorInactive
   */
  @NonNull
  public ColorStateList getTickInactiveTintList() {
    return tickColorInactive;
  }

  /**
   * Sets the color of the ticks on the inactive portion of the track.
   *
   * @see #getTickInactiveTintList()
   * @see #setTickTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Slider_tickColorInactive
   */
  public void setTickInactiveTintList(@NonNull ColorStateList tickColor) {
    if (tickColor.equals(tickColorInactive)) {
      return;
    }
    tickColorInactive = tickColor;
    inactiveTicksPaint.setColor(getColorForState(tickColorInactive));
    invalidate();
  }

  /**
   * Returns whether the tick marks are visible. Only used when the slider is in discrete mode.
   *
   * @see #setTickVisible(boolean)
   * @attr ref com.google.android.material.R.styleable#Slider_tickVisible
   */
  public boolean isTickVisible() {
    return tickVisible;
  }

  /**
   * Sets whether the tick marks are visible. Only used when the slider is in discrete mode.
   *
   * @param tickVisible The visibility of tick marks.
   * @attr ref com.google.android.material.R.styleable#Slider_tickVisible
   */
  public void setTickVisible(boolean tickVisible) {
    if (this.tickVisible != tickVisible) {
      this.tickVisible = tickVisible;
      postInvalidate();
    }
  }

  /**
   * Returns the color of the track if the active and inactive parts aren't different.
   *
   * @throws IllegalStateException If {@code trackColorActive} and {@code trackColorInactive} have
   *     been set to different values.
   * @see #setTrackTintList(ColorStateList)
   * @see #setTrackInactiveTintList(ColorStateList)
   * @see #setTrackActiveTintList(ColorStateList)
   * @see #getTrackInactiveTintList()
   * @see #getTrackActiveTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_trackColor
   */
  @NonNull
  public ColorStateList getTrackTintList() {
    if (!trackColorInactive.equals(trackColorActive)) {
      throw new IllegalStateException(
          "The inactive and active parts of the track are different colors. Use the"
              + " getInactiveTrackColor() and getActiveTrackColor() methods instead.");
    }
    return trackColorActive;
  }

  /**
   * Sets the color of the track.
   *
   * @see #setTrackInactiveTintList(ColorStateList)
   * @see #setTrackActiveTintList(ColorStateList)
   * @see #getTrackTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_trackColor
   */
  public void setTrackTintList(@NonNull ColorStateList trackColor) {
    setTrackInactiveTintList(trackColor);
    setTrackActiveTintList(trackColor);
  }

  /**
   * Returns the color of the active portion of the track.
   *
   * @see #setTrackActiveTintList(ColorStateList)
   * @see #setTrackTintList(ColorStateList)
   * @see #getTrackTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_trackColorActive
   */
  @NonNull
  public ColorStateList getTrackActiveTintList() {
    return trackColorActive;
  }

  /**
   * Sets the color of the active portion of the track.
   *
   * @see #getTrackActiveTintList()
   * @see #setTrackTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Slider_trackColorActive
   */
  public void setTrackActiveTintList(@NonNull ColorStateList trackColor) {
    if (trackColor.equals(trackColorActive)) {
      return;
    }
    trackColorActive = trackColor;
    activeTrackPaint.setColor(getColorForState(trackColorActive));
    invalidate();
  }

  /**
   * Returns the color of the inactive portion of the track.
   *
   * @see #setTrackInactiveTintList(ColorStateList)
   * @see #setTrackTintList(ColorStateList)
   * @see #getTrackTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_trackColorInactive
   */
  @NonNull
  public ColorStateList getTrackInactiveTintList() {
    return trackColorInactive;
  }

  /**
   * Sets the color of the inactive portion of the track.
   *
   * @see #getTrackInactiveTintList()
   * @see #setTrackTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#Slider_trackColorInactive
   */
  public void setTrackInactiveTintList(@NonNull ColorStateList trackColor) {
    if (trackColor.equals(trackColorInactive)) {
      return;
    }
    trackColorInactive = trackColor;
    inactiveTrackPaint.setColor(getColorForState(trackColorInactive));
    invalidate();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    // When we're disabled, set the layer type to hardware so we can clear the track out from behind
    // the thumb.
    setLayerType(enabled ? LAYER_TYPE_NONE : LAYER_TYPE_HARDWARE, null);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // The label is attached on the Overlay relative to the content.
    for (TooltipDrawable label : labels) {
      attachLabelToContentView(label);
    }
  }

  private void attachLabelToContentView(TooltipDrawable label) {
    label.setRelativeToView(ViewUtils.getContentView(this));
  }

  @Override
  protected void onDetachedFromWindow() {
    if (accessibilityEventSender != null) {
      removeCallbacks(accessibilityEventSender);
    }

    labelsAreAnimatedIn = false;
    for (TooltipDrawable label : labels) {
      detachLabelFromContentView(label);
    }

    super.onDetachedFromWindow();
  }

  private void detachLabelFromContentView(TooltipDrawable label) {
    ViewOverlayImpl contentViewOverlay = ViewUtils.getContentViewOverlay(this);
    if (contentViewOverlay != null) {
      contentViewOverlay.remove(label);
      label.detachView(ViewUtils.getContentView(this));
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(
        widthMeasureSpec,
        MeasureSpec.makeMeasureSpec(
            widgetHeight
                + (labelBehavior == LABEL_WITHIN_BOUNDS ? labels.get(0).getIntrinsicHeight() : 0),
            MeasureSpec.EXACTLY));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    updateTrackWidth(w);
    updateHaloHotspot();
  }

  private void maybeCalculateTicksCoordinates() {
    if (stepSize <= 0.0f) {
      return;
    }

    validateConfigurationIfDirty();

    int tickCount = (int) ((valueTo - valueFrom) / stepSize + 1);
    // Limit the tickCount if they will be too dense.
    tickCount = Math.min(tickCount, trackWidth / (trackHeight * 2) + 1);
    if (ticksCoordinates == null || ticksCoordinates.length != tickCount * 2) {
      ticksCoordinates = new float[tickCount * 2];
    }

    float interval = trackWidth / (float) (tickCount - 1);
    for (int i = 0; i < tickCount * 2; i += 2) {
      ticksCoordinates[i] = trackSidePadding + i / 2 * interval;
      ticksCoordinates[i + 1] = calculateTop();
    }
  }

  private void updateTrackWidth(int width) {
    // Update the visible track width.
    trackWidth = Math.max(width - trackSidePadding * 2, 0);

    // Update the visible tick coordinates.
    maybeCalculateTicksCoordinates();
  }

  private void updateHaloHotspot() {
    // Set the hotspot as the halo if RippleDrawable is being used.
    if (!shouldDrawCompatHalo() && getMeasuredWidth() > 0) {
      final Drawable background = getBackground();
      if (background instanceof RippleDrawable) {
        int x = (int) (normalizeValue(values.get(focusedThumbIdx)) * trackWidth + trackSidePadding);
        int y = calculateTop();
        DrawableCompat.setHotspotBounds(
            background, x - haloRadius, y - haloRadius, x + haloRadius, y + haloRadius);
      }
    }
  }

  private int calculateTop() {
    return trackTop
        + (labelBehavior == LABEL_WITHIN_BOUNDS ? labels.get(0).getIntrinsicHeight() : 0);
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    if (dirtyConfig) {
      validateConfigurationIfDirty();

      // Update the visible tick coordinates.
      maybeCalculateTicksCoordinates();
    }

    super.onDraw(canvas);

    int top = calculateTop();

    drawInactiveTrack(canvas, trackWidth, top);
    if (Collections.max(getValues()) > valueFrom) {
      drawActiveTrack(canvas, trackWidth, top);
    }

    maybeDrawTicks(canvas);

    if ((thumbIsPressed || isFocused()) && isEnabled()) {
      maybeDrawHalo(canvas, trackWidth, top);

      // Draw labels if there is an active thumb.
      if (activeThumbIdx != -1) {
        ensureLabelsAdded();
      }
    }

    drawThumbs(canvas, trackWidth, top);
  }

  /**
   * Returns a float array where {@code float[0]} is the normalized left position and {@code
   * float[1]} is the normalized right position of the range.
   */
  private float[] getActiveRange() {
    float max = Collections.max(getValues());
    float min = Collections.min(getValues());
    float left = normalizeValue(values.size() == 1 ? valueFrom : min);
    float right = normalizeValue(max);

    // In RTL we draw things in reverse, so swap the left and right range values
    return isRtl() ? new float[] {right, left} : new float[] {left, right};
  }

  private void drawInactiveTrack(@NonNull Canvas canvas, int width, int top) {
    float[] activeRange = getActiveRange();
    float right = trackSidePadding + activeRange[1] * width;
    if (right < trackSidePadding + width) {
      canvas.drawLine(right, top, trackSidePadding + width, top, inactiveTrackPaint);
    }

    // Also draw inactive track to the left if there is any
    float left = trackSidePadding + activeRange[0] * width;
    if (left > trackSidePadding) {
      canvas.drawLine(trackSidePadding, top, left, top, inactiveTrackPaint);
    }
  }

  /**
   * Returns a number between 0 and 1 indicating where on the track this value should sit with 0
   * being on the far left, and 1 on the far right.
   */
  private float normalizeValue(float value) {
    float normalized = (value - valueFrom) / (valueTo - valueFrom);
    if (isRtl()) {
      return 1 - normalized;
    }
    return normalized;
  }

  private void drawActiveTrack(@NonNull Canvas canvas, int width, int top) {
    float[] activeRange = getActiveRange();
    float right = trackSidePadding + activeRange[1] * width;
    float left = trackSidePadding + activeRange[0] * width;
    canvas.drawLine(left, top, right, top, activeTrackPaint);
  }

  private void maybeDrawTicks(@NonNull Canvas canvas) {
    if (!tickVisible || stepSize <= 0.0f) {
      return;
    }

    float[] activeRange = getActiveRange();
    int leftPivotIndex = pivotIndex(ticksCoordinates, activeRange[0]);
    int rightPivotIndex = pivotIndex(ticksCoordinates, activeRange[1]);

    // Draw inactive ticks to the left of the smallest thumb.
    canvas.drawPoints(ticksCoordinates, 0, leftPivotIndex * 2, inactiveTicksPaint);

    // Draw active ticks between the thumbs.
    canvas.drawPoints(
        ticksCoordinates,
        leftPivotIndex * 2,
        rightPivotIndex * 2 - leftPivotIndex * 2,
        activeTicksPaint);

    // Draw inactive ticks to the right of the largest thumb.
    canvas.drawPoints(
        ticksCoordinates,
        rightPivotIndex * 2,
        ticksCoordinates.length - rightPivotIndex * 2,
        inactiveTicksPaint);
  }

  private void drawThumbs(@NonNull Canvas canvas, int width, int top) {
    // Clear out the track behind the thumb if we're in a disable state since the thumb is
    // transparent.
    if (!isEnabled()) {
      for (Float value : values) {
        canvas.drawCircle(
            trackSidePadding + normalizeValue(value) * width, top, thumbRadius, thumbPaint);
      }
    }

    for (Float value : values) {
      canvas.save();
      canvas.translate(
          trackSidePadding + (int) (normalizeValue(value) * width) - thumbRadius,
          top - thumbRadius);
      thumbDrawable.draw(canvas);
      canvas.restore();
    }
  }

  private void maybeDrawHalo(@NonNull Canvas canvas, int width, int top) {
    // Only draw the halo for devices that aren't using the ripple.
    if (shouldDrawCompatHalo()) {
      int centerX = (int) (trackSidePadding + normalizeValue(values.get(focusedThumbIdx)) * width);
      if (VERSION.SDK_INT < VERSION_CODES.P) {
        // In this case we can clip the rect to allow drawing outside the bounds.
        canvas.clipRect(
            centerX - haloRadius,
            top - haloRadius,
            centerX + haloRadius,
            top + haloRadius,
            Op.UNION);
      }
      canvas.drawCircle(centerX, top, haloRadius, haloPaint);
    }
  }

  private boolean shouldDrawCompatHalo() {
    return forceDrawCompatHalo
        || VERSION.SDK_INT < VERSION_CODES.LOLLIPOP
        || !(getBackground() instanceof RippleDrawable);
  }

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (!isEnabled()) {
      return false;
    }
    float x = event.getX();
    touchPosition = (x - trackSidePadding) / trackWidth;
    touchPosition = Math.max(0, touchPosition);
    touchPosition = Math.min(1, touchPosition);

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        touchDownX = x;

        // If we're inside a vertical scrolling container,
        // we should start dragging in ACTION_MOVE
        if (isInVerticalScrollingContainer()) {
          break;
        }

        getParent().requestDisallowInterceptTouchEvent(true);

        if (!pickActiveThumb()) {
          // Couldn't determine the active thumb yet.
          break;
        }

        requestFocus();
        thumbIsPressed = true;
        snapTouchPosition();
        updateHaloHotspot();
        invalidate();
        onStartTrackingTouch();
        break;
      case MotionEvent.ACTION_MOVE:
        if (!thumbIsPressed) {
          // Check if we're trying to scroll vertically instead of dragging this Slider
          if (isInVerticalScrollingContainer() && abs(x - touchDownX) < scaledTouchSlop) {
            return false;
          }
          getParent().requestDisallowInterceptTouchEvent(true);
          onStartTrackingTouch();
        }

        if (!pickActiveThumb()) {
          // Couldn't determine the active thumb yet.
          break;
        }

        thumbIsPressed = true;
        snapTouchPosition();
        updateHaloHotspot();
        invalidate();
        break;
      case MotionEvent.ACTION_UP:
        thumbIsPressed = false;
        // We need to handle a tap if the last event was down at the same point.
        if (lastEvent != null
            && lastEvent.getActionMasked() == MotionEvent.ACTION_DOWN
            && abs(lastEvent.getX() - event.getX()) <= scaledTouchSlop
            && abs(lastEvent.getY() - event.getY()) <= scaledTouchSlop) {
          if (pickActiveThumb()) {
            onStartTrackingTouch();
          }
        }

        if (activeThumbIdx != -1) {
          snapTouchPosition();
          activeThumbIdx = -1;
          onStopTrackingTouch();
        }
        ensureLabelsRemoved();
        invalidate();
        break;
      default:
        // Nothing to do in this case.
    }

    // Set if the thumb is pressed. This will cause the ripple to be drawn.
    setPressed(thumbIsPressed);

    lastEvent = MotionEvent.obtain(event);
    return true;
  }

  /**
   * Calculates the index the closest tick coordinates that the thumb should snap to.
   *
   * @param coordinates Tick coordinates defined in {@code #setTicksCoordinates()}.
   * @param position Actual thumb position.
   * @return Index of the closest tick coordinate.
   */
  private static int pivotIndex(float[] coordinates, float position) {
    return Math.round(position * (coordinates.length / 2 - 1));
  }

  private double snapPosition(float position) {
    if (stepSize > 0.0f) {
      int stepCount = (int) ((valueTo - valueFrom) / stepSize);
      return Math.round(position * stepCount) / (double) stepCount;
    }

    return position;
  }

  /**
   * Tries to pick the active thumb if one hasn't already been set. This will pick the closest thumb
   * if there is only one thumb under the touch position. If there is more than one thumb under the
   * touch position, it will wait for enough drag left or right to determine which thumb to pick.
   */
  protected boolean pickActiveThumb() {
    if (activeThumbIdx != -1) {
      return true;
    }

    float touchValue = getValueOfTouchPositionAbsolute();
    float touchX = valueToX(touchValue);
    activeThumbIdx = 0;
    float activeThumbDiff = abs(values.get(activeThumbIdx) - touchValue);
    for (int i = 1; i < values.size(); i++) {
      float valueDiff = abs(values.get(i) - touchValue);
      float valueX = valueToX(values.get(i));
      if (compare(valueDiff, activeThumbDiff) > 1) {
        break;
      }

      boolean movingForward = isRtl() ? (valueX - touchX) > 0 : (valueX - touchX) < 0;
      // Keep replacing the activeThumbIdx, while the diff decreases.
      // If the diffs are equal we'll pick the thumb based on which direction we are dragging.
      if (compare(valueDiff, activeThumbDiff) < 0) {
        activeThumbDiff = valueDiff;
        activeThumbIdx = i;
        continue;
      }

      if (compare(valueDiff, activeThumbDiff) == 0) {
        // Two thumbs on the same value and we don't have enough movement to use direction yet.
        if (abs(valueX - touchX) < scaledTouchSlop) {
          activeThumbIdx = -1;
          return false;
        }

        if (movingForward) {
          activeThumbDiff = valueDiff;
          activeThumbIdx = i;
        }
      }
    }

    return activeThumbIdx != -1;
  }

  private float getValueOfTouchPositionAbsolute() {
    float position = touchPosition;
    if (isRtl()) {
      position = 1 - position;
    }
    return (position * (valueTo - valueFrom) + valueFrom);
  }

  /**
   * Snaps the thumb position to the closest tick coordinates in discrete mode, and the input
   * position in continuous mode.
   *
   * @return true, if {@code #thumbPosition is updated}; false, otherwise.
   */
  private boolean snapTouchPosition() {
    return snapActiveThumbToValue(getValueOfTouchPosition());
  }

  private boolean snapActiveThumbToValue(float value) {
    return snapThumbToValue(activeThumbIdx, value);
  }

  private boolean snapThumbToValue(int idx, float value) {
    // Check if the new value equals a value that was already set.
    if (abs(value - values.get(idx)) < THRESHOLD) {
      return false;
    }

    float newValue = getClampedValue(idx, value);
    // Replace the old value with the new value of the touch position.
    values.set(idx, newValue);
    focusedThumbIdx = idx;

    dispatchOnChangedFromUser(idx);
    return true;
  }

  /** Thumbs cannot cross each other, clamp the value to a bound or the value next to it. */
  private float getClampedValue(int idx, float value) {
    float minSeparation = stepSize == 0 ? getMinSeparation() : 0;
    minSeparation = separationUnit == UNIT_PX ? dimenToValue(minSeparation) : minSeparation;
    if (isRtl()) {
      minSeparation = -minSeparation;
    }

    float upperBound = idx + 1 >= values.size() ? valueTo : values.get(idx + 1) - minSeparation;
    float lowerBound = idx - 1 < 0 ? valueFrom : values.get(idx - 1) + minSeparation;
    return clamp(value, lowerBound, upperBound);
  }

  private float dimenToValue(float dimen) {
    if (dimen == 0) {
      return 0;
    }
    return ((dimen - trackSidePadding) / trackWidth) * (valueFrom - valueTo) + valueFrom;
  }

  protected void setSeparationUnit(int separationUnit) {
    this.separationUnit = separationUnit;
  }

  protected float getMinSeparation() {
    return 0;
  }

  private float getValueOfTouchPosition() {
    double position = snapPosition(touchPosition);

    // We might need to invert the touch position to get the correct value.
    if (isRtl()) {
      position = 1 - position;
    }
    return (float) (position * (valueTo - valueFrom) + valueFrom);
  }

  private float valueToX(float value) {
    return normalizeValue(value) * trackWidth + trackSidePadding;
  }

  /**
   * A helper method to get the current animated value of a {@link ValueAnimator}. If the target
   * animator is null or not running, return the default value provided.
   */
  private static float getAnimatorCurrentValueOrDefault(
      ValueAnimator animator, float defaultValue) {
    // If the in animation is interrupting the out animation, attempt to smoothly interrupt by
    // getting the current value of the out animator.
    if (animator != null && animator.isRunning()) {
      float value = (float) animator.getAnimatedValue();
      animator.cancel();
      return value;
    }

    return defaultValue;
  }

  /**
   * Create an animator that shows or hides all slider labels.
   *
   * @param enter True if this animator should show (reveal) labels. False if this animator should
   *     hide labels.
   * @return A value animator that, when run, will animate all labels in or out using {@link
   *     TooltipDrawable#setRevealFraction(float)}.
   */
  private ValueAnimator createLabelAnimator(boolean enter) {
    float startFraction = enter ? 0F : 1F;
    // Update the start fraction to the current animated value of the label, if any.
    startFraction =
        getAnimatorCurrentValueOrDefault(
            enter ? labelsOutAnimator : labelsInAnimator, startFraction);
    float endFraction = enter ? 1F : 0F;
    ValueAnimator animator = ValueAnimator.ofFloat(startFraction, endFraction);
    animator.setDuration(enter ? LABEL_ANIMATION_ENTER_DURATION : LABEL_ANIMATION_EXIT_DURATION);
    animator.setInterpolator(
        enter
            ? AnimationUtils.DECELERATE_INTERPOLATOR
            : AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
    animator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator animation) {
            float fraction = (float) animation.getAnimatedValue();
            for (TooltipDrawable label : labels) {
              label.setRevealFraction(fraction);
            }
            // Ensure the labels are redrawn even if the slider has stopped moving
            ViewCompat.postInvalidateOnAnimation(BaseSlider.this);
          }
        });
    return animator;
  }

  private void ensureLabelsRemoved() {
    // If the labels are animated in or in the process of animating in, create and start a new
    // animator to animate out the labels and remove them once the animation ends.
    if (labelsAreAnimatedIn) {
      labelsAreAnimatedIn = false;
      labelsOutAnimator = createLabelAnimator(false);
      labelsInAnimator = null;
      labelsOutAnimator.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              for (TooltipDrawable label : labels) {
                ViewUtils.getContentViewOverlay(BaseSlider.this).remove(label);
              }
            }
          });
      labelsOutAnimator.start();
    }
  }

  private void ensureLabelsAdded() {
    if (labelBehavior == LABEL_GONE) {
      // If the label shouldn't be drawn we can skip this.
      return;
    }

    // If the labels are not animating in, start an animator to show them. ensureLabelsAdded will
    // be called multiple times by BaseSlider's draw method, making this check necessary to avoid
    // creating and starting an animator for each draw call.
    if (!labelsAreAnimatedIn) {
      labelsAreAnimatedIn = true;
      labelsInAnimator = createLabelAnimator(true);
      labelsOutAnimator = null;
      labelsInAnimator.start();
    }

    Iterator<TooltipDrawable> labelItr = labels.iterator();

    for (int i = 0; i < values.size() && labelItr.hasNext(); i++) {
      if (i == focusedThumbIdx) {
        // We position the focused thumb last so it's displayed on top, so skip it for now.
        continue;
      }

      setValueForLabel(labelItr.next(), values.get(i));
    }

    if (!labelItr.hasNext()) {
      throw new IllegalStateException(
          String.format(
              "Not enough labels(%d) to display all the values(%d)", labels.size(), values.size()));
    }

    // Now set the label for the focused thumb so it's on top.
    setValueForLabel(labelItr.next(), values.get(focusedThumbIdx));
  }

  private String formatValue(float value) {
    if (hasLabelFormatter()) {
      return formatter.getFormattedValue(value);
    }

    return String.format((int) value == value ? "%.0f" : "%.2f", value);
  }

  private void setValueForLabel(TooltipDrawable label, float value) {
    label.setText(formatValue(value));

    int left =
        trackSidePadding
            + (int) (normalizeValue(value) * trackWidth)
            - label.getIntrinsicWidth() / 2;
    int top = calculateTop() - (labelPadding + thumbRadius);
    label.setBounds(left, top - label.getIntrinsicHeight(), left + label.getIntrinsicWidth(), top);

    // Calculate the difference between the bounds of this view and the bounds of the root view to
    // correctly position this view in the overlay layer.
    Rect rect = new Rect(label.getBounds());
    DescendantOffsetUtils.offsetDescendantRect(ViewUtils.getContentView(this), this, rect);
    label.setBounds(rect);

    ViewUtils.getContentViewOverlay(this).add(label);
  }

  private void invalidateTrack() {
    inactiveTrackPaint.setStrokeWidth(trackHeight);
    activeTrackPaint.setStrokeWidth(trackHeight);
    inactiveTicksPaint.setStrokeWidth(trackHeight / 2.0f);
    activeTicksPaint.setStrokeWidth(trackHeight / 2.0f);
  }

  /**
   * If this returns true, we can't start dragging the Slider immediately when we receive a {@link
   * MotionEvent#ACTION_DOWN}. Instead, we must wait for a {@link MotionEvent#ACTION_MOVE}. Copied
   * and modified from hidden method of {@link View} isInScrollingContainer.
   *
   * @return true if any of this View's parents is a scrolling View and can scroll vertically.
   */
  private boolean isInVerticalScrollingContainer() {
    ViewParent p = getParent();
    while (p instanceof ViewGroup) {
      ViewGroup parent = (ViewGroup) p;
      boolean canScrollVertically = parent.canScrollVertically(1) || parent.canScrollVertically(-1);
      if (canScrollVertically && parent.shouldDelayChildPressedState()) {
        return true;
      }
      p = p.getParent();
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private void dispatchOnChangedProgramatically() {
    for (L listener : changeListeners) {
      for (Float value : values) {
        listener.onValueChange((S) this, value, false);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void dispatchOnChangedFromUser(int idx) {
    for (L listener : changeListeners) {
      listener.onValueChange((S) this, values.get(idx), true);
    }
    if (accessibilityManager != null && accessibilityManager.isEnabled()) {
      scheduleAccessibilityEventSender(idx);
    }
  }

  @SuppressWarnings("unchecked")
  private void onStartTrackingTouch() {
    for (T listener : touchListeners) {
      listener.onStartTrackingTouch((S) this);
    }
  }

  @SuppressWarnings("unchecked")
  private void onStopTrackingTouch() {
    for (T listener : touchListeners) {
      listener.onStopTrackingTouch((S) this);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    inactiveTrackPaint.setColor(getColorForState(trackColorInactive));
    activeTrackPaint.setColor(getColorForState(trackColorActive));
    inactiveTicksPaint.setColor(getColorForState(tickColorInactive));
    activeTicksPaint.setColor(getColorForState(tickColorActive));
    for (TooltipDrawable label : labels) {
      if (label.isStateful()) {
        label.setState(getDrawableState());
      }
    }
    if (thumbDrawable.isStateful()) {
      thumbDrawable.setState(getDrawableState());
    }
    haloPaint.setColor(getColorForState(haloColor));
    haloPaint.setAlpha(HALO_ALPHA);
  }

  @ColorInt
  private int getColorForState(@NonNull ColorStateList colorStateList) {
    return colorStateList.getColorForState(getDrawableState(), colorStateList.getDefaultColor());
  }

  @VisibleForTesting
  void forceDrawCompatHalo(boolean force) {
    forceDrawCompatHalo = force;
  }

  @Override
  public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    if (!isEnabled()) {
      return super.onKeyDown(keyCode, event);
    }

    // If there's only one thumb, we can select it right away.
    if (values.size() == 1) {
      activeThumbIdx = 0;
    }

    // If there is no active thumb, key events will be used to pick the thumb to change.
    if (activeThumbIdx == -1) {
      Boolean handled = onKeyDownNoActiveThumb(keyCode, event);
      return handled != null ? handled : super.onKeyDown(keyCode, event);
    }

    isLongPress |= event.isLongPress();
    Float increment = calculateIncrementForKey(keyCode);
    if (increment != null) {
      if (snapActiveThumbToValue(values.get(activeThumbIdx) + increment)) {
        updateHaloHotspot();
        postInvalidate();
      }
      return true;
    }
    switch (keyCode) {
      case KeyEvent.KEYCODE_TAB:
        if (event.hasNoModifiers()) {
          return moveFocus(1);
        }

        if (event.isShiftPressed()) {
          return moveFocus(-1);
        }
        return false;
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
        activeThumbIdx = -1;
        ensureLabelsRemoved();
        postInvalidate();
        return true;
      default:
        // Nothing to do in this case.
    }

    return super.onKeyDown(keyCode, event);
  }

  private Boolean onKeyDownNoActiveThumb(int keyCode, @NonNull KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_TAB:
        if (event.hasNoModifiers()) {
          return moveFocus(1);
        }

        if (event.isShiftPressed()) {
          return moveFocus(-1);
        }
        return false;
      case KeyEvent.KEYCODE_DPAD_LEFT:
        moveFocusInAbsoluteDirection(-1);
        return true;
      case KeyEvent.KEYCODE_MINUS:
        moveFocus(-1);
        return true;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        moveFocusInAbsoluteDirection(1);
        return true;
      case KeyEvent.KEYCODE_EQUALS:
        // Numpad Plus == Shift + Equals, at least in AVD, so fall through.
      case KeyEvent.KEYCODE_PLUS:
        moveFocus(1);
        return true;
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
        activeThumbIdx = focusedThumbIdx;
        postInvalidate();
        return true;
      default:
        // Nothing to do in this case.
    }

    return null;
  }

  @Override
  public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    isLongPress = false;
    return super.onKeyUp(keyCode, event);
  }

  final boolean isRtl() {
    return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  /**
   * Attempts to move focus to next or previous thumb <i>independent of layout direction</i> and
   * returns whether the focused thumb changed. If focused thumb didn't change, we're at the view
   * boundary for specified {@code direction} and focus may be moved to next or previous view
   * instead.
   *
   * @see #moveFocusInAbsoluteDirection(int)
   */
  private boolean moveFocus(int direction) {
    int oldFocusedThumbIdx = focusedThumbIdx;
    // Prevent integer overflow.
    final long newFocusedThumbIdx = (long) oldFocusedThumbIdx + direction;
    focusedThumbIdx = (int) clamp(newFocusedThumbIdx, 0, values.size() - 1);
    if (focusedThumbIdx == oldFocusedThumbIdx) {
      // Move focus to next or previous view.
      return false;
    }
    if (activeThumbIdx != -1) {
      activeThumbIdx = focusedThumbIdx;
    }
    updateHaloHotspot();
    postInvalidate();
    return true;
  }

  /**
   * Attempts to move focus to the <i>left or right</i> of currently focused thumb and returns
   * whether the focused thumb changed. If focused thumb didn't change, we're at the view boundary
   * for specified {@code direction} and focus may be moved to next or previous view instead.
   *
   * @see #moveFocus(int)
   */
  private boolean moveFocusInAbsoluteDirection(int direction) {
    if (isRtl()) {
      // Prevent integer overflow.
      direction = direction == Integer.MIN_VALUE ? Integer.MAX_VALUE : -direction;
    }
    return moveFocus(direction);
  }

  private Float calculateIncrementForKey(int keyCode) {
    // If this is a long press, increase the increment so it will only take around 20 steps.
    // Otherwise choose the smallest valid increment.
    float increment = isLongPress ? calculateStepIncrement(20) : calculateStepIncrement();
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        return isRtl() ? increment : -increment;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        return isRtl() ? -increment : increment;
      case KeyEvent.KEYCODE_MINUS:
        return -increment;
      case KeyEvent.KEYCODE_EQUALS:
        // Numpad Plus == Shift + Equals, at least in AVD, so fall through.
      case KeyEvent.KEYCODE_PLUS:
        return increment;
      default:
        return null;
    }
  }

  /** Returns a small valid step increment to use when adding an offset to an existing value */
  private float calculateStepIncrement() {
    return stepSize == 0 ? 1 : stepSize;
  }

  /**
   * Returns a valid increment based on the {@code stepSize} (if it's set) that will allow
   * approximately {@code stepFactor} steps to cover the whole range.
   */
  private float calculateStepIncrement(int stepFactor) {
    float increment = calculateStepIncrement();
    float numSteps = (valueTo - valueFrom) / increment;
    if (numSteps <= stepFactor) {
      return increment;
    }

    return Math.round((numSteps / stepFactor)) * increment;
  }

  @Override
  protected void onFocusChanged(
      boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
    super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    if (!gainFocus) {
      activeThumbIdx = -1;
      ensureLabelsRemoved();
      accessibilityHelper.clearKeyboardFocusForVirtualView(focusedThumbIdx);
    } else {
      focusThumbOnFocusGained(direction);
      accessibilityHelper.requestKeyboardFocusForVirtualView(focusedThumbIdx);
    }
  }

  private void focusThumbOnFocusGained(int direction) {
    switch (direction) {
      case FOCUS_BACKWARD:
        moveFocus(Integer.MAX_VALUE);
        break;
      case FOCUS_LEFT:
        moveFocusInAbsoluteDirection(Integer.MAX_VALUE);
        break;
      case FOCUS_FORWARD:
        moveFocus(Integer.MIN_VALUE);
        break;
      case FOCUS_RIGHT:
        moveFocusInAbsoluteDirection(Integer.MIN_VALUE);
        break;
      case FOCUS_UP:
      case FOCUS_DOWN:
      default:
        // Don't make assumptions about where exactly focus came from. Use previously focused thumb.
    }
  }

  @VisibleForTesting
  final int getAccessibilityFocusedVirtualViewId() {
    return accessibilityHelper.getAccessibilityFocusedVirtualViewId();
  }

  @NonNull
  @Override
  public CharSequence getAccessibilityClassName() {
    return SeekBar.class.getName();
  }

  @Override
  public boolean dispatchHoverEvent(@NonNull MotionEvent event) {
    return accessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
  }

  @Override
  public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
    // We explicitly don't pass the key event to the accessibilityHelper because it doesn't handle
    // focus correctly in some cases (Such as moving left after moving right a few times).
    return super.dispatchKeyEvent(event);
  }

  /**
   * Schedule a command for sending an accessibility event. </br> Note: A command is used to ensure
   * that accessibility events are sent at most one in a given time frame to save system resources
   * while the value changes quickly.
   */
  private void scheduleAccessibilityEventSender(int idx) {
    if (accessibilityEventSender == null) {
      accessibilityEventSender = new AccessibilityEventSender();
    } else {
      removeCallbacks(accessibilityEventSender);
    }
    accessibilityEventSender.setVirtualViewId(idx);
    postDelayed(accessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
  }

  /** Command for sending an accessibility event. */
  private class AccessibilityEventSender implements Runnable {
    int virtualViewId = -1;

    void setVirtualViewId(int virtualViewId) {
      this.virtualViewId = virtualViewId;
    }

    @Override
    public void run() {
      accessibilityHelper.sendEventForVirtualView(
          virtualViewId, AccessibilityEvent.TYPE_VIEW_SELECTED);
    }
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SliderState sliderState = new SliderState(superState);
    sliderState.valueFrom = valueFrom;
    sliderState.valueTo = valueTo;
    sliderState.values = new ArrayList<>(values);
    sliderState.stepSize = stepSize;
    sliderState.hasFocus = hasFocus();
    return sliderState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    SliderState sliderState = (SliderState) state;
    super.onRestoreInstanceState(sliderState.getSuperState());

    valueFrom = sliderState.valueFrom;
    valueTo = sliderState.valueTo;
    setValuesInternal(sliderState.values);
    stepSize = sliderState.stepSize;
    if (sliderState.hasFocus) {
      requestFocus();
    }
    dispatchOnChangedProgramatically();
  }

  static class SliderState extends BaseSavedState {

    float valueFrom;
    float valueTo;
    ArrayList<Float> values;
    float stepSize;
    boolean hasFocus;

    public static final Creator<SliderState> CREATOR =
        new Creator<SliderState>() {

          @NonNull
          @Override
          public SliderState createFromParcel(@NonNull Parcel source) {
            return new SliderState(source);
          }

          @NonNull
          @Override
          public SliderState[] newArray(int size) {
            return new SliderState[size];
          }
        };

    SliderState(Parcelable superState) {
      super(superState);
    }

    private SliderState(@NonNull Parcel source) {
      super(source);
      valueFrom = source.readFloat();
      valueTo = source.readFloat();
      values = new ArrayList<>();
      source.readList(values, Float.class.getClassLoader());
      stepSize = source.readFloat();
      hasFocus = source.createBooleanArray()[0];
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeFloat(valueFrom);
      dest.writeFloat(valueTo);
      dest.writeList(values);
      dest.writeFloat(stepSize);
      boolean[] booleans = new boolean[1];
      booleans[0] = hasFocus;
      dest.writeBooleanArray(booleans);
    }
  }

  void updateBoundsForVirturalViewId(int virtualViewId, Rect virtualViewBounds) {
    int x = trackSidePadding + (int) (normalizeValue(getValues().get(virtualViewId)) * trackWidth);
    int y = calculateTop();

    virtualViewBounds.set(x - thumbRadius, y - thumbRadius, x + thumbRadius, y + thumbRadius);
  }

  private static class AccessibilityHelper extends ExploreByTouchHelper {

    private final BaseSlider<?, ?, ?> slider;
    Rect virtualViewBounds = new Rect();

    AccessibilityHelper(BaseSlider<?, ?, ?> slider) {
      super(slider);
      this.slider = slider;
    }

    @Override
    protected int getVirtualViewAt(float x, float y) {
      for (int i = 0; i < slider.getValues().size(); i++) {
        slider.updateBoundsForVirturalViewId(i, virtualViewBounds);
        if (virtualViewBounds.contains((int) x, (int) y)) {
          return i;
        }
      }

      return ExploreByTouchHelper.HOST_ID;
    }

    @Override
    protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
      for (int i = 0; i < slider.getValues().size(); i++) {
        virtualViewIds.add(i);
      }
    }

    @Override
    protected void onPopulateNodeForVirtualView(
        int virtualViewId, AccessibilityNodeInfoCompat info) {

      info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS);

      List<Float> values = slider.getValues();
      final float value = values.get(virtualViewId);
      float valueFrom = slider.getValueFrom();
      float valueTo = slider.getValueTo();

      if (slider.isEnabled()) {
        if (value > valueFrom) {
          info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
        }
        if (value < valueTo) {
          info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
        }
      }

      info.setRangeInfo(RangeInfoCompat.obtain(RANGE_TYPE_FLOAT, valueFrom, valueTo, value));
      info.setClassName(SeekBar.class.getName());
      StringBuilder contentDescription = new StringBuilder();
      // Add the content description of the slider.
      if (slider.getContentDescription() != null) {
        contentDescription.append(slider.getContentDescription()).append(",");
      }
      // Add the range to the content description.
      if (values.size() > 1) {
        contentDescription.append(startOrEndDescription(virtualViewId));
        contentDescription.append(slider.formatValue(value));
      }
      info.setContentDescription(contentDescription.toString());

      slider.updateBoundsForVirturalViewId(virtualViewId, virtualViewBounds);
      info.setBoundsInParent(virtualViewBounds);
    }

    @NonNull
    private String startOrEndDescription(int virtualViewId) {
      List<Float> values = slider.getValues();
      if (virtualViewId == values.size() - 1) {
        return slider.getContext().getString(R.string.material_slider_range_end);
      }

      if (virtualViewId == 0) {
        return slider.getContext().getString(R.string.material_slider_range_start);
      }

      return "";
    }

    @Override
    protected boolean onPerformActionForVirtualView(
        int virtualViewId, int action, Bundle arguments) {
      if (!slider.isEnabled()) {
        return false;
      }

      switch (action) {
        case android.R.id.accessibilityActionSetProgress:
          {
            if (arguments == null
                || !arguments.containsKey(
                    AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE)) {
              return false;
            }
            float value =
                arguments.getFloat(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_PROGRESS_VALUE);
            if (slider.snapThumbToValue(virtualViewId, value)) {
              slider.updateHaloHotspot();
              slider.postInvalidate();
              invalidateVirtualView(virtualViewId);
              return true;
            }
            return false;
          }
        case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD:
        case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD:
          {
            float increment = slider.calculateStepIncrement(20);
            if (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD) {
              increment = -increment;
            }

            // Swap the increment if we're in RTL.
            if (slider.isRtl()) {
              increment = -increment;
            }

            List<Float> values = slider.getValues();
            float clamped =
                clamp(
                    values.get(virtualViewId) + increment,
                    slider.getValueFrom(),
                    slider.getValueTo());
            if (slider.snapThumbToValue(virtualViewId, clamped)) {
              slider.updateHaloHotspot();
              slider.postInvalidate();
              invalidateVirtualView(virtualViewId);
              return true;
            }
            return false;
          }
        default:
          return false;
      }
    }
  }
}
