/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.tooltip.TooltipDrawable;
import java.util.Locale;

/**
 * A widget that allows picking a value within a given range by sliding a thumb along a horizontal
 * line.
 *
 * <p>The slider can function either as a continuous slider, or as a discrete slider. The mode of
 * operation is controlled by the value of the step size. If the step size is set to 0, the slider
 * operates as a continuous slider where the slider's thumb can be moved to any position along the
 * horizontal line. If the step size is set to a number greater than 0, the slider operates as a
 * discrete slider where the slider's thumb will snap to the closest tick mark. See {@link
 * #setStepSize(float)}.
 *
 * <p>The slider displays a line on which the thumb can be dragged to select a value.
 *
 * <p>On interaction in discrete mode, tick marks are displayed along the line and the thumb
 * automatically snaps to the closest tick mark.
 *
 * <p>The {@link OnChangeListener} interface defines a callback to be invoked when the slider
 * changes.
 *
 * <p>The {@link LabelFormatter} interface defines a formatter to be used to render text within the
 * bubble shown while in discrete mode.
 *
 * <p>{@link BasicLabelFormatter} is a simple implementation of the {@link LabelFormatter} that
 * displays the selected value using letters to indicate magnitude (e.g.: 1.5K, 3M, 12B, etc..).
 *
 * <p>With the default style {@link
 * com.google.android.material.R.style.Widget_MaterialComponents_Slider}, colorPrimary is used to
 * customize the color of the slider. The following attributes are used to customize the slider's
 * appearance further:
 *
 * <ul>
 *   <li>{@code trackColor}: The color of the whole track. This is a short hand for setting both the
 *       {@code activeTrackColor} and {@code inactiveTrackColor} to the same thing. This takes
 *       precedence over {@code activeTrackColor} and {@code inactiveTrackColor}.
 *   <li>{@code activeTrackColor}: The color of the active part of the track.
 *   <li>{@code inactiveTrackColor}: The color of the inactive part of the track.
 *   <li>{@code thumbColor}: the color of the slider's thumb.
 *   <li>{@code tickColor}: the color of the slider's tick marks. Only used when the slider is in
 *       discrete mode.
 *   <li>{@code activeTickColor}: the color of the slider's tick marks for the active part of the
 *       track. Only used when the slider is in discrete mode.
 *   <li>{@code inactiveTickColor}: the color of the slider's tick marks for the inactive part of
 *       the track. Only used when the slider is in discrete mode.
 *   <li>{@code labelStyle}: the style to apply to the value indicator {@link TooltipDrawable}.
 * </ul>
 *
 * <p>The following XML attributes are used to set the slider's various parameters of operation:
 *
 * <ul>
 *   <li>{@code android:valueFrom}: <b>Required.</b> The slider's minimum value. This attribute is
 *       required, if missing, an {@link InflateException} is thrown.
 *   <li>{@code android:valueTo}: <b>Required.</b> The slider's maximum value. This attribute is
 *       required, if missing, an {@link InflateException} is thrown.
 *   <li>{@code android:value}: <b>Optional.</b> The initial value of the slider. If not specified,
 *       the slider's minimum value {@code android:valueFrom} is used.
 *   <li>{@code android:stepSize}: <b>Optional.</b> This value dictates whether the slider operates
 *       in continuous mode, or in discrete mode. If missing or equal to 0, the slider operates in
 *       continuous mode. If greater than 0 and evenly divides the range described by {@code
 *       valueFrom} and {@code valueTo}, the slider operates in discrete mode. If negative, or
 *       greater than 0 but not a factor of the range described by {@code valueFrom} and {@code
 *       valueTo}, an {@link IllegalArgumentException} is thrown.
 * </ul>
 *
 * <p>Note: the slider does not accept {@link View.OnFocusChangeListener}s.
 *
 * @attr ref com.google.android.material.R.styleable#Slider_android_value
 * @attr ref com.google.android.material.R.styleable#Slider_android_valueFrom
 * @attr ref com.google.android.material.R.styleable#Slider_android_valueTo
 * @attr ref com.google.android.material.R.styleable#Slider_android_stepSize
 * @attr ref com.google.android.material.R.styleable#Slider_trackColor
 * @attr ref com.google.android.material.R.styleable#Slider_activeTrackColor
 * @attr ref com.google.android.material.R.styleable#Slider_inactiveTrackColor
 * @attr ref com.google.android.material.R.styleable#Slider_thumbColor
 * @attr ref com.google.android.material.R.styleable#Slider_haloColor
 * @attr ref com.google.android.material.R.styleable#Slider_tickColor
 * @attr ref com.google.android.material.R.styleable#Slider_activeTickColor
 * @attr ref com.google.android.material.R.styleable#Slider_inactiveTickColor
 * @attr ref com.google.android.material.R.styleable#Slider_labelStyle
 * @attr ref com.google.android.material.R.styleable#Slider_floatingLabel
 * @attr ref com.google.android.material.R.styleable#Slider_thumbRadius
 * @attr ref com.google.android.material.R.styleable#Slider_thumbElevation
 * @attr ref com.google.android.material.R.styleable#Slider_haloRadius
 */
