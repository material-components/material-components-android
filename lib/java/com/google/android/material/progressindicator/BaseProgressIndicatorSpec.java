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

package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static com.google.android.material.progressindicator.DeterminateDrawable.FULL_AMPLITUDE_PROGRESS_MAX;
import static com.google.android.material.progressindicator.DeterminateDrawable.FULL_AMPLITUDE_PROGRESS_MIN;
import static com.google.android.material.resources.MaterialResources.getDimensionPixelSize;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.BaseProgressIndicator.HideAnimationBehavior;
import com.google.android.material.progressindicator.BaseProgressIndicator.ShowAnimationBehavior;

/**
 * This class contains the parameters shared between linear type and circular type. The parameters
 * reflect the attributes defined in {@code R.styleable.BaseProgressIndicator}.
 */
public abstract class BaseProgressIndicatorSpec {
  /** The thickness of the track and indicator. */
  @Px public int trackThickness;

  /**
   * When this is greater than 0, the corners of both the track and the indicator will be rounded
   * with this radius. If the radius is greater than half of the track width, an {@code
   * IllegalArgumentException} will be thrown during initialization.
   */
  @Px public int trackCornerRadius;

  /**
   * The fraction of the track thickness to be used as the corner radius. And the stroke ROUND cap
   * is used to prevent artifacts like (b/319309456), when 0.5f is specified.
   */
  public float trackCornerRadiusFraction;

  /**
   * When this is true, the {#link trackCornerRadiusFraction} takes effect. Otherwise, the {@link
   * trackCornerRadius} takes effect.
   */
  public boolean useRelativeTrackCornerRadius;

  /**
   * The color array used in the indicator. In determinate mode, only the first item will be used.
   */
  @NonNull public int[] indicatorColors = new int[0];

  /**
   * The color used in the track. If not defined, it will be set to the indicatorColors and apply
   * the first disable alpha value from the theme.
   */
  @ColorInt public int trackColor;

  /** The animation behavior to show the indicator and track. */
  @ShowAnimationBehavior public int showAnimationBehavior;

  /** The animation behavior to hide the indicator and track. */
  @HideAnimationBehavior public int hideAnimationBehavior;

  /** The size of the gap between the indicator and the rest of the track. */
  @Px public int indicatorTrackGapSize;

  /** The size of the wavelength in determinate mode, if a wave effect is configured. */
  @Px public int wavelengthDeterminate;

  /** The size of the wavelength in indeterminate mode, if a wave effect is configured. */
  @Px public int wavelengthIndeterminate;

  /** The size of the amplitude, if a wave effect is configured. */
  @Px public int waveAmplitude;

  /** The speed of the waveform, if a wave effect is configured. */
  @Px public int waveSpeed;

  /** The scale of the animation duration in indeterminate mode. */
  @FloatRange(from = 0.1f, to = 10f)
  public float indeterminateAnimatorDurationScale;

  /** The normalized progress, at which the full wave amplitude will be ramped up. */
  @FloatRange(from = 0f, to = 1f)
  public float waveAmplitudeRampProgressMin;

  /** The normalized progress, at which the full wave amplitude will be ramped down. */
  @FloatRange(from = 0f, to = 1f)
  public float waveAmplitudeRampProgressMax;

  /**
   * Instantiates BaseProgressIndicatorSpec.
   *
   * <p>If attributes are missing, the values defined in the default style {@link
   * R.style#Widget_MaterialComponents_ProgressIndicator} will be loaded.
   *
   * @param context Current themed context.
   * @param attrs Component's attributes set.
   */
  protected BaseProgressIndicatorSpec(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes final int defStyleAttr,
      @StyleRes final int defStyleRes) {
    int defaultIndicatorSize =
        context.getResources().getDimensionPixelSize(R.dimen.mtrl_progress_track_thickness);
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.BaseProgressIndicator, defStyleAttr, defStyleRes);
    trackThickness =
        getDimensionPixelSize(
            context, a, R.styleable.BaseProgressIndicator_trackThickness, defaultIndicatorSize);
    TypedValue trackCornerRadiusValue =
        a.peekValue(R.styleable.BaseProgressIndicator_trackCornerRadius);
    if (trackCornerRadiusValue != null) {
      if (trackCornerRadiusValue.type == TypedValue.TYPE_DIMENSION) {
        trackCornerRadius =
            min(
                TypedValue.complexToDimensionPixelSize(
                    trackCornerRadiusValue.data, a.getResources().getDisplayMetrics()),
                trackThickness / 2);
        useRelativeTrackCornerRadius = false;
      } else if (trackCornerRadiusValue.type == TypedValue.TYPE_FRACTION) {
        trackCornerRadiusFraction = min(trackCornerRadiusValue.getFraction(1.0f, 1.0f), 0.5f);
        useRelativeTrackCornerRadius = true;
      }
    }
    showAnimationBehavior =
        a.getInt(
            R.styleable.BaseProgressIndicator_showAnimationBehavior,
            BaseProgressIndicator.SHOW_NONE);
    hideAnimationBehavior =
        a.getInt(
            R.styleable.BaseProgressIndicator_hideAnimationBehavior,
            BaseProgressIndicator.HIDE_NONE);
    indicatorTrackGapSize =
        a.getDimensionPixelSize(R.styleable.BaseProgressIndicator_indicatorTrackGapSize, 0);

