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

import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.LinearProgressIndicator.IndeterminateAnimationType;
import com.google.android.material.progressindicator.LinearProgressIndicator.IndicatorDirection;

/**
 * This class contains the parameters for drawing a linear type progress indicator. The parameters
 * reflect the attributes defined in {@code R.styleable.BaseProgressIndicator} and {@code
 * R.styleable.LinearProgressIndicator}.
 */
public final class LinearProgressIndicatorSpec extends BaseProgressIndicatorSpec {

  /** The type of animation of indeterminate mode. */
  @IndeterminateAnimationType public int indeterminateAnimationType;

  /** The direction in which the indicator will swipe or grow to. */
  @IndicatorDirection public int indicatorDirection;

  boolean drawHorizontallyInverse;

  /** The desired size of the stop indicator at the end of the track. */
  @Px public int trackStopIndicatorSize;

  /** The padding of the stop indicator at the end of the track. */
  @Nullable public Integer trackStopIndicatorPadding;

  @Px public int trackInnerCornerRadius;
  public float trackInnerCornerRadiusFraction;
  public boolean useRelativeTrackInnerCornerRadius;
  public boolean hasInnerCornerRadius;

  /**
   * Instantiates the spec for {@link LinearProgressIndicator}.
   *
   * <p>If attributes in {@code R.styleable.LinearProgressIndicator} are missing, the values in the
   * default style {@code Widget.MaterialComponents.LinearProgressIndicator} will be loaded. If
   * attributes in {@code R.styleable.BaseProgressIndicator} are missing, the values in the default
   * style {@code Widget.MaterialComponents.ProgressIndicator} will be loaded.
   *
   * @param context Current themed context.
   * @param attrs Component's attributes set.
   */
  public LinearProgressIndicatorSpec(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.linearProgressIndicatorStyle);
  }

  public LinearProgressIndicatorSpec(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    this(context, attrs, defStyleAttr, LinearProgressIndicator.DEF_STYLE_RES);
  }

  public LinearProgressIndicatorSpec(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes final int defStyleAttr,
      @StyleRes final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.LinearProgressIndicator,
            R.attr.linearProgressIndicatorStyle,
            LinearProgressIndicator.DEF_STYLE_RES);
    indeterminateAnimationType =
        a.getInt(
            R.styleable.LinearProgressIndicator_indeterminateAnimationType,
            LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_DISJOINT);
    indicatorDirection =
        a.getInt(
            R.styleable.LinearProgressIndicator_indicatorDirectionLinear,
            LinearProgressIndicator.INDICATOR_DIRECTION_LEFT_TO_RIGHT);
    trackStopIndicatorSize =
        a.getDimensionPixelSize(R.styleable.LinearProgressIndicator_trackStopIndicatorSize, 0);
    if (a.hasValue(R.styleable.LinearProgressIndicator_trackStopIndicatorPadding)) {
      trackStopIndicatorPadding =
          a.getDimensionPixelSize(R.styleable.LinearProgressIndicator_trackStopIndicatorPadding, 0);
    }
    TypedValue trackInnerCornerRadiusValue =
        a.peekValue(R.styleable.LinearProgressIndicator_trackInnerCornerRadius);
    if (trackInnerCornerRadiusValue != null) {
      if (trackInnerCornerRadiusValue.type == TypedValue.TYPE_DIMENSION) {
        trackInnerCornerRadius =
            min(
                TypedValue.complexToDimensionPixelSize(
                    trackInnerCornerRadiusValue.data, a.getResources().getDisplayMetrics()),
                trackThickness / 2);
        useRelativeTrackInnerCornerRadius = false;
        hasInnerCornerRadius = true;
      } else if (trackInnerCornerRadiusValue.type == TypedValue.TYPE_FRACTION) {
        trackInnerCornerRadiusFraction =
            min(trackInnerCornerRadiusValue.getFraction(1.0f, 1.0f), 0.5f);
        useRelativeTrackInnerCornerRadius = true;
        hasInnerCornerRadius = true;
      }
    }
    a.recycle();

    validateSpec();

    drawHorizontallyInverse =
        indicatorDirection == LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT;
  }

  public int getTrackInnerCornerRadiusInPx() {
    return !hasInnerCornerRadius
        ? getTrackCornerRadiusInPx()
        : useRelativeTrackInnerCornerRadius
            ? (int) (trackThickness * trackInnerCornerRadiusFraction)
            : trackInnerCornerRadius;
  }

  @Px
  int getActualTrackStopIndicatorSize() {
    return min(trackStopIndicatorSize, trackThickness);
  }

  @Override
  public boolean useStrokeCap() {
    return super.useStrokeCap() && getTrackInnerCornerRadiusInPx() == getTrackCornerRadiusInPx();
  }

  @Override
  void validateSpec() {
    super.validateSpec();
    if (trackStopIndicatorSize < 0) {
      // Throws an exception if trying to use a negative stop indicator size.
      throw new IllegalArgumentException("Stop indicator size must be >= 0.");
    }
    if (indeterminateAnimationType
        == LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS) {
      if ((getTrackCornerRadiusInPx() > 0
              || (hasInnerCornerRadius && getTrackInnerCornerRadiusInPx() > 0))
          && indicatorTrackGapSize == 0) {
        // Throws an exception if trying to use the cornered indicator/track with contiguous
        // indeterminate animation type without gap.
        throw new IllegalArgumentException(
            "Rounded corners without gap are not supported in contiguous indeterminate animation.");
      }
      if (indicatorColors.length < 3) {
        // Throws an exception if trying to set contiguous indeterminate animation with less than 3
        // indicator colors.
        throw new IllegalArgumentException(
            "Contiguous indeterminate animation must be used with 3 or more indicator colors.");
      }
    }
  }
}