public class Slider extends View {

  private static final String TAG = Slider.class.getSimpleName();
  private static final String EXCEPTION_ILLEGAL_VALUE =
      "Slider value must be greater or equal to valueFrom, and lower or equal to valueTo";
  private static final String EXCEPTION_ILLEGAL_DISCRETE_VALUE =
      "Value must be equal to valueFrom plus a multiple of stepSize when using stepSize";
  private static final String EXCEPTION_ILLEGAL_VALUE_FROM =
      "valueFrom must be smaller than valueTo";
  private static final String EXCEPTION_ILLEGAL_VALUE_TO = "valueTo must be greater than valueFrom";
  private static final String EXCEPTION_ILLEGAL_STEP_SIZE =
      "The stepSize must be 0, or a factor of the valueFrom-valueTo range";

  private static final int HALO_ALPHA = 63;

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Slider;

  @NonNull private final Paint inactiveTrackPaint;
  @NonNull private final Paint activeTrackPaint;
  @NonNull private final Paint thumbPaint;
  @NonNull private final Paint haloPaint;
  @NonNull private final Paint ticksPaint;

  @NonNull private TooltipDrawable label;

  private int widgetHeight;
  private int widgetHeightLabel;
  private boolean floatingLabel;
  private int lineHeight;
  private int trackSidePadding;
  private int trackTop;
  private int trackTopLabel;
  private int thumbRadius;
  private int haloRadius;
  private int labelPadding;
  private OnChangeListener listener;
  private LabelFormatter formatter;
  private boolean thumbIsPressed = false;
  private float valueFrom;
  private float valueTo;
  private float thumbPosition = 0.0f; // The position of the thumb normalised to a [0.0, 1.0] range.
  private float stepSize = 0.0f;
  private float[] ticksCoordinates;
  private int trackWidth;
  private boolean forceDrawCompatShadow;

  @NonNull private ColorStateList inactiveTrackColor;
  @NonNull private ColorStateList activeTrackColor;
  @NonNull private ColorStateList thumbColor;
  @NonNull private ColorStateList haloColor;
  @NonNull private ColorStateList tickColor;

  @NonNull private final MaterialShapeDrawable thumbDrawable = new MaterialShapeDrawable();

  /** Interface definition for a callback invoked when a slider's value is changed. */
  public interface OnChangeListener {
    void onValueChange(Slider slider, float value);
  }

  /**
   * Interface definition for applying custom formatting to the text displayed inside the bubble
   * shown when a slider is used in discrete mode.
   */
  public interface LabelFormatter {
    @NonNull
    String getFormattedValue(float value);
  }

  /**
   * A simple implementation of the {@link LabelFormatter} interface, that limits the number
   * displayed inside a discrete slider's bubble to three digits, and a single-character suffix that
   * denotes magnitude (e.g.: 1.5K, 2.2M, 1.3B, 2T).
   */
  public static final class BasicLabelFormatter implements LabelFormatter {

    private static final long TRILLION = 1000000000000L;
    private static final int BILLION = 1000000000;
    private static final int MILLION = 1000000;
    private static final int THOUSAND = 1000;

