/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.google.android.material.animation;

import android.animation.TimeInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

/**
 * Utility class for animations containing Material interpolators.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class AnimationUtils {

  public static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  public static final TimeInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR =
      new FastOutSlowInInterpolator();
  public static final TimeInterpolator FAST_OUT_LINEAR_IN_INTERPOLATOR =
      new FastOutLinearInInterpolator();
  public static final TimeInterpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR =
      new LinearOutSlowInInterpolator();
  public static final TimeInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

  /** Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}. */
  public static float lerp(float startValue, float endValue, float fraction) {
    return startValue + (fraction * (endValue - startValue));
  }

  /** Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}. */
  public static int lerp(int startValue, int endValue, float fraction) {
    return startValue + Math.round(fraction * (endValue - startValue));
  }

  /**
   * Linear interpolation between {@code outputMin} and {@code outputMax} when {@code value} is
   * between {@code inputMin} and {@code inputMax}.
   *
   * <p>Note that {@code value} will be coerced into {@code inputMin} and {@code inputMax}.This
   * function can handle input and output ranges that span positive and negative numbers.
   */
  public static float lerp(
      float outputMin, float outputMax, float inputMin, float inputMax, float value) {
    if (value <= inputMin) {
      return outputMin;
    }
    if (value >= inputMax) {
      return outputMax;
    }

    return lerp(outputMin, outputMax, (value - inputMin) / (inputMax - inputMin));
  }
}
