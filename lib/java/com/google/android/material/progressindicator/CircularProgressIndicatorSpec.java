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
import com.google.android.material.progressindicator.CircularProgressIndicator.HideBehavior;
import com.google.android.material.progressindicator.CircularProgressIndicator.IndicatorDirection;
import com.google.android.material.progressindicator.CircularProgressIndicator.ShowBehavior;

/**
 * This class contains the parameters for drawing a circular type progress indicator. The parameters
 * reflect the attributes defined in {@link R.styleable.BaseProgressIndicator} and {@link
 * R.styleable.CircularProgressIndicator}.
 */
public class CircularProgressIndicatorSpec implements AnimatedVisibilityChangeBehavior {

  private final BaseProgressIndicatorSpec baseSpec;

  /** The radius of the central line of the circular progress stroke. */
  @Px public int indicatorRadius;

  /** The extra space from the outer edge of the indicator to the edge of the canvas. */
  @Px public int indicatorInset;

  /** The direction in which the indicator will rotate and grow to. */
  @IndicatorDirection public int indicatorDirection;

  /** The animation direction to show the indicator and track. */
  @ShowBehavior public int showBehavior;

  /** The animation direction to hide the indicator and track. */
  @HideBehavior public int hideBehavior;

  /**
   * Instantiates CircularProgressIndicatorSpec.
   *
   * <p>If attributes in {@link R.styleable#CircularProgressIndicator} are missing, the values in
   * the default style {@link R.style#Widget_MaterialComponents_CircularProgressIndicator} will be
   * loaded. Attributes defined in {@link R.styleable#BaseProgressIndicator} will be loaded by
   * {@link BaseProgressIndicatorSpec#BaseProgressIndicatorSpec(Context, AttributeSet, int)}.
   *
   * <p>If there's an existing {@link BaseProgressIndicatorSpec}, please use {@link
   * #CircularProgressIndicatorSpec(Context, AttributeSet, BaseProgressIndicatorSpec)}.
   *
   * @param context Current themed context.
   * @param attrs Component's attributes set.
   */
  public CircularProgressIndicatorSpec(@NonNull Context context, @Nullable AttributeSet attrs) {
    this.baseSpec =
        new BaseProgressIndicatorSpec(context, attrs, R.attr.circularProgressIndicatorStyle);
    loadSpecFromAttributes(context, attrs);
  }

  public CircularProgressIndicatorSpec(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @NonNull BaseProgressIndicatorSpec baseSpec) {
    this.baseSpec = baseSpec;
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
    showBehavior =
        a.getInt(
            R.styleable.CircularProgressIndicator_showBehaviorCircular,
            CircularProgressIndicator.SHOW_NONE);
    hideBehavior =
        a.getInt(
            R.styleable.CircularProgressIndicator_hideBehaviorCircular,
            CircularProgressIndicator.HIDE_NONE);
    a.recycle();
  }

  protected void validateSpec() {
    if (indicatorRadius < baseSpec.indicatorSize / 2) {
      // Throws an exception if circularRadius is less than half of the indicatorSize, which will
      // result in a part of the inner side of the indicator overshoots the center, and the visual
      // becomes undefined.
      throw new IllegalArgumentException(
          "The circularRadius cannot be less than half of the indicatorSize.");
    }
  }

  @Override
  public boolean shouldAnimateToShow() {
    return showBehavior != CircularProgressIndicator.SHOW_NONE;
  }

  @Override
  public boolean shouldAnimateToHide() {
    return hideBehavior != CircularProgressIndicator.HIDE_NONE;
  }

  /** Returns the base spec included in this circular spec. */
  @NonNull
  public BaseProgressIndicatorSpec getBaseSpec() {
    return baseSpec;
  }
}
