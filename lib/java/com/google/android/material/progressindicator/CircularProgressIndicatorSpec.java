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
import static java.lang.Math.max;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.CircularProgressIndicator.IndeterminateAnimationType;
import com.google.android.material.progressindicator.CircularProgressIndicator.IndicatorDirection;

/**
 * This class contains the parameters for drawing a circular type progress indicator. The parameters
 * reflect the attributes defined in {@code R.styleable.BaseProgressIndicator} and {@code
 * R.styleable.CircularProgressIndicator}.
 */
public final class CircularProgressIndicatorSpec extends BaseProgressIndicatorSpec {

  @IndeterminateAnimationType public int indeterminateAnimationType;

  /** The size (outer diameter) of the spinner. */
  @Px public int indicatorSize;

  /** The extra space from the outer edge of the indicator to the edge of the canvas. */
  @Px public int indicatorInset;

  /** The direction in which the indicator will rotate and grow to. */
  @IndicatorDirection public int indicatorDirection;

  /** Whether to show the track in the indeterminate mode. */
  public boolean indeterminateTrackVisible;

  /**
   * Instantiates the spec for {@link CircularProgressIndicator}.
   *
   * <p>If attributes in {@code R.styleable.CircularProgressIndicator} are missing, the values in
   * the default style {@code Widget.MaterialComponents.CircularProgressIndicator} will be loaded.
   * If attributes in {@code R.styleable.BaseProgressIndicator} are missing, the values in the
   * default style {@code Widget.MaterialComponents.ProgressIndicator} will be loaded.
   *
   * @param context Current themed context.
   * @param attrs Component's attributes set.
   */
  public CircularProgressIndicatorSpec(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.circularProgressIndicatorStyle);
  }

  public CircularProgressIndicatorSpec(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    this(context, attrs, defStyleAttr, CircularProgressIndicator.DEF_STYLE_RES);
  }

  public CircularProgressIndicatorSpec(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes final int defStyleAttr,
      @StyleRes final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    int defaultIndicatorSize =
        context.getResources().getDimensionPixelSize(R.dimen.mtrl_progress_circular_size_medium);
    int defaultIndicatorInset =
        context.getResources().getDimensionPixelSize(R.dimen.mtrl_progress_circular_inset_medium);
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.CircularProgressIndicator, defStyleAttr, defStyleRes);
    indeterminateAnimationType =
        a.getInt(
            R.styleable.CircularProgressIndicator_indeterminateAnimationTypeCircular,
            CircularProgressIndicator.INDETERMINATE_ANIMATION_TYPE_ADVANCE);
    indicatorSize =
        max(
            getDimensionPixelSize(
                context,
                a,
                R.styleable.CircularProgressIndicator_indicatorSize,
                defaultIndicatorSize),
            trackThickness * 2);
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
    indeterminateTrackVisible =
        a.getBoolean(R.styleable.CircularProgressIndicator_indeterminateTrackVisible, true);
    a.recycle();

    validateSpec();
  }
}
