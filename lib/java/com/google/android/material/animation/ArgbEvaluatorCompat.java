/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.animation;

import android.animation.TypeEvaluator;
import androidx.annotation.NonNull;

/**
 * This evaluator can be used to perform type interpolation between integer values that represent
 * ARGB colors.
 *
 * <p>This compat version is copied from {@link android.animation.ArgbEvaluator} with critical bug
 * fixes for older versions of Android.
 */
public class ArgbEvaluatorCompat implements TypeEvaluator<Integer> {
  private static final ArgbEvaluatorCompat instance = new ArgbEvaluatorCompat();

  /**
   * Returns an instance of <code>ArgbEvaluatorCompat</code> that may be used in {@link
   * android.animation.ValueAnimator#setEvaluator(TypeEvaluator)}. The same instance may be used in
   * multiple <code>Animator</code>s because it holds no state.
   *
   * @return An instance of <code>ArgbEvaluator</code>.
   */
  @NonNull
  public static ArgbEvaluatorCompat getInstance() {
    return instance;
  }

  /**
   * This function returns the calculated in-between value for a color given integers that represent
   * the start and end values in the four bytes of the 32-bit int. Each channel is separately
   * linearly interpolated and the resulting calculated values are recombined into the return value.
   *
   * @param fraction The fraction from the starting to the ending values
   * @param startValue A 32-bit int value representing colors in the separate bytes of the parameter
   * @param endValue A 32-bit int value representing colors in the separate bytes of the parameter
   * @return A value that is calculated to be the linearly interpolated result, derived by
   *     separating the start and end values into separate color channels and interpolating each one
   *     separately, recombining the resulting values in the same way.
   */
  @NonNull
  @Override
  public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
    int startInt = startValue;
    float startA = ((startInt >> 24) & 0xff) / 255.0f;
    float startR = ((startInt >> 16) & 0xff) / 255.0f;
    float startG = ((startInt >> 8) & 0xff) / 255.0f;
    float startB = (startInt & 0xff) / 255.0f;

    int endInt = endValue;
    float endA = ((endInt >> 24) & 0xff) / 255.0f;
    float endR = ((endInt >> 16) & 0xff) / 255.0f;
    float endG = ((endInt >> 8) & 0xff) / 255.0f;
    float endB = (endInt & 0xff) / 255.0f;

    // convert from sRGB to linear
    startR = (float) Math.pow(startR, 2.2);
    startG = (float) Math.pow(startG, 2.2);
    startB = (float) Math.pow(startB, 2.2);

    endR = (float) Math.pow(endR, 2.2);
    endG = (float) Math.pow(endG, 2.2);
    endB = (float) Math.pow(endB, 2.2);

    // compute the interpolated color in linear space
    float a = startA + fraction * (endA - startA);
    float r = startR + fraction * (endR - startR);
    float g = startG + fraction * (endG - startG);
    float b = startB + fraction * (endB - startB);

    // convert back to sRGB in the [0..255] range
    a = a * 255.0f;
    r = (float) Math.pow(r, 1.0 / 2.2) * 255.0f;
    g = (float) Math.pow(g, 1.0 / 2.2) * 255.0f;
    b = (float) Math.pow(b, 1.0 / 2.2) * 255.0f;

    return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
  }
}