    @NonNull
    @Override
    public String getFormattedValue(float value) {
      if (value >= TRILLION) {
        return String.format(Locale.US, "%.1fT", value / TRILLION);
      } else if (value >= BILLION) {
        return String.format(Locale.US, "%.1fB", value / BILLION);
      } else if (value >= MILLION) {
        return String.format(Locale.US, "%.1fM", value / MILLION);
      } else if (value >= THOUSAND) {
        return String.format(Locale.US, "%.1fK", value / THOUSAND);
      } else {
        return String.format(Locale.US, "%.0f", value);
      }
    }
  }

  public Slider(@NonNull Context context) {
    this(context, null);
  }

  public Slider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.sliderStyle);
  }

  public Slider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    loadResources(context.getResources());
    processAttributes(context, attrs, defStyleAttr);

    inactiveTrackPaint = new Paint();
    inactiveTrackPaint.setStyle(Style.STROKE);
    inactiveTrackPaint.setStrokeWidth(lineHeight);

    activeTrackPaint = new Paint();
    activeTrackPaint.setStyle(Style.STROKE);
    activeTrackPaint.setStrokeWidth(lineHeight);

    thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    thumbPaint.setStyle(Style.FILL);
    thumbPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

    haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    haloPaint.setStyle(Style.FILL);

    ticksPaint = new Paint();
    ticksPaint.setStyle(Style.STROKE);
    ticksPaint.setStrokeWidth(lineHeight);

    Drawable background = getBackground();
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      if (background instanceof RippleDrawable) {
        ((RippleDrawable) background).setColor(haloColor);
        DrawableUtils.setRippleDrawableRadius(background, haloRadius);
      }
    }

    super.setOnFocusChangeListener(
        new OnFocusChangeListener() {
          @Override
          public void onFocusChange(View v, boolean hasFocus) {
            invalidate();
          }
        });

    setFocusable(true);

    // Set up the thumb drawable to always show the compat shadow.
    thumbDrawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    // When we're disabled, set the layer type to hardware so we can clear the track out from behind
    // the thumb. When enabled set the layer type to none so that the halo can be drawn outside the
    // bounds of the slider.
    setLayerType(enabled ? LAYER_TYPE_NONE : LAYER_TYPE_HARDWARE, null);
  }

  @Override
  public void setOnFocusChangeListener(View.OnFocusChangeListener listener) {}

  private void loadResources(@NonNull Resources resources) {
    widgetHeight = resources.getDimensionPixelSize(R.dimen.mtrl_slider_widget_height);
    widgetHeightLabel = resources.getDimensionPixelSize(R.dimen.mtrl_slider_widget_height_label);

    lineHeight = resources.getDimensionPixelSize(R.dimen.mtrl_slider_line_height);

    trackSidePadding = resources.getDimensionPixelOffset(R.dimen.mtrl_slider_track_side_padding);
    trackTop = resources.getDimensionPixelOffset(R.dimen.mtrl_slider_track_top);
    trackTopLabel = resources.getDimensionPixelOffset(R.dimen.mtrl_slider_track_top_label);

    labelPadding = resources.getDimensionPixelSize(R.dimen.mtrl_slider_label_padding);
  }

  private void processAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.Slider, defStyleAttr, DEF_STYLE_RES);
    valueFrom = a.getFloat(R.styleable.Slider_android_valueFrom, 0.0f);
    valueTo = a.getFloat(R.styleable.Slider_android_valueTo, 1.0f);
    setValue(a.getFloat(R.styleable.Slider_android_value, valueFrom));
    stepSize = a.getFloat(R.styleable.Slider_android_stepSize, 0.0f);

    boolean hasTrackColor = a.hasValue(R.styleable.Slider_trackColor);

    int inactiveTrackColorRes =
        hasTrackColor ? R.styleable.Slider_trackColor : R.styleable.Slider_inactiveTrackColor;
    int activeTrackColorRes =
        hasTrackColor ? R.styleable.Slider_trackColor : R.styleable.Slider_activeTrackColor;

    inactiveTrackColor = MaterialResources.getColorStateList(context, a, inactiveTrackColorRes);
    activeTrackColor = MaterialResources.getColorStateList(context, a, activeTrackColorRes);
    thumbColor = MaterialResources.getColorStateList(context, a, R.styleable.Slider_thumbColor);
    thumbDrawable.setFillColor(thumbColor);
    haloColor = MaterialResources.getColorStateList(context, a, R.styleable.Slider_haloColor);
    tickColor = MaterialResources.getColorStateList(context, a, R.styleable.Slider_activeTickColor);

    label = parseLabelDrawable(context, a);

    setThumbRadius(a.getDimensionPixelSize(R.styleable.Slider_thumbRadius, 0));
    haloRadius = a.getDimensionPixelSize(R.styleable.Slider_haloRadius, 0);

    setThumbElevation(a.getDimension(R.styleable.Slider_thumbElevation, 0));

    floatingLabel = a.getBoolean(R.styleable.Slider_floatingLabel, true);
    a.recycle();

    validateValueFrom();
    validateValueTo();
    validateStepSize();
  }

  @NonNull
  private TooltipDrawable parseLabelDrawable(@NonNull Context context, @NonNull TypedArray a) {
    TooltipDrawable label =
        TooltipDrawable.createFromAttributes(
            context,
            null,
            0,
            a.getResourceId(
                R.styleable.Slider_labelStyle, R.style.Widget_MaterialComponents_Tooltip));
    label.setRelativeToView(this);

    return label;
  }

  private void validateValueFrom() {
    if (valueFrom >= valueTo) {
      Log.e(TAG, EXCEPTION_ILLEGAL_VALUE_FROM);
      throw new IllegalArgumentException(EXCEPTION_ILLEGAL_VALUE_FROM);
    }
  }

  private void validateValueTo() {
    if (valueTo <= valueFrom) {
      Log.e(TAG, EXCEPTION_ILLEGAL_VALUE_TO);
      throw new IllegalArgumentException(EXCEPTION_ILLEGAL_VALUE_TO);
    }
  }

  private void validateStepSize() {
    if (stepSize < 0.0f) {
      Log.e(TAG, EXCEPTION_ILLEGAL_STEP_SIZE);
      throw new IllegalArgumentException(EXCEPTION_ILLEGAL_STEP_SIZE);
    } else if (stepSize > 0.0f && (valueTo - valueFrom) % stepSize != 0.0f) {
      Log.e(TAG, EXCEPTION_ILLEGAL_STEP_SIZE);
      throw new IllegalArgumentException(EXCEPTION_ILLEGAL_STEP_SIZE);
    }
  }

  /** Returns the slider's {@code valueFrom} value. */
  public float getValueFrom() {
    return valueFrom;
  }

  /**
   * Sets the slider's {@code valueFrom} value.
   *
   * <p>The {@code valueFrom} value must be strictly lower than the {@code valueTo} value. If that
   * is not the case, an {@link IllegalArgumentException} will be thrown.
   *
   * @param valueFrom The minimum value for the slider's range of values
   * @throws IllegalArgumentException If {@code valueFrom} is greater or equal to {@code valueTo}
   */
  public void setValueFrom(float valueFrom) {
    this.valueFrom = valueFrom;
    validateValueFrom();
  }

  /** Returns the slider's {@code valueTo} value. */
  public float getValueTo() {
    return valueTo;
  }

  /**
   * Sets the slider's {@code valueTo} value.
   *
   * <p>The {@code valueTo} value must be strictly greater than the {@code valueFrom} value. If that
   * is not the case, an {@link IllegalArgumentException} will be thrown.
   *
   * @param valueTo The maximum value for the slider's range of values
   * @throws IllegalArgumentException If {@code valueTo} is lesser or equal to {@code valueFrom}
   */
  public void setValueTo(float valueTo) {
    this.valueTo = valueTo;
    validateValueTo();
  }

  /** Returns the value of the slider. */
  public float getValue() {
    return thumbPosition * (valueTo - valueFrom) + valueFrom;
  }

  /**
   * Sets the value of the slider.
   *
   * <p>The thumb value must be greater or equal to {@code valueFrom}, and lesser or equal to {@code
   * valueTo}. If that is not the case, an {@link IllegalArgumentException} will be thrown.
   *
   * <p>If the slider is in discrete mode (i.e. the tick increment value is greater than 0), the
   * thumb's value must be set to a value falls on a tick (i.e.: {@code value == valueFrom + x *
   * stepSize}, where {@code x} is an integer equal to or greater than 0). If that is not the case,
   * an {@link IllegalArgumentException} will be thrown.
   *
   * @param value The value to which to set the slider
   * @throws IllegalArgumentException If the value is not within {@code valueFrom} and {@code
   *     valueTo}. If stepSize is greater than 0 and value does not fall on a tick
   */
  public void setValue(float value) {
    if (isValueValid(value)) {
      thumbPosition = (value - valueFrom) / (valueTo - valueFrom);
      if (hasOnChangeListener()) {
        listener.onValueChange(this, getValue());
      }
      invalidate();
    }
  }

  private boolean isValueValid(float value) {
    if (value < valueFrom || value > valueTo) {
      Log.e(TAG, EXCEPTION_ILLEGAL_VALUE);
      return false;
    }
    if (stepSize > 0.0f && ((valueFrom - value) % stepSize) != 0.0f) {
      Log.e(TAG, EXCEPTION_ILLEGAL_DISCRETE_VALUE);
      return false;
    }
    return true;
  }

  /**
   * Returns the step size used to mark the ticks.
   *
   * <p>A step size of 0 means that the slider is operating in continuous mode. A step size greater
   * than 0 means that the slider is operating in discrete mode.
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
   * {@link IllegalArgumentException} will be thrown.
   *
   * <p>Setting this value to a negative value will result in an {@link IllegalArgumentException}.
   *
   * @param stepSize The interval value at which ticks must be drawn. Set to 0 to operate the slider
   *     in continuous mode and not have any ticks.
   * @throws IllegalArgumentException If the step size is not a factor of the {@code
   *     valueFrom}-{@code valueTo} range. If the step size is less than 0
   */
  public void setStepSize(float stepSize) {
    this.stepSize = stepSize;
    validateStepSize();
    requestLayout();
  }

  /**
   * Returns {@code true} if the slider has an {@link OnChangeListener} attached, {@code false}
   * otherwise.
   */
  public boolean hasOnChangeListener() {
    return listener != null;
  }

  /**
   * Registers a callback to be invoked when the slider changes.
   *
   * @param listener The callback to run when the slider changes
   */
  public void setOnChangeListener(@Nullable OnChangeListener listener) {
    this.listener = listener;
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

  /** Sets the elevation of the thumb. */
  public void setThumbElevation(float elevation) {
    thumbDrawable.setElevation(elevation);
    postInvalidate();
  }

  /** Sets the elevation of the thumb from a dimension resource. */
  public void setThumbElevationResource(@DimenRes int elevation) {
    setThumbElevation(getResources().getDimension(elevation));
  }

  /** Returns the elevation of the thumb. */
  public float getThumbElevation() {
    return thumbDrawable.getElevation();
  }

  /** Sets the radius of the thumb in pixels. */
  public void setThumbRadius(@IntRange(from = 0) @Dimension int radius) {
    thumbRadius = radius;

    thumbDrawable.setShapeAppearanceModel(
        ShapeAppearanceModel.builder().setAllCorners(CornerFamily.ROUNDED, thumbRadius).build());
    thumbDrawable.setBounds(0, 0, thumbRadius * 2, thumbRadius * 2);

    postInvalidate();
  }

  /** Sets the radius of the thumb from a dimension resource. */
  public void setThumbRadiusResource(@DimenRes int radius) {
    setThumbRadius(getResources().getDimensionPixelSize(radius));
  }

  /** Returns the radius of the thumb. */
  @Dimension
  public int getThumbRadius() {
    return thumbRadius;
  }

  /** Sets the radius of the halo in pixels. */
  public void setHaloRadius(@IntRange(from = 0) @Dimension int radius) {
    haloRadius = radius;
    postInvalidate();
  }

  /** Sets the radius of the halo from a dimension resource. */
  public void setHaloRadiusResource(@DimenRes int radius) {
    setHaloRadius(getResources().getDimensionPixelSize(radius));
  }

  /**
   * If true, height will be added to make space for the label, otherwise the label will be drawn on
   * top of views above this one.
   */
  public void setFloatingLabel(boolean floatingLabel) {
    if (this.floatingLabel != floatingLabel) {
      this.floatingLabel = floatingLabel;
      requestLayout();
    }
  }

  /** If the height of this view is increased to make space for the label. */
  public boolean isFloatingLabel() {
    return floatingLabel;
  }

  /** Returns the radius of the halo. */
  @Dimension()
  public int getHaloRadius() {
    return haloRadius;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(
        widthMeasureSpec,
        MeasureSpec.makeMeasureSpec(
            floatingLabel ? widgetHeight : widgetHeightLabel, MeasureSpec.EXACTLY));
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    updateTrackWidthAndTicksCoordinates(w);
    updateHaloHotSpot();
  }

  private void updateTrackWidthAndTicksCoordinates(int viewWidth) {
    trackWidth = viewWidth - trackSidePadding * 2;
    if (stepSize > 0.0f) {
      int tickCount = (int) ((valueTo - valueFrom) / stepSize + 1);
      if (ticksCoordinates == null || ticksCoordinates.length != tickCount * 2) {
        ticksCoordinates = new float[tickCount * 2];
      }
      float interval = trackWidth / (float) (tickCount - 1);
      for (int i = 0; i < tickCount * 2; i += 2) {
        ticksCoordinates[i] = trackSidePadding + i / 2 * interval;
        ticksCoordinates[i + 1] = calculateTop();
      }
    }
  }

  private void updateHaloHotSpot() {
    // Set the hotspot as the halo above lollipop.
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && getMeasuredWidth() > 0) {
      final Drawable background = getBackground();
      if (background instanceof RippleDrawable) {
        int x = (int) (thumbPosition * trackWidth + trackSidePadding);
        int y = calculateTop();
        DrawableCompat.setHotspotBounds(
            background, x - haloRadius, y - haloRadius, x + haloRadius, y + haloRadius);
      }
    }
  }

  private int calculateTop() {
    return floatingLabel ? trackTop : trackTopLabel;
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);

    int top = calculateTop();

    drawTrack(canvas, trackWidth, top);
    if (thumbPosition > 0.0f) {
      drawMarker(canvas, trackWidth, top);
    }

    if ((thumbIsPressed || isFocused()) && isEnabled()) {
      if (stepSize > 0.0f) {
        drawTicks(canvas);
      }

      maybeDrawHalo(canvas, trackWidth, top);
      drawLabel(canvas, trackWidth, top);
    }

    drawThumb(canvas, trackWidth, top);
  }

  private void drawTrack(@NonNull Canvas canvas, int width, int top) {
    float right = trackSidePadding + thumbPosition * width;
    if (right < trackSidePadding + width) {
      canvas.drawLine(right, top, trackSidePadding + width, top, inactiveTrackPaint);
    }
  }

  private void drawMarker(@NonNull Canvas canvas, int width, int top) {
    float left = trackSidePadding + thumbPosition * width;
    canvas.drawLine(trackSidePadding, top, left, top, activeTrackPaint);
  }

  private void drawTicks(@NonNull Canvas canvas) {
    canvas.drawPoints(ticksCoordinates, ticksPaint);
  }

  private void drawLabel(@NonNull Canvas canvas, int width, int top) {
    int left = trackSidePadding + (int) (thumbPosition * width) - label.getIntrinsicWidth() / 2;
    top -= labelPadding + thumbRadius;
    label.setBounds(left, top - label.getIntrinsicHeight(), left + label.getIntrinsicWidth(), top);
    label.draw(canvas);
  }

  private void drawThumb(@NonNull Canvas canvas, int width, int top) {
    // Clear out the track behind the thumb if we're in a disable state since the thumb is
    // transparent.
    if (!isEnabled()) {
      canvas.drawCircle(trackSidePadding + thumbPosition * width, top, thumbRadius, thumbPaint);
    }

    canvas.save();
    canvas.translate(
        trackSidePadding + (int) (thumbPosition * width) - thumbRadius, top - thumbRadius);
    thumbDrawable.draw(canvas);
    canvas.restore();
  }

  private void maybeDrawHalo(@NonNull Canvas canvas, int width, int top) {
    // Only draw the halo for devices which don't support the ripple.
    if (forceDrawCompatShadow || VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      canvas.drawCircle(trackSidePadding + thumbPosition * width, top, haloRadius, haloPaint);
    }
  }

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (!isEnabled()) {
      return false;
    }
    float x = event.getX();
    float position = (x - trackSidePadding) / trackWidth;
    position = Math.max(0, position);
    position = Math.min(1, position);

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        getParent().requestDisallowInterceptTouchEvent(true);
        requestFocus();
        thumbIsPressed = true;
        thumbPosition = position;
        snapThumbPosition();
        updateHaloHotSpot();
        invalidate();
        if (hasOnChangeListener()) {
          listener.onValueChange(this, getValue());
        }
        break;
      case MotionEvent.ACTION_MOVE:
        thumbPosition = position;
        snapThumbPosition();
        updateHaloHotSpot();
        invalidate();
        if (hasOnChangeListener()) {
          listener.onValueChange(this, getValue());
        }
        break;
      case MotionEvent.ACTION_UP:
        getParent().requestDisallowInterceptTouchEvent(false);
        thumbIsPressed = false;
        thumbPosition = position;
        snapThumbPosition();
        invalidate();
        break;
      default:
        // Nothing to do in this case.
    }
    float value = getValue();
    if (hasLabelFormatter()) {
      label.setText(formatter.getFormattedValue(value));
    } else {
      label.setText(String.format((int) value == value ? "%.0f" : "%.2f", value));
    }

    // Set if the thumb is pressed. This will cause the ripple to be drawn.
    setPressed(thumbIsPressed);
    return true;
  }

  private void snapThumbPosition() {
    if (stepSize > 0.0f) {
      int intervalsCovered = Math.round(thumbPosition * (ticksCoordinates.length / 2 - 1));
      thumbPosition = (float) intervalsCovered / (ticksCoordinates.length / 2 - 1);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    inactiveTrackPaint.setColor(getColorForState(inactiveTrackColor));
    activeTrackPaint.setColor(getColorForState(activeTrackColor));
    ticksPaint.setColor(getColorForState(tickColor));
    if (label.isStateful()) {
      label.setState(getDrawableState());
    }
    if (thumbDrawable.isStateful()) {
      thumbDrawable.setState(getDrawableState());
    }
    haloPaint.setColor(getColorForState(thumbColor));
    haloPaint.setAlpha(HALO_ALPHA);
  }

  @ColorInt
  private int getColorForState(@NonNull ColorStateList colorStateList) {
    return colorStateList.getColorForState(getDrawableState(), colorStateList.getDefaultColor());
  }

  @VisibleForTesting
  void forceDrawCompatShadow(boolean force) {
    forceDrawCompatShadow = force;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SliderState sliderState = new SliderState(superState);
    sliderState.valueFrom = valueFrom;
    sliderState.valueTo = valueTo;
    sliderState.thumbPosition = thumbPosition;
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
    thumbPosition = sliderState.thumbPosition;
    stepSize = sliderState.stepSize;
    if (sliderState.hasFocus) {
      requestFocus();
    }
    if (hasOnChangeListener()) {
      listener.onValueChange(this, getValue());
    }
  }

  static class SliderState extends BaseSavedState {

    float valueFrom;
    float valueTo;
    float thumbPosition;
    float stepSize;
    float[] ticksCoordinates;
    boolean hasFocus;

    public static final Parcelable.Creator<SliderState> CREATOR =
        new Parcelable.Creator<SliderState>() {

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
      thumbPosition = source.readFloat();
      stepSize = source.readFloat();
      source.readFloatArray(ticksCoordinates);
      hasFocus = source.createBooleanArray()[0];
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeFloat(valueFrom);
      dest.writeFloat(valueTo);
      dest.writeFloat(thumbPosition);
      dest.writeFloat(stepSize);
      dest.writeFloatArray(ticksCoordinates);
      boolean[] booleans = new boolean[1];
      booleans[0] = hasFocus;
      dest.writeBooleanArray(booleans);
    }
  }
}
