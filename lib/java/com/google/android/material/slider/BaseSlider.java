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

import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_CONTROLS;
import static android.view.accessibility.AccessibilityManager.FLAG_CONTENT_TEXT;
import static androidx.core.math.MathUtils.clamp;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.RangeInfoCompat.RANGE_TYPE_FLOAT;
import static com.google.android.material.shape.CornerFamily.ROUNDED;
import static com.google.android.material.slider.LabelFormatter.LABEL_FLOATING;
import static com.google.android.material.slider.LabelFormatter.LABEL_GONE;
import static com.google.android.material.slider.LabelFormatter.LABEL_VISIBLE;
import static com.google.android.material.slider.LabelFormatter.LABEL_WITHIN_BOUNDS;
import static com.google.android.material.slider.SliderOrientation.HORIZONTAL;
import static com.google.android.material.slider.SliderOrientation.VERTICAL;
import static com.google.android.material.slider.TickVisibilityMode.TICK_VISIBILITY_AUTO_HIDE;
import static com.google.android.material.slider.TickVisibilityMode.TICK_VISIBILITY_AUTO_LIMIT;
import static com.google.android.material.slider.TickVisibilityMode.TICK_VISIBILITY_HIDDEN;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Float.compare;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.math.MathContext.DECIMAL64;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.RangeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.tooltip.TooltipDrawable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
 *   <li>{@code thumbWidth}: The width of the slider's thumb.
 *   <li>{@code thumbHeight}: The height of the slider's thumb.
 *   <li>{@code thumbRadius}: The radius of the slider's thumb.
 *   <li>{@code thumbTrackGapSize}: The size of the gap between the thumb and the track.
 *   <li>{@code tickColorActive}: the color of the slider's tick marks for the active part of the
 *       track. Only used when the slider is in discrete mode.
 *   <li>{@code tickColorInactive}: the color of the slider's tick marks for the inactive part of
 *       the track. Only used when the slider is in discrete mode.
 *   <li>{@code tickColor}: the color of the slider's tick marks. Only used when the slider is in
 *       discrete mode. This is a short hand for setting both the {@code tickColorActive} and {@code
 *       tickColorInactive} to the same thing. This takes precedence over {@code tickColorActive}
 *       and {@code tickColorInactive}.
 *   <li>{@code tickVisible} (<b>deprecated</b>, use {@code tickVisibilityMode} instead): Whether to
 *       show the tick marks. Only used when the slider is in discrete mode.
 *   <li>{@code tickVisibilityMode}: Mode to specify the visibility of tick marks. Only used when
 *       the slider is in discrete mode.
 *   <li>{@code trackColorActive}: The color of the active part of the track.
 *   <li>{@code trackColorInactive}: The color of the inactive part of the track.
 *   <li>{@code trackColor}: The color of the whole track. This is a short hand for setting both the
 *       {@code trackColorActive} and {@code trackColorInactive} to the same thing. This takes
 *       precedence over {@code trackColorActive} and {@code trackColorInactive}.
 *   <li>{@code trackHeight}: The height of the track.
 *   <li>{@code trackCornerSize}: The corner size on the outside of the track.
 *   <li>{@code trackInsideCornerSize}: The corner size on the inside of the track (visible with
 *       gap).
 *   <li>{@code trackStopIndicatorSize}: The size of the stop indicator at the edges of the track.
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
 * @attr ref com.google.android.material.R.styleable#Slider_android_orientation
 * @attr ref com.google.android.material.R.styleable#Slider_android_stepSize
 * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
 * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
 * @attr ref com.google.android.material.R.styleable#Slider_haloColor
 * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
 * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
 * @attr ref com.google.android.material.R.styleable#Slider_labelStyle
 * @attr ref com.google.android.material.R.styleable#Slider_thumbColor
 * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
 * @attr ref com.google.android.material.R.styleable#Slider_thumbWidth
 * @attr ref com.google.android.material.R.styleable#Slider_thumbHeight
 * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
 * @attr ref com.google.android.material.R.styleable#Slider_thumbTrackGapSize
 * @attr ref com.google.android.material.R.styleable#Slider_tickColor
 * @attr ref com.google.android.material.R.styleable#Slider_tickColorActive
 * @attr ref com.google.android.material.R.styleable#Slider_tickColorInactive
 * @attr ref com.google.android.material.R.styleable#Slider_tickVisible
 * @attr ref com.google.android.material.R.styleable#Slider_tickVisibilityMode
 * @attr ref com.google.android.material.R.styleable#Slider_trackColor
 * @attr ref com.google.android.material.R.styleable#Slider_trackColorActive
 * @attr ref com.google.android.material.R.styleable#Slider_trackColorInactive
 * @attr ref com.google.android.material.R.styleable#Slider_trackHeight
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveStart
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveEnd
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveColor
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveStart
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveEnd
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveColor
 * @attr ref com.google.android.material.R.styleable#Slider_trackIconSize
 * @attr ref com.google.android.material.R.styleable#Slider_trackCornerSize
 * @attr ref com.google.android.material.R.styleable#Slider_trackInsideCornerSize
 * @attr ref com.google.android.material.R.styleable#Slider_trackStopIndicatorSize
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
  private static final String EXCEPTION_ILLEGAL_STEP_SIZE =
      "The stepSize(%s) must be 0, or a factor of the valueFrom(%s)-valueTo(%s) range";
  private static final String EXCEPTION_ILLEGAL_MIN_SEPARATION =
      "minSeparation(%s) must be greater or equal to 0";
  private static final String EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE_UNIT =
      "minSeparation(%s) cannot be set as a dimension when using stepSize(%s)";
  private static final String EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE =
      "minSeparation(%s) must be greater or equal and a multiple of stepSize(%s) when using"
          + " stepSize(%s)";
  private static final String EXCEPTION_ILLEGAL_CONTINUOUS_MODE_TICK_COUNT =
      "The continuousModeTickCount(%s) must be greater than or equal to 0";
  private static final String WARNING_FLOATING_POINT_ERROR =
      "Floating point value used for %s(%s). Using floats can have rounding errors which may"
          + " result in incorrect values. Instead, consider using integers with a custom"
          + " LabelFormatter to display the value correctly.";
  private static final String WARNING_PARSE_ERROR =
      "Error parsing value(%s), valueFrom(%s), and valueTo(%s) into a float.";

  private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 200;
  private static final int MIN_TIMEOUT_TOOLTIP_WITH_ACCESSIBILITY = 10000;
  private static final int MAX_TIMEOUT_TOOLTIP_WITH_ACCESSIBILITY = 120000;
  private static final int HALO_ALPHA = 63;
  private static final double THRESHOLD = .0001;
  private static final float THUMB_WIDTH_PRESSED_RATIO = .5f;
  private static final int TRACK_CORNER_SIZE_UNSET = -1;
  private static final float TOUCH_SLOP_RATIO = .8f;

  static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Slider;
  static final int UNIT_VALUE = 1;
  static final int UNIT_PX = 0;

  private static final int DEFAULT_LABEL_ANIMATION_ENTER_DURATION = 83;
  private static final int DEFAULT_LABEL_ANIMATION_EXIT_DURATION = 117;
  private static final int LABEL_ANIMATION_ENTER_DURATION_ATTR = R.attr.motionDurationMedium4;
  private static final int LABEL_ANIMATION_EXIT_DURATION_ATTR = R.attr.motionDurationShort3;
  private static final int LABEL_ANIMATION_ENTER_EASING_ATTR =
      R.attr.motionEasingEmphasizedInterpolator;
  private static final int LABEL_ANIMATION_EXIT_EASING_ATTR =
      R.attr.motionEasingEmphasizedAccelerateInterpolator;

  private static final float TOP_LABEL_PIVOT_X = 0.5f;
  private static final float TOP_LABEL_PIVOT_Y = 1.2f;

  private static final float LEFT_LABEL_PIVOT_X = 1.2f;
  private static final float LEFT_LABEL_PIVOT_Y = 0.5f;

  private static final float RIGHT_LABEL_PIVOT_X = -0.2f;
  private static final float RIGHT_LABEL_PIVOT_Y = 0.5f;

  @NonNull private final Paint inactiveTrackPaint;
  @NonNull private final Paint activeTrackPaint;
  @NonNull private final Paint thumbPaint;
  @NonNull private final Paint haloPaint;
  @NonNull private final Paint inactiveTicksPaint;
  @NonNull private final Paint activeTicksPaint;
  @NonNull private final Paint stopIndicatorPaint;
  @NonNull private final AccessibilityHelper accessibilityHelper;
  private final AccessibilityManager accessibilityManager;
  private AccessibilityEventSender accessibilityEventSender;

  private int labelStyle;
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
  private int defaultTrackThickness;
  private int defaultTickActiveRadius;
  private int defaultTickInactiveRadius;
  private int minTickSpacing;

  @Px private int minTouchTargetSize;

  @Orientation private int widgetOrientation;
  private int minWidgetThickness;
  private int widgetThickness;
  private int labelBehavior;
  private int trackThickness;
  private int trackSidePadding;
  private int thumbWidth;
  private int thumbHeight;
  private int haloRadius;
  private int thumbTrackGapSize;
  private int defaultThumbWidth = -1;
  private int defaultThumbTrackGapSize = -1;
  private int trackStopIndicatorSize;
  private int trackCornerSize;
  private int trackInsideCornerSize;
  private boolean centered = false;
  @Nullable private Drawable trackIconActiveStart;
  private boolean trackIconActiveStartMutated = false;
  @Nullable private Drawable trackIconActiveEnd;
  private boolean trackIconActiveEndMutated = false;
  @Nullable private ColorStateList trackIconActiveColor;
  @Nullable private Drawable trackIconInactiveStart;
  private boolean trackIconInactiveStartMutated = false;
  @Nullable private Drawable trackIconInactiveEnd;
  private boolean trackIconInactiveEndMutated = false;
  @Nullable private ColorStateList trackIconInactiveColor;
  @Px private int trackIconSize;
  @Px private int trackIconPadding;
  private int labelPadding;
  private float touchDownAxis1;
  private float touchDownAxis2;
  private MotionEvent lastEvent;
  @NonNull private final Rect viewRect = new Rect();
  @NonNull List<Rect> exclusionRects = new ArrayList<>();
  @NonNull private List<Float> previousDownTouchEventValues = new ArrayList<>();
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
  private int continuousModeTickCount = 0;
  private float[] ticksCoordinates;
  private int tickVisibilityMode;
  private int tickActiveRadius;
  private int tickInactiveRadius;
  private int trackWidth;
  private boolean forceDrawCompatHalo;
  private boolean isLongPress = false;
  private boolean dirtyConfig;

  @NonNull private ColorStateList haloColor;
  @NonNull private ColorStateList tickColorActive;
  @NonNull private ColorStateList tickColorInactive;
  @NonNull private ColorStateList trackColorActive;
  @NonNull private ColorStateList trackColorInactive;

  @NonNull private final Path trackPath = new Path();
  @NonNull private final RectF activeTrackRect = new RectF();
  @NonNull private final RectF inactiveTrackLeftRect = new RectF();
  @NonNull private final RectF inactiveTrackRightRect = new RectF();
  @NonNull private final RectF cornerRect = new RectF();
  @NonNull private final Rect labelRect = new Rect();
  @NonNull private final RectF iconRectF = new RectF();
  @NonNull private final Rect iconRect = new Rect();
  @NonNull private final Matrix rotationMatrix = new Matrix();
  @NonNull private final List<MaterialShapeDrawable> defaultThumbDrawables = new ArrayList<>();

  @Nullable private Drawable customThumbDrawable;
  @NonNull private List<Drawable> customThumbDrawablesForValues = Collections.emptyList();

  private float thumbElevation;
  private float thumbStrokeWidth;
  @Nullable private ColorStateList thumbStrokeColor;
  @NonNull private ColorStateList thumbTintList;

  private float touchPosition;
  @SeparationUnit private int separationUnit = UNIT_PX;

  private final int tooltipTimeoutMillis;

  @NonNull
  private final ViewTreeObserver.OnScrollChangedListener onScrollChangedListener =
      this::updateLabels;

  @NonNull
  private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = this::updateLabels;

  @NonNull
  private final Runnable resetActiveThumbIndex =
      () -> {
        setActiveThumbIndex(-1);
        invalidate();
      };

  private boolean thisAndAncestorsVisible;

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
   *   <li>{@code LABEL_VISIBLE}: The label will never be hidden.
   * </ul>
   */
  @IntDef({LABEL_FLOATING, LABEL_WITHIN_BOUNDS, LABEL_GONE, LABEL_VISIBLE})
  @Retention(RetentionPolicy.SOURCE)
  @interface LabelBehavior {}

  @IntDef({UNIT_PX, UNIT_VALUE})
  @Retention(RetentionPolicy.SOURCE)
  @interface SeparationUnit {}

  @IntDef({HORIZONTAL, VERTICAL})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Orientation {}

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

    // Initialize with just this view's visibility.
    thisAndAncestorsVisible = isShown();

    inactiveTrackPaint = new Paint();
    activeTrackPaint = new Paint();

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

    stopIndicatorPaint = new Paint();
    stopIndicatorPaint.setStyle(Style.FILL);
    stopIndicatorPaint.setStrokeCap(Cap.ROUND);

    loadResources(context.getResources());
    processAttributes(context, attrs, defStyleAttr);

    setFocusable(true);
    setClickable(true);

    scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    accessibilityHelper = new AccessibilityHelper(this);
    ViewCompat.setAccessibilityDelegate(this, accessibilityHelper);

    accessibilityManager =
        (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
      tooltipTimeoutMillis =
          accessibilityManager.getRecommendedTimeoutMillis(
              MIN_TIMEOUT_TOOLTIP_WITH_ACCESSIBILITY, FLAG_CONTENT_CONTROLS | FLAG_CONTENT_TEXT);
    } else {
      tooltipTimeoutMillis = MAX_TIMEOUT_TOOLTIP_WITH_ACCESSIBILITY;
    }
  }

  private void loadResources(@NonNull Resources resources) {
    minWidgetThickness = resources.getDimensionPixelSize(R.dimen.mtrl_slider_widget_height);

    minTrackSidePadding = resources.getDimensionPixelOffset(R.dimen.mtrl_slider_track_side_padding);
    trackSidePadding = minTrackSidePadding;

    defaultThumbRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_thumb_radius);
    defaultTrackThickness = resources.getDimensionPixelSize(R.dimen.mtrl_slider_track_height);

    defaultTickActiveRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_tick_radius);
    defaultTickInactiveRadius = resources.getDimensionPixelSize(R.dimen.mtrl_slider_tick_radius);
    minTickSpacing = resources.getDimensionPixelSize(R.dimen.mtrl_slider_tick_min_spacing);

    labelPadding = resources.getDimensionPixelSize(R.dimen.mtrl_slider_label_padding);

    trackIconPadding = resources.getDimensionPixelOffset(R.dimen.m3_slider_track_icon_padding);
  }

  private void processAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Slider, defStyleAttr, DEF_STYLE_RES);

    setOrientation(a.getInt(R.styleable.Slider_android_orientation, HORIZONTAL));

    labelStyle =
        a.getResourceId(R.styleable.Slider_labelStyle, R.style.Widget_MaterialComponents_Tooltip);

    valueFrom = a.getFloat(R.styleable.Slider_android_valueFrom, 0.0f);
    valueTo = a.getFloat(R.styleable.Slider_android_valueTo, 1.0f);
    setCentered(a.getBoolean(R.styleable.Slider_centered, false));
    stepSize = a.getFloat(R.styleable.Slider_android_stepSize, 0.0f);
    continuousModeTickCount = a.getInt(R.styleable.Slider_continuousModeTickCount, 0);

    float defaultMinTouchTargetSize =
        MaterialAttributes.resolveMinimumAccessibleTouchTarget(context);
    minTouchTargetSize =
        (int)
            Math.ceil(
                a.getDimension(R.styleable.Slider_minTouchTargetSize, defaultMinTouchTargetSize));

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
    setThumbTintList(
        thumbColor != null
            ? thumbColor
            : AppCompatResources.getColorStateList(context, R.color.material_slider_thumb_color));
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

    tickVisibilityMode =
        a.hasValue(R.styleable.Slider_tickVisibilityMode)
            ? a.getInt(R.styleable.Slider_tickVisibilityMode, -1)
            : convertToTickVisibilityMode(a.getBoolean(R.styleable.Slider_tickVisible, true));

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

    setThumbTrackGapSize(a.getDimensionPixelSize(R.styleable.Slider_thumbTrackGapSize, 0));
    setTrackStopIndicatorSize(
        a.getDimensionPixelSize(R.styleable.Slider_trackStopIndicatorSize, 0));
    setTrackCornerSize(
        a.getDimensionPixelSize(R.styleable.Slider_trackCornerSize, TRACK_CORNER_SIZE_UNSET));
    setTrackInsideCornerSize(a.getDimensionPixelSize(R.styleable.Slider_trackInsideCornerSize, 0));
    setTrackIconActiveStart(
        MaterialResources.getDrawable(context, a, R.styleable.Slider_trackIconActiveStart));
    setTrackIconActiveEnd(
        MaterialResources.getDrawable(context, a, R.styleable.Slider_trackIconActiveEnd));
    setTrackIconActiveColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Slider_trackIconActiveColor));
    setTrackIconInactiveStart(
        MaterialResources.getDrawable(context, a, R.styleable.Slider_trackIconInactiveStart));
    setTrackIconInactiveEnd(
        MaterialResources.getDrawable(context, a, R.styleable.Slider_trackIconInactiveEnd));
    setTrackIconInactiveColor(
        MaterialResources.getColorStateList(context, a, R.styleable.Slider_trackIconInactiveColor));
    setTrackIconSize(a.getDimensionPixelSize(R.styleable.Slider_trackIconSize, 0));

    int radius = a.getDimensionPixelSize(R.styleable.Slider_thumbRadius, 0);
    int thumbWidth = a.getDimensionPixelSize(R.styleable.Slider_thumbWidth, radius * 2);
    int thumbHeight = a.getDimensionPixelSize(R.styleable.Slider_thumbHeight, radius * 2);
    setThumbWidth(thumbWidth);
    setThumbHeight(thumbHeight);
    setHaloRadius(a.getDimensionPixelSize(R.styleable.Slider_haloRadius, 0));

    setThumbElevation(a.getDimension(R.styleable.Slider_thumbElevation, 0));

    setTrackHeight(a.getDimensionPixelSize(R.styleable.Slider_trackHeight, 0));

    setTickActiveRadius(
        a.getDimensionPixelSize(R.styleable.Slider_tickRadiusActive, trackStopIndicatorSize / 2));
    setTickInactiveRadius(
        a.getDimensionPixelSize(R.styleable.Slider_tickRadiusInactive, trackStopIndicatorSize / 2));

    setLabelBehavior(a.getInt(R.styleable.Slider_labelBehavior, LABEL_FLOATING));

    if (!a.getBoolean(R.styleable.Slider_android_enabled, true)) {
      setEnabled(false);
    }

    setValues(valueFrom);

    a.recycle();
  }

  private boolean maybeIncreaseTrackSidePadding() {
    int increasedSidePaddingByThumb = max(thumbWidth / 2 - defaultThumbRadius, 0);
    int increasedSidePaddingByTrack = max((trackThickness - defaultTrackThickness) / 2, 0);
    int increasedSidePaddingByActiveTick = max(tickActiveRadius - defaultTickActiveRadius, 0);
    int increasedSidePaddingByInactiveTick = max(tickInactiveRadius - defaultTickInactiveRadius, 0);
    int newTrackSidePadding =
        minTrackSidePadding
            + max(
                max(increasedSidePaddingByThumb, increasedSidePaddingByTrack),
                max(increasedSidePaddingByActiveTick, increasedSidePaddingByInactiveTick));

    if (trackSidePadding == newTrackSidePadding) {
      return false;
    }
    trackSidePadding = newTrackSidePadding;
    if (isLaidOut()) {
      updateTrackWidth(isVertical() ? getHeight() : getWidth());
    }
    return true;
  }

  private boolean valueLandsOnTick(float value) {
    // Check that the value is a multiple of stepSize given the offset of valueFrom.
    double result =
        new BigDecimal(Float.toString(value))
            .subtract(new BigDecimal(Float.toString(valueFrom)), DECIMAL64)
            .doubleValue();
    return isMultipleOfStepSize(result);
  }

  private boolean isMultipleOfStepSize(double value) {
    // We're using BigDecimal here to avoid floating point rounding errors.
    double result =
        new BigDecimal(Double.toString(value))
            .divide(new BigDecimal(Float.toString(stepSize)), DECIMAL64)
            .doubleValue();

    // If the result is a whole number, it means the value is a multiple of stepSize.
    return Math.abs(Math.round(result) - result) < THRESHOLD;
  }

  private void validateStepSize() {
    if (stepSize > 0.0f && !valueLandsOnTick(valueTo)) {
      throw new IllegalStateException(
          String.format(EXCEPTION_ILLEGAL_STEP_SIZE, stepSize, valueFrom, valueTo));
    }
  }

  private void validateValues() {
    if (valueFrom >= valueTo) {
      throw new IllegalStateException(
          String.format(EXCEPTION_ILLEGAL_VALUE_FROM, valueFrom, valueTo));
    }

    for (Float value : values) {
      if (value < valueFrom || value > valueTo) {
        throw new IllegalStateException(
            String.format(EXCEPTION_ILLEGAL_VALUE, value, valueFrom, valueTo));
      }
      if (stepSize > 0.0f && !valueLandsOnTick(value)) {
        throw new IllegalStateException(
            String.format(EXCEPTION_ILLEGAL_DISCRETE_VALUE, value, valueFrom, stepSize, stepSize));
      }
    }
  }

  private void validateMinSeparation() {
    final float minSeparation = getMinSeparation();
    if (minSeparation < 0) {
      throw new IllegalStateException(
          String.format(EXCEPTION_ILLEGAL_MIN_SEPARATION, minSeparation));
    }
    if (stepSize > 0 && minSeparation > 0) {
      if (separationUnit != UNIT_VALUE) {
        throw new IllegalStateException(
            String.format(
                EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE_UNIT, minSeparation, stepSize));
      }
      if (minSeparation < stepSize || !isMultipleOfStepSize(minSeparation)) {
        throw new IllegalStateException(
            String.format(
                EXCEPTION_ILLEGAL_MIN_SEPARATION_STEP_SIZE, minSeparation, stepSize, stepSize));
      }
    }
  }

  private void warnAboutFloatingPointError() {
    if (stepSize == 0) {
      // Only warn if slider uses a step value.
      return;
    }

    if ((int) stepSize != stepSize) {
      Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERROR, "stepSize", stepSize));
    }

    if ((int) valueFrom != valueFrom) {
      Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERROR, "valueFrom", valueFrom));
    }

    if ((int) valueTo != valueTo) {
      Log.w(TAG, String.format(WARNING_FLOATING_POINT_ERROR, "valueTo", valueTo));
    }
  }

  private void validateConfigurationIfDirty() {
    if (dirtyConfig) {
      validateValues();
      validateStepSize();
      validateMinSeparation();
      warnAboutFloatingPointError();
      dirtyConfig = false;
    }
  }

  public void scheduleTooltipTimeout() {
    removeCallbacks(resetActiveThumbIndex);
    postDelayed(resetActiveThumbIndex, tooltipTimeoutMillis);
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
    updateDefaultThumbDrawables();
    // Only update the focused thumb index. The active thumb index will be updated on touch.
    focusedThumbIdx = 0;
    updateHaloHotspot();
    createLabelPool();
    dispatchOnChangedProgrammatically();
    postInvalidate();
  }

  private void updateDefaultThumbDrawables() {
    if (defaultThumbDrawables.size() != values.size()) {
     defaultThumbDrawables.clear();
     for (int i = 0; i < values.size(); i++) {
       // Create default thumbs to make sure each one is an independent drawable.
       defaultThumbDrawables.add(createNewDefaultThumb());
     }
    }
  }

  private MaterialShapeDrawable createNewDefaultThumb() {
    MaterialShapeDrawable thumb = new MaterialShapeDrawable();
    thumb.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
    thumb.setFillColor(getThumbTintList());
    thumb.setShapeAppearanceModel(
        ShapeAppearanceModel.builder().setAllCorners(ROUNDED, thumbWidth / 2f).build());
    thumb.setBounds(0, 0, thumbWidth, thumbHeight);
    thumb.setElevation(getThumbElevation());
    thumb.setStrokeWidth(getThumbStrokeWidth());
    thumb.setStrokeTint(getThumbStrokeColor());
    thumb.setState(getDrawableState());
    return thumb;
  }

  private void createLabelPool() {
    // If there are too many labels, remove the extra ones from the end.
    if (labels.size() > values.size()) {
      List<TooltipDrawable> tooltipDrawables = labels.subList(values.size(), labels.size());
      for (TooltipDrawable label : tooltipDrawables) {
        if (isAttachedToWindow()) {
          detachLabelFromContentView(label);
        }
      }
      tooltipDrawables.clear();
    }

    // If there's not enough labels, add more.
    while (labels.size() < values.size()) {
      // Because there's currently no way to copy the TooltipDrawable we use this to make more
      // if more thumbs are added.
      TooltipDrawable tooltipDrawable =
          TooltipDrawable.createFromAttributes(getContext(), null, 0, labelStyle);
      labels.add(tooltipDrawable);
      if (isAttachedToWindow()) {
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
          String.format(EXCEPTION_ILLEGAL_STEP_SIZE, stepSize, valueFrom, valueTo));
    }
    if (this.stepSize != stepSize) {
      this.stepSize = stepSize;
      dirtyConfig = true;
      postInvalidate();
    }
  }

  /**
   * Returns the tick count used in continuous mode.
   *
   * @see #setContinuousModeTickCount(int)
   * @attr ref com.google.android.material.R.styleable#Slider_continuousModeTickCount
   */
  public int getContinuousModeTickCount() {
    return continuousModeTickCount;
  }

  /**
   * Sets the number of ticks to display in continuous mode. Default is 0.
   *
   * <p>This allows for showing purely visual ticks in continuous mode.
   *
   * <p>Setting this value to a negative value will result in an {@link IllegalArgumentException}.
   *
   * @param continuousModeTickCount The number of ticks that must be drawn in continuous mode count
   * @throws IllegalArgumentException If the continuous mode tick count is less than 0
   * @see #getContinuousModeTickCount()
   * @attr ref com.google.android.material.R.styleable#Slider_continuousModeTickCount
   */
  public void setContinuousModeTickCount(int continuousModeTickCount) {
    if (continuousModeTickCount < 0) {
      throw new IllegalArgumentException(
          String.format(EXCEPTION_ILLEGAL_CONTINUOUS_MODE_TICK_COUNT, continuousModeTickCount));
    }
    if (this.continuousModeTickCount != continuousModeTickCount) {
      this.continuousModeTickCount = continuousModeTickCount;
      dirtyConfig = true;
      postInvalidate();
    }
  }

  /**
   * Sets the custom thumb drawable which will be used for all value positions. Note that the custom
   * drawable provided will be resized to match the thumb radius set by {@link #setThumbRadius(int)}
   * or {@link #setThumbRadiusResource(int)}. Be aware that the image quality may be compromised
   * during resizing.
   *
   * @see #setCustomThumbDrawable(Drawable)
   * @see #setCustomThumbDrawablesForValues(int...)
   * @see #setCustomThumbDrawablesForValues(Drawable...)
   */
  void setCustomThumbDrawable(@DrawableRes int drawableResId) {
    setCustomThumbDrawable(getResources().getDrawable(drawableResId));
  }

  /**
   * Sets the custom thumb drawable which will be used for all value positions. Note that the custom
   * drawable provided will be resized to match the thumb radius set by {@link #setThumbRadius(int)}
   * or {@link #setThumbRadiusResource(int)}. Be aware that the image quality may be compromised
   * during resizing.
   *
   * @see #setCustomThumbDrawable(int)
   * @see #setCustomThumbDrawablesForValues(int...)
   * @see #setCustomThumbDrawablesForValues(Drawable...)
   */
  void setCustomThumbDrawable(@NonNull Drawable drawable) {
    customThumbDrawable = initializeCustomThumbDrawable(drawable);
    customThumbDrawablesForValues.clear();
    postInvalidate();
  }

  /**
   * Sets custom thumb drawables. The drawables provided will be used in its corresponding value
   * position - i.e., the first drawable will be used to indicate the first value, and so on. If the
   * number of drawables is less than the number of values, the default drawable will be used for
   * the remaining values.
   *
   * <p>Note that the custom drawables provided will be resized to match the thumb radius set by
   * {@link #setThumbRadius(int)} or {@link #setThumbRadiusResource(int)}. Be aware that the image
   * quality may be compromised during resizing.
   *
   * @see #setCustomThumbDrawablesForValues(Drawable...)
   */
  void setCustomThumbDrawablesForValues(@NonNull @DrawableRes int... customThumbDrawableResIds) {
    Drawable[] customThumbDrawables = new Drawable[customThumbDrawableResIds.length];
    for (int i = 0; i < customThumbDrawableResIds.length; i++) {
      customThumbDrawables[i] = getResources().getDrawable(customThumbDrawableResIds[i]);
    }
    setCustomThumbDrawablesForValues(customThumbDrawables);
  }

  /**
   * Sets custom thumb drawables. The drawables provided will be used in its corresponding value
   * position - i.e., the first drawable will be used to indicate the first value, and so on. If the
   * number of drawables is less than the number of values, the default drawable will be used for
   * the remaining values.
   *
   * <p>Note that the custom drawables provided will be resized to match the thumb radius set by
   * {@link #setThumbRadius(int)} or {@link #setThumbRadiusResource(int)}. Be aware that the image
   * quality may be compromised during resizing.
   *
   * @see #setCustomThumbDrawablesForValues(int...)
   */
  void setCustomThumbDrawablesForValues(@NonNull Drawable... customThumbDrawables) {
    this.customThumbDrawable = null;
    this.customThumbDrawablesForValues = new ArrayList<>();
    for (Drawable originalDrawable : customThumbDrawables) {
      this.customThumbDrawablesForValues.add(initializeCustomThumbDrawable(originalDrawable));
    }
    postInvalidate();
  }

  private Drawable initializeCustomThumbDrawable(Drawable originalDrawable) {
    Drawable drawable = originalDrawable.mutate().getConstantState().newDrawable();
    adjustCustomThumbDrawableBounds(drawable);
    return drawable;
  }

  private void adjustCustomThumbDrawableBounds(Drawable drawable) {
    adjustCustomThumbDrawableBounds(thumbWidth, drawable);
  }

  private void adjustCustomThumbDrawableBounds(
      @IntRange(from = 0) @Px int width, Drawable drawable) {
    int originalWidth = drawable.getIntrinsicWidth();
    int originalHeight = drawable.getIntrinsicHeight();
    if (originalWidth == -1 && originalHeight == -1) {
      drawable.setBounds(0, 0, width, thumbHeight);
    } else {
      float scaleRatio = (float) max(width, thumbHeight) / max(originalWidth, originalHeight);
      drawable.setBounds(
          0, 0, (int) (originalWidth * scaleRatio), (int) (originalHeight * scaleRatio));
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
   * Registers a callback to be invoked when the slider changes. On the RangeSlider implementation,
   * the listener is invoked once for each value.
   *
   * @param listener The callback to run when the slider changes
   */
  public void addOnChangeListener(@NonNull L listener) {
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
    return thumbElevation;
  }

  /**
   * Sets the elevation of the thumb.
   *
   * @see #getThumbElevation()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
   */
  public void setThumbElevation(float elevation) {
    if (elevation == thumbElevation) {
      return;
    }
    thumbElevation = elevation;
    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      defaultThumbDrawables.get(i).setElevation(thumbElevation);
    }
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
   * Returns the radius of the thumb. Note that setting this will also affect custom drawables set
   * through {@link #setCustomThumbDrawable(int)}, {@link #setCustomThumbDrawable(Drawable)}, {@link
   * #setCustomThumbDrawablesForValues(int...)}, and {@link
   * #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #setThumbRadius(int)
   * @see #setThumbRadiusResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
   */
  @Px
  public int getThumbRadius() {
    return thumbWidth / 2;
  }

  /**
   * Sets the radius of the thumb in pixels. Note that setting this will also affect custom
   * drawables set through {@link #setCustomThumbDrawable(int)}, {@link
   * #setCustomThumbDrawable(Drawable)}, {@link #setCustomThumbDrawablesForValues(int...)}, and
   * {@link #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #getThumbRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
   */
  public void setThumbRadius(@IntRange(from = 0) @Px int radius) {
    setThumbWidth(radius * 2);
    setThumbHeight(radius * 2);
  }

  /**
   * Sets the radius of the thumb from a dimension resource. Note that setting this will also affect
   * custom drawables set through {@link #setCustomThumbDrawable(int)}, {@link
   * #setCustomThumbDrawable(Drawable)}, {@link #setCustomThumbDrawablesForValues(int...)}, and
   * {@link #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #getThumbRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
   */
  public void setThumbRadiusResource(@DimenRes int radius) {
    setThumbRadius(getResources().getDimensionPixelSize(radius));
  }

  /**
   * Returns the width of the thumb. Note that setting this will also affect custom drawables set
   * through {@link #setCustomThumbDrawable(int)}, {@link #setCustomThumbDrawable(Drawable)}, {@link
   * #setCustomThumbDrawablesForValues(int...)}, and {@link
   * #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #setThumbWidth(int)
   * @see #setThumbWidthResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbWidth
   */
  @Px
  public int getThumbWidth() {
    return thumbWidth;
  }

  /**
   * Sets the width of the thumb in pixels. Note that setting this will also affect custom drawables
   * set through {@link #setCustomThumbDrawable(int)}, {@link #setCustomThumbDrawable(Drawable)},
   * {@link #setCustomThumbDrawablesForValues(int...)}, and {@link
   * #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #getThumbWidth()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbWidth
   */
  public void setThumbWidth(@IntRange(from = 0) @Px int width) {
    if (width == thumbWidth) {
      return;
    }

    thumbWidth = width;
    // Update custom thumbs, if any.
    if (customThumbDrawable != null) {
      adjustCustomThumbDrawableBounds(width, customThumbDrawable);
    }
    for (int i = 0; i < customThumbDrawablesForValues.size(); i++) {
      adjustCustomThumbDrawableBounds(width, customThumbDrawablesForValues.get(i));
    }
    // Update default thumb(s).
    setThumbWidth(width, /* thumbIndex= */ null);
  }

  private void setThumbWidth(@IntRange(from = 0) @Px int width, @Nullable Integer thumbIndex) {
    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      if (thumbIndex == null || i == thumbIndex) {
        defaultThumbDrawables
            .get(i)
            .setShapeAppearanceModel(
                ShapeAppearanceModel.builder().setAllCorners(ROUNDED, width / 2f).build());
        defaultThumbDrawables.get(i).setBounds(0, 0, width, thumbHeight);
      }
    }

    updateWidgetLayout(false);
  }

  /**
   * Sets the width of the thumb from a dimension resource. Note that setting this will also affect
   * custom drawables set through {@link #setCustomThumbDrawable(int)}, {@link
   * #setCustomThumbDrawable(Drawable)}, {@link #setCustomThumbDrawablesForValues(int...)}, and
   * {@link #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #getThumbWidth()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbWidth
   */
  public void setThumbWidthResource(@DimenRes int width) {
    setThumbWidth(getResources().getDimensionPixelSize(width));
  }

  /**
   * Returns the height of the thumb. Note that setting this will also affect custom drawables set
   * through {@link #setCustomThumbDrawable(int)}, {@link #setCustomThumbDrawable(Drawable)}, {@link
   * #setCustomThumbDrawablesForValues(int...)}, and {@link
   * #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #setThumbHeight(int)
   * @see #setThumbHeightResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbHeight
   */
  @Px
  public int getThumbHeight() {
    return thumbHeight;
  }

  /**
   * Sets the height of the thumb in pixels. Note that setting this will also affect custom
   * drawables set through {@link #setCustomThumbDrawable(int)}, {@link
   * #setCustomThumbDrawable(Drawable)}, {@link #setCustomThumbDrawablesForValues(int...)}, and
   * {@link #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #getThumbHeight()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbHeight
   */
  public void setThumbHeight(@IntRange(from = 0) @Px int height) {
    if (height == thumbHeight) {
      return;
    }

    thumbHeight = height;

    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      defaultThumbDrawables.get(i).setBounds(0, 0, thumbWidth, thumbHeight);
    }

    if (customThumbDrawable != null) {
      adjustCustomThumbDrawableBounds(customThumbDrawable);
    }
    for (Drawable customDrawable : customThumbDrawablesForValues) {
      adjustCustomThumbDrawableBounds(customDrawable);
    }

    updateWidgetLayout(false);
  }

  /**
   * Sets the height of the thumb from a dimension resource. Note that setting this will also affect
   * custom drawables set through {@link #setCustomThumbDrawable(int)}, {@link
   * #setCustomThumbDrawable(Drawable)}, {@link #setCustomThumbDrawablesForValues(int...)}, and
   * {@link #setCustomThumbDrawablesForValues(Drawable...)}.
   *
   * @see #getThumbHeight()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbHeight
   */
  public void setThumbHeightResource(@DimenRes int height) {
    setThumbHeight(getResources().getDimensionPixelSize(height));
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
    if (thumbStrokeColor == this.thumbStrokeColor) {
      return;
    }

    this.thumbStrokeColor = thumbStrokeColor;
    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      defaultThumbDrawables.get(i).setStrokeColor(thumbStrokeColor);
    }

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
  @Nullable
  public ColorStateList getThumbStrokeColor() {
    return thumbStrokeColor;
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
    if (thumbStrokeWidth == this.thumbStrokeWidth) {
      return;
    }

    this.thumbStrokeWidth = thumbStrokeWidth;
    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      defaultThumbDrawables.get(i).setStrokeWidth(thumbStrokeWidth);
    }

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
    return thumbStrokeWidth;
  }

  /**
   * Returns the radius of the halo.
   *
   * @see #setHaloRadius(int)
   * @see #setHaloRadiusResource(int)
   * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
   */
  @Px
  public int getHaloRadius() {
    return haloRadius;
  }

  /**
   * Sets the radius of the halo in pixels.
   *
   * @see #getHaloRadius()
   * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
   */
  public void setHaloRadius(@IntRange(from = 0) @Px int radius) {
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
      updateWidgetLayout(true);
    }
  }

  /**
   * Returns whether the labels should be always shown based on the {@link LabelBehavior}.
   *
   * @see LabelBehavior
   * @attr ref com.google.android.material.R.styleable#Slider_labelBehavior
   */
  private boolean shouldAlwaysShowLabel() {
    return this.labelBehavior == LABEL_VISIBLE;
  }

  /** Returns the side padding of the track. */
  @Px
  public int getTrackSidePadding() {
    return trackSidePadding;
  }

  /** Returns the width of the track in pixels. */
  @Px
  public int getTrackWidth() {
    return trackWidth;
  }

  /**
   * Returns the height of the track in pixels.
   *
   * @see #setTrackHeight(int)
   * @attr ref com.google.android.material.R.styleable#Slider_trackHeight
   */
  @Px
  public int getTrackHeight() {
    return trackThickness;
  }

  /**
   * Set the height of the track in pixels.
   *
   * @see #getTrackHeight()
   * @attr ref com.google.android.material.R.styleable#Slider_trackHeight
   */
  public void setTrackHeight(@IntRange(from = 0) @Px int trackHeight) {
    if (this.trackThickness != trackHeight) {
      this.trackThickness = trackHeight;
      invalidateTrack();
      updateWidgetLayout(false);
    }
  }

  /**
   * Returns the radius of the active tick in pixels.
   *
   * @attr ref com.google.android.material.R.styleable#Slider_activeTickRadius
   * @see #setTickActiveRadius(int)
   */
  @Px
  public int getTickActiveRadius() {
    return tickActiveRadius;
  }

  /**
   * Set the radius of the active tick in pixels.
   *
   * @attr ref com.google.android.material.R.styleable#Slider_activeTickRadius
   * @see #getTickActiveRadius()
   */
  public void setTickActiveRadius(@IntRange(from = 0) @Px int tickActiveRadius) {
    if (this.tickActiveRadius != tickActiveRadius) {
      this.tickActiveRadius = tickActiveRadius;
      activeTicksPaint.setStrokeWidth(tickActiveRadius * 2);
      updateWidgetLayout(false);
    }
  }

  /**
   * Returns the radius of the inactive tick in pixels.
   *
   * @attr ref com.google.android.material.R.styleable#Slider_inactiveTickRadius
   * @see #setTickInactiveRadius(int)
   */
  @Px
  public int getTickInactiveRadius() {
    return tickInactiveRadius;
  }

  /**
   * Set the radius of the inactive tick in pixels.
   *
   * @attr ref com.google.android.material.R.styleable#Slider_inactiveTickRadius
   * @see #getTickInactiveRadius()
   */
  public void setTickInactiveRadius(@IntRange(from = 0) @Px int tickInactiveRadius) {
    if (this.tickInactiveRadius != tickInactiveRadius) {
      this.tickInactiveRadius = tickInactiveRadius;
      inactiveTicksPaint.setStrokeWidth(tickInactiveRadius * 2);
      updateWidgetLayout(false);
    }
  }

  private void updateWidgetLayout(boolean forceRefresh) {
    boolean sizeChanged = maybeIncreaseWidgetThickness();
    boolean sidePaddingChanged = maybeIncreaseTrackSidePadding();
    if (isVertical()) {
      updateRotationMatrix();
    }
    if (sizeChanged || forceRefresh) {
      requestLayout();
    } else if (sidePaddingChanged) {
      postInvalidate();
    }
  }

  private boolean maybeIncreaseWidgetThickness() {
    int paddings;
    if (isVertical()) {
      paddings = getPaddingLeft() + getPaddingRight();
    } else {
      paddings = getPaddingTop() + getPaddingBottom();
    }
    int minHeightRequiredByTrack = trackThickness + paddings;
    int minHeightRequiredByThumb = thumbHeight + paddings;

    int newWidgetHeight =
        max(minWidgetThickness, max(minHeightRequiredByTrack, minHeightRequiredByThumb));
    if (newWidgetHeight == widgetThickness) {
      return false;
    }
    widgetThickness = newWidgetHeight;
    return true;
  }

  private void updateRotationMatrix() {
    float pivot = calculateTrackCenter();
    rotationMatrix.reset();
    rotationMatrix.setRotate(90, pivot, pivot);
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
    return thumbTintList;
  }

  /**
   * Sets the color of the thumb.
   *
   * @see #getThumbTintList()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbColor
   */
  public void setThumbTintList(@NonNull ColorStateList thumbColor) {
    if (thumbColor.equals(thumbTintList)) {
      return;
    }

    thumbTintList = thumbColor;
    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      defaultThumbDrawables.get(i).setFillColor(thumbTintList);
    }

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
   * @attr ref com.google.android.material.R.styleable#Slider_tickVisible
   */
  public boolean isTickVisible() {
    switch (tickVisibilityMode) {
      case TICK_VISIBILITY_AUTO_LIMIT:
        return true;
      case TICK_VISIBILITY_AUTO_HIDE:
        return getDesiredTickCount() <= getMaxTickCount();
      case TICK_VISIBILITY_HIDDEN:
        return false;
      default:
        throw new IllegalStateException("Unexpected tickVisibilityMode: " + tickVisibilityMode);
    }
  }

  /**
   * Sets whether the tick marks are visible. Only used when the slider is in discrete mode.
   *
   * @param tickVisible The visibility of tick marks.
   * @attr ref com.google.android.material.R.styleable#Slider_tickVisible
   * @deprecated Use {@link #setTickVisibilityMode(int)} instead.
   */
  @Deprecated
  public void setTickVisible(boolean tickVisible) {
    setTickVisibilityMode(convertToTickVisibilityMode(tickVisible));
  }

  @TickVisibilityMode
  private int convertToTickVisibilityMode(boolean tickVisible) {
    return tickVisible ? TICK_VISIBILITY_AUTO_LIMIT : TICK_VISIBILITY_HIDDEN;
  }

  /**
   * Returns the current tick visibility mode.
   *
   * @see #setTickVisibilityMode(int)
   * @attr ref com.google.android.material.R.styleable#Slider_tickVisibilityMode
   */
  @TickVisibilityMode
  public int getTickVisibilityMode() {
    return tickVisibilityMode;
  }

  /**
   * Sets the tick visibility mode. Only used when the slider is in discrete mode.
   *
   * @see #getTickVisibilityMode()
   * @attr ref com.google.android.material.R.styleable#Slider_tickVisibilityMode
   */
  public void setTickVisibilityMode(@TickVisibilityMode int tickVisibilityMode) {
    if (this.tickVisibilityMode != tickVisibilityMode) {
      this.tickVisibilityMode = tickVisibilityMode;
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

  /**
   * Returns the size of the gap between the thumb and the track.
   *
   * @see #setThumbTrackGapSize(int)
   * @attr ref com.google.android.material.R.styleable#Slider_thumbTrackGapSize
   */
  public int getThumbTrackGapSize() {
    return thumbTrackGapSize;
  }

  /**
   * Sets the size of the gap between the thumb and the track.
   *
   * @see #getThumbTrackGapSize()
   * @attr ref com.google.android.material.R.styleable#Slider_thumbTrackGapSize
   */
  public void setThumbTrackGapSize(@Px int thumbTrackGapSize) {
    if (this.thumbTrackGapSize == thumbTrackGapSize) {
      return;
    }
    this.thumbTrackGapSize = thumbTrackGapSize;
    invalidate();
  }

  /**
   * Returns the size of the stop indicator at the edges of the track.
   *
   * @see #setTrackStopIndicatorSize(int)
   * @attr ref com.google.android.material.R.styleable#Slider_trackStopIndicatorSize
   */
  public int getTrackStopIndicatorSize() {
    return trackStopIndicatorSize;
  }

  /**
   * Sets the size of the stop indicator at the edges of the track.
   *
   * @see #getTrackStopIndicatorSize()
   * @attr ref com.google.android.material.R.styleable#Slider_trackStopIndicatorSize
   */
  public void setTrackStopIndicatorSize(@Px int trackStopIndicatorSize) {
    if (this.trackStopIndicatorSize == trackStopIndicatorSize) {
      return;
    }
    this.trackStopIndicatorSize = trackStopIndicatorSize;
    stopIndicatorPaint.setStrokeWidth(trackStopIndicatorSize);
    invalidate();
  }

  /**
   * Returns the corner size on the outside of the track.
   *
   * @see #setTrackCornerSize(int)
   * @attr ref com.google.android.material.R.styleable#Slider_trackCornerSize
   */
  @Px
  public int getTrackCornerSize() {
    if (trackCornerSize == TRACK_CORNER_SIZE_UNSET) {
      return trackThickness / 2; // full rounded corners by default when unset
    }
    return trackCornerSize;
  }

  /**
   * Sets the corner size on the outside of the track.
   *
   * @see #getTrackCornerSize()
   * @attr ref com.google.android.material.R.styleable#Slider_trackCornerSize
   */
  public void setTrackCornerSize(@Px int cornerSize) {
    if (this.trackCornerSize == cornerSize) {
      return;
    }
    this.trackCornerSize = cornerSize;
    invalidate();
  }

  /**
   * Returns the corner size on the inside of the track (visible with gap).
   *
   * @see #setTrackInsideCornerSize(int)
   * @attr ref com.google.android.material.R.styleable#Slider_trackInsideCornerSize
   */
  public int getTrackInsideCornerSize() {
    return trackInsideCornerSize;
  }

  /**
   * Sets the corner size on the inside of the track (visible with gap).
   *
   * @see #getTrackInsideCornerSize()
   * @attr ref com.google.android.material.R.styleable#Slider_trackInsideCornerSize
   */
  public void setTrackInsideCornerSize(@Px int cornerSize) {
    if (this.trackInsideCornerSize == cornerSize) {
      return;
    }
    this.trackInsideCornerSize = cornerSize;
    invalidate();
  }

  /**
   * Sets the active track start icon.
   *
   * @param icon Drawable to use for the active track's start icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveStart
   * @see #setTrackIconActiveStart(int)
   * @see #getTrackIconActiveStart()
   */
  public void setTrackIconActiveStart(@Nullable Drawable icon) {
    if (icon == trackIconActiveStart) {
      return;
    }

    trackIconActiveStart = icon;
    trackIconActiveStartMutated = false;
    updateTrackIconActiveStart();
    invalidate();
  }

  private void updateTrackIconActiveStart() {
    if (trackIconActiveStart != null) {
      if (!trackIconActiveStartMutated && trackIconActiveColor != null) {
        trackIconActiveStart = DrawableCompat.wrap(trackIconActiveStart).mutate();
        trackIconActiveStartMutated = true;
      }

      if (trackIconActiveStartMutated) {
        trackIconActiveStart.setTintList(trackIconActiveColor);
      }
    }
  }

  /**
   * Sets the active track start icon.
   *
   * @param iconResourceId Drawable resource ID to use for the active track's start icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveStart
   * @see #setTrackIconActiveStart(Drawable)
   * @see #getTrackIconActiveStart()
   */
  public void setTrackIconActiveStart(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setTrackIconActiveStart(icon);
  }

  /**
   * Gets the active track start icon shown, if present.
   *
   * @return Start icon shown for this active track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveStart
   * @see #setTrackIconActiveStart(Drawable)
   * @see #setTrackIconActiveStart(int)
   */
  @Nullable
  public Drawable getTrackIconActiveStart() {
    return trackIconActiveStart;
  }

  /**
   * Sets the active track end icon.
   *
   * @param icon Drawable to use for the active track's end icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveEnd
   * @see #setTrackIconActiveEnd(int)
   * @see #getTrackIconActiveEnd()
   */
  public void setTrackIconActiveEnd(@Nullable Drawable icon) {
    if (icon == trackIconActiveEnd) {
      return;
    }

    trackIconActiveEnd = icon;
    trackIconActiveEndMutated = false;
    updateTrackIconActiveEnd();
    invalidate();
  }

  private void updateTrackIconActiveEnd() {
    if (trackIconActiveEnd != null) {
      if (!trackIconActiveEndMutated && trackIconActiveColor != null) {
        trackIconActiveEnd = DrawableCompat.wrap(trackIconActiveEnd).mutate();
        trackIconActiveEndMutated = true;
      }

      if (trackIconActiveEndMutated) {
        trackIconActiveEnd.setTintList(trackIconActiveColor);
      }
    }
  }

  /**
   * Sets the active track end icon.
   *
   * @param iconResourceId Drawable resource ID to use for the active track's end icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveEnd
   * @see #setTrackIconActiveEnd(Drawable)
   * @see #getTrackIconActiveEnd()
   */
  public void setTrackIconActiveEnd(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setTrackIconActiveEnd(icon);
  }

  /**
   * Gets the active track end icon shown, if present.
   *
   * @return End icon shown for this active track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveEnd
   * @see #setTrackIconActiveEnd(Drawable)
   * @see #setTrackIconActiveEnd(int)
   */
  @Nullable
  public Drawable getTrackIconActiveEnd() {
    return trackIconActiveEnd;
  }

  /**
   * Sets the track icons size.
   *
   * @param size size to use for the track icons.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconSize
   * @see #getTrackIconSize()
   */
  public void setTrackIconSize(@Px int size) {
    if (this.trackIconSize == size) {
      return;
    }
    this.trackIconSize = size;
    invalidate();
  }

  /**
   * Gets the track icons size shown, if present.
   *
   * @return Size of the icons shown for this track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconSize
   * @see #setTrackIconSize(int)
   */
  public int getTrackIconSize() {
    return trackIconSize;
  }

  /**
   * Sets the active track icon color.
   *
   * @param color color to use for the active track's icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveColor
   * @see #getTrackIconActiveColor()
   */
  public void setTrackIconActiveColor(@Nullable ColorStateList color) {
    if (color == trackIconActiveColor) {
      return;
    }

    trackIconActiveColor = color;
    updateTrackIconActiveStart();
    updateTrackIconActiveEnd();
    invalidate();
  }

  /**
   * Gets the active track icon color shown, if present.
   *
   * @return Color of the icon shown for this active track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconActiveColor
   * @see #setTrackIconActiveColor(ColorStateList)
   */
  @Nullable
  public ColorStateList getTrackIconActiveColor() {
    return trackIconActiveColor;
  }

  /**
   * Sets the inactive track start icon.
   *
   * @param icon Drawable to use for the inactive track's start icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveStart
   * @see #setTrackIconInactiveStart(int)
   * @see #getTrackIconInactiveStart()
   */
  public void setTrackIconInactiveStart(@Nullable Drawable icon) {
    if (icon == trackIconInactiveStart) {
      return;
    }

    trackIconInactiveStart = icon;
    trackIconInactiveStartMutated = false;
    updateTrackIconInactiveStart();
    invalidate();
  }

  private void updateTrackIconInactiveStart() {
    if (trackIconInactiveStart != null) {
      if (!trackIconInactiveStartMutated && trackIconInactiveColor != null) {
        trackIconInactiveStart = DrawableCompat.wrap(trackIconInactiveStart).mutate();
        trackIconInactiveStartMutated = true;
      }

      if (trackIconInactiveStartMutated) {
        trackIconInactiveStart.setTintList(trackIconInactiveColor);
      }
    }
  }

  /**
   * Sets the inactive track start icon.
   *
   * @param iconResourceId Drawable resource ID to use for the inactive track's start icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveStart
   * @see #setTrackIconInactiveStart(Drawable)
   * @see #getTrackIconInactiveStart()
   */
  public void setTrackIconInactiveStart(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setTrackIconInactiveStart(icon);
  }

  /**
   * Gets the inactive track start icon shown, if present.
   *
   * @return Start icon shown for this inactive track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveStart
   * @see #setTrackIconInactiveStart(Drawable)
   * @see #setTrackIconInactiveStart(int)
   */
  @Nullable
  public Drawable getTrackIconInactiveStart() {
    return trackIconInactiveStart;
  }

  /**
   * Sets the inactive track end icon.
   *
   * @param icon Drawable to use for the inactive track's end icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveEnd
   * @see #setTrackIconInactiveEnd(int)
   * @see #getTrackIconInactiveEnd()
   */
  public void setTrackIconInactiveEnd(@Nullable Drawable icon) {
    if (icon == trackIconInactiveEnd) {
      return;
    }

    trackIconInactiveEnd = icon;
    trackIconInactiveEndMutated = false;
    updateTrackIconInactiveEnd();
    invalidate();
  }

  private void updateTrackIconInactiveEnd() {
    if (trackIconInactiveEnd != null) {
      if (!trackIconInactiveEndMutated && trackIconInactiveColor != null) {
        trackIconInactiveEnd = DrawableCompat.wrap(trackIconInactiveEnd).mutate();
        trackIconInactiveEndMutated = true;
      }

      if (trackIconInactiveEndMutated) {
        trackIconInactiveEnd.setTintList(trackIconInactiveColor);
      }
    }
  }

  /**
   * Sets the inactive track end icon.
   *
   * @param iconResourceId Drawable resource ID to use for the inactive track's end icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveEnd
   * @see #setTrackIconInactiveEnd(Drawable)
   * @see #getTrackIconInactiveEnd()
   */
  public void setTrackIconInactiveEnd(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setTrackIconInactiveEnd(icon);
  }

  /**
   * Gets the inactive track end icon shown, if present.
   *
   * @return End icon shown for this inactive track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveEnd
   * @see #setTrackIconInactiveEnd(Drawable)
   * @see #setTrackIconInactiveEnd(int)
   */
  @Nullable
  public Drawable getTrackIconInactiveEnd() {
    return trackIconInactiveEnd;
  }

  /**
   * Sets the inactive track icon color.
   *
   * @param color color to use for the inactive track's icon.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveColor
   * @see #getTrackIconInactiveColor()
   */
  public void setTrackIconInactiveColor(@Nullable ColorStateList color) {
    if (color == trackIconInactiveColor) {
      return;
    }

    trackIconInactiveColor = color;
    updateTrackIconInactiveStart();
    updateTrackIconInactiveEnd();
    invalidate();
  }

  /**
   * Gets the inactive track icon color shown, if present.
   *
   * @return Color of the icon shown for this inactive track, if present.
   * @attr ref com.google.android.material.R.styleable#Slider_trackIconInactiveColor
   * @see #setTrackIconInactiveColor(ColorStateList)
   */
  @Nullable
  public ColorStateList getTrackIconInactiveColor() {
    return trackIconInactiveColor;
  }

  @Override
  protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    // When the visibility is set to VISIBLE, onDraw() is called again which adds or removes labels
    // according to the setting.
    if (visibility != VISIBLE) {
      final ViewOverlay contentViewOverlay = getContentViewOverlay();
      if (contentViewOverlay == null) {
        return;
      }
      for (TooltipDrawable label : labels) {
        contentViewOverlay.remove(label);
      }
    }
  }

  @Nullable
  private ViewOverlay getContentViewOverlay() {
    final View contentView = ViewUtils.getContentView(this);
    return contentView == null ? null : contentView.getOverlay();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    // When we're disabled, set the layer type to hardware so we can clear the track out from behind
    // the thumb.
    setLayerType(enabled ? LAYER_TYPE_NONE : LAYER_TYPE_HARDWARE, null);
  }

  public void setOrientation(@Orientation int orientation) {
    if (this.widgetOrientation == orientation) {
      return;
    }
    this.widgetOrientation = orientation;
    updateWidgetLayout(true);
  }

  /**
   * Sets the slider to be in centered configuration, meaning the starting value is positioned in
   * the middle of the slider.
   *
   * @param isCentered boolean to use for the slider's centered configuration.
   * @attr ref com.google.android.material.R.styleable#Slider_centered
   * @see #isCentered()
   */
  public void setCentered(boolean isCentered) {
    if (this.centered == isCentered) {
      return;
    }
    this.centered = isCentered;

    // if centered, the default value is at the center
    if (isCentered) {
      setValues((valueFrom + valueTo) / 2f);
    } else {
      setValues(valueFrom);
    }

    updateWidgetLayout(true);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    // Update factoring in the visibility of all ancestors.
    thisAndAncestorsVisible = isShown();

    getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
    getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
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
    getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
    getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    super.onDetachedFromWindow();
  }

  private void detachLabelFromContentView(TooltipDrawable label) {
    final View contentView = ViewUtils.getContentView(this);
    if (contentView == null) {
      return;
    }

    contentView.getOverlay().remove(label);
    label.detachView(contentView);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int labelSize = 0;
    if (labelBehavior == LABEL_WITHIN_BOUNDS || shouldAlwaysShowLabel()) {
      labelSize = labels.get(0).getIntrinsicHeight();
    }
    int spec = MeasureSpec.makeMeasureSpec(widgetThickness + labelSize, MeasureSpec.EXACTLY);
    if (isVertical()) {
      super.onMeasure(spec, heightMeasureSpec);
    } else {
      super.onMeasure(widthMeasureSpec, spec);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    updateTrackWidth(isVertical() ? h : w);
    updateHaloHotspot();
  }

  private void updateTicksCoordinates() {
    validateConfigurationIfDirty();

    // Continuous mode.
    if (stepSize <= 0.0f) {
      updateTicksCoordinates(continuousModeTickCount);
      return;
    }

    final int tickCount;
    switch (tickVisibilityMode) {
      case TICK_VISIBILITY_AUTO_LIMIT:
        tickCount = min(getDesiredTickCount(), getMaxTickCount());
        break;
      case TICK_VISIBILITY_AUTO_HIDE:
        int desiredTickCount = getDesiredTickCount();
        tickCount = desiredTickCount <= getMaxTickCount() ? desiredTickCount : 0;
        break;
      case TICK_VISIBILITY_HIDDEN:
        tickCount = 0;
        break;
      default:
        throw new IllegalStateException("Unexpected tickVisibilityMode: " + tickVisibilityMode);
    }

    updateTicksCoordinates(tickCount);
  }

  private void updateTicksCoordinates(int tickCount) {
    if (tickCount == 0) {
      ticksCoordinates = null;
      return;
    }

    if (ticksCoordinates == null || ticksCoordinates.length != tickCount * 2) {
      ticksCoordinates = new float[tickCount * 2];
    }

    float interval = trackWidth / (float) (tickCount - 1);
    float trackCenterY = calculateTrackCenter();

    for (int i = 0; i < tickCount * 2; i += 2) {
      ticksCoordinates[i] = trackSidePadding + i / 2f * interval;
      ticksCoordinates[i + 1] = trackCenterY;
    }

    if (isVertical()) {
      rotationMatrix.mapPoints(ticksCoordinates);
    }
  }

  private int getDesiredTickCount() {
    return (int) ((valueTo - valueFrom) / stepSize + 1);
  }

  private int getMaxTickCount() {
    return trackWidth / minTickSpacing + 1;
  }

  private void updateTrackWidth(int width) {
    // Update the visible track width.
    trackWidth = max(width - trackSidePadding * 2, 0);

    // Update the visible tick coordinates.
    updateTicksCoordinates();
  }

  private void updateHaloHotspot() {
    // Set the hotspot as the halo if RippleDrawable is being used.
    if (!shouldDrawCompatHalo() && getMeasuredWidth() > 0) {
      final Drawable background = getBackground();
      if (background instanceof RippleDrawable) {
        float x = normalizeValue(values.get(focusedThumbIdx)) * trackWidth + trackSidePadding;
        int y = calculateTrackCenter();
        float[] haloBounds = {x - haloRadius, y - haloRadius, x + haloRadius, y + haloRadius};
        if (isVertical()) {
          rotationMatrix.mapPoints(haloBounds);
        }
        background.setHotspotBounds(
            (int) haloBounds[0], (int) haloBounds[1], (int) haloBounds[2], (int) haloBounds[3]);
      }
    }
  }

  private int calculateTrackCenter() {
    return widgetThickness / 2
        + (labelBehavior == LABEL_WITHIN_BOUNDS || shouldAlwaysShowLabel()
            ? labels.get(0).getIntrinsicHeight()
            : 0);
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    if (dirtyConfig) {
      validateConfigurationIfDirty();

      // Update the visible tick coordinates.
      updateTicksCoordinates();
    }

    super.onDraw(canvas);

    int yCenter = calculateTrackCenter();

    drawInactiveTracks(canvas, trackWidth, yCenter);
    drawActiveTracks(canvas, trackWidth, yCenter);

    if (isRtl() || isVertical()) {
      drawTrackIcons(canvas, activeTrackRect, inactiveTrackLeftRect);
    } else {
      drawTrackIcons(canvas, activeTrackRect, inactiveTrackRightRect);
    }

    maybeDrawTicks(canvas);
    maybeDrawStopIndicator(canvas, yCenter);

    if ((thumbIsPressed || isFocused()) && isEnabled()) {
      maybeDrawCompatHalo(canvas, trackWidth, yCenter);
    }

    updateLabels();

    drawThumbs(canvas, trackWidth, yCenter);
  }

  /**
   * Returns a float array where {@code float[0]} is the normalized left position and {@code
   * float[1]} is the normalized right position of the range.
   */
  private float[] getActiveRange() {
    float min = values.get(0);
    float max = values.get(values.size() - 1);
    float left = normalizeValue(values.size() == 1 ? valueFrom : min);
    float right = normalizeValue(max);

    // When centered, the active range is bound by the center.
    if (isCentered()) {
      left = min(.5f, right);
      right = max(.5f, right);
    }

    // In RTL we draw things in reverse, so swap the left and right range values.
    return !isCentered() && (isRtl() || isVertical())
        ? new float[] {right, left}
        : new float[] {left, right};
  }

  private void drawInactiveTracks(@NonNull Canvas canvas, int width, int yCenter) {
    float[] activeRange = getActiveRange();
    float top = yCenter - trackThickness / 2f;
    float bottom = yCenter + trackThickness / 2f;

    int leftGapSize;
    if (isCentered() && activeRange[0] == 0.5f) {
      leftGapSize = thumbTrackGapSize;
    } else {
      leftGapSize = calculateThumbTrackGapSize(isRtl() || isVertical() ? values.size() - 1 : 0);
    }
    drawInactiveTrackSection(
        trackSidePadding - getTrackCornerSize(),
        trackSidePadding + activeRange[0] * width - leftGapSize,
        top,
        bottom,
        canvas,
        inactiveTrackLeftRect,
        FullCornerDirection.LEFT,
        leftGapSize);

    int rightGapSize;
    if (isCentered() && activeRange[1] == 0.5f) {
      rightGapSize = thumbTrackGapSize;
    } else {
      rightGapSize = calculateThumbTrackGapSize(isRtl() || isVertical() ? 0 : values.size() - 1);
    }
    drawInactiveTrackSection(
        trackSidePadding + activeRange[1] * width + rightGapSize,
        trackSidePadding + width + getTrackCornerSize(),
        top,
        bottom,
        canvas,
        inactiveTrackRightRect,
        FullCornerDirection.RIGHT,
        rightGapSize);
  }

  private void drawInactiveTrackSection(
      float from,
      float to,
      float top,
      float bottom,
      @NonNull Canvas canvas,
      RectF rect,
      FullCornerDirection direction,
      int gapSize) {
    if (to - from > getTrackCornerSize() - gapSize) {
      rect.set(from, top, to, bottom);
    } else {
      rect.setEmpty();
    }
    updateTrack(canvas, inactiveTrackPaint, rect, getTrackCornerSize(), direction);
  }

  /**
   * Returns a number between 0 and 1 indicating where on the track this value should sit with 0
   * being on the far left, and 1 on the far right.
   */
  private float normalizeValue(float value) {
    float normalized = (value - valueFrom) / (valueTo - valueFrom);
    if (isRtl() || isVertical()) {
      return 1 - normalized;
    }
    return normalized;
  }

  private void drawActiveTracks(@NonNull Canvas canvas, int width, int yCenter) {
    float[] activeRange = getActiveRange();
    float right = trackSidePadding + activeRange[1] * width;
    float left = trackSidePadding + activeRange[0] * width;
    if (left >= right) {
      activeTrackRect.setEmpty();
      return;
    }

    FullCornerDirection direction = FullCornerDirection.NONE;
    if (values.size() == 1 && !isCentered()) { // Only 1 thumb
      direction = isRtl() || isVertical() ? FullCornerDirection.RIGHT : FullCornerDirection.LEFT;
    }

    for (int i = 0; i < values.size(); i++) {
      if (values.size() > 1) {
        if (i > 0) {
          left = valueToX(values.get(i - 1));
        }
        right = valueToX(values.get(i));
        if (isRtl() || isVertical()) { // Swap left right
          float temp = left;
          left = right;
          right = temp;
        }
      }

      int trackCornerSize = getTrackCornerSize();
      switch (direction) {
        case NONE:
          if (i > 0) {
            left += calculateThumbTrackGapSize(i - 1);
            right -= calculateThumbTrackGapSize(i);
          } else if (activeRange[1] == .5f) { // centered, active track ends at the center
            left += calculateThumbTrackGapSize(i);
          } else if (activeRange[0] == .5f) { // centered, active track starts at the center
            right -= calculateThumbTrackGapSize(i);
          }
          break;
        case LEFT:
          left -= trackCornerSize;
          right -= calculateThumbTrackGapSize(i);
          break;
        case RIGHT:
          left += calculateThumbTrackGapSize(i);
          right += trackCornerSize;
          break;
        default:
          // fall through
      }

      // Nothing to draw if left is bigger than right.
      if (left >= right) {
        activeTrackRect.setEmpty();
        continue;
      }

      activeTrackRect.set(
          left, yCenter - trackThickness / 2f, right, yCenter + trackThickness / 2f);
      updateTrack(canvas, activeTrackPaint, activeTrackRect, trackCornerSize, direction);
    }
  }

  private float calculateStartTrackCornerSize(float trackCornerSize) {
    if (values.isEmpty() || !hasGapBetweenThumbAndTrack()) {
      return trackCornerSize;
    }
    int firstIdx = isRtl() || isVertical() ? values.size() - 1 : 0;
    float currentX = valueToX(values.get(firstIdx)) - trackSidePadding;
    if (currentX < trackCornerSize) {
      return max(currentX, trackInsideCornerSize);
    }
    return trackCornerSize;
  }

  private float calculateEndTrackCornerSize(float trackCornerSize) {
    if (values.isEmpty() || !hasGapBetweenThumbAndTrack()) {
      return trackCornerSize;
    }
    int lastIdx = isRtl() || isVertical() ? 0 : values.size() - 1;
    float currentX = valueToX(values.get(lastIdx)) - trackSidePadding;
    if (currentX > trackWidth - trackCornerSize) {
      return max(trackWidth - currentX, trackInsideCornerSize);
    }
    return trackCornerSize;
  }

  private void drawTrackIcons(
      @NonNull Canvas canvas,
      @NonNull RectF activeTrackBounds,
      @NonNull RectF inactiveTrackBounds) {
    if (!hasTrackIcons()) {
      return;
    }

    if (values.size() > 1) {
      Log.w(TAG, "Track icons can only be used when only 1 thumb is present.");
    }

    // draw track start icons
    calculateBoundsAndDrawTrackIcon(canvas, activeTrackBounds, trackIconActiveStart, true);
    calculateBoundsAndDrawTrackIcon(canvas, inactiveTrackBounds, trackIconInactiveStart, true);
    // draw track end icons
    calculateBoundsAndDrawTrackIcon(canvas, activeTrackBounds, trackIconActiveEnd, false);
    calculateBoundsAndDrawTrackIcon(canvas, inactiveTrackBounds, trackIconInactiveEnd, false);
  }

  private boolean hasTrackIcons() {
    return trackIconActiveStart != null
        || trackIconActiveEnd != null
        || trackIconInactiveStart != null
        || trackIconInactiveEnd != null;
  }

  private void calculateBoundsAndDrawTrackIcon(
      @NonNull Canvas canvas,
      @NonNull RectF trackBounds,
      @Nullable Drawable icon,
      boolean isStart) {
    if (icon != null) {
      calculateTrackIconBounds(trackBounds, iconRectF, trackIconSize, trackIconPadding, isStart);
      if (!iconRectF.isEmpty()) {
        drawTrackIcon(canvas, iconRectF, icon);
      }
    }
  }

  private void drawTrackIcon(
      @NonNull Canvas canvas, @NonNull RectF iconBounds, @NonNull Drawable icon) {
    if (isVertical()) {
      rotationMatrix.mapRect(iconBounds);
    }
    iconBounds.round(iconRect);
    icon.setBounds(iconRect);
    icon.draw(canvas);
  }

  private void calculateTrackIconBounds(
      @NonNull RectF trackBounds,
      @NonNull RectF iconBounds,
      @Px int iconSize,
      @Px int iconPadding,
      boolean isStart) {
    if (trackBounds.right - trackBounds.left >= iconSize + 2 * iconPadding) {
      float iconLeft =
          (isStart ^ (isRtl() || isVertical()))
              ? trackBounds.left + iconPadding
              : trackBounds.right - iconPadding - iconSize;
      float iconTop = calculateTrackCenter() - iconSize / 2f;
      float iconRight = iconLeft + iconSize;
      float iconBottom = iconTop + iconSize;
      iconBounds.set(iconLeft, iconTop, iconRight, iconBottom);
    } else {
      // not enough space to draw icon
      iconBounds.setEmpty();
    }
  }

  private boolean hasGapBetweenThumbAndTrack() {
    return thumbTrackGapSize > 0;
  }

  private int calculateThumbTrackGapSize(int index) {
    if (thumbIsPressed
        && index == activeThumbIdx
        && customThumbDrawable == null
        && customThumbDrawablesForValues.isEmpty()) {
      int activeThumbWidth = Math.round(thumbWidth * THUMB_WIDTH_PRESSED_RATIO);
      int delta = thumbWidth - activeThumbWidth;
      return thumbTrackGapSize - delta / 2;
    }
    return thumbTrackGapSize;
  }

  // The direction where the track has full corners.
  private enum FullCornerDirection {
    BOTH,
    LEFT,
    RIGHT,
    NONE
  }

  private void updateTrack(
      Canvas canvas, Paint paint, RectF bounds, float cornerSize, FullCornerDirection direction) {
    if (bounds.isEmpty()) {
      return;
    }

    float leftCornerSize = calculateStartTrackCornerSize(cornerSize);
    float rightCornerSize = calculateEndTrackCornerSize(cornerSize);
    switch (direction) {
      case BOTH:
        break;
      case LEFT:
        rightCornerSize = trackInsideCornerSize;
        break;
      case RIGHT:
        leftCornerSize = trackInsideCornerSize;
        break;
      case NONE:
        leftCornerSize = trackInsideCornerSize;
        rightCornerSize = trackInsideCornerSize;
        break;
    }

    paint.setStyle(Style.FILL);
    paint.setStrokeCap(Cap.BUTT);
    // TODO(b/373654533): activate anti-aliasing for legacy Slider
    if (hasGapBetweenThumbAndTrack()) {
      paint.setAntiAlias(true);
    }

    RectF rotated = new RectF(bounds);
    if (isVertical()) {
      rotationMatrix.mapRect(rotated);
    }
    // Draws track path with rounded corners.
    trackPath.reset();
    if (bounds.width() >= leftCornerSize + rightCornerSize) {
      // Fills one rounded rectangle.
      trackPath.addRoundRect(
          rotated, getCornerRadii(leftCornerSize, rightCornerSize), Direction.CW);
      canvas.drawPath(trackPath, paint);
    } else {
      // Clips the canvas and draws the fully rounded track.
      float minCornerSize = min(leftCornerSize, rightCornerSize);
      float maxCornerSize = max(leftCornerSize, rightCornerSize);
      canvas.save();
      // Clips the canvas using the current bounds with the smaller corner size.
      trackPath.addRoundRect(rotated, minCornerSize, minCornerSize, Direction.CW);
      canvas.clipPath(trackPath);
      // Then draws a rectangle with the minimum width for full corners.
      switch (direction) {
        case LEFT:
          cornerRect.set(bounds.left, bounds.top, bounds.left + 2 * maxCornerSize, bounds.bottom);
          break;
        case RIGHT:
          cornerRect.set(bounds.right - 2 * maxCornerSize, bounds.top, bounds.right, bounds.bottom);
          break;
        default:
          cornerRect.set(
              bounds.centerX() - maxCornerSize,
              bounds.top,
              bounds.centerX() + maxCornerSize,
              bounds.bottom);
      }
      if (isVertical()) {
        rotationMatrix.mapRect(cornerRect);
      }
      canvas.drawRoundRect(cornerRect, maxCornerSize, maxCornerSize, paint);
      canvas.restore();
    }
  }

  private float[] getCornerRadii(float leftSide, float rightSide) {
    if (isVertical()) {
      return new float[] {
        leftSide, leftSide, leftSide, leftSide, rightSide, rightSide, rightSide, rightSide
      };
    } else {
      return new float[] {
        leftSide, leftSide,
        rightSide, rightSide,
        rightSide, rightSide,
        leftSide, leftSide
      };
    }
  }

  private void maybeDrawTicks(@NonNull Canvas canvas) {
    if (ticksCoordinates == null || ticksCoordinates.length == 0) {
      return;
    }

    float[] activeRange = getActiveRange();

    // Calculate the index of the left tick of the active track.
    final int leftActiveTickIndex =
        (int) Math.ceil(activeRange[0] * (ticksCoordinates.length / 2f - 1));

    // Calculate the index of the right tick of the active track.
    final int rightActiveTickIndex =
        (int) Math.floor(activeRange[1] * (ticksCoordinates.length / 2f - 1));

    // Draw ticks on the left inactive track (if any).
    if (leftActiveTickIndex > 0) {
      drawTicks(0, leftActiveTickIndex * 2, canvas, inactiveTicksPaint);
    }

    // Draw ticks on the active track (if any).
    if (leftActiveTickIndex <= rightActiveTickIndex) {
      drawTicks(leftActiveTickIndex * 2, (rightActiveTickIndex + 1) * 2, canvas, activeTicksPaint);
    }

    // Draw ticks on the right inactive track (if any).
    if ((rightActiveTickIndex + 1) * 2 < ticksCoordinates.length) {
      drawTicks(
          (rightActiveTickIndex + 1) * 2, ticksCoordinates.length, canvas, inactiveTicksPaint);
    }
  }

  private void drawTicks(int from, int to, Canvas canvas, Paint paint) {
    for (int i = from; i < to; i += 2) {
      float coordinateToCheck = isVertical() ? ticksCoordinates[i + 1] : ticksCoordinates[i];
      if (isOverlappingThumb(coordinateToCheck)
          || (isCentered() && isOverlappingCenterGap(coordinateToCheck))) {
        continue;
      }
      canvas.drawPoint(ticksCoordinates[i], ticksCoordinates[i + 1], paint);
    }
  }

  private boolean isOverlappingThumb(float tickCoordinate) {
    for (int i = 0; i < values.size(); i++) {
      float valueToX = valueToX(values.get(i));
      float threshold = calculateThumbTrackGapSize(i) + thumbWidth / 2f;
      if (tickCoordinate >= valueToX - threshold && tickCoordinate <= valueToX + threshold) {
        return true;
      }
    }
    return false;
  }

  private boolean isOverlappingCenterGap(float tickCoordinate) {
    float trackCenter = (trackWidth + trackSidePadding * 2) / 2f;
    return tickCoordinate >= trackCenter - thumbTrackGapSize
        && tickCoordinate <= trackCenter + thumbTrackGapSize;
  }

  private void maybeDrawStopIndicator(@NonNull Canvas canvas, int yCenter) {
    if (trackStopIndicatorSize <= 0 || values.isEmpty()) {
      return;
    }

    // Draw stop indicator at the end of the track.
    if (values.get(values.size() - 1) < valueTo) {
      drawStopIndicator(canvas, valueToX(valueTo), yCenter);
    }
    // Centered, multiple thumbs, inactive track may be visible at the start.
    if (isCentered() || (values.size() > 1 && values.get(0) > valueFrom)) {
      drawStopIndicator(canvas, valueToX(valueFrom), yCenter);
    }
  }

  private void drawStopIndicator(@NonNull Canvas canvas, float x, float y) {
    // Prevent drawing indicator on the thumbs.
    for (int i = 0; i < values.size(); i++) {
      float valueToX = valueToX(values.get(i));
      float threshold = calculateThumbTrackGapSize(i) + thumbWidth / 2f;
      if (x >= valueToX - threshold && x <= valueToX + threshold) {
        return;
      }
    }
    if (isVertical()) {
      canvas.drawPoint(y, x, stopIndicatorPaint);
    } else {
      canvas.drawPoint(x, y, stopIndicatorPaint);
    }
  }

  private void drawThumbs(@NonNull Canvas canvas, int width, int yCenter) {
    for (int i = 0; i < values.size(); i++) {
      float value = values.get(i);
      if (customThumbDrawable != null) {
        drawThumbDrawable(canvas, width, yCenter, value, customThumbDrawable);
      } else if (i < customThumbDrawablesForValues.size()) {
        drawThumbDrawable(canvas, width, yCenter, value, customThumbDrawablesForValues.get(i));
      } else {
        // Clear out the track behind the thumb if we're in a disabled state since the thumb is
        // transparent.
        if (!isEnabled()) {
          canvas.drawCircle(
              trackSidePadding + normalizeValue(value) * width,
              yCenter,
              getThumbRadius(),
              thumbPaint);
        }
        drawThumbDrawable(canvas, width, yCenter, value, defaultThumbDrawables.get(i));
      }
    }
  }

  private void drawThumbDrawable(
      @NonNull Canvas canvas, int width, int top, float value, @NonNull Drawable thumbDrawable) {
    canvas.save();
    if (isVertical()) {
      canvas.concat(rotationMatrix);
    }
    canvas.translate(
        trackSidePadding
            + (int) (normalizeValue(value) * width)
            - (thumbDrawable.getBounds().width() / 2f),
        top - (thumbDrawable.getBounds().height() / 2f));
    thumbDrawable.draw(canvas);
    canvas.restore();
  }

  private void maybeDrawCompatHalo(@NonNull Canvas canvas, int width, int top) {
    // Only draw the halo for devices that aren't using the ripple.
    if (shouldDrawCompatHalo()) {
      float centerX = trackSidePadding + normalizeValue(values.get(focusedThumbIdx)) * width;
      float[] bounds = {centerX, top};
      if (isVertical()) {
        rotationMatrix.mapPoints(bounds);
      }
      if (VERSION.SDK_INT < VERSION_CODES.P) {
        // In this case we can clip the rect to allow drawing outside the bounds.
        canvas.clipRect(
            bounds[0] - haloRadius,
            bounds[1] - haloRadius,
            bounds[0] + haloRadius,
            bounds[1] + haloRadius,
            Op.UNION);
      }
      canvas.drawCircle(bounds[0], bounds[1], haloRadius, haloPaint);
    }
  }

  private boolean shouldDrawCompatHalo() {
    return forceDrawCompatHalo || !(getBackground() instanceof RippleDrawable);
  }

  @Override
  public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    viewRect.left = 0;
    viewRect.top = 0;
    viewRect.right = right - left;
    viewRect.bottom = bottom - top;

    if (!exclusionRects.contains(viewRect)) {
      exclusionRects.add(viewRect);
    }

    // Make sure that the slider takes precedence over back navigation gestures.
    ViewCompat.setSystemGestureExclusionRects(this, exclusionRects);
  }

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (!isEnabled()) {
      return false;
    }

    float eventCoordinateAxis1 = isVertical() ? event.getY() : event.getX();
    float eventCoordinateAxis2 = isVertical() ? event.getX() : event.getY();
    touchPosition = (eventCoordinateAxis1 - trackSidePadding) / trackWidth;
    touchPosition = max(0, touchPosition);
    touchPosition = min(1, touchPosition);

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        touchDownAxis1 = eventCoordinateAxis1;
        touchDownAxis2 = eventCoordinateAxis2;
        previousDownTouchEventValues.clear();
        previousDownTouchEventValues = getValues();

        // If we're inside a vertical scrolling container,
        // we should start dragging in ACTION_MOVE
        if (!isVertical() && isPotentialVerticalScroll(event)) {
          break;
        }
        // If we're inside a horizontal scrolling container,
        // we should start dragging in ACTION_MOVE
        if (isVertical() && isPotentialHorizontalScroll(event)) {
          break;
        }

        getParent().requestDisallowInterceptTouchEvent(true);

        if (!pickActiveThumb()) {
          // Couldn't determine the active thumb yet.
          break;
        }

        requestFocus();
        thumbIsPressed = true;
        updateThumbWidthWhenPressed();
        onStartTrackingTouch();

        snapTouchPosition();
        updateHaloHotspot();
        invalidate();
        break;
      case MotionEvent.ACTION_MOVE:
        if (!thumbIsPressed) {
          // Check if we're trying to scroll vertically instead of dragging this Slider
          if (!isVertical()
              && isPotentialVerticalScroll(event)
              && abs(eventCoordinateAxis1 - touchDownAxis1) < scaledTouchSlop) {
            return false;
          }
          // Check if we're trying to scroll horizontally instead of dragging this Slider
          if (isVertical()
              && isPotentialHorizontalScroll(event)
              && abs(eventCoordinateAxis2 - touchDownAxis2) < scaledTouchSlop * TOUCH_SLOP_RATIO) {
            return false;
          }
          getParent().requestDisallowInterceptTouchEvent(true);

          if (!pickActiveThumb()) {
            // Couldn't determine the active thumb yet.
            break;
          }

          thumbIsPressed = true;
          updateThumbWidthWhenPressed();
          onStartTrackingTouch();
        }

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
          updateHaloHotspot();
          resetThumbWidth();
          activeThumbIdx = -1;
          onStopTrackingTouch();
        }
        invalidate();
        break;
      case MotionEvent.ACTION_CANCEL:
        thumbIsPressed = false;
        // Make sure that we reset the state of the slider if a cancel event happens.
        snapThumbToPreviousDownTouchEventValue();
        updateHaloHotspot();
        resetThumbWidth();
        activeThumbIdx = -1;
        onStopTrackingTouch();
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

  private void updateThumbWidthWhenPressed() {
    // Update default thumb width when pressed.
    if (hasGapBetweenThumbAndTrack()
        && customThumbDrawable == null
        && customThumbDrawablesForValues.isEmpty()) {
      defaultThumbWidth = thumbWidth;
      defaultThumbTrackGapSize = thumbTrackGapSize;
      int pressedThumbWidth = Math.round(thumbWidth * THUMB_WIDTH_PRESSED_RATIO);
      // Only the currently pressed thumb should change width.
      setThumbWidth(pressedThumbWidth, /* thumbIndex= */ activeThumbIdx);
    }
  }

  private void resetThumbWidth() {
    // Reset the default thumb width.
    if (hasGapBetweenThumbAndTrack() && defaultThumbWidth != -1 && defaultThumbTrackGapSize != -1) {
      // Only the currently pressed thumb should change width.
      setThumbWidth(defaultThumbWidth, /* thumbIndex= */ activeThumbIdx);
    }
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
      if (compare(valueDiff, activeThumbDiff) > 0) {
        break;
      }

      boolean movingForward =
          (isRtl() || isVertical()) ? (valueX - touchX) > 0 : (valueX - touchX) < 0;
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
    if (isRtl() || isVertical()) {
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

  @CanIgnoreReturnValue
  private boolean snapThumbToValue(int idx, float value) {
    focusedThumbIdx = idx;

    // Check if the new value equals a value that was already set.
    if (abs(value - values.get(idx)) < THRESHOLD) {
      return false;
    }

    float newValue = getClampedValue(idx, value);
    // Replace the old value with the new value of the touch position.
    values.set(idx, newValue);

    dispatchOnChangedFromUser(idx);
    return true;
  }

  private void snapThumbToPreviousDownTouchEventValue() {
    if (activeThumbIdx != -1 && !previousDownTouchEventValues.isEmpty()) {
      for (int i = 0; i < values.size(); i++) {
        if (i == activeThumbIdx) {
          snapThumbToValue(i, previousDownTouchEventValues.get(i));
          break;
        }
      }
    }
  }

  /** Thumbs cannot cross each other, clamp the value to a bound or the value next to it. */
  private float getClampedValue(int idx, float value) {
    float minSeparation = getMinSeparation();
    minSeparation = separationUnit == UNIT_PX ? dimenToValue(minSeparation) : minSeparation;
    if (isRtl() || isVertical()) {
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
    dirtyConfig = true;
    postInvalidate();
  }

  protected float getMinSeparation() {
    return 0;
  }

  private float getValueOfTouchPosition() {
    double position = snapPosition(touchPosition);

    // We might need to invert the touch position to get the correct value.
    if (isRtl() || isVertical()) {
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
    int duration;
    TimeInterpolator interpolator;
    if (enter) {
      duration =
          MotionUtils.resolveThemeDuration(
              getContext(),
              LABEL_ANIMATION_ENTER_DURATION_ATTR,
              DEFAULT_LABEL_ANIMATION_ENTER_DURATION);
      interpolator =
          MotionUtils.resolveThemeInterpolator(
              getContext(),
              LABEL_ANIMATION_ENTER_EASING_ATTR,
              AnimationUtils.DECELERATE_INTERPOLATOR);
    } else {
      duration =
          MotionUtils.resolveThemeDuration(
              getContext(),
              LABEL_ANIMATION_EXIT_DURATION_ATTR,
              DEFAULT_LABEL_ANIMATION_EXIT_DURATION);
      interpolator =
          MotionUtils.resolveThemeInterpolator(
              getContext(),
              LABEL_ANIMATION_EXIT_EASING_ATTR,
              AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
    }
    animator.setDuration(duration);
    animator.setInterpolator(interpolator);
    animator.addUpdateListener(
        animation -> {
          float fraction = (float) animation.getAnimatedValue();
          for (TooltipDrawable label : labels) {
            label.setRevealFraction(fraction);
          }
          // Ensure the labels are redrawn even if the slider has stopped moving
          postInvalidateOnAnimation();
        });
    return animator;
  }

  private void updateLabels() {
    updateLabelPivots();

    switch (labelBehavior) {
      case LABEL_GONE:
        ensureLabelsRemoved();
        break;
      case LABEL_VISIBLE:
        if (isEnabled() && isSliderVisibleOnScreen()) {
          ensureLabelsAdded(/* showLabelOnAllThumbs= */ true);
        } else {
          ensureLabelsRemoved();
        }
        break;
      case LABEL_FLOATING:
      case LABEL_WITHIN_BOUNDS:
        if (activeThumbIdx != -1 && isEnabled()) {
          ensureLabelsAdded(/* showLabelOnAllThumbs= */ false);
        } else {
          ensureLabelsRemoved();
        }
        break;
      default:
        throw new IllegalArgumentException("Unexpected labelBehavior: " + labelBehavior);
    }
  }

  private void updateLabelPivots() {
    // Set the pivot point so that the label pops up in the direction from the thumb.
    final float labelPivotX;
    final float labelPivotY;

    final boolean isVertical = isVertical();
    final boolean isRtl = isRtl();
    if (isVertical && isRtl) {
      labelPivotX = RIGHT_LABEL_PIVOT_X;
      labelPivotY = RIGHT_LABEL_PIVOT_Y;
    } else if (isVertical) {
      labelPivotX = LEFT_LABEL_PIVOT_X;
      labelPivotY = LEFT_LABEL_PIVOT_Y;
    } else {
      labelPivotX = TOP_LABEL_PIVOT_X;
      labelPivotY = TOP_LABEL_PIVOT_Y;
    }

    for (TooltipDrawable label : labels) {
      label.setPivots(labelPivotX, labelPivotY);
    }
  }

  private boolean isSliderVisibleOnScreen() {
    final Rect contentViewBounds = new Rect();
    ViewUtils.getContentView(this).getHitRect(contentViewBounds);
    return getLocalVisibleRect(contentViewBounds) && isThisAndAncestorsVisible();
  }

  private boolean isThisAndAncestorsVisible() {
    // onVisibilityAggregated is only available on N+ devices, so on pre-N devices we check if this
    // view and its ancestors are visible each time, in case one of the visibilities has changed.
    return (VERSION.SDK_INT >= VERSION_CODES.N) ? thisAndAncestorsVisible : isShown();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    // Setting visible to user to false prevents duplicate announcements by making only our virtual
    // view accessible, not the parent container.
    info.setVisibleToUser(false);
  }

  @Override
  public void onVisibilityAggregated(boolean isVisible) {
    super.onVisibilityAggregated(isVisible);
    this.thisAndAncestorsVisible = isVisible;
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
              final ViewOverlay contentViewOverlay = getContentViewOverlay();
              if (contentViewOverlay == null) {
                return;
              }

              for (TooltipDrawable label : labels) {
                contentViewOverlay.remove(label);
              }
            }
          });
      labelsOutAnimator.start();
    }
  }

  private void ensureLabelsAdded(boolean showLabelOnAllThumbs) {
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

    if (showLabelOnAllThumbs) {
      for (int i = 0; i < values.size() && labelItr.hasNext(); i++) {
        if (i == focusedThumbIdx) {
          // We position the focused thumb last so it's displayed on top, so skip it for now.
          continue;
        }
        setValueForLabel(labelItr.next(), values.get(i));
      }
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
    positionLabel(label, value);
    final ViewOverlay contentViewOverlay = getContentViewOverlay();
    if (contentViewOverlay == null) {
      return;
    }

    contentViewOverlay.add(label);
  }

  private void positionLabel(TooltipDrawable label, float value) {
    // Calculate the difference between the bounds of this view and the bounds of the root view to
    // correctly position this view in the overlay layer.
    calculateLabelBounds(label, value);
    if (isVertical()) {
      RectF labelBounds = new RectF(labelRect);
      rotationMatrix.mapRect(labelBounds);
      labelBounds.round(labelRect);
    }
    DescendantOffsetUtils.offsetDescendantRect(ViewUtils.getContentView(this), this, labelRect);
    label.setBounds(labelRect);
  }

  private void calculateLabelBounds(TooltipDrawable label, float value) {
    int left;
    int right;
    int bottom;
    int top;

    if (isVertical()) {
      left =
          trackSidePadding
              + (int) (normalizeValue(value) * trackWidth)
              - label.getIntrinsicHeight() / 2;
      right = left + label.getIntrinsicHeight();
      if (isRtl()) {
        bottom = calculateTrackCenter() - (labelPadding + thumbHeight / 2);
        top = bottom - label.getIntrinsicWidth();
      } else {
        top = calculateTrackCenter() + (labelPadding + thumbHeight / 2);
        bottom = top + label.getIntrinsicWidth();
      }
    } else {
      left =
          trackSidePadding
              + (int) (normalizeValue(value) * trackWidth)
              - label.getIntrinsicWidth() / 2;
      right = left + label.getIntrinsicWidth();
      bottom = calculateTrackCenter() - (labelPadding + thumbHeight / 2);
      top = bottom - label.getIntrinsicHeight();
    }
    labelRect.set(left, top, right, bottom);
  }

  private void invalidateTrack() {
    inactiveTrackPaint.setStrokeWidth(trackThickness);
    activeTrackPaint.setStrokeWidth(trackThickness);
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

  private boolean isInHorizontalScrollingContainer() {
    ViewParent p = getParent();
    while (p instanceof ViewGroup) {
      ViewGroup parent = (ViewGroup) p;
      boolean canScrollHorizontally =
          parent.canScrollHorizontally(1) || parent.canScrollHorizontally(-1);
      if (canScrollHorizontally && parent.shouldDelayChildPressedState()) {
        return true;
      }
      p = p.getParent();
    }
    return false;
  }

  private static boolean isMouseEvent(MotionEvent event) {
    return event.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE;
  }

  private boolean isPotentialVerticalScroll(MotionEvent event) {
    return !isMouseEvent(event) && isInVerticalScrollingContainer();
  }

  private boolean isPotentialHorizontalScroll(MotionEvent event) {
    return !isMouseEvent(event) && isInHorizontalScrollingContainer();
  }

  @SuppressWarnings("unchecked")
  private void dispatchOnChangedProgrammatically() {
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
    stopIndicatorPaint.setColor(getColorForState(tickColorInactive));
    for (TooltipDrawable label : labels) {
      if (label.isStateful()) {
        label.setState(getDrawableState());
      }
    }
    for (int i = 0; i < defaultThumbDrawables.size(); i++) {
      if (defaultThumbDrawables.get(i).isStateful()) {
        defaultThumbDrawables.get(i).setState(getDrawableState());
      }
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

    activeThumbIdx = focusedThumbIdx;

    isLongPress |= event.isLongPress();
    Float increment = calculateIncrementForKey(keyCode);
    if (increment != null) {
      if (snapActiveThumbToValue(values.get(activeThumbIdx) + increment)) {
        updateHaloHotspot();
        postInvalidate();
      }
      return true;
    }

    if (keyCode == KeyEvent.KEYCODE_TAB) {
      resetThumbWidth();
      if (event.hasNoModifiers()) {
        return moveFocus(1);
      }

      if (event.isShiftPressed()) {
        return moveFocus(-1);
      }
      return false;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    isLongPress = false;
    return super.onKeyUp(keyCode, event);
  }

  final boolean isRtl() {
    return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

  public boolean isVertical() {
    return widgetOrientation == VERTICAL;
  }

  public boolean isCentered() {
    return centered;
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
    activeThumbIdx = focusedThumbIdx;
    updateThumbWidthWhenPressed();
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
    if (isRtl() || isVertical()) {
      // Prevent integer overflow.
      direction = direction == Integer.MIN_VALUE ? Integer.MAX_VALUE : -direction;
    }
    return moveFocus(direction);
  }

  @Nullable
  private Float calculateIncrementForKey(int keyCode) {
    // If this is a long press, increase the increment so it will only take around 20 steps.
    // Otherwise choose the smallest valid increment.
    float increment = isLongPress ? calculateStepIncrement(20) : calculateStepIncrement();
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        return isRtl() ? increment : -increment;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        return isRtl() ? -increment : increment;
      case KeyEvent.KEYCODE_PLUS:
      case KeyEvent.KEYCODE_EQUALS:
        return increment;
      case KeyEvent.KEYCODE_MINUS:
        return -increment;
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
      resetThumbWidth();
      activeThumbIdx = -1;
      accessibilityHelper.clearKeyboardFocusForVirtualView(focusedThumbIdx);
    } else {
      // If activeThumbIdx != -1, a touch gesture is in progress and has already
      // picked the thumb to focus. Don't interfere.
      if (activeThumbIdx == -1) {
        focusThumbOnFocusGained(direction);
        activeThumbIdx = focusedThumbIdx;
      }
      resetThumbWidth();
      updateThumbWidthWhenPressed();
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

  void updateBoundsForVirtualViewId(int virtualViewId, Rect virtualViewBounds) {
    int x = trackSidePadding + (int) (normalizeValue(getValues().get(virtualViewId)) * trackWidth);
    int y = calculateTrackCenter();
    int touchTargetOffsetX = max(thumbWidth / 2, minTouchTargetSize / 2);
    int touchTargetOffsetY = max(thumbHeight / 2, minTouchTargetSize / 2);
    RectF rect =
        new RectF(
            x - touchTargetOffsetX,
            y - touchTargetOffsetY,
            x + touchTargetOffsetX,
            y + touchTargetOffsetY);
    if (isVertical()) {
      rotationMatrix.mapRect(rect);
    }
    virtualViewBounds.set((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
  }

  public static class AccessibilityHelper extends ExploreByTouchHelper {

    private final BaseSlider<?, ?, ?> slider;
    final Rect virtualViewBounds = new Rect();

    AccessibilityHelper(BaseSlider<?, ?, ?> slider) {
      super(slider);
      this.slider = slider;
    }

    @Override
    protected int getVirtualViewAt(float x, float y) {
      for (int i = 0; i < slider.getValues().size(); i++) {
        slider.updateBoundsForVirtualViewId(i, virtualViewBounds);
        if (virtualViewBounds.contains((int) x, (int) y)) {
          return i;
        }
      }

      return ExploreByTouchHelper.HOST_ID;
    }

    @Override
    protected void getVisibleVirtualViews(@NonNull List<Integer> virtualViewIds) {
      for (int i = 0; i < slider.getValues().size(); i++) {
        virtualViewIds.add(i);
      }
    }

    @Override
    protected void onPopulateNodeForVirtualView(
        int virtualViewId, @NonNull AccessibilityNodeInfoCompat info) {

      info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS);

      List<Float> values = slider.getValues();
      float value = values.get(virtualViewId);
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

      NumberFormat nf = NumberFormat.getNumberInstance();
      nf.setMaximumFractionDigits(2);
      try {
        valueFrom = nf.parse(nf.format(valueFrom)).floatValue();
        valueTo = nf.parse(nf.format(valueTo)).floatValue();
        value = nf.parse(nf.format(value)).floatValue();
      } catch (ParseException e) {
        Log.w(TAG, String.format(WARNING_PARSE_ERROR, value, valueFrom, valueTo));
      }

      info.setRangeInfo(RangeInfoCompat.obtain(RANGE_TYPE_FLOAT, valueFrom, valueTo, value));
      info.setClassName(SeekBar.class.getName());
      StringBuilder contentDescription = new StringBuilder();
      // Add the content description of the slider.
      if (slider.getContentDescription() != null) {
        contentDescription.append(slider.getContentDescription()).append(",");
      }
      // Add the range/value to the content description.
      String verbalValue = slider.formatValue(value);
      String verbalValueType = slider.getContext().getString(R.string.material_slider_value);
      if (values.size() > 1) {
        verbalValueType = startOrEndDescription(virtualViewId);
      }
      CharSequence stateDescription = ViewCompat.getStateDescription(slider);
      if (!TextUtils.isEmpty(stateDescription)) {
        info.setStateDescription(stateDescription);
      } else {
        contentDescription.append(
            String.format(Locale.getDefault(), "%s, %s", verbalValueType, verbalValue));
      }
      info.setContentDescription(contentDescription.toString());

      slider.updateBoundsForVirtualViewId(virtualViewId, virtualViewBounds);
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
        int virtualViewId, int action, @Nullable Bundle arguments) {
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
              slider.setActiveThumbIndex(virtualViewId);
              slider.scheduleTooltipTimeout();
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