    int wavelength = abs(a.getDimensionPixelSize(R.styleable.BaseProgressIndicator_wavelength, 0));
    wavelengthDeterminate =
        abs(
            a.getDimensionPixelSize(
                R.styleable.BaseProgressIndicator_wavelengthDeterminate, wavelength));
    wavelengthIndeterminate =
        abs(
            a.getDimensionPixelSize(
                R.styleable.BaseProgressIndicator_wavelengthIndeterminate, wavelength));
    waveAmplitude =
        abs(a.getDimensionPixelSize(R.styleable.BaseProgressIndicator_waveAmplitude, 0));
    waveSpeed = a.getDimensionPixelSize(R.styleable.BaseProgressIndicator_waveSpeed, 0);
    indeterminateAnimatorDurationScale =
        a.getFloat(R.styleable.BaseProgressIndicator_indeterminateAnimatorDurationScale, 1);
    waveAmplitudeRampProgressMin =
        a.getFloat(
                R.styleable.BaseProgressIndicator_waveAmplitudeRampProgressMin,
                FULL_AMPLITUDE_PROGRESS_MIN);
    waveAmplitudeRampProgressMax =
        a.getFloat(
                R.styleable.BaseProgressIndicator_waveAmplitudeRampProgressMax,
                FULL_AMPLITUDE_PROGRESS_MAX);

    loadIndicatorColors(context, a);
    loadTrackColor(context, a);

    a.recycle();
  }

  /**
   * Loads the indicatorColor from attributes if existing; otherwise, uses theme primary color for
   * the indicator. This method doesn't recycle the {@link TypedArray} argument.
   *
   * @param context The current active context.
   * @param typedArray The TypedArray object of the attributes.
   * @throws IllegalArgumentException if an empty color array is used.
   */
  private void loadIndicatorColors(@NonNull Context context, @NonNull TypedArray typedArray) {
    if (!typedArray.hasValue(R.styleable.BaseProgressIndicator_indicatorColor)) {
      // Uses theme primary color for indicator if not provided in the attribute set.
      indicatorColors =
          new int[] {
            MaterialColors.getColor(context, androidx.appcompat.R.attr.colorPrimary, -1)
          };
      return;
    }

    TypedValue indicatorColorValue =
        typedArray.peekValue(R.styleable.BaseProgressIndicator_indicatorColor);

    if (indicatorColorValue.type != TypedValue.TYPE_REFERENCE) {
      indicatorColors =
          new int[] {typedArray.getColor(R.styleable.BaseProgressIndicator_indicatorColor, -1)};
      return;
    }

    indicatorColors =
        context
            .getResources()
            .getIntArray(
                typedArray.getResourceId(R.styleable.BaseProgressIndicator_indicatorColor, -1));
    if (indicatorColors.length == 0) {
      throw new IllegalArgumentException(
          "indicatorColors cannot be empty when indicatorColor is not used.");
    }
  }

  /**
   * Loads the trackColor from attributes if existing; otherwise, uses the first value in {@link
   * BaseProgressIndicatorSpec#indicatorColors} applying the alpha value for disable items from
   * theme. This method doesn't recycle the {@link TypedArray} argument.
   *
   * @param context The current active context.
   * @param typedArray The TypedArray object of the attributes.
   */
  private void loadTrackColor(@NonNull Context context, @NonNull TypedArray typedArray) {
    if (typedArray.hasValue(R.styleable.BaseProgressIndicator_trackColor)) {
      trackColor = typedArray.getColor(R.styleable.BaseProgressIndicator_trackColor, -1);
      return;
    }

    trackColor = indicatorColors[0];

    TypedArray disabledAlphaArray =
        context.getTheme().obtainStyledAttributes(new int[] {android.R.attr.disabledAlpha});
    float defaultOpacity = disabledAlphaArray.getFloat(0, BaseProgressIndicator.DEFAULT_OPACITY);
    disabledAlphaArray.recycle();

    int trackAlpha = (int) (BaseProgressIndicator.MAX_ALPHA * defaultOpacity);
    trackColor = MaterialColors.compositeARGBWithAlpha(trackColor, trackAlpha);
  }

  public boolean isShowAnimationEnabled() {
    return showAnimationBehavior != BaseProgressIndicator.SHOW_NONE;
  }

  public boolean isHideAnimationEnabled() {
    return hideAnimationBehavior != BaseProgressIndicator.HIDE_NONE;
  }

  public boolean hasWavyEffect(boolean isDeterminate) {
    return waveAmplitude > 0
        && ((!isDeterminate && wavelengthIndeterminate > 0)
            || (isDeterminate && wavelengthDeterminate > 0));
  }

  /**
   * Returns the track corner radius in pixels.
   *
   * <p>If {@link #useRelativeTrackCornerRadius} is true, the track corner radius is calculated
   * using the track thickness and the track corner radius fraction. Otherwise, the track corner
   * radius is returned directly.
   */
  public int getTrackCornerRadiusInPx() {
    return useRelativeTrackCornerRadius
        ? (int) (trackThickness * trackCornerRadiusFraction)
        : trackCornerRadius;
  }

  /**
   * Returns true if the stroke ROUND cap should be used to prevent artifacts like (b/319309456),
   * when fully rounded corners are specified.
   */
  public boolean useStrokeCap() {
    return useRelativeTrackCornerRadius && trackCornerRadiusFraction == 0.5f;
  }

  @CallSuper
  void validateSpec() {
    if (indicatorTrackGapSize < 0) {
      // Throws an exception if trying to use a negative gap size.
      throw new IllegalArgumentException("indicatorTrackGapSize must be >= 0.");
    }
  }
}
