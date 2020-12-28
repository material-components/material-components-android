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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.LinearProgressIndicator.IndeterminateAnimationType;
import com.google.android.material.progressindicator.LinearProgressIndicator.IndicatorDirection;

/**
 * This class contains the parameters for drawing a linear type progress indicator. The parameters
 * reflect the attributes defined in {@link R.styleable#BaseProgressIndicator} and {@link
 * R.styleable#LinearProgressIndicator}.
 */
public final class LinearProgressIndicatorSpec extends BaseProgressIndicatorSpec {

  /** The type of animation of indeterminate mode. */
  @IndeterminateAnimationType public int indeterminateAnimationType;

  /** The direction in which the indicator will swipe or grow to. */
  @IndicatorDirection public int indicatorDirection;

  boolean drawHorizontallyInverse;

  /**
   * Instantiates the spec for {@link LinearProgressIndicator}.
   *
   * <p>If attributes in {@link R.styleable#LinearProgressIndicator} are missing, the values in the
   * default style {@link R.style#Widget_MaterialComponents_LinearProgressIndicator} will be loaded.
   * If attributes in {@link R.styleable#BaseProgressIndicator} are missing, the values in the
   * default style {@link R.style#Widget_MaterialComponents_ProgressIndicator} will be loaded.
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
    a.recycle();

    validateSpec();

    drawHorizontallyInverse =
        indicatorDirection == LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT;
  }

  @Override
  void validateSpec() {
    if (indeterminateAnimationType
        == LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS) {
      if (trackCornerRadius > 0) {
        // Throws an exception if trying to use the cornered indicator/track with contiguous
        // indeterminate animation type.
        throw new IllegalArgumentException(
            "Rounded corners are not supported in contiguous indeterminate animation.");
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
