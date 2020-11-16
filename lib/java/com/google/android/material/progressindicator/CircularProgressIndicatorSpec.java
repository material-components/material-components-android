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

import static com.google.android.material.resources.MaterialResources.getDimensionPixelSize;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.CircularProgressIndicator.IndicatorDirection;

/**
 * This class contains the parameters for drawing a circular type progress indicator. The parameters
 * reflect the attributes defined in {@link R.styleable#BaseProgressIndicator} and {@link
 * R.styleable#CircularProgressIndicator}.
 */
public final class CircularProgressIndicatorSpec extends BaseProgressIndicatorSpec {

  /** The radius of the central line of the circular progress stroke. */
  @Px public int indicatorRadius;

  /** The extra space from the outer edge of the indicator to the edge of the canvas. */
  @Px public int indicatorInset;

  /** The direction in which the indicator will rotate and grow to. */
  @IndicatorDirection public int indicatorDirection;

  /**
   * Instantiates the spec for {@link CircularProgressIndicator}.
   *
   * <p>If attributes in {@link R.styleable#CircularProgressIndicator} are missing, the values in
   * the default style {@link R.style#Widget_MaterialComponents_CircularProgressIndicator} will be
   * loaded. If attributes in {@link R.styleable#BaseProgressIndicator} are missing, the values in
   * the default style {@link R.style#Widget_MaterialComponents_ProgressIndicator} will be loaded.
   *
   * @param context Current themed context.
   * @param attrs Component's attributes set.
   */
  public CircularProgressIndicatorSpec(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs, R.attr.circularProgressIndicatorStyle);
    loadSpecFromAttributes(context, attrs);
  }

  private void loadSpecFromAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
    loadAttributes(context, attrs);
    validateSpec();
  }

  private void loadAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
    int defaultIndicatorRadius =
        context.getResources().getDimensionPixelSize(R.dimen.mtrl_progress_circular_radius);
    int defaultIndicatorInset =
        context.getResources().getDimensionPixelSize(R.dimen.mtrl_progress_circular_inset);
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.CircularProgressIndicator,
            R.attr.circularProgressIndicatorStyle,
            CircularProgressIndicator.DEF_STYLE_RES);
    indicatorRadius =
        getDimensionPixelSize(
            context,
            a,
            R.styleable.CircularProgressIndicator_indicatorRadius,
            defaultIndicatorRadius);
    indicatorInset =
        getDimensionPixelSize(
            context,
            a,
            R.styleable.CircularProgressIndicator_indicatorInset,
            defaultIndicatorInset);
    indicatorDirection =
        a.getInt(
            R.styleable.CircularProgressIndicator_indicatorDirectionCircular,
            CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE);
    a.recycle();
  }

  @Override
  void validateSpec() {
    if (indicatorRadius < indicatorSize / 2) {
      // Throws an exception if circularRadius is less than half of the indicatorSize, which will
      // result in a part of the inner side of the indicator overshoots the center, and the visual
      // becomes undefined.
      throw new IllegalArgumentException(
          "The circularRadius cannot be less than half of the indicatorSize.");
    }
  }
}
