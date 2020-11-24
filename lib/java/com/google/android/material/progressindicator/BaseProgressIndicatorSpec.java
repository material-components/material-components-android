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
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
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
 * reflect the attributes defined in {@link R.styleable#BaseProgressIndicator}.
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
   * The color array used in the indicator. In determinate mode, only the first item will be used.
   */
  @NonNull public int[] indicatorColors = new int[0];

  /**
   * The color used in the track. If not defined, it will be set to the indicatorColors and
   * apply the first disable alpha value from the theme.
   */
  @ColorInt public int trackColor;

  /** The animation behavior to show the indicator and track. */
  @ShowAnimationBehavior public int showAnimationBehavior;

  /** The animation behavior to hide the indicator and track. */
  @HideAnimationBehavior public int hideAnimationBehavior;

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
    trackCornerRadius =
        min(
            getDimensionPixelSize(
                context, a, R.styleable.BaseProgressIndicator_trackCornerRadius, 0),
            trackThickness / 2);
    showAnimationBehavior =
        a.getInt(
            R.styleable.BaseProgressIndicator_showAnimationBehavior,
            BaseProgressIndicator.SHOW_NONE);
    hideAnimationBehavior =
        a.getInt(
            R.styleable.BaseProgressIndicator_hideAnimationBehavior,
            BaseProgressIndicator.HIDE_NONE);

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
      indicatorColors = new int[] {MaterialColors.getColor(context, R.attr.colorPrimary, -1)};
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

  abstract void validateSpec();
}
